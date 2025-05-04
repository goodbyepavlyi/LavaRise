package me.goodbyepavlyi.lavarise.game;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.arena.Arena;
import me.goodbyepavlyi.lavarise.arena.utils.ArenaConfig;
import me.goodbyepavlyi.lavarise.arena.models.ArenaPlayer;
import me.goodbyepavlyi.lavarise.configs.Config;
import me.goodbyepavlyi.lavarise.game.models.GameMap;
import me.goodbyepavlyi.lavarise.game.models.GameScoreboard;
import me.goodbyepavlyi.lavarise.utils.Logger;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Game {
    public static int GameSpectatorSpawnYLavaOffset;

    private final Arena arena;
    private final LavaRiseInstance instance;
    private final GameMap gameMap;
    private GamePhase gamePhase = GamePhase.GRACE;
    private final GameScoreboard gameScoreboard;
    private long gameTime;
    private int currentLavaY;
    public boolean isPVPEnabled = false;
    private BukkitTask _pvpGracePeriodTask;

    public Game(Arena arena, LavaRiseInstance instance) {
        this.arena = arena;
        this.instance = instance;
        this.gameScoreboard = new GameScoreboard(instance, arena, this);
        this.gameMap = new GameMap(instance, this, arena);

        GameSpectatorSpawnYLavaOffset = this.instance.getConfiguration().GameSpectatorSpawnYLavaOffset();
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

    public GameMap getGameMap() {
        return gameMap;
    }

    public GameScoreboard getGameScoreboard() {
        return gameScoreboard;
    }

    public GamePhase getGamePhase() {
        return this.gamePhase;
    }

    public void setGamePhase(GamePhase gamePhase) {
        this.gamePhase = gamePhase;

        switch (gamePhase) {
            case LAVA -> {
                this.arena.announceMessage(Arena.AnnouncementType.GAME_LAVAPHASE_START);
                this.playVisualEffect(Config.VisualEffectType.LAVA);
            }
            case DEATHMATCH -> {
                this.enablePVP();
                this.startDeathmatchDamage();
                this.arena.announceMessage(Arena.AnnouncementType.GAME_LAVAPHASE_END);
                this.playVisualEffect(Config.VisualEffectType.DEATHMATCH);
            }
        }
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

    public ArenaPlayer getWinner() {
        return this.arena.getPlayers().stream()
            .filter(arenaPlayer -> !arenaPlayer.isSpectator())
            .findFirst()
            .orElse(null);
    }

    public void start() {
        this.arena.setState(Arena.State.IN_GAME);
        this.gameTime = System.currentTimeMillis();

        this.gameMap.createGameWorld();
        this.currentLavaY = this.arena.getConfig().getGameArea(ArenaConfig.GameArea.BOTTOM).getBlockY();

        this.gameScoreboard.startScoreboardUpdates();
        this.spawnPlayers();

        this.arena.getTasks().add(new BukkitRunnable() {
            @Override
            public void run() {
                setGamePhase(GamePhase.LAVA);
                Logger.debug(String.format("Game phase transitioned to %s in arena '%s'.", gamePhase, arena.getName()));
                gameMap.fillLavaPeriodically();
                enablePVP();
            }
        }.runTaskLater(this.instance, (this.instance.getConfiguration().GameGracePhaseTime() * 20L)));

        Logger.debug(String.format("Game started in arena %s", this.arena.getName()));
    }

    public void stop() {
        Logger.debug(String.format("Stopping game in arena '%s'.", this.arena.getName()));
        this.arena.setState(Arena.State.ENDING);
        this.arena.announceMessage(Arena.AnnouncementType.GAME_END);

        this.gameScoreboard.stopScoreboardUpdateTask();
        this.gameMap.stopLavaFillTask();

        if (!this.instance.isEnabled()) {
            _stop();
            return;
        }

        this.arena.getTasks().add(new BukkitRunnable() {
            @Override
            public void run() {
                _stop();
            }
        }.runTaskLater(this.instance, this.instance.getConfiguration().GameEndGameDelay() * 20L));
    }

    private void _stop() {
        arena.doForAllPlayers(player -> {
            arena.removePlayer(player, true);
            getSpectators().forEach(spectator -> player.showPlayer(instance, spectator.getPlayer()));
        });

        gameMap.deleteGameWorld();

        arena.reset();
        Logger.debug(String.format("Game in arena '%s' has been stopped and reset.", arena.getName()));
    }

    public void spawnPlayers() {
        this.arena.doForAllPlayers(player -> {
            player.setFallDistance(0F);
            player.teleport(this.gameMap.getSpawnpoint(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            
            // Default initialization of player
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setFireTicks(0);

            // Default inventory
            player.getInventory().clear();
            player.getInventory().addItem(
                this.instance.getConfiguration().GameItems()
                    .toArray(ItemStack[]::new)
            );
        });
    }

    private void executeCommands(List<String> commands, String playerName) {
        commands.forEach(command ->
                this.instance.getServer().dispatchCommand(this.instance.getServer().getConsoleSender(), command.replace("%player%", playerName))
        );
    }

    public void checkForWinner() {
        Logger.debug(String.format("Checking for a winner in arena '%s'.", this.arena.getName()));
        long remainingPlayers = this.arena.getPlayers().stream()
            .filter(arenaPlayer -> !arenaPlayer.isSpectator())
            .count();

        if (remainingPlayers > 1) {
            Logger.debug(String.format("Multiple players remaining (%d) in arena '%s', cannot determine winner yet.", remainingPlayers, this.arena.getName()));
            return;
        }

        ArenaPlayer winner = this.getWinner();
        if (winner != null) {
            Logger.debug(String.format("Winner found: '%s' in arena '%s'.", winner.getPlayer().getName(), this.arena.getName()));
            this.executeCommands(this.instance.getConfiguration().GameCommandsWinner(), winner.getPlayer().getName());

            this.arena.doForAllArenaPlayersExcept(arenaPlayer ->
                this.executeCommands(this.instance.getConfiguration().GameCommandsLosers(), arenaPlayer.getPlayer().getName()), winner);

            winner.getStatistics().addWin();
            this.playVisualEffect(Config.VisualEffectType.WINNER);
        }

        this.arena.doForAllArenaPlayers(arenaPlayer ->
            this.executeCommands(this.instance.getConfiguration().GameCommandsPlayers(), arenaPlayer.getPlayer().getName()));

        this.stop();
    }

    public void removePlayer(ArenaPlayer arenaPlayer) {
        Logger.debug(String.format("Removing player '%s' from the game in arena '%s'.", arenaPlayer.getPlayer().getName(), this.arena.getName()));
        this.arena.getPlayers().remove(arenaPlayer);

        this.arena.announceMessage(Arena.AnnouncementType.PLAYER_DEATH, arenaPlayer.getPlayer().getName());
        this.checkForWinner();
    }

    public void makeSpectator(Player player) {
        ArenaPlayer arenaPlayer = this.arena.getPlayer(player.getUniqueId());
        if (arenaPlayer == null) {
            Logger.debug(String.format("Player '%s' could not be found in arena '%s'.", player.getName(), this.arena.getName()));
            return;
        }

        if (arenaPlayer.isSpectator()) {
            Logger.debug(String.format("Player '%s' is already a spectator in arena '%s'.", player.getName(), this.arena.getName()));
            return;
        }

        arenaPlayer.setSpectator(true);
        this.arena.doForAllPlayers(p -> p.hidePlayer(this.instance, player));
        player.setGameMode(this.instance.getConfiguration().GameSpectatorGameMode());
        player.setAllowFlight(true);
        player.setFlying(true);
        player.teleport(this.gameMap.createSpectatorSpawnpoint(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        this.arena.delayAction(player, p -> p.setFireTicks(0));
        Logger.debug(String.format("Player '%s' has become a spectator in arena '%s'.", player.getName(), this.arena.getName()));

        this.checkForWinner();

        ArenaPlayer winner = this.getWinner();
        if (winner != null) {
            Config.VisualEffectType type = this.getWinner().getPlayerUUID() == arenaPlayer.getPlayerUUID() ? Config.VisualEffectType.WINNER : Config.VisualEffectType.SPECTATOR;
            Config.VisualEffectTitleConfig titleConfig = this.instance.getConfiguration().getGameVisualEffect(type).getTitle();
            player.sendTitle(titleConfig.getTitle(), titleConfig.getSubtitle(), titleConfig.getFadeIn(), titleConfig.getStay(), titleConfig.getFadeOut());
        }
    }

    private void enablePVP() {
        if (!arena.getConfig().getPVP()) {
            Logger.debug(String.format("PVP is disabled in arena '%s'.", this.arena.getName()));
            return;
        }

        if (this.isPVPEnabled) {
            Logger.debug(String.format("PVP is already enabled in arena '%s'.", this.arena.getName()));
            return;
        }

        if (this.instance.getConfiguration().GamePVPGracePeriod() == 0 || this.gamePhase == GamePhase.DEATHMATCH) {
            if (this._pvpGracePeriodTask != null) this._pvpGracePeriodTask.cancel();
            if (this.gamePhase != GamePhase.DEATHMATCH) this.playVisualEffect(Config.VisualEffectType.PVP);

            isPVPEnabled = true;
            this.arena.announceMessage(Arena.AnnouncementType.GAME_PVP_ENABLED);
            return;
        }

        this._pvpGracePeriodTask = new BukkitRunnable() {
            @Override
            public void run() {
                isPVPEnabled = true;
                playVisualEffect(Config.VisualEffectType.PVP);
                arena.announceMessage(Arena.AnnouncementType.GAME_PVP_ENABLED);
            }
        }.runTaskLater(this.instance, this.instance.getConfiguration().GamePVPGracePeriod() * 20L);
        this.arena.getTasks().add(this._pvpGracePeriodTask);
    }

    private void playVisualEffect(Config.VisualEffectType visualEffectType) {
        Config.VisualEffectConfig visualEffectConfig = this.instance.getConfiguration().getGameVisualEffect(visualEffectType);

        if (visualEffectConfig.getSound().isEnabled()) {
            this.arena.doForAllPlayersExceptSpectators(
                player -> player.playSound(player.getLocation(), visualEffectConfig.getSound().getSound(), visualEffectConfig.getSound().getVolume(), visualEffectConfig.getSound().getPitch())
            );
        }

        if (visualEffectConfig.getTitle() != null && visualEffectConfig.getTitle().isEnabled()) {
            this.arena.doForAllPlayersExceptSpectators(
                player -> player.sendTitle(visualEffectConfig.getTitle().getTitle(), visualEffectConfig.getTitle().getSubtitle(), visualEffectConfig.getTitle().getFadeIn(), visualEffectConfig.getTitle().getStay(), visualEffectConfig.getTitle().getFadeOut())
            );
        }

        if (visualEffectConfig.getParticle() != null && visualEffectConfig.getParticle().isEnabled()) {
            if (visualEffectType == Config.VisualEffectType.WINNER) {
                Player winner = this.getWinner().getPlayer();
                this.arena.doForAllPlayers(
                    player -> player.spawnParticle(visualEffectConfig.getParticle().getParticle(), winner.getLocation(), visualEffectConfig.getParticle().getAmount(), visualEffectConfig.getParticle().getOffsetX(), visualEffectConfig.getParticle().getOffsetY(), visualEffectConfig.getParticle().getOffsetZ(), visualEffectConfig.getParticle().getSpeed())
                );
            }
        }
    }

    private void startDeathmatchDamage() {
        if (!this.instance.getConfiguration().GameDeathmatchDamageEnabled()) {
            Logger.debug(String.format("Deathmatch damage is disabled in arena '%s'.", this.arena.getName()));
            return;
        }

        Logger.debug(String.format("Starting deathmatch damage in arena '%s'.", this.arena.getName()));

        this.arena.getTasks().add(new BukkitRunnable() {
            @Override
            public void run() {
                arena.getPlayersExceptSpectators()
                    .forEach(arenaPlayer -> arenaPlayer.getPlayer().damage(instance.getConfiguration().GameDeathmatchDamageAmount()));
            }
        }.runTaskTimer(
            this.instance,
            this.instance.getConfiguration().GameDeathmatchDamageDelay() * 20L,
            this.instance.getConfiguration().GameDeathmatchDamageInterval() * 20L
        ));
    }
}