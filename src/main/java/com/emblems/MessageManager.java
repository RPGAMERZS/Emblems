package com.emblems;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class MessageManager {
   private final JavaPlugin plugin;
   private String prefix;

   public MessageManager(JavaPlugin plugin) {
      this.plugin = plugin;
   }

   public void load() {
      FileConfiguration config = plugin.getConfig();
      prefix = ChatColor.translateAlternateColorCodes('&', config.getString("prefix", "&8[&bEmblems&8] &r"));
   }

   public String format(String path, String... replacements) {
      String msg = plugin.getConfig().getString("messages." + path);
      if (msg == null || msg.isEmpty()) return "";
      msg = ChatColor.translateAlternateColorCodes('&', msg);
      msg = msg.replace("%prefix%", prefix);
      if (replacements != null) {
         for (int i = 0; i < replacements.length - 1; i += 2) {
            String key = replacements[i];
            String value = replacements[i + 1] != null ? replacements[i + 1] : "";
            msg = msg.replace("%" + key + "%", value);
         }
      }
      return msg;
   }

   public List<String> formatList(String path, String... replacements) {
      List<String> lines = plugin.getConfig().getStringList("messages." + path);
      if (lines == null || lines.isEmpty()) return new java.util.ArrayList<>();
      for (int i = 0; i < lines.size(); i++) {
         String line = lines.get(i);
         line = ChatColor.translateAlternateColorCodes('&', line);
         line = line.replace("%prefix%", prefix);
         if (replacements != null) {
            for (int r = 0; r < replacements.length - 1; r += 2) {
               String key = replacements[r];
               String value = replacements[r + 1] != null ? replacements[r + 1] : "";
               line = line.replace("%" + key + "%", value);
            }
         }
         lines.set(i, line);
      }
      return lines;
   }
}
