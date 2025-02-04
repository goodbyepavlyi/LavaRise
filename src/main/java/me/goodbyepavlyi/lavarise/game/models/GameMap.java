package me.goodbyepavlyi.lavarise.game.models;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.arena.Arena;
import me.goodbyepavlyi.lavarise.arena.utils.ArenaConfig;
import me.goodbyepavlyi.lavarise.configs.Config;
import me.goodbyepavlyi.lavarise.game.Game;
import me.goodbyepavlyi.lavarise.utils.Logger;
import me.goodbyepavlyi.lavarise.utils.WorldUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class GameMap {
    private final LavaRiseInstance instance;
    private final Game game;
    private final Arena arena;
    private World gameWorld;
    private List<Location> spawnpoints;
    private BukkitTask lavaFillTask;

    public GameMap(LavaRiseInstance instance, Game game, Arena arena) {
        this.instance = instance;
        this.game = game;
        this.arena = arena;
    }

    public String getMapName() {
        return String.format("LavaRiseGame-%s", this.arena.getName());
    }

    public void createGameWorld() {
        this.gameWorld = WorldUtils.copyWorld(this.instance, this.arena.getConfig().getGameAreaWorld(), this.getMapName());
    }

    public void deleteGameWorld() {
        WorldUtils.deleteWorld(this.instance, this.gameWorld.getName());
    }

    public Location getSpawnpoint() {
        if (this.spawnpoints == null || this.spawnpoints.isEmpty()) this.createSpawnpoints();

        int index = (int) (Math.random() * this.spawnpoints.size());
        Location spawnpoint = this.spawnpoints.get(index);
        this.spawnpoints.remove(index);

        return spawnpoint;
    }

    public void createSpawnpoints() {
        Location gameAreaTop = this.arena.getConfig().getGameArea(ArenaConfig.GameArea.TOP);
        Location gameAreaBottom = this.arena.getConfig().getGameArea(ArenaConfig.GameArea.BOTTOM);
        int amount = this.arena.getPlayers().size();
        List<Location> spawnpoints = new ArrayList<>(amount);

        int minX = Math.min(gameAreaTop.getBlockX(), gameAreaBottom.getBlockX());
        int maxX = Math.max(gameAreaTop.getBlockX(), gameAreaBottom.getBlockX());
        int minZ = Math.min(gameAreaTop.getBlockZ(), gameAreaBottom.getBlockZ());
        int maxZ = Math.max(gameAreaTop.getBlockZ(), gameAreaBottom.getBlockZ());

        while (spawnpoints.size() < amount) {
            int x = minX + (int) (Math.random() * (maxX - minX + 1));
            int z = minZ + (int) (Math.random() * (maxZ - minZ + 1));

            for (int y = gameAreaTop.getBlockY(); y >= gameAreaBottom.getBlockY(); y--) {
                Location potentialLocation = new Location(this.gameWorld, x + 0.5, y, z + 0.5);
                Block blockBelow = potentialLocation.clone().subtract(0, 1, 0).getBlock();
                Block currentBlock = potentialLocation.getBlock();

                // Check if the block below is solid, and the current block is not obstructed
                if (!blockBelow.isEmpty() && !blockBelow.isLiquid() && currentBlock.getType() == Material.AIR) {
                    spawnpoints.add(potentialLocation);
                    Logger.debug(String.format("Spawnpoint created at %s for arena '%s'.", potentialLocation, arena.getName()));
                    break;
                }
            }
        }

        this.spawnpoints = spawnpoints;
    }

    public Location createSpectatorSpawnpoint() {
        Location gameAreaTop = this.arena.getConfig().getGameArea(ArenaConfig.GameArea.TOP);
        Location gameAreaBottom = this.arena.getConfig().getGameArea(ArenaConfig.GameArea.BOTTOM);

        int x = (gameAreaTop.getBlockX() + gameAreaBottom.getBlockX()) / 2;
        int z = (gameAreaTop.getBlockZ() + gameAreaBottom.getBlockZ()) / 2;

        // Get the highest block in the area but limit the height to within the game area
        double highestY = this.gameWorld.getHighestBlockYAt(x, z);
        double y = Math.min(Math.max(highestY, this.game.getCurrentLavaY()), gameAreaTop.getY());

        Location spectatorLocation = new Location(this.gameWorld, x, y + Game.GameSpectatorSpawnYLavaOffset, z);
        Logger.debug(String.format("Created spectator spawn point at %s for arena '%s'.", spectatorLocation, this.arena.getName()));
        return spectatorLocation;
    }

    public void fillArea(Material material, int x1, int y1, int z1, int x2, int y2, int z2) {
        Logger.debug(String.format("Filling area in arena '%s' with %s from (%d, %d, %d) to (%d, %d, %d).",
                arena.getName(), material, x1, y1, z1, x2, y2, z2));

        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++)
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++)
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++)
                    this.gameWorld.getBlockAt(x, y, z).setType(material);
    }

    public void fillLava() {
        Location gameAreaBottom = this.arena.getConfig().getGameArea(ArenaConfig.GameArea.BOTTOM);
        Location gameAreaTop = this.arena.getConfig().getGameArea(ArenaConfig.GameArea.TOP);

        this.game.setCurrentLavaY(this.game.getCurrentLavaY() + 1);
        this.fillArea(
            Material.LAVA,
            gameAreaBottom.getBlockX(), gameAreaBottom.getBlockY(), gameAreaBottom.getBlockZ(),
            gameAreaTop.getBlockX(), this.game.getCurrentLavaY(), gameAreaTop.getBlockZ()
        );

        Logger.debug(String.format("Lava filled on level Y %d in arena '%s'.", this.game.getCurrentLavaY(), this.arena.getName()));
        this.game.getGameScoreboard().update();

        if (this.game.getCurrentLavaY() >= gameAreaTop.getBlockY()
                || this.arena.getConfig().isLavaLevelSet() && this.game.getCurrentLavaY() >= this.arena.getConfig().getLavaLevel()) {
            Logger.debug(String.format("Lava has reached the top of the map in arena '%s'.", this.arena.getName()));
            this.game.setGamePhase(Game.GamePhase.DEATHMATCH);
            this.stopLavaFillTask();
        }
    }

    public void fillLavaPeriodically() {
        this.fillLavaPeriodically(this.instance.getConfiguration().GameLavaRisingTimeDefault(), 0);
    }

    public void fillLavaPeriodically(int time, int delay) {
        this.lavaFillTask = this.instance.getServer().getScheduler().runTaskTimer(
            this.instance,
            () -> {
                if (!this.game.getGamePhase().equals(Game.GamePhase.LAVA)) return;
                this.fillLava();

                Config.LavaLevelConfig lavaLevelConfig = this.instance.getConfiguration().getGameLavaRisingTime(this.game.getCurrentLavaY());
                Config.LavaLevelConfig nextConfig = this.instance.getConfiguration().getGameLavaRisingTime(this.game.getCurrentLavaY() + 1);

                if (lavaLevelConfig.getLevel() != nextConfig.getLevel()) {
                    Logger.debug(String.format("Changing lava fill task time to %d for arena '%s'.", nextConfig.getTime(), this.arena.getName()));
                    this.stopLavaFillTask();
                    this.fillLavaPeriodically(lavaLevelConfig.getTime(), lavaLevelConfig.getTime());
                }
            }, delay * 20L, time * 20L
        );

        this.arena.getTasks().add(this.lavaFillTask);

        Logger.debug(String.format("Lava fill task started for arena '%s', with time %d and delay %d.", this.arena.getName(), time, delay));
    }

    public void stopLavaFillTask() {
        if (this.lavaFillTask != null) {
            this.arena.getTasks().remove(this.lavaFillTask);
            this.lavaFillTask.cancel();
            Logger.debug(String.format("Lava fill task stopped for arena '%s'.", this.arena.getName()));
        }
    }

    public boolean isLocationInsideMap(Location location) {
        Location gameAreaBottom = this.arena.getConfig().getGameArea(ArenaConfig.GameArea.BOTTOM);
        Location gameAreaTop = this.arena.getConfig().getGameArea(ArenaConfig.GameArea.TOP);

        int locationX = location.getBlockX();
        int locationY = location.getBlockY();
        int locationZ = location.getBlockZ();

        int bottomX = Math.min(gameAreaBottom.getBlockX(), gameAreaTop.getBlockX());
        int bottomY = Math.min(gameAreaBottom.getBlockY(), gameAreaTop.getBlockY());
        int bottomZ = Math.min(gameAreaBottom.getBlockZ(), gameAreaTop.getBlockZ());

        int topX = Math.max(gameAreaBottom.getBlockX(), gameAreaTop.getBlockX());
        int topY = Math.max(gameAreaBottom.getBlockY(), gameAreaTop.getBlockY());
        int topZ = Math.max(gameAreaBottom.getBlockZ(), gameAreaTop.getBlockZ());

        return (locationX >= bottomX && locationX <= topX)
                && (locationY >= bottomY && locationY <= topY)
                && (locationZ >= bottomZ && locationZ <= topZ);
    }
}
