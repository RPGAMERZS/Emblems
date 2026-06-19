package com.emblems;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EmblemCommand implements CommandExecutor {
   private final EmblemsPlugin plugin;

   public EmblemCommand(EmblemsPlugin plugin) {
      this.plugin = plugin;
   }

   private String msg(String path, String... replacements) {
      return this.plugin.getMessageManager().format(path, replacements);
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      try {
         if (args.length == 0) {
            if (!(sender instanceof Player)) {
               sender.sendMessage(msg("general.gui-only"));
               return true;
            } else if (!sender.hasPermission("emblem.use")) {
               sender.sendMessage(msg("general.no-permission"));
               return true;
            } else if (this.plugin.getGuiManager() == null) {
               sender.sendMessage(msg("general.gui-not-initialized"));
               this.plugin.getLogger().severe("GuiManager is null - plugin not properly initialized!");
               return true;
            } else {
               this.plugin.getGuiManager().open((Player)sender);
               return true;
            }
         } else {
            switch (args[0].toLowerCase()) {
               case "gui":
                  return this.handleGui(sender);
               case "list":
                  return this.handleList(sender);
               case "set":
                  return this.handleSet(sender, args);
               case "reset":
                  return this.handleReset(sender);
               case "admin":
                  return this.handleAdmin(sender, args);
               case "reload":
                  return this.handleReload(sender);
               default:
                  sender.sendMessage(msg("general.usage"));
                  return true;
            }
         }
      } catch (Exception e) {
         sender.sendMessage(msg("general.error"));
         this.plugin.getLogger().severe("Error in /emblem command: " + e.getMessage());
         e.printStackTrace();
         return true;
      }
   }

   private boolean handleGui(CommandSender sender) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(msg("general.player-only"));
         return true;
      } else if (!sender.hasPermission("emblem.use")) {
         sender.sendMessage(msg("general.no-permission"));
         return true;
      } else {
         this.plugin.getGuiManager().open((Player)sender);
         return true;
      }
   }

   private boolean handleList(CommandSender sender) {
      if (!sender.hasPermission("emblem.use")) {
         sender.sendMessage(msg("general.no-permission"));
         return true;
      } else {
         PlayerData data = sender instanceof Player ? this.plugin.getDataManager().getPlayerData(((Player)sender).getUniqueId()) : null;
          List<String> available = new ArrayList<>();

         for(Emblem e : this.plugin.getConfigManager().getEmblems()) {
            if (data != null && (data.hasPurchased(e.getId()) || sender.hasPermission("emblem.all") || e.getPermission() != null && !e.getPermission().isEmpty() && sender.hasPermission(e.getPermission()))) {
               available.add(e.getDisplayname());
            }
         }

         sender.sendMessage(msg("list.header"));
         if (data != null && !data.getActiveId().equals("none")) {
            Emblem active = this.plugin.getConfigManager().getEmblem(data.getActiveId());
            sender.sendMessage(msg("list.active", "active", active != null ? active.getDisplayname() : "None"));
         } else {
            sender.sendMessage(msg("list.active-none"));
         }

         String listStr = available.isEmpty() ? msg("list.none") : String.join("§7, ", available);
         sender.sendMessage(msg("list.available", "list", listStr));
         return true;
      }
   }

   private boolean handleSet(CommandSender sender, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(msg("general.player-only"));
         return true;
      } else if (!sender.hasPermission("emblem.use")) {
         sender.sendMessage(msg("general.no-permission"));
         return true;
      } else if (args.length < 2) {
         sender.sendMessage(msg("set.usage"));
         return true;
      } else {
         Player player = (Player)sender;
         String id = args[1];
         if (id.equalsIgnoreCase("none")) {
            this.plugin.unequipEmblem(player);
            player.sendMessage(msg("set.removed"));
            return true;
         } else {
            Emblem emblem = this.plugin.getConfigManager().getEmblem(id);
            if (emblem == null) {
               player.sendMessage(msg("general.emblem-not-found", "id", id));
               return true;
            } else {
               PlayerData data = this.plugin.getDataManager().getPlayerData(player.getUniqueId());
               boolean hasPermission = emblem.getPermission() == null || emblem.getPermission().isEmpty() || player.hasPermission(emblem.getPermission()) || player.hasPermission("emblem.all");
               boolean hasPurchased = data.hasPurchased(id);
               if (!hasPermission && !hasPurchased) {
                  player.sendMessage(msg("set.locked"));
                  return true;
               } else {
                  this.plugin.equipEmblem(player, id);
                  player.sendMessage(msg("set.success", "emblem", emblem.getDisplayname()));
                  return true;
               }
            }
         }
      }
   }

   private boolean handleReset(CommandSender sender) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(msg("general.player-only"));
         return true;
      } else if (!sender.hasPermission("emblem.use")) {
         sender.sendMessage(msg("general.no-permission"));
         return true;
      } else {
         Player player = (Player)sender;
         this.plugin.unequipEmblem(player);
         player.sendMessage(msg("reset.success"));
         return true;
      }
   }

   private boolean handleAdmin(CommandSender sender, String[] args) {
      if (!sender.hasPermission("emblem.admin")) {
         sender.sendMessage(msg("general.no-permission"));
         return true;
      } else if (args.length < 3) {
         sender.sendMessage(msg("admin.usage"));
         return true;
      } else {
         String action = args[1].toLowerCase();
         Player target = Bukkit.getPlayer(args[2]);
         if (target == null) {
            sender.sendMessage(msg("admin.player-not-found"));
            return true;
         } else {
            PlayerData data = this.plugin.getDataManager().getPlayerData(target.getUniqueId());
            switch (action) {
               case "set": {
                   if (args.length < 4) {
                      sender.sendMessage(msg("admin.set-usage"));
                      return true;
                   }

                   String id = args[3];
                   if (id.equalsIgnoreCase("none")) {
                      data.setActiveId("none");
                      sender.sendMessage(msg("admin.set-removed", "target", target.getName()));
                   } else {
                      Emblem e = this.plugin.getConfigManager().getEmblem(id);
                      if (e == null) {
                         sender.sendMessage(msg("general.emblem-not-found", "id", id));
                         return true;
                      }

                      data.setActiveId(id);
                      sender.sendMessage(msg("admin.set-success", "target", target.getName(), "emblem", e.getDisplayname()));
                   }

                   this.plugin.getDataManager().savePlayer(target.getUniqueId());
                   break;
                }
                case "reset": {
                   data.setActiveId("none");
                   this.plugin.getDataManager().savePlayer(target.getUniqueId());
                   sender.sendMessage(msg("admin.set-removed", "target", target.getName()));
                   break;
                }
                case "give": {
                   if (args.length < 4) {
                      sender.sendMessage(msg("admin.give-usage"));
                      return true;
                   }

                   String id = args[3];
                   if (!this.plugin.getConfigManager().emblemExists(id)) {
                      sender.sendMessage(msg("general.emblem-not-found", "id", id));
                      return true;
                   }

                   data.addPurchased(id);
                   this.plugin.getDataManager().savePlayer(target.getUniqueId());
                   sender.sendMessage(msg("admin.give-success", "id", id, "target", target.getName()));
                   break;
                }
                case "take": {
                   if (args.length < 4) {
                      sender.sendMessage(msg("admin.take-usage"));
                      return true;
                   }

                   String id = args[3];
                   if (!this.plugin.getConfigManager().emblemExists(id)) {
                      sender.sendMessage(msg("general.emblem-not-found", "id", id));
                      return true;
                   }

                   data.removePurchased(id);
                   if (id.equals(data.getActiveId())) {
                      data.setActiveId("none");
                   }

                   this.plugin.getDataManager().savePlayer(target.getUniqueId());
                   sender.sendMessage(msg("admin.take-success", "id", id, "target", target.getName()));
                   break;
                }
               default:
                  sender.sendMessage(msg("admin.usage"));
            }

            return true;
         }
      }
   }

   private boolean handleReload(CommandSender sender) {
      if (!sender.hasPermission("emblem.admin")) {
         sender.sendMessage(msg("general.no-permission"));
         return true;
      } else {
         this.plugin.reloadPluginConfig();
         sender.sendMessage(msg("general.reload-success"));
         return true;
      }
   }
}
