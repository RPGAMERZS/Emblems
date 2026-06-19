package com.emblems;

import org.bukkit.entity.Player;

public interface EconomyProvider {
    double getBalance(Player player);
    boolean has(Player player, double amount);
    boolean withdraw(Player player, double amount);
    String getName();
}
