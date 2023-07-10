package com.goodbyepavlyi.lavarise.plugin.arena;

import com.goodbyepavlyi.lavarise.plugin.LavaRise;
import com.goodbyepavlyi.lavarise.plugin.arena.utils.ArenaOptions;
import com.goodbyepavlyi.lavarise.plugin.game.Game;
import com.goodbyepavlyi.lavarise.plugin.game.utils.GamePlayer;
import com.goodbyepavlyi.lavarise.plugin.queue.Queue;
import com.goodbyepavlyi.lavarise.plugin.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

public class Arena {
    private final LavaRise instance;
    private final String name;
    private final Set<GamePlayer> players;
    private final ArenaOptions options;
    private Game game;
    private Queue queue;
    private State state;
    private final List<BukkitTask> tasks;

    public Arena(LavaRise instance, String name) {
        this.instance = instance;
        this.name = name;
        this.players = new HashSet<>();

        this.options = new ArenaOptions(instance, this);

        this.game = new Game(instance, this);
        this.queue = new Queue(instance, this);
        this.state = State.WAITING;

        this.tasks = new ArrayList<>();
    }

    public enum State {
        WAITING, STARTING, IN_GAME
    }

    public enum Announce {
        QUEUE_PLAYERJOIN,
        QUEUE_PLAYERLEAVE,
        QUEUE_CANCELLED,
        QUEUE_GAME_STARTING_IN,
        GAME_START,
        GAME_END,
        PLAYER_DEATH,
    }

    public void announce(Announce announceType, String... arguments) {
        String message = null;

        switch (announceType) {
            case QUEUE_PLAYERJOIN:
                if (arguments.length < 1) break;

                message = instance.getMessages().QUEUE_PLAYERJOIN(
                    arguments[0],
                    this.getPlayers().size(),
                    this.getOptions().getMaximumPlayers()
                );
                break;
            case QUEUE_PLAYERLEAVE:
                if (arguments.length < 1) break;

                message = instance.getMessages().QUEUE_PLAYERLEAVE(
                    arguments[0],
                    this.getPlayers().size(),
                    this.getOptions().getMaximumPlayers()
                );
                break;
            case QUEUE_CANCELLED:
                message = instance.getMessages().QUEUE_CANCELLED();
                break;
            case QUEUE_GAME_STARTING_IN:
                if (arguments.length < 1) break;

                message = instance.getMessages().QUEUE_GAMESTARTINGIN(
                    Integer.parseInt(arguments[0])
                );
                break;
            case GAME_START:
                message = instance.getMessages().GAME_EVENTS_GAMESTART();
                break;
            case GAME_END:
                message = instance.getMessages().GAME_EVENTS_GAMEENDED();
                break;
            case PLAYER_DEATH:
                if (arguments.length < 1) break;

                message = instance.getMessages().GAME_EVENTS_PLAYERDEATH(
                    arguments[0]
                );
                break;
        }

        if (message == null) return;

        this.broadcast(message);
    }

    public String getName() {
        return this.name;
    }

    public Set<GamePlayer> getPlayers() {
        return this.players;
    }

    public GamePlayer getPlayer(UUID playerUUID) {
        return this.players.stream()
                .filter(gamePlayer -> gamePlayer.getPlayerUUID().equals(playerUUID))
                .findFirst()
                .orElse(null);
    }

    public ArenaOptions getOptions() {
        return this.options;
    }

    public Queue getQueue() {
        return this.queue;
    }

    public Game getGame() {
        return this.game;
    }

    public State getState() {
        return this.state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public List<BukkitTask> getTasks() {
        return this.tasks;
    }

    private void broadcast(String message) {
        this.players.forEach(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            if (player == null) return;

            player.sendMessage(ChatUtils.color(message));
        });
    }

    public void doForAllPlayers(Consumer<GamePlayer> callback) {
        this.getPlayers().forEach(gamePlayer -> {
            if (gamePlayer == null) return;

            callback.accept(gamePlayer);
        });
    }

    public void saveConfig() {
        this.instance.getArenasConfig().getConfig().createSection(this.name);
        this.instance.getArenasConfig().getConfig().set(this.name, this.options.getConfig());

        this.instance.debug(Level.INFO, String.format("Saved configuration for arena %s", this.name));
    }

    public void deleteConfig() {
        this.instance.getArenasConfig().getConfig().set(this.name, null);

        this.instance.debug(Level.INFO, String.format("Deleted configuration for arena %s", this.name));
    }

    public boolean readyToUse() {
        boolean minimalPlayersSet = this.options.getMinimumPlayers() != 0;
        boolean maximumPlayersSet = this.options.getMaximumPlayers() != 0;
        boolean lobbySet = this.options.getLobby() != null;
        boolean gameAreaTopSet = this.options.getGameArea(ArenaOptions.GameArea.TOP) != null;
        boolean gameAreaBottomSet = this.options.getGameArea(ArenaOptions.GameArea.BOTTOM) != null;

        boolean arenaReady = minimalPlayersSet
                && maximumPlayersSet
                && lobbySet
                && gameAreaTopSet
                && gameAreaBottomSet;

        if (!arenaReady)
            this.instance.debug(Level.WARNING, String.format("Arena %s is not ready to use", this.name));

        return arenaReady;
    }

    public void reset() {
        this.players.clear();

        this.game = new Game(this.instance, this);
        this.queue = new Queue(this.instance, this);
        this.setState(State.WAITING);

        this.getTasks().forEach(BukkitTask::cancel);
        this.getTasks().clear();

        this.instance.debug(Level.INFO, String.format("Reset arena %s", this.name));
    }
}