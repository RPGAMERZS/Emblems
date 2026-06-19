package com.emblems;

import org.bukkit.entity.Player;

public class NoneEconomyProvider implements EconomyProvider {
    @Override
    public double getBalance(Player player) {
        return 0;
    }

    @Override
    public boolean has(Player player, double amount) {
        return false;
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        return false;
    }

    @Override
    public String getName() {
        return "None";
    }
}
