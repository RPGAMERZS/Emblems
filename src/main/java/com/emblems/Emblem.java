package com.emblems;

import java.util.List;

public class Emblem {
   private final String id;
   private final String tag;
   private final String displayname;
   private final String permission;
   private final int price;
   private final String material;
   private final boolean enchanted;
   private final List<String> lore;
   private final String noPermissionMessage;
   private final String notEnoughMoneyMessage;

   public Emblem(String id, String tag, String displayname, String permission, int price, String material, boolean enchanted, List<String> lore, String noPermissionMessage, String notEnoughMoneyMessage) {
      this.id = id;
      this.tag = tag;
      this.displayname = displayname;
      this.permission = permission;
      this.price = price;
      this.material = material;
      this.enchanted = enchanted;
      this.lore = lore;
      this.noPermissionMessage = noPermissionMessage;
      this.notEnoughMoneyMessage = notEnoughMoneyMessage;
   }

   public String getId() {
      return this.id;
   }

   public String getTag() {
      return this.tag;
   }

   public String getDisplayname() {
      return this.displayname;
   }

   public String getPermission() {
      return this.permission;
   }

   public int getPrice() {
      return this.price;
   }

   public String getMaterial() {
      return this.material;
   }

   public boolean isEnchanted() {
      return this.enchanted;
   }

   public List<String> getLore() {
      return this.lore;
   }

   public String getNoPermissionMessage() {
      return this.noPermissionMessage;
   }

   public String getNotEnoughMoneyMessage() {
      return this.notEnoughMoneyMessage;
   }

   public boolean isPurchasable() {
      return this.price > 0;
   }
}
