package com.emblems;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

public class VaultEconomyProvider implements EconomyProvider {
    private final Economy economy;

    public VaultEconomyProvider(Economy economy) {
        this.economy = economy;
    }

    @Override
    public double getBalance(Player player) {
        return economy.getBalance(player);
    }

    @Override
    public boolean has(Player player, double amount) {
        return economy.has(player, amount);
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    @Override
    public String getName() {
        return economy.getName();
    }
}
