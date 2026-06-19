package com.emblems;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
   private final JavaPlugin plugin;
   private FileConfiguration guiConfig;
   private File guiFile;
   private final Map<String, Emblem> emblems;
   private static final Pattern UNICODE_PATTERN = Pattern.compile("\\\\u([0-9a-fA-F]{4})");

   public ConfigManager(JavaPlugin plugin) {
      this.plugin = plugin;
      this.emblems = new LinkedHashMap<>();
   }

   public void load() {
      this.plugin.saveDefaultConfig();
      this.plugin.reloadConfig();
      this.loadGuiConfig();
      this.loadEmblems();
   }

   private void loadGuiConfig() {
      this.guiFile = new File(this.plugin.getDataFolder(), "gui.yml");
      if (!this.guiFile.exists()) {
         this.plugin.saveResource("gui.yml", false);
      }

      this.guiConfig = YamlConfiguration.loadConfiguration(this.guiFile);
      InputStream defStream = this.plugin.getResource("gui.yml");
      if (defStream != null) {
         this.guiConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defStream)));
      }

   }

   private String parseUnicode(String input) {
      if (input != null && !input.isEmpty()) {
         Matcher matcher = UNICODE_PATTERN.matcher(input);
         StringBuffer sb = new StringBuffer();

         while(matcher.find()) {
            matcher.appendReplacement(sb, String.valueOf((char)Integer.parseInt(matcher.group(1), 16)));
         }

         matcher.appendTail(sb);
         return sb.toString();
      } else {
         return input;
      }
   }

   private List<String> parseUnicode(List<String> input) {
      if (input == null) {
         return new ArrayList<>();
      } else {
         List<String> result = new ArrayList<>();

         for(String line : input) {
            result.add(this.parseUnicode(line));
         }

         return result;
      }
   }

   private String getField(ConfigurationSection section, String primary, String fallback) {
      if (section.contains(primary)) {
         return section.getString(primary);
      } else {
         return section.contains(fallback) ? section.getString(fallback) : "";
      }
   }

   private void loadEmblems() {
      this.emblems.clear();
      FileConfiguration config = this.plugin.getConfig();
      ConfigurationSection section = config.getConfigurationSection("emblems");
      if (section != null) {
         for(String key : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(key);
            if (entry != null) {
               String rawTag = this.parseUnicode(this.getField(entry, "Tag", "symbol"));
               String tag = ChatColor.translateAlternateColorCodes('&', rawTag);
               String displayname = ChatColor.translateAlternateColorCodes('&', this.getField(entry, "Displayname", "name"));
               if (displayname.isEmpty()) {
                  displayname = key;
               }

               String permission = this.getField(entry, "Permission", "permission");
               int price;
               if (entry.contains("Price")) {
                  price = entry.getInt("Price");
               } else {
                  price = entry.getInt("cost", 0);
               }

               String material = entry.getString("Material");
               if (material == null) {
                  material = this.getField(entry, "material", "");
               }

               if (material.isEmpty()) {
                  material = "PAPER";
               }

               boolean enchanted = entry.getBoolean("enchanted", false);
               ConfigurationSection loreSection = entry.getConfigurationSection("Lore");
               List<String> lore = new ArrayList<>();
               if (loreSection != null) {
                  lore = this.parseUnicode(loreSection.getStringList("Locked"));
                  if (lore.isEmpty()) {
                     lore = this.parseUnicode(loreSection.getStringList("Unlocked"));
                  }

                  if (lore.isEmpty()) {
                     lore = this.parseUnicode(loreSection.getStringList("Selected"));
                  }

                  if (lore.isEmpty()) {
                     lore = this.parseUnicode(loreSection.getStringList(""));
                  }
               }

               List<String> flatLore = entry.getStringList("Lore");
               if (!flatLore.isEmpty()) {
                  lore = this.parseUnicode(flatLore);
               }

               if (lore.isEmpty()) {
                  lore.add("&7Click to interact");
               }

               String noPermissionMsg = this.getField(entry, "No-permission-message", "no-permission-message");
               String notEnoughMoneyMsg = this.getField(entry, "Not-enough-money-message", "not-enough-money-message");
               this.emblems.put(key, new Emblem(key, tag, displayname, permission, price, material, enchanted, lore, noPermissionMsg, notEnoughMoneyMsg));
            }
         }

      }
   }

   public Emblem getEmblem(String id) {
      return (Emblem)this.emblems.get(id);
   }

   public Collection<Emblem> getEmblems() {
      return this.emblems.values();
   }

   public int getEmblemCount() {
      return this.emblems.size();
   }

   public FileConfiguration getGuiConfig() {
      return this.guiConfig;
   }

   public void reloadGuiConfig() {
      this.guiConfig = YamlConfiguration.loadConfiguration(this.guiFile);
   }

   public boolean emblemExists(String id) {
      return this.emblems.containsKey(id);
   }
}
