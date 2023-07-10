package com.goodbyepavlyi.lavarise.plugin.queue;

import com.goodbyepavlyi.lavarise.plugin.LavaRise;
import com.goodbyepavlyi.lavarise.plugin.arena.Arena;
import com.goodbyepavlyi.lavarise.plugin.arena.utils.ArenaScoreboard;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.logging.Level;

public class QueueScoreboard extends ArenaScoreboard {
    private final Queue queue;
    private BukkitTask scoreboardUpdateTask;

    public QueueScoreboard(LavaRise instance, Arena arena, Queue queue) {
        super(instance, arena);

        this.queue = queue;
        instance.debug(Level.INFO, String.format("QueueScoreboard created for arena: %s", arena.getName()));
    }

    public void updateScoreboard() {
        String title = this.getInstance().getMessages().QUEUE_SCOREBOARD_TITLE();

        String countdown = this.queue.getCountdown() == 0 ? this.getInstance().getMessages().QUEUE_SCOREBOARD_DURATIONWAITING() : String.valueOf(this.queue.getCountdown());
        List<String> boardLines = this.getInstance().getMessages().QUEUE_SCOREBOARD_LINES(
                countdown,
                this.getArena().getPlayers().size(),
                this.getArena().getOptions().getMaximumPlayers()
        );

        this.updateScoreboard(title, boardLines);
        this.getInstance().debug(Level.INFO, String.format("Scoreboard updated for arena: %s", this.getArena().getName()));
    }

    public void updateScoreboardPeriodically() {
        if (this.scoreboardUpdateTask != null) return;

        this.scoreboardUpdateTask = this.getInstance().getServer().getScheduler().runTaskTimer(
                this.getInstance(),
                this::updateScoreboard,
                0L,
                20L
        );

        this.getArena().getTasks().add(this.scoreboardUpdateTask);
        this.getInstance().debug(Level.INFO, String.format("Scoreboard periodic update started for arena: %s", this.getArena().getName()));
    }

    public void stopUpdatingPeriodically() {
        if (this.scoreboardUpdateTask == null) return;

        this.scoreboardUpdateTask.cancel();
        this.getArena().getTasks().remove(this.scoreboardUpdateTask);
        this.scoreboardUpdateTask = null;
        this.getInstance().debug(Level.INFO, String.format("Scoreboard periodic update stopped for arena: %s", this.getArena().getName()));
    }
}
