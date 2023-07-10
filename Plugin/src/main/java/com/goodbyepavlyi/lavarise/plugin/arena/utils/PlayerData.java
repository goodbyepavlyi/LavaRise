package com.goodbyepavlyi.lavarise.plugin.arena.utils;

import com.goodbyepavlyi.lavarise.plugin.game.utils.GamePlayer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class PlayerData {
    private final GamePlayer gamePlayer;
    private ItemStack[] inventoryContents;
    private ItemStack[] armorContents;
    private Location location;
    private double health;
    private int foodLevel;
    private GameMode gameMode;

    public PlayerData(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public PlayerData(GamePlayer gamePlayer, ItemStack[] inventoryContents, ItemStack[] armorContents, Location location, double health, int foodLevel, GameMode gameMode) {
        this.gamePlayer = gamePlayer;
        this.inventoryContents = inventoryContents;
        this.armorContents = armorContents;
        this.location = location;
        this.health = health;
        this.foodLevel = foodLevel;
        this.gameMode = gameMode;
    }

    public void restoreInventoryContents() {
        this.gamePlayer.getPlayer().getInventory().setContents(this.inventoryContents);
        this.gamePlayer.getPlayer().updateInventory();
    }

    public void restoreArmorContents() {
        this.gamePlayer.getPlayer().getInventory().setArmorContents(this.armorContents);
    }

    public void restoreLocation() {
        this.gamePlayer.getPlayer().teleport(this.location);
    }

    public void restoreHealth() {
        this.gamePlayer.getPlayer().setHealth(this.health);
    }

    public void restoreFoodLevel() {
        this.gamePlayer.getPlayer().setFoodLevel(this.foodLevel);
    }

    public void restoreGameMode() {
        this.gamePlayer.getPlayer().setGameMode(this.gameMode);
    }

    public void restoreAll() {
        this.restoreArmorContents();
        this.restoreInventoryContents();
        this.restoreHealth();
        this.restoreFoodLevel();
        this.restoreGameMode();
        this.restoreLocation();
    }
}
