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
    }

    public void restoreInventoryContents() {
        this.arenaPlayer.getPlayer().getInventory().setContents(this.inventoryContents);
        this.arenaPlayer.getPlayer().updateInventory();
        Logger.debug(String.format("Restored inventory contents for player %s", this.arenaPlayer.getPlayer().getName()));
    }

    public void restoreArmorContents() {
        this.arenaPlayer.getPlayer().getInventory().setArmorContents(this.armorContents);
        Logger.debug(String.format("Restored armor contents for player %s", this.arenaPlayer.getPlayer().getName()));
    }

    public void restoreLocation() {
        this.arenaPlayer.getPlayer().teleport(this.location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        Logger.debug(String.format("Restored location for player %s to %s", this.arenaPlayer.getPlayer().getName(), this.location));
    }

    public void restoreHealth() {
        this.arenaPlayer.getPlayer().setHealth(this.health);
        Logger.debug(String.format("Restored health for player %s", this.arenaPlayer.getPlayer().getName()));
    }

    public void restoreFoodLevel() {
        this.arenaPlayer.getPlayer().setFoodLevel(this.foodLevel);
        Logger.debug(String.format("Restored food level for player %s", this.arenaPlayer.getPlayer().getName()));
    }

    public void restoreGameMode() {
        this.arenaPlayer.getPlayer().setGameMode(this.gameMode);
        Logger.debug(String.format("Restored game mode for player %s", this.arenaPlayer.getPlayer().getName()));
    }

    public void restoreAllowFlight() {
        this.arenaPlayer.getPlayer().setAllowFlight(this.allowFlight);
        Logger.debug(String.format("Restored allow flight for player %s", this.arenaPlayer.getPlayer().getName()));
    }
}
