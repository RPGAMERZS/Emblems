package com.emblems;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class GuiManager implements Listener {
   private static final Map<String, String> LEGACY_MATERIALS = new HashMap<>();
   private final EmblemsPlugin plugin;
   private final EconomyProvider economy;
   private final Map<UUID, GuiSession> openInventories;

   public GuiManager(EmblemsPlugin plugin, EconomyProvider economy) {
      this.plugin = plugin;
      this.economy = economy;
      this.openInventories = new ConcurrentHashMap<>();
   }

   public void open(Player player) {
      this.open(player, 0);
   }

   private void open(Player player, int page) {
      player.closeInventory();
      this.openInventories.remove(player.getUniqueId());
      ConfigManager configManager = this.plugin.getConfigManager();
      FileConfiguration guiConfig = configManager.getGuiConfig();
      ConfigurationSection gui = guiConfig.getConfigurationSection("gui");
      if (gui == null) {
         gui = guiConfig.createSection("gui");
      }

      String title = ChatColor.translateAlternateColorCodes('&', gui.getString("title", "&8Emblem Selector"));
      int rows = gui.getInt("rows", 6);
      int size = rows * 9;
      ConfigurationSection borderCfg = gui.getConfigurationSection("border");
      ConfigurationSection infoCfg = gui.getConfigurationSection("player-info");
      ConfigurationSection navCfg = gui.getConfigurationSection("navigation");
      ConfigurationSection closeCfg = gui.getConfigurationSection("close");
      int prevSlot = navCfg != null ? navCfg.getInt("previous.slot", -1) : -1;
      int nextSlot = navCfg != null ? navCfg.getInt("next.slot", -1) : -1;
      int closeSlot = closeCfg != null ? closeCfg.getInt("slot", -1) : -1;
      Inventory inv = Bukkit.createInventory((InventoryHolder)null, size, title);
      PlayerData data = this.plugin.getDataManager().getPlayerData(player.getUniqueId());
      String activeId = data.getActiveId();
      List<Emblem> emblemList = new ArrayList<>(configManager.getEmblems());
      int contentRows = rows - 2;
      int slotsPerPage = contentRows * 7;
      int startIndex = page * slotsPerPage;
      int endIndex = Math.min(startIndex + slotsPerPage, emblemList.size());
      this.applyBorder(inv, rows, borderCfg);
      this.addPlayerInfo(inv, player, data, emblemList.size(), infoCfg);
      this.addNavigation(inv, page, emblemList.size(), slotsPerPage, navCfg, closeCfg);
      Map<Integer, String> slotMap = new HashMap<>();

      for(int i = startIndex; i < endIndex; ++i) {
         Emblem emblem = (Emblem)emblemList.get(i);
         boolean isEquipped = emblem.getId().equals(activeId);
         boolean alwaysUnlocked = emblem.getPrice() <= 0;
         boolean isUnlocked = alwaysUnlocked || data.hasPurchased(emblem.getId()) || player.hasPermission("emblem.all") || emblem.getPermission() != null && !emblem.getPermission().isEmpty() && player.hasPermission(emblem.getPermission());
         ItemStack item = this.createEmblemItem(emblem, isEquipped, isUnlocked);
         int contentIndex = i - startIndex;
         int col = contentIndex % 7;
         int row = contentIndex / 7 + 1;
         int slot = row * 9 + col + 1;
         if (slot >= 0 && slot < size) {
            inv.setItem(slot, item);
            slotMap.put(slot, emblem.getId());
         }
      }

      this.openInventories.put(player.getUniqueId(), new GuiSession(inv, page, slotMap, prevSlot, nextSlot, closeSlot));
      player.openInventory(inv);
   }

   private void applyBorder(Inventory inv, int rows, ConfigurationSection cfg) {
      int size = rows * 9;
      String matName = cfg != null ? cfg.getString("material", "GRAY_STAINED_GLASS_PANE") : "GRAY_STAINED_GLASS_PANE";
      int dura = cfg != null ? cfg.getInt("durability", 15) : 15;
      String name = ChatColor.translateAlternateColorCodes('&', cfg != null ? cfg.getString("name", " ") : " ");
      Material mat = this.resolveMaterial(matName);
      if (mat == null) {
         mat = Material.matchMaterial("STAINED_GLASS_PANE");
      }

      ItemStack item = new ItemStack(mat, 1, (short)dura);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(name.isEmpty() ? " " : name);
      item.setItemMeta(meta);

      for(int i = 0; i < 9; ++i) {
         inv.setItem(i, item);
         inv.setItem(size - 9 + i, item);
      }

      for(int r = 1; r < rows - 1; ++r) {
         inv.setItem(r * 9, item);
         inv.setItem(r * 9 + 8, item);
      }

   }

   private void addPlayerInfo(Inventory inv, Player player, PlayerData data, int totalEmblems, ConfigurationSection cfg) {
      if (cfg != null && cfg.getBoolean("enabled", true)) {
         int slot = cfg.getInt("slot", 4);
         String matName = cfg.getString("material", "SKULL_ITEM");
         int dura = cfg.getInt("durability", 3);
         String nameFormat = cfg.getString("name", "&b%player_name%");
         List<String> loreFormat = cfg.getStringList("lore");
         Material mat = this.resolveMaterial(matName);
         if (mat == null) {
            mat = Material.matchMaterial("SKULL_ITEM");
         }

         ItemStack item = new ItemStack(mat, 1, (short)dura);
         ItemMeta meta = item.getItemMeta();
         String activeName = "None";
         if (!data.getActiveId().equals("none")) {
            Emblem active = this.plugin.getConfigManager().getEmblem(data.getActiveId());
            if (active != null) {
               activeName = active.getDisplayname();
            }
         }

         double balance = this.economy.getBalance(player);
         String balanceStr = this.formatNumber(balance);
         int unlockedCount = data.getPurchased().size();
         String displayName = ChatColor.translateAlternateColorCodes('&', nameFormat.replace("%player_name%", player.getName()).replace("%active%", activeName).replace("%unlocked_count%", String.valueOf(unlockedCount)).replace("%total%", String.valueOf(totalEmblems)).replace("%balance%", balanceStr));
         meta.setDisplayName(displayName);
          if (meta instanceof SkullMeta) {
             try {
                ((SkullMeta) meta).setOwner(player.getName());
             } catch (Exception var23) {
             }
          }

         List<String> lore = new ArrayList<>();

         for(String line : loreFormat) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line.replace("%player_name%", player.getName()).replace("%active%", activeName).replace("%unlocked_count%", String.valueOf(unlockedCount)).replace("%total%", String.valueOf(totalEmblems)).replace("%balance%", balanceStr)));
         }

         meta.setLore(lore);
         item.setItemMeta(meta);
         inv.setItem(slot, item);
      }
   }

   private void addNavigation(Inventory inv, int page, int totalEmblems, int slotsPerPage, ConfigurationSection navCfg, ConfigurationSection closeCfg) {
      int maxPage = (int)Math.ceil((double)totalEmblems / (double)slotsPerPage) - 1;
      if (navCfg != null) {
         ConfigurationSection prevCfg = navCfg.getConfigurationSection("previous");
         if (prevCfg != null && page > 0) {
            ItemStack prev = this.buildNavItem(prevCfg, this.plugin.getMessageManager().format("gui.previous"));
            inv.setItem(prevCfg.getInt("slot", 48), prev);
         }

         ConfigurationSection nextCfg = navCfg.getConfigurationSection("next");
         if (nextCfg != null && page < maxPage) {
            ItemStack next = this.buildNavItem(nextCfg, this.plugin.getMessageManager().format("gui.next"));
            inv.setItem(nextCfg.getInt("slot", 50), next);
         }
      }

      if (closeCfg != null) {
         ItemStack close = this.buildNavItem(closeCfg, this.plugin.getMessageManager().format("gui.close"));
         inv.setItem(closeCfg.getInt("slot", 49), close);
      }

   }

   private ItemStack buildNavItem(ConfigurationSection cfg, String defaultName) {
      String matName = cfg.getString("material", "GRAY_STAINED_GLASS_PANE");
      int dura = cfg.getInt("durability", 0);
      String name = ChatColor.translateAlternateColorCodes('&', cfg.getString("name", defaultName));
      Material mat = this.resolveMaterial(matName);
      if (mat == null) {
         mat = Material.matchMaterial("STAINED_GLASS_PANE");
      }

      ItemStack item = new ItemStack(mat, 1, (short)dura);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(name);
      item.setItemMeta(meta);
      return item;
   }

   private ItemStack createEmblemItem(Emblem emblem, boolean isEquipped, boolean isUnlocked) {
      String matName = emblem.getMaterial();
      Material mat = this.resolveMaterial(matName);
      if (mat == null) {
         mat = Material.PAPER;
      }

      ItemStack item = new ItemStack(mat, 1);
      ItemMeta meta = item.getItemMeta();
      String displayName = ChatColor.translateAlternateColorCodes('&', emblem.getDisplayname());
      meta.setDisplayName(displayName);
      List<String> processed = new ArrayList<>();

      for(String line : emblem.getLore()) {
         processed.add(ChatColor.translateAlternateColorCodes('&', line.replace("%emblem_tag%", emblem.getTag()).replace("%emblem_displayname%", emblem.getDisplayname()).replace("%price%", String.valueOf(emblem.getPrice()))));
      }

      if (isEquipped) {
         processed.add("");
         processed.add(this.plugin.getMessageManager().format("gui.equipped-lore"));
         processed.add(this.plugin.getMessageManager().format("gui.unequip-lore"));
      } else if (isUnlocked) {
         processed.add("");
         processed.add(this.plugin.getMessageManager().format("gui.equip-lore"));
      }

      meta.setLore(processed);
      item.setItemMeta(meta);
      return item;
   }

   private String formatNumber(double amount) {
      return amount >= (double)1000.0F ? NumberFormat.getNumberInstance(Locale.US).format((long)amount) : String.valueOf((long)amount);
   }

   @EventHandler
   public void onDrag(InventoryDragEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         GuiSession session = (GuiSession)this.openInventories.get(event.getWhoClicked().getUniqueId());
         if (session != null && session.inventory.equals(event.getInventory())) {
            event.setCancelled(true);
         }

      }
   }

   @EventHandler
   public void onClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player) {
         Player player = (Player)event.getWhoClicked();
         GuiSession session = (GuiSession)this.openInventories.get(player.getUniqueId());
         if (session != null) {
            boolean isTopInv = event.getView().getTopInventory().equals(session.inventory);
            if (isTopInv) {
               event.setCancelled(true);
               int rawSlot = event.getRawSlot();
               if (rawSlot >= 0 && rawSlot < session.inventory.getSize()) {
                  if (rawSlot == session.closeSlot) {
                     player.closeInventory();
                  } else if (rawSlot == session.prevSlot) {
                     this.openInventories.remove(player.getUniqueId());
                     this.open(player, Math.max(0, session.page - 1));
                  } else if (rawSlot == session.nextSlot) {
                     this.openInventories.remove(player.getUniqueId());
                     this.open(player, session.page + 1);
                  } else {
                     String emblemId = (String)session.slotMap.get(rawSlot);
                     if (emblemId != null) {
                        Emblem clickedEmblem = this.plugin.getConfigManager().getEmblem(emblemId);
                        if (clickedEmblem != null) {
                           PlayerData data = this.plugin.getDataManager().getPlayerData(player.getUniqueId());
                           String activeId = data.getActiveId();
                           boolean isEquipped = clickedEmblem.getId().equals(activeId);
                           boolean alwaysUnlocked = clickedEmblem.getPrice() <= 0;
                           boolean hasPerm = clickedEmblem.getPermission() != null && !clickedEmblem.getPermission().isEmpty() && player.hasPermission(clickedEmblem.getPermission());
                           boolean hasPurchased = data.hasPurchased(clickedEmblem.getId());
                           boolean isUnlocked = alwaysUnlocked || hasPerm || hasPurchased || player.hasPermission("emblem.all");
                           if (event.isRightClick() && player.hasPermission("emblem.admin")) {
                              if (hasPurchased) {
                                 data.removePurchased(clickedEmblem.getId());
                                 if (clickedEmblem.getId().equals(data.getActiveId())) {
                                    this.plugin.unequipEmblem(player);
                                 }

                                  player.sendMessage(this.plugin.getMessageManager().format("admin.removed", "emblem", clickedEmblem.getDisplayname()));
                               } else {
                                  data.addPurchased(clickedEmblem.getId());
                                  player.sendMessage(this.plugin.getMessageManager().format("admin.granted", "emblem", clickedEmblem.getDisplayname()));
                              }

                              this.plugin.getDataManager().savePlayer(player.getUniqueId());
                              this.openInventories.remove(player.getUniqueId());
                              this.open(player, 0);
                            } else if (isEquipped) {
                               this.plugin.unequipEmblem(player);
                               player.sendMessage(this.plugin.getMessageManager().format("equip.unequipped"));
                               player.closeInventory();
                            } else if (isUnlocked) {
                               this.plugin.equipEmblem(player, clickedEmblem.getId());
                               player.sendMessage(this.plugin.getMessageManager().format("equip.success", "emblem", clickedEmblem.getDisplayname()));
                               player.closeInventory();
                            } else if (clickedEmblem.isPurchasable()) {
                               boolean success = this.plugin.getPurchaseManager().purchase(player, clickedEmblem);
                               if (success) {
                                  this.plugin.equipEmblem(player, clickedEmblem.getId());
                                  player.sendMessage(this.plugin.getMessageManager().format("equip.success", "emblem", clickedEmblem.getDisplayname()));
                               }

                               this.openInventories.remove(player.getUniqueId());
                               this.open(player, 0);
                            } else {
                               player.sendMessage(this.plugin.getMessageManager().format("purchase.locked"));
                            }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onClose(InventoryCloseEvent event) {
      if (event.getPlayer() instanceof Player) {
         this.openInventories.remove(event.getPlayer().getUniqueId());
      }

   }

   public boolean hasOpenInventory(Player player) {
      return this.openInventories.containsKey(player.getUniqueId());
   }

   public void cleanup() {
      this.openInventories.clear();
   }

   private Material resolveMaterial(String name) {
      if (name != null && !name.isEmpty()) {
         String clean = name.toUpperCase().replace(" ", "_").replace("MINECRAFT:", "");
         Material mat = Material.matchMaterial(clean);
         if (mat != null) {
            return mat;
         } else {
            String legacy = (String)LEGACY_MATERIALS.get(clean);
            if (legacy != null) {
               mat = Material.matchMaterial(legacy);
               if (mat != null) {
                  return mat;
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   static {
      LEGACY_MATERIALS.put("GOLDEN_HELMET", "GOLD_HELMET");
      LEGACY_MATERIALS.put("GOLDEN_CHESTPLATE", "GOLD_CHESTPLATE");
      LEGACY_MATERIALS.put("GOLDEN_LEGGINGS", "GOLD_LEGGINGS");
      LEGACY_MATERIALS.put("GOLDEN_BOOTS", "GOLD_BOOTS");
      LEGACY_MATERIALS.put("GOLDEN_SWORD", "GOLD_SWORD");
      LEGACY_MATERIALS.put("GOLDEN_AXE", "GOLD_AXE");
      LEGACY_MATERIALS.put("GOLDEN_PICKAXE", "GOLD_PICKAXE");
      LEGACY_MATERIALS.put("GOLDEN_SHOVEL", "GOLD_SPADE");
      LEGACY_MATERIALS.put("GOLDEN_HOE", "GOLD_HOE");
      LEGACY_MATERIALS.put("GOLDEN_APPLE", "GOLDEN_APPLE");
      LEGACY_MATERIALS.put("GOLDEN_CARROT", "GOLDEN_CARROT");
      LEGACY_MATERIALS.put("POPPY", "RED_ROSE");
      LEGACY_MATERIALS.put("DANDELION", "YELLOW_FLOWER");
      LEGACY_MATERIALS.put("SUNFLOWER", "DOUBLE_PLANT");
      LEGACY_MATERIALS.put("SNOWBALL", "SNOW_BALL");
      LEGACY_MATERIALS.put("GUNPOWDER", "SULPHUR");
      LEGACY_MATERIALS.put("BLACK_DYE", "INK_SACK");
      LEGACY_MATERIALS.put("GRASS_BLOCK", "GRASS");
      LEGACY_MATERIALS.put("OAK_WOOD", "LOG");
      LEGACY_MATERIALS.put("OAK_LOG", "LOG");
      LEGACY_MATERIALS.put("OAK_PLANKS", "WOOD");
      LEGACY_MATERIALS.put("OAK_STAIRS", "WOOD_STAIRS");
      LEGACY_MATERIALS.put("OAK_FENCE", "FENCE");
      LEGACY_MATERIALS.put("OAK_FENCE_GATE", "FENCE_GATE");
      LEGACY_MATERIALS.put("OAK_DOOR", "WOOD_DOOR");
      LEGACY_MATERIALS.put("OAK_SLAB", "WOOD_STEP");
      LEGACY_MATERIALS.put("STONE_SLAB", "STEP");
      LEGACY_MATERIALS.put("CRAFTING_TABLE", "WORKBENCH");
      LEGACY_MATERIALS.put("MAP", "EMPTY_MAP");
      LEGACY_MATERIALS.put("BONE_MEAL", "INK_SACK");
      LEGACY_MATERIALS.put("COOKED_BEEF", "COOKED_BEEF");
      LEGACY_MATERIALS.put("COOKED_PORKCHOP", "GRILLED_PORK");
      LEGACY_MATERIALS.put("COOKED_CHICKEN", "COOKED_CHICKEN");
      LEGACY_MATERIALS.put("PORKCHOP", "PORK");
      LEGACY_MATERIALS.put("BEEF", "RAW_BEEF");
      LEGACY_MATERIALS.put("CHICKEN", "RAW_CHICKEN");
      LEGACY_MATERIALS.put("MUSHROOM_STEW", "MUSHROOM_SOUP");
      LEGACY_MATERIALS.put("SMOOTH_STONE", "STONE");
      LEGACY_MATERIALS.put("COBBLESTONE_STAIRS", "COBBLESTONE_STAIRS");
      LEGACY_MATERIALS.put("STONE_STAIRS", "COBBLESTONE_STAIRS");
      LEGACY_MATERIALS.put("REDSTONE_TORCH", "REDSTONE_TORCH_ON");
      LEGACY_MATERIALS.put("REDSTONE_WALL_TORCH", "REDSTONE_TORCH_ON");
      LEGACY_MATERIALS.put("FIRE_CHARGE", "FIREBALL");
      LEGACY_MATERIALS.put("FIREWORK_ROCKET", "FIREWORK");
      LEGACY_MATERIALS.put("FIREWORK_STAR", "FIREWORK_CHARGE");
      LEGACY_MATERIALS.put("EXPERIENCE_BOTTLE", "EXP_BOTTLE");
      LEGACY_MATERIALS.put("WRITABLE_BOOK", "BOOK_AND_QUILL");
      LEGACY_MATERIALS.put("WRITTEN_BOOK", "WRITTEN_BOOK");
      LEGACY_MATERIALS.put("CARVED_PUMPKIN", "PUMPKIN");
      LEGACY_MATERIALS.put("MELON", "MELON_BLOCK");
      LEGACY_MATERIALS.put("ATTACHED_MELON_STEM", "MELON_STEM");
      LEGACY_MATERIALS.put("ATTACHED_PUMPKIN_STEM", "PUMPKIN_STEM");
      LEGACY_MATERIALS.put("SWEET_BERRIES", "SWEET_BERRIES");
      LEGACY_MATERIALS.put("CAMPFIRE", "CAMPFIRE");
      LEGACY_MATERIALS.put("SOUL_CAMPFIRE", "SOUL_CAMPFIRE");
      LEGACY_MATERIALS.put("NETHERRACK", "NETHERRACK");
      LEGACY_MATERIALS.put("END_STONE", "ENDER_STONE");
      LEGACY_MATERIALS.put("END_STONE_BRICKS", "ENDER_STONE");
      LEGACY_MATERIALS.put("PRISMARINE_BRICKS", "PRISMARINE");
      LEGACY_MATERIALS.put("DARK_PRISMARINE", "PRISMARINE");
      LEGACY_MATERIALS.put("PLAYER_HEAD", "SKULL_ITEM");
      LEGACY_MATERIALS.put("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE");
   }

   private static class GuiSession {
      final Inventory inventory;
      final int page;
      final Map<Integer, String> slotMap;
      final int prevSlot;
      final int nextSlot;
      final int closeSlot;

      GuiSession(Inventory inventory, int page, Map<Integer, String> slotMap, int prevSlot, int nextSlot, int closeSlot) {
         this.inventory = inventory;
         this.page = page;
         this.slotMap = slotMap;
         this.prevSlot = prevSlot;
         this.nextSlot = nextSlot;
         this.closeSlot = closeSlot;
      }
   }
}
