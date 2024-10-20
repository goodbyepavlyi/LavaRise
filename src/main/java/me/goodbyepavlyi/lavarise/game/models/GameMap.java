package me.goodbyepavlyi.lavarise.game.models;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.arena.Arena;
import me.goodbyepavlyi.lavarise.arena.utils.ArenaConfig;
import me.goodbyepavlyi.lavarise.game.Game;
import me.goodbyepavlyi.lavarise.utils.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameMap {
    private final LavaRiseInstance instance;
    private final Game game;
    private final Arena arena;
    private List<Location> spawnpoints;
    private final Map<Location, BlockState> originalBlocks;
    private BukkitTask lavaFillTask;

    public GameMap(LavaRiseInstance instance, Game game, Arena arena) {
        this.instance = instance;
        this.game = game;
        this.arena = arena;
        this.originalBlocks = new HashMap<>();
    }

    public List<Location> getSpawnpoints() {
        if (this.spawnpoints == null) {
            this.createSpawnpoints();
            Logger.debug(String.format("Spawnpoints created for arena '%s'.", arena.getName()));
        }

        return this.spawnpoints;
    }

    public void createSpawnpoints() {
        Location gameAreaTop = this.arena.getConfig().getGameArea(ArenaConfig.GameArea.TOP);
        Location gameAreaBottom = this.arena.getConfig().getGameArea(ArenaConfig.GameArea.BOTTOM);
        int amount = this.arena.getConfig().getMaximumPlayers();
        List<Location> spawnpoints = new ArrayList<>(amount);

        for (int i = 0; i < amount; i++) {
            double x = (gameAreaBottom.getX() + gameAreaTop.getX()) / 2.0 + (Math.random() - 0.5) * 10.0;
            double z = (gameAreaBottom.getZ() + gameAreaTop.getZ()) / 2.0 + (Math.random() - 0.5) * 10.0;

            // Get the highest block in the area but limit the height to within the game area
            double highestY = gameAreaBottom.getWorld().getHighestBlockYAt((int) x, (int) z);
            double minY = gameAreaBottom.getY();
            double maxY = gameAreaTop.getY();

            // Ensure the Y-coordinate is within bounds
            double y = Math.min(Math.max(highestY, minY), maxY);

            Location randomLocation = new Location(gameAreaBottom.getWorld(), x, y, z);
            Block block = randomLocation.getBlock();

            int yOffset = 0;
            while (!block.getType().isSolid() && randomLocation.getY() <= maxY) {
                yOffset += 1;
                randomLocation.add(0, 1, 0);
                block = randomLocation.getBlock();

                if (yOffset >= 10) {
                    Logger.debug(String.format("Failed to create spawnpoint at %s for arena '%s'.", randomLocation, arena.getName()));
                    break;
                }
            }

            spawnpoints.add(randomLocation);
            Logger.debug(String.format("Spawnpoint created at %s for arena '%s'.", randomLocation, arena.getName()));
        }

        this.spawnpoints = spawnpoints;
    }

    public void fillArea(Material material, int x1, int y1, int z1, int x2, int y2, int z2) {
        World gameAreaWorld = this.arena.getConfig().getGameAreaWorld();
        Logger.debug(String.format("Filling area in arena '%s' with %s from (%d, %d, %d) to (%d, %d, %d).",
                arena.getName(), material, x1, y1, z1, x2, y2, z2));

        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++)
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++)
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++)
                    gameAreaWorld.getBlockAt(x, y, z).setType(material);
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

        if (this.game.getCurrentLavaY() >= gameAreaTop.getBlockY()) {
            Logger.debug(String.format("Lava has reached the top of the map in arena '%s'.", this.arena.getName()));
            this.stopLavaFillTask();
        }
    }

    public void fillLavaPeriodically() {
        this.lavaFillTask = this.instance.getServer().getScheduler().runTaskTimer(
            this.instance,
            () -> {
                if (!this.game.getGamePhase().equals(Game.GamePhase.LAVA)) return;
                this.fillLava();
            },
            0L,
            (this.instance.getConfiguration().GameLavaRisingTime() * 20L)
        );

        Logger.debug(String.format("Lava fill task started for arena '%s'.", this.arena.getName()));
    }

    public void stopLavaFillTask() {
        if (this.lavaFillTask != null) {
            this.lavaFillTask.cancel();
            Logger.debug(String.format("Lava fill task stopped for arena '%s'.", this.arena.getName()));
        }
    }

    public void saveOriginalBlocks() {
        World gameAreaWorld = this.arena.getConfig().getGameAreaWorld();
        Location gameAreaTop = this.arena.getConfig().getGameArea(ArenaConfig.GameArea.TOP);
        Location gameAreaBottom = this.arena.getConfig().getGameArea(ArenaConfig.GameArea.BOTTOM);

        for (int x = Math.min(gameAreaTop.getBlockX(), gameAreaBottom.getBlockX()); x <= Math.max(gameAreaTop.getBlockX(), gameAreaBottom.getBlockX()); x++)
            for (int z = Math.min(gameAreaTop.getBlockZ(), gameAreaBottom.getBlockZ()); z <= Math.max(gameAreaTop.getBlockZ(), gameAreaBottom.getBlockZ()); z++)
                for (int y = 0; y <= gameAreaWorld.getMaxHeight(); y++) {
                    Location blockLocation = new Location(gameAreaWorld, x, y, z);
                    BlockState blockState = blockLocation.getBlock().getState();
                    this.originalBlocks.put(blockLocation, blockState);
                }

        Logger.debug(String.format("Original blocks saved for arena '%s'.", this.arena.getName()));
    }

    public void restoreOriginalBlocks() {
        for (Map.Entry<Location, BlockState> entry : this.originalBlocks.entrySet()) {
            Location blockLocation = entry.getKey();
            BlockState originalBlockState = entry.getValue();

            Block block = blockLocation.getBlock();
            block.setType(originalBlockState.getType());
            block.getState().update(true, false);
        }

        Logger.debug(String.format("Original blocks restored for arena '%s'.", this.arena.getName()));
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

        boolean insideMap = (locationX >= bottomX && locationX <= topX)
                && (locationY >= bottomY && locationY <= topY)
                && (locationZ >= bottomZ && locationZ <= topZ);

        Logger.debug(String.format("Location %s is%s inside the map of arena '%s'.", location, insideMap ? "" : " not", this.arena.getName()));
        return insideMap;
    }
}
