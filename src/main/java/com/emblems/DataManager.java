package com.emblems;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class DataManager {
   private final JavaPlugin plugin;
   private final Map<UUID, PlayerData> cache;
   private final File dataFolder;

   public DataManager(JavaPlugin plugin) {
      this.plugin = plugin;
      this.cache = new ConcurrentHashMap<>();
      this.dataFolder = new File(plugin.getDataFolder(), "data");
      if (!this.dataFolder.exists()) {
         this.dataFolder.mkdirs();
      }

   }

   public void loadPlayer(UUID uuid) {
      PlayerData pd = new PlayerData(uuid);
      File file = new File(this.dataFolder, uuid.toString() + ".yml");
      if (file.exists()) {
         YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
         pd.setActiveId(config.getString("active", "none"));
         pd.getPurchased().addAll(config.getStringList("purchased"));
      }

      this.cache.put(uuid, pd);
   }

   public void savePlayer(UUID uuid) {
      PlayerData pd = (PlayerData)this.cache.get(uuid);
      if (pd != null) {
         File file = new File(this.dataFolder, uuid.toString() + ".yml");
         YamlConfiguration config = new YamlConfiguration();
         config.set("active", pd.getActiveId());
         config.set("purchased", pd.getPurchased().isEmpty() ? null : new ArrayList(pd.getPurchased()));

         try {
            config.save(file);
         } catch (IOException e) {
            this.plugin.getLogger().severe("Could not save player data for " + uuid + ": " + e.getMessage());
         }

      }
   }

   public void unloadPlayer(UUID uuid) {
      this.savePlayer(uuid);
      this.cache.remove(uuid);
   }

   public PlayerData getPlayerData(UUID uuid) {
      PlayerData pd = (PlayerData)this.cache.get(uuid);
      if (pd == null) {
         this.loadPlayer(uuid);
         pd = (PlayerData)this.cache.get(uuid);
      }

      return pd;
   }

   public Collection<PlayerData> getAllPlayerData() {
      return this.cache.values();
   }

   public void saveAll() {
      for(UUID uuid : this.cache.keySet()) {
         this.savePlayer(uuid);
      }

   }
}
