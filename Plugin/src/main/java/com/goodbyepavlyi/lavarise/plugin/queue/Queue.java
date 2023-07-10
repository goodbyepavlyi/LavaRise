package com.goodbyepavlyi.lavarise.plugin.queue;

import com.goodbyepavlyi.lavarise.plugin.LavaRise;
import com.goodbyepavlyi.lavarise.plugin.arena.Arena;
import com.goodbyepavlyi.lavarise.plugin.game.utils.GamePlayer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Level;

public class Queue {
    private final LavaRise instance;
    private final Arena arena;
    private final QueueScoreboard queueScoreboard;
    private final QueueItems queueItems;
    private int countdown;
    private BukkitTask countdownTask;

    public Queue(LavaRise instance, Arena arena) {
        this.instance = instance;
        this.arena = arena;
        this.queueScoreboard = new QueueScoreboard(instance, arena, this);
        this.queueItems = new QueueItems(instance);
    }

    public int getCountdown() {
        return this.countdown;
    }

    public void addPlayer(Player player) {
        GamePlayer gamePlayer = new GamePlayer(this.instance, player);
        this.arena.getPlayers().add(gamePlayer);

        this.arena.announce(Arena.Announce.QUEUE_PLAYERJOIN, player.getName());

        this.queueScoreboard.updateScoreboardPeriodically();

        gamePlayer.saveData();

        player.teleport(this.arena.getOptions().getLobby(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.setFireTicks(0);
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20.0F);
        player.setFoodLevel(20);
        player.getInventory().setArmorContents(null);
        player.getInventory().clear();
        player.getInventory().setItem(8, queueItems.getLeaveItem());

        if (this.canStart())
            this.startCountdown();

        this.instance.debug(Level.INFO, String.format("Player %s added to the queue in arena %s", player.getName(), arena.getName()));
    }

    public void removePlayer(Player player) {
        GamePlayer gamePlayer = this.arena.getPlayer(player.getUniqueId());

        this.arena.announce(Arena.Announce.QUEUE_PLAYERLEAVE, player.getName());

        this.arena.getPlayers().remove(gamePlayer);

        gamePlayer.restoreData();

        if (!this.canStart()) {
            this.arena.setState(Arena.State.WAITING);
            this.arena.announce(Arena.Announce.QUEUE_CANCELLED);
            this.stopCountdown();
        }

        this.instance.debug(Level.INFO, String.format("Player %s removed from the queue in arena %s", player.getName(), arena.getName()));
    }

    public boolean canStart() {
        return this.arena.getPlayers().size() >= this.arena.getOptions().getMinimumPlayers()
                && this.arena.getState().equals(Arena.State.WAITING)
                && !this.arena.getState().equals(Arena.State.STARTING);
    }

    public boolean isFull() {
        return this.arena.getPlayers().size() >= this.arena.getOptions().getMaximumPlayers();
    }

    public void startCountdown() {
        this.arena.setState(Arena.State.STARTING);

        this.countdown = this.instance.getConfiguration().QUEUE_COUNTDOWN();

        this.countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (countdown <= 0) {
                    stopCountdown();
                    arena.announce(Arena.Announce.GAME_START);
                    arena.getGame().start();
                    return;
                }

                if (countdown % 15 == 0 || countdown <= 3)
                    arena.announce(Arena.Announce.QUEUE_GAME_STARTING_IN, String.valueOf(countdown));

                countdown--;
            }
        }.runTaskTimer(this.instance, 0L, 20L);

        this.arena.getTasks().add(this.countdownTask);

        this.instance.debug(Level.INFO, String.format("Countdown started in arena %s", arena.getName()));
    }

    public void stopCountdown() {
        if (this.countdownTask == null) return;

        this.countdownTask.cancel();
        this.arena.getTasks().remove(this.countdownTask);
        this.countdownTask = null;
        this.countdown = 0;
        this.queueScoreboard.stopUpdatingPeriodically();

        this.instance.debug(Level.INFO, String.format("Countdown stopped in arena %s", arena.getName()));
    }
}
