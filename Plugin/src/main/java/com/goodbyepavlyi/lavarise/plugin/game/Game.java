package com.goodbyepavlyi.lavarise.plugin.game;

import com.goodbyepavlyi.lavarise.plugin.LavaRise;
import com.goodbyepavlyi.lavarise.plugin.arena.Arena;
import com.goodbyepavlyi.lavarise.plugin.arena.utils.ArenaOptions;
import com.goodbyepavlyi.lavarise.plugin.game.utils.GamePlayer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

public class Game {
    private final LavaRise instance;
    private final Arena arena;
    private final GameMap gameMap;
    private GamePhase gamePhase;
    private final GameScoreboard gameScoreboard;
    private long gameTime;
    private int currentLavaY;

    public Game(LavaRise instance, Arena arena) {
        this.instance = instance;
        this.arena = arena;
        this.gameScoreboard = new GameScoreboard(instance, arena, this);
        this.gameMap = new GameMap(instance, this, arena);
        this.gamePhase = GamePhase.GRACE;
    }

    public enum GamePhase {
        GRACE("Grace"),
        LAVA("Lava"),
        DEATHMATCH("Deathmatch");

        private final String name;

        GamePhase(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public LavaRise getInstance() {
        return this.instance;
    }

    public Arena getArena() {
        return this.arena;
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public GamePhase getGamePhase() {
        return this.gamePhase;
    }

    public void setGamePhase(GamePhase gamePhase) {
        this.gamePhase = gamePhase;
    }

    public long getGameTime() {
        return this.gameTime;
    }

    public int getCurrentLavaY() {
        return this.currentLavaY;
    }

    public void setCurrentLavaY(int currentLavaY) {
        this.currentLavaY = currentLavaY;
    }

    public void removePlayer(Player player) {
        GamePlayer gamePlayer = this.getArena().getPlayer(player.getUniqueId());
        this.getArena().getPlayers().remove(gamePlayer);
    }

    public void start() {
        this.arena.setState(Arena.State.IN_GAME);
        this.gameTime = System.currentTimeMillis();

        this.gameMap.saveOriginalBlocks();
        this.gameMap.createSpawnpoints();

        this.currentLavaY = this.getArena().getOptions().getGameArea(ArenaOptions.GameArea.BOTTOM).getBlockY();

        this.gameScoreboard.updateScoreboardPeriodically();
        this.setupPlayers();

        this.switchToLavaMode();
        this.gameMap.fillLavaPeriodically();

        this.instance.debug(Level.INFO, String.format("Game started in arena %s", this.arena.getName()));
    }

    public void stop() {
        this.arena.announce(Arena.Announce.GAME_END);

        this.arena.doForAllPlayers(GamePlayer::restoreData);

        this.gameMap.getLavaFillTask().cancel();
        this.gameMap.restoreOriginalBlocks();

        this.arena.reset();

        this.instance.debug(Level.INFO, String.format("Game stopped in arena %s", this.arena.getName()));
    }

    public void setupPlayers() {
        this.arena.doForAllPlayers(gamePlayer -> {
            Player player = gamePlayer.getPlayer();

            // Teleport player to spawn-point
            Location spawnPoint = this.gameMap.getSpawnpoints().remove((int) (Math.random() * this.gameMap.getSpawnpoints().size()));
            player.teleport(spawnPoint);
            
            // Default initialization of player
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setFireTicks(0);

            // Default inventory
            player.getInventory().clear();
            player.getInventory().addItem(
                    this.instance.getConfiguration().GAME_ITEMS().stream()
                            .map(ItemStack::new)
                            .toArray(ItemStack[]::new)
            );

            this.instance.debug(Level.INFO, String.format("Player %s has been set up in arena %s", player.getName(), this.arena.getName()));
        });
    }

    public void switchToLavaMode() {
        this.instance.getServer().getScheduler().runTaskLater(
            this.instance,
            () -> {
                this.setGamePhase(GamePhase.LAVA);
                this.instance.debug(Level.INFO, String.format("Switched to Lava mode in arena %s", this.arena.getName()));
            },
            (this.instance.getConfiguration().GAME_GRACEPHASETIME() * 20L)
        );
    }

    public void checkForWinner() {
        if (this.arena.getPlayers().stream().filter(gamePlayer -> !gamePlayer.isSpectator()).count() > 1) return;

        this.stop();

        this.instance.debug(Level.INFO, String.format("Checked for winner in arena %s", this.arena.getName()));
    }

    public void makeSpectator(Player player) {
        GamePlayer gamePlayer = this.arena.getPlayer(player.getUniqueId());

        gamePlayer.spectate();

        this.instance.debug(Level.INFO, String.format("Player %s has become a spectator in arena %s", player.getName(), this.arena.getName()));
    }
}