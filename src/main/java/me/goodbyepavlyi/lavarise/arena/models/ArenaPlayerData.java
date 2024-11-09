package me.goodbyepavlyi.lavarise.arena.models;

import me.goodbyepavlyi.lavarise.utils.Logger;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

public class ArenaPlayerData {
    private final ArenaPlayer arenaPlayer;
    private ItemStack[] inventoryContents;
    private ItemStack[] armorContents;
    private Location location;
    private double health;
    private int foodLevel;
    private GameMode gameMode;
    private boolean allowFlight;

    public ArenaPlayerData(ArenaPlayer arenaPlayer) {
        this.arenaPlayer = arenaPlayer;
    }

    public ArenaPlayerData(ArenaPlayer arenaPlayer, Player player) {
        this.arenaPlayer = arenaPlayer;
        this.storePlayerData(player);
    }

    private void storePlayerData(Player player) {
        this.inventoryContents = player.getInventory().getContents();
        this.armorContents = player.getInventory().getArmorContents();
        this.location = player.getLocation();
        this.health = player.getHealth();
        this.foodLevel = player.getFoodLevel();
        this.gameMode = player.getGameMode();
        this.allowFlight = player.getAllowFlight();

        Logger.debug(String.format("Stored data for player %s", player.getName()));
    }

    public void restoreAll() {
        restoreLocation();
        restoreArmorContents();
        restoreInventoryContents();
        restoreHealth();
        restoreFoodLevel();
        restoreGameMode();
        restoreAllowFlight();

        Logger.debug(String.format("Restored data for player %s", this.arenaPlayer.getPlayer().getName()));
    }

    public void restoreInventoryContents() {
        this.arenaPlayer.getPlayer().getInventory().setContents(this.inventoryContents);
        this.arenaPlayer.getPlayer().updateInventory();
    }

    public void restoreArmorContents() {
        this.arenaPlayer.getPlayer().getInventory().setArmorContents(this.armorContents);
    }

    public void restoreLocation() {
        this.arenaPlayer.getPlayer().teleport(this.location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    public void restoreHealth() {
        this.arenaPlayer.getPlayer().setHealth(this.health);
    }

    public void restoreFoodLevel() {
        this.arenaPlayer.getPlayer().setFoodLevel(this.foodLevel);
    }

    public void restoreGameMode() {
        this.arenaPlayer.getPlayer().setGameMode(this.gameMode);
    }

    public void restoreAllowFlight() {
        this.arenaPlayer.getPlayer().setAllowFlight(this.allowFlight);
    }
}
