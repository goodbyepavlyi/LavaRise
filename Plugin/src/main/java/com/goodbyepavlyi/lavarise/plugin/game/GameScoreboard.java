package com.goodbyepavlyi.lavarise.plugin.game;

import com.goodbyepavlyi.lavarise.plugin.LavaRise;
import com.goodbyepavlyi.lavarise.plugin.arena.Arena;
import com.goodbyepavlyi.lavarise.plugin.arena.utils.ArenaScoreboard;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.logging.Level;

public class GameScoreboard extends ArenaScoreboard {
    private final Game game;

    public GameScoreboard(LavaRise instance, Arena arena, Game game) {
        super(instance, arena);

        this.game = game;
    }

    public void updateScoreboard() {
        String title = this.getInstance().getMessages().GAME_SCOREBOARD_TITLE();

        List<String> boardLines = this.getInstance().getMessages().GAME_SCOREBOARD_LINES(
                this.getArena().getPlayers().size(),
                this.game.getCurrentLavaY(),
                (System.currentTimeMillis() - this.game.getGameTime()),
                this.game.getGamePhase()
        );

        this.updateScoreboard(title, boardLines);

        this.getInstance().debug(Level.INFO, String.format("Scoreboard updated for arena %s", this.getArena().getName()));
    }

    public void updateScoreboardPeriodically() {
        BukkitTask scoreboardUpdateTask = this.getInstance().getServer().getScheduler().runTaskTimer(
                this.getInstance(),
                this::updateScoreboard,
                0L,
                20L
        );

        this.getArena().getTasks().add(scoreboardUpdateTask);

        this.getInstance().debug(Level.INFO, String.format("Scoreboard update task started for arena %s", this.getArena().getName()));
    }
}
