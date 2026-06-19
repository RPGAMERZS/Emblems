package com.emblems;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerData {
   private final UUID uuid;
   private String activeId;
   private final Set<String> purchased;

   public PlayerData(UUID uuid) {
      this.uuid = uuid;
      this.activeId = "none";
      this.purchased = new HashSet<>();
   }

   public UUID getUuid() {
      return this.uuid;
   }

   public String getActiveId() {
      return this.activeId;
   }

   public void setActiveId(String activeId) {
      this.activeId = activeId;
   }

   public Set<String> getPurchased() {
      return this.purchased;
   }

   public boolean hasPurchased(String id) {
      return this.purchased.contains(id);
   }

   public void addPurchased(String id) {
      this.purchased.add(id);
   }

   public void removePurchased(String id) {
      this.purchased.remove(id);
   }
}
