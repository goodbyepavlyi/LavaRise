package me.goodbyepavlyi.lavarise.queue;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.arena.Arena;
import me.goodbyepavlyi.lavarise.arena.utils.ArenaScoreboard;

import java.util.List;

public class QueueScoreboard extends ArenaScoreboard {
    private final Queue queue;

    public QueueScoreboard(LavaRiseInstance instance, Arena arena, Queue queue) {
        super(instance, arena);
        this.queue = queue;
    }

    public void update() {
        String title = this.instance.getMessages().QueueScoreboardTitle();
        List<String> boardLines = this.instance.getMessages().QueueScoreboardLines(
            this.queue.getCountdown() == 0
                ? this.instance.getMessages().QueueScoreboardDurationWaiting()
                : String.valueOf(this.queue.getCountdown()),
            this.getArena().getPlayers().size(),
            this.getArena().getConfig().getMaximumPlayers()
        );

        this.update(title, boardLines);
    }

    public void startScoreboardUpdates() {
        this.startScoreboardUpdateTask(this::update);
    }
}
