package com.emblems;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class EmblemExpansion extends PlaceholderExpansion {
   private final EmblemsPlugin plugin;

   public EmblemExpansion(EmblemsPlugin plugin) {
      this.plugin = plugin;
   }

   public String getIdentifier() {
      return "emblems";
   }

   public String getAuthor() {
      return "rp_gamerzs";
   }

   public String getVersion() {
      return "1.0.0";
   }

   public boolean persist() {
      return true;
   }

   public boolean canRegister() {
      return true;
   }

   public String onPlaceholderRequest(Player player, String identifier) {
      if (player == null) {
         return "";
      } else {
         PlayerData data = this.plugin.getDataManager().getPlayerData(player.getUniqueId());
         if (data == null) {
            return "";
         } else {
            String activeId = data.getActiveId();
            if (activeId != null && !activeId.equals("none")) {
               Emblem emblem = this.plugin.getConfigManager().getEmblem(activeId);
               if (emblem == null) {
                  return "";
               } else {
                  switch (identifier.toLowerCase()) {
                     case "display":
                        return emblem.getTag();
                     case "id":
                        return emblem.getId();
                     case "name":
                        return emblem.getDisplayname();
                     default:
                        return "";
                  }
               }
            } else if (identifier.equalsIgnoreCase("display")) {
               return "";
            } else if (identifier.equalsIgnoreCase("id")) {
               return "none";
            } else {
               return identifier.equalsIgnoreCase("name") ? "None" : "";
            }
         }
      }
   }
}
