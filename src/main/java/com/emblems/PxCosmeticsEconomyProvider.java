package com.emblems;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.refinedev.cosmetics.pxCosmetics;
import xyz.refinedev.cosmetics.profile.CosmeticProfile;

public class PxCosmeticsEconomyProvider implements EconomyProvider {
    private final JavaPlugin plugin;

    public PxCosmeticsEconomyProvider(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean debugLogs() {
        return plugin.getConfig().getBoolean("economy.debug-logs", false);
    }

    @Override
    public double getBalance(Player player) {
        CosmeticProfile profile = getProfile(player);
        double bal = profile != null ? profile.getCoins() : 0;
        if (debugLogs()) {
            plugin.getLogger().info("[PxCosmetics] getBalance(" + player.getName() + ") = " + bal);
        }
        return bal;
    }

    @Override
    public boolean has(Player player, double amount) {
        CosmeticProfile profile = getProfile(player);
        boolean result = profile != null && profile.getCoins() >= (int) amount;
        if (debugLogs()) {
            plugin.getLogger().info("[PxCosmetics] has(" + player.getName() + ", " + amount + ") = " + result + " (coins=" + (profile != null ? profile.getCoins() : "null") + ")");
        }
        return result;
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        CosmeticProfile profile = getProfile(player);
        if (profile == null) {
            plugin.getLogger().warning("[PxCosmetics] withdraw(" + player.getName() + ", " + amount + ") FAILED - profile is null");
            return false;
        }
        if (profile.getCoins() < (int) amount) {
            plugin.getLogger().warning("[PxCosmetics] withdraw(" + player.getName() + ", " + amount + ") FAILED - insufficient coins (" + profile.getCoins() + ")");
            return false;
        }
        profile.setCoins(profile.getCoins() - (int) amount);
        if (debugLogs()) {
            plugin.getLogger().info("[PxCosmetics] withdraw(" + player.getName() + ", " + amount + ") SUCCESS");
        }
        return true;
    }

    @Override
    public String getName() {
        return "PXCosmetics";
    }

    private CosmeticProfile getProfile(Player player) {
        pxCosmetics api = pxCosmetics.getInstance();
        if (api == null) {
            plugin.getLogger().warning("[PxCosmetics] pxCosmetics.getInstance() returned null!");
            return null;
        }
        if (api.getProfileHandler() == null) {
            plugin.getLogger().warning("[PxCosmetics] api.getProfileHandler() returned null!");
            return null;
        }
        CosmeticProfile profile = api.getProfileHandler().getProfile(player);
        if (profile == null) {
            plugin.getLogger().warning("[PxCosmetics] getProfile(" + player.getName() + ") returned null!");
        }
        return profile;
    }
}
