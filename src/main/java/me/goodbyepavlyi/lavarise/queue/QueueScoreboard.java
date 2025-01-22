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
        String duration = this.queue.getCountdown() == 0
            ? this.instance.getMessages().QueueScoreboardDurationWaiting()
            : String.valueOf(this.queue.getCountdown());
        int currentPlayers = this.getArena().getPlayers().size();
        int maxPlayers = this.getArena().getConfig().getMaximumPlayers();
        
        String title = this.instance.getMessages().QueueScoreboardTitle(duration, currentPlayers, maxPlayers);
        List<String> boardLines = this.instance.getMessages().QueueScoreboardLines(duration, currentPlayers, maxPlayers);

        this.update(title, boardLines);
    }

    public void startScoreboardUpdates() {
        this.startScoreboardUpdateTask(this::update);
    }
}
