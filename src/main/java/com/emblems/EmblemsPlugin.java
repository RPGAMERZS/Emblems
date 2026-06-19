package com.emblems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EmblemsPlugin extends JavaPlugin implements Listener {
   private ConfigManager configManager;
   private DataManager dataManager;
   private PurchaseManager purchaseManager;
   private GuiManager guiManager;
    private EmblemExpansion expansion;
    private EconomyProvider economyProvider;
    private MessageManager messageManager;
    private static final Set<String> COMMANDS = new HashSet<>(Arrays.asList("/emblem", "/emblems", "/em"));

    public void onEnable() {
       this.configManager = new ConfigManager(this);
       this.configManager.load();
       this.messageManager = new MessageManager(this);
       this.messageManager.load();
       if (!this.setupEconomy()) {
          this.getServer().getPluginManager().disablePlugin(this);
          return;
       }
       this.getLogger().info("Economy provider: " + this.economyProvider.getName());
       this.dataManager = new DataManager(this);
       this.purchaseManager = new PurchaseManager(this, this.economyProvider);
       this.guiManager = new GuiManager(this, this.economyProvider);
       if (this.getCommand("emblem") == null) {
          this.getLogger().severe("Command 'emblem' is not defined in plugin.yml!");
       } else {
          this.getCommand("emblem").setExecutor(new EmblemCommand(this));
       }

       this.getServer().getPluginManager().registerEvents(this.guiManager, this);
       this.getServer().getPluginManager().registerEvents(this, this);
       this.registerExpansion();

       for(Player player : Bukkit.getOnlinePlayers()) {
          this.dataManager.loadPlayer(player.getUniqueId());
          this.validateActiveEmblem(player);
          this.updateTabDisplay(player);
       }

       this.getLogger().info("Emblems v" + this.getDescription().getVersion() + " enabled.");
    }

   public void onDisable() {
      if (this.dataManager != null) {
         for(Player player : Bukkit.getOnlinePlayers()) {
            this.dataManager.unloadPlayer(player.getUniqueId());
         }

         this.dataManager.saveAll();
      }

      if (this.guiManager != null) {
         this.guiManager.cleanup();
      }

      if (this.expansion != null) {
         this.expansion.unregister();
      }

      this.getLogger().info("Emblems disabled.");
   }

    private boolean setupEconomy() {
       String preferred = getConfig().getString("economy.provider", "VAULT").toUpperCase();
       String first = preferred.equals("PXCOSMETICS") ? "PXCOSMETICS" : "VAULT";
       String second = preferred.equals("PXCOSMETICS") ? "VAULT" : "PXCOSMETICS";
       if (tryProvider(first)) return true;
       if (tryProvider(second)) return true;
       this.economyProvider = new NoneEconomyProvider();
       this.getLogger().warning("No economy provider available. Purchases disabled; free/permission emblems still work.");
       return true;
    }

    private boolean tryProvider(String name) {
       if (name.equals("PXCOSMETICS")) {
          if (Bukkit.getPluginManager().getPlugin("PXCosmetics") != null
                || Bukkit.getPluginManager().getPlugin("pxCosmetics") != null) {
             this.economyProvider = new PxCosmeticsEconomyProvider(this);
             this.getLogger().info("PXCosmetics economy hooked.");
             return true;
          }
          this.getLogger().warning("PXCosmetics provider requested but plugin not found.");
          return false;
       }
       if (this.getServer().getPluginManager().getPlugin("Vault") == null) return false;
       RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
       if (rsp == null) return false;
       Economy vault = rsp.getProvider();
       if (vault == null) return false;
       this.economyProvider = new VaultEconomyProvider(vault);
       this.getLogger().info("Vault economy hooked: " + vault.getName());
       return true;
    }

   private void registerExpansion() {
      if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
         this.expansion = new EmblemExpansion(this);
         if (this.expansion.register()) {
            this.getLogger().info("PlaceholderAPI expansion registered.");
         } else {
            this.getLogger().warning("Failed to register PlaceholderAPI expansion.");
         }
      } else {
         this.getLogger().info("PlaceholderAPI not found. Placeholders will use built-in display.");
      }

   }

   public void updateTabDisplay(Player player) {
      PlayerData data = this.dataManager.getPlayerData(player.getUniqueId());
      String activeId = data.getActiveId();
      if (activeId != null && !activeId.equals("none")) {
         Emblem emblem = this.configManager.getEmblem(activeId);
         if (emblem == null) {
            player.setPlayerListName((String)null);
         } else {
            String tag = emblem.getTag();
            String prefix = ChatColor.translateAlternateColorCodes('&', "&r" + tag + " &r");
            player.setPlayerListName(prefix + player.getName());
         }
      } else {
         player.setPlayerListName((String)null);
      }
   }

   @EventHandler
   public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
      Player player = event.getPlayer();
      String msg = event.getMessage().toLowerCase().trim();
      String[] parts = msg.split(" ", 2);
      String baseCommand = parts[0];
      if (COMMANDS.contains(baseCommand)) {
         event.setCancelled(true);
          if (!player.hasPermission("emblem.use")) {
             player.sendMessage(this.messageManager.format("general.no-permission"));
         } else {
            String argsStr = parts.length > 1 ? parts[1] : "";
            String[] args = argsStr.isEmpty() ? new String[0] : argsStr.split(" ");
            if (args.length == 0) {
               this.guiManager.open(player);
            } else {
               String sub = args[0].toLowerCase();
               if (!sub.equals("gui") && !sub.equals("menu")) {
                  List<String> argList = new ArrayList<>();
                  argList.add(args[0]);
                  if (args.length > 1) {
                     for(int i = 1; i < args.length; ++i) {
                        argList.add(args[i]);
                     }
                  }

                  StringBuilder full = new StringBuilder("emblem");

                  for(String a : argList) {
                     full.append(" ").append(a);
                  }

                  Bukkit.dispatchCommand(player, full.toString());
               } else {
                  this.guiManager.open(player);
               }
            }
         }
      }
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      this.dataManager.loadPlayer(player.getUniqueId());
      this.validateActiveEmblem(player);
      this.updateTabDisplay(player);
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.dataManager.unloadPlayer(event.getPlayer().getUniqueId());
   }

   private void validateActiveEmblem(Player player) {
      PlayerData data = this.dataManager.getPlayerData(player.getUniqueId());
      String activeId = data.getActiveId();
      if (activeId != null && !activeId.equals("none")) {
         Emblem emblem = this.configManager.getEmblem(activeId);
         if (emblem == null) {
            data.setActiveId("none");
            this.dataManager.savePlayer(player.getUniqueId());
            this.updateTabDisplay(player);
         } else {
            boolean hasPermission = emblem.getPermission() == null || emblem.getPermission().isEmpty() || player.hasPermission(emblem.getPermission()) || player.hasPermission("emblem.all");
            boolean hasPurchased = data.hasPurchased(activeId);
            if (!hasPermission && !hasPurchased) {
               data.setActiveId("none");
               this.dataManager.savePlayer(player.getUniqueId());
               this.updateTabDisplay(player);
                player.sendMessage(this.messageManager.format("join.emblem-removed"));
            }

         }
      }
   }

   public void equipEmblem(Player player, String emblemId) {
      PlayerData data = this.dataManager.getPlayerData(player.getUniqueId());
      data.setActiveId(emblemId);
      this.dataManager.savePlayer(player.getUniqueId());
      this.updateTabDisplay(player);
   }

   public void unequipEmblem(Player player) {
      PlayerData data = this.dataManager.getPlayerData(player.getUniqueId());
      data.setActiveId("none");
      this.dataManager.savePlayer(player.getUniqueId());
      this.updateTabDisplay(player);
   }

   public void reloadPluginConfig() {
       super.reloadConfig();
       this.configManager.load();
       this.messageManager.load();
    }

   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   public DataManager getDataManager() {
      return this.dataManager;
   }

   public PurchaseManager getPurchaseManager() {
      return this.purchaseManager;
   }

   public GuiManager getGuiManager() {
      return this.guiManager;
   }

    public EconomyProvider getEconomyProvider() {
        return this.economyProvider;
     }

   public MessageManager getMessageManager() {
      return this.messageManager;
   }
}
