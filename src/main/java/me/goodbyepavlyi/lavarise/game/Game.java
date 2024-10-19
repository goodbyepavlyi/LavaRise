package me.goodbyepavlyi.lavarise.game;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.arena.Arena;
import me.goodbyepavlyi.lavarise.arena.utils.ArenaConfig;
import me.goodbyepavlyi.lavarise.arena.models.ArenaPlayer;
import me.goodbyepavlyi.lavarise.game.models.GameMap;
import me.goodbyepavlyi.lavarise.game.models.GameScoreboard;
import me.goodbyepavlyi.lavarise.utils.Logger;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.stream.Collectors;

public class Game {
    private static int GameSpectatorSpawnYLavaOffset;

    private final Arena arena;
    private final LavaRiseInstance instance;
    private final GameMap gameMap;
    private GamePhase gamePhase;
    private final GameScoreboard gameScoreboard;
    private long gameTime;
    private int currentLavaY;

    public Game(Arena arena, LavaRiseInstance instance) {
        this.arena = arena;
        this.instance = instance;
        this.gameScoreboard = new GameScoreboard(instance, arena, this);
        this.gameMap = new GameMap(instance, this, arena);
        this.gamePhase = GamePhase.GRACE;

        GameSpectatorSpawnYLavaOffset = this.instance.getConfiguration().GameSpectatorSpawnYLavaOffset();
    }

    public enum GamePhase {
        GRACE("Grace"),
        LAVA("Lava");
//        DEATHMATCH("Deathmatch");

        private final String name;

        GamePhase(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public GameScoreboard getGameScoreboard() {
        return gameScoreboard;
    }

    public GamePhase getGamePhase() {
        return this.gamePhase;
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

    public Set<ArenaPlayer> getSpectators() {
        return this.arena.getPlayers().stream()
            .filter(ArenaPlayer::isSpectator)
            .collect(Collectors.toSet());
    }

    public boolean isSpectator(Player player) {
        return this.arena.getPlayer(player.getUniqueId()).isSpectator();
    }

    public void start() {
        this.arena.setState(Arena.State.IN_GAME);
        this.gameTime = System.currentTimeMillis();

        this.gameMap.saveOriginalBlocks();
        this.gameMap.createSpawnpoints();

        this.currentLavaY = this.arena.getConfig().getGameArea(ArenaConfig.GameArea.BOTTOM).getBlockY();

        this.gameScoreboard.startScoreboardUpdates();
        this.spawnPlayers();

        new BukkitRunnable() {
            @Override
            public void run() {
                gamePhase = GamePhase.LAVA;
                Logger.debug(String.format("Switched to Lava mode in arena %s", arena.getName()));

                gameMap.fillLavaPeriodically();
            }
        }.runTaskLater(this.instance, (this.instance.getConfiguration().GameGracePhaseTime() * 20L));

        Logger.debug(String.format("Game started in arena %s", this.arena.getName()));
    }

    public void stop() {
        this.arena.announceMessage(Arena.AnnouncementType.GAME_END);

        this.gameScoreboard.stopScoreboardUpdateTask();

        this.gameMap.getLavaFillTask().cancel();
        this.gameMap.restoreOriginalBlocks();

        this.arena.doForAllPlayers(player -> {
            this.arena.removePlayer(player, true);
            this.getSpectators().forEach(spectator -> player.showPlayer(this.instance, spectator.getPlayer()));
        });

        this.arena.reset();
        Logger.debug(String.format("Game stopped in arena %s", this.arena.getName()));
    }

    public void spawnPlayers() {
        this.arena.doForAllPlayers(player -> {
            Location spawnPoint = this.gameMap.getSpawnpoints().remove((int) (Math.random() * this.gameMap.getSpawnpoints().size()));
            player.teleport(spawnPoint, PlayerTeleportEvent.TeleportCause.PLUGIN);
            
            // Default initialization of player
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setFireTicks(0);

            // Default inventory
            player.getInventory().clear();
            player.getInventory().addItem(
                    this.instance.getConfiguration().GameItems().stream()
                            .map(ItemStack::new)
                            .toArray(ItemStack[]::new)
            );

            Logger.debug(String.format("Player %s has been spawned in arena %s", player.getName(), this.arena.getName()));
        });
    }

    public Location createSpectatorSpawnpoint() {
        Location topLocation = this.arena.getConfig().getGameArea(ArenaConfig.GameArea.TOP);
        Location bottomLocation = this.arena.getConfig().getGameArea(ArenaConfig.GameArea.BOTTOM);

        Location spectatorLocation = new Location(
            topLocation.getWorld(),
            (topLocation.getX() + bottomLocation.getX()) / 2,
            this.currentLavaY + GameSpectatorSpawnYLavaOffset,
            (topLocation.getZ() + bottomLocation.getZ()) / 2);

        while (spectatorLocation.getWorld().getBlockAt(spectatorLocation).getType().isSolid())
            spectatorLocation.add(0, 1, 0);

        return spectatorLocation;
    }

    public void checkForWinner() {
        long remainingPlayers = this.arena.getPlayers().stream()
                .filter(arenaPlayer -> !arenaPlayer.isSpectator())
                .count();

        if (remainingPlayers > 1)
            return;

        if (remainingPlayers == 1) {
            ArenaPlayer winner = this.arena.getPlayers().stream()
                    .filter(arenaPlayer -> !arenaPlayer.isSpectator())
                    .findFirst()
                    .orElse(null);

            assert winner != null;
            this.instance.getConfiguration().GameCommandsWinner().forEach(command -> {
                String winnerCommand = command.replace("%winner%", winner.getPlayer().getName());
                this.instance.getServer().dispatchCommand(this.instance.getServer().getConsoleSender(), winnerCommand);
            });
        }

        this.stop();
        Logger.debug(String.format("Checked for winner in arena %s", this.arena.getName()));
    }

    public void removePlayer(ArenaPlayer arenaPlayer) {
        Logger.debug(String.format("Player %s removed from the game in arena %s", arenaPlayer.getPlayer().getName(), this.arena.getName()));
        this.arena.getPlayers().remove(arenaPlayer);

        this.arena.announceMessage(Arena.AnnouncementType.PLAYER_DEATH, arenaPlayer.getPlayer().getName());
        this.checkForWinner();
    }

    public void makeSpectator(Player player) {
        ArenaPlayer arenaPlayer = this.arena.getPlayer(player.getUniqueId());
        if (arenaPlayer == null) return;
        if (arenaPlayer.isSpectator()) {
            Logger.debug(String.format("Player %s is already a spectator in arena %s", player.getName(), this.arena.getName()));
            return;
        }

        arenaPlayer.setSpectator(true);
        this.arena.doForAllPlayers(p -> p.hidePlayer(this.instance, player));
        player.setAllowFlight(true);
        player.setFlying(true);
        player.teleport(this.createSpectatorSpawnpoint(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        this.arena.delayFireTicks(player);

        this.checkForWinner();
        Logger.debug(String.format("Player %s has become a spectator in arena %s", player.getName(), this.arena.getName()));
    }
}