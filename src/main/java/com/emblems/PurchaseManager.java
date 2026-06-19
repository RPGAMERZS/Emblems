package com.emblems;

import org.bukkit.entity.Player;

public class PurchaseManager {
   private final EmblemsPlugin plugin;
   private final EconomyProvider economy;

   public PurchaseManager(EmblemsPlugin plugin, EconomyProvider economy) {
      this.plugin = plugin;
      this.economy = economy;
   }

   public boolean purchase(Player player, Emblem emblem) {
      PlayerData data = this.plugin.getDataManager().getPlayerData(player.getUniqueId());
      if (data.hasPurchased(emblem.getId())) {
         player.sendMessage(this.plugin.getMessageManager().format("purchase.already-owned"));
         return false;
      } else if (emblem.getPermission() != null && !emblem.getPermission().isEmpty() && player.hasPermission(emblem.getPermission())) {
         player.sendMessage(this.plugin.getMessageManager().format("purchase.has-permission"));
         return false;
      } else {
         int cost = emblem.getPrice();
         if (cost <= 0) {
            data.addPurchased(emblem.getId());
            this.plugin.getDataManager().savePlayer(player.getUniqueId());
            player.sendMessage(this.plugin.getMessageManager().format("purchase.obtained", "emblem", emblem.getDisplayname()));
            return true;
          } else if (this.economy.has(player, (double)cost)) {
             this.economy.withdraw(player, (double)cost);
            data.addPurchased(emblem.getId());
            this.plugin.getDataManager().savePlayer(player.getUniqueId());
            player.sendMessage(this.plugin.getMessageManager().format("purchase.success", "emblem", emblem.getDisplayname()));
            return true;
         } else {
            String msg = emblem.getNotEnoughMoneyMessage();
            if (msg != null && !msg.isEmpty()) {
               player.sendMessage(msg.replace("{TAG}", emblem.getTag()).replace("{DISPLAYNAME}", emblem.getDisplayname()).replace("{PRICE}", String.valueOf(cost)));
            } else {
               player.sendMessage(this.plugin.getMessageManager().format("purchase.not-enough-money"));
            }

            return false;
         }
      }
   }
}
