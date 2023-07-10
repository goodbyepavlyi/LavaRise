package com.goodbyepavlyi.lavarise.plugin.arena.utils;

import com.goodbyepavlyi.lavarise.plugin.LavaRise;
import com.goodbyepavlyi.lavarise.plugin.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;
import java.util.logging.Level;

public class ArenaScoreboard {
    private final LavaRise instance;
    private final Arena arena;
    private Scoreboard scoreboard;
    private Objective scoreboardObjective;

    public ArenaScoreboard(LavaRise instance, Arena arena) {
        this.instance = instance;
        this.arena = arena;

        this.initializeScoreboard();

        this.instance.debug(Level.INFO, String.format("Initialized scoreboard for arena %s", this.arena.getName()));
    }

    public LavaRise getInstance() {
        return this.instance;
    }

    public Arena getArena() {
        return this.arena;
    }

    private void initializeScoreboard() {
        this.scoreboard = this.instance.getServer().getScoreboardManager().getNewScoreboard();

        String arenaName = this.arena.getName().length() >= 16 ? this.arena.getName().substring(0, 16) : this.arena.getName();
        this.scoreboardObjective = this.scoreboard.registerNewObjective(arenaName, "dummy");
        this.scoreboardObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void resetScoreboard() {
        this.scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        this.initializeScoreboard();

        this.instance.debug(Level.INFO, String.format("Reset scoreboard for arena %s", this.arena.getName()));
    }

    public void updateScoreboard(String title, List<String> boardLines) {
        this.resetScoreboard();

        this.scoreboardObjective.setDisplayName(title);

        for (int index = boardLines.size() - 1; index >= 0; index--) {
            String line = boardLines.get(index).length() >= 40 ? boardLines.get(index).substring(0, 40) : boardLines.get(index);
            this.scoreboardObjective.getScore(line).setScore(boardLines.size() - index);
        }

        this.showScoreboard();

        this.instance.debug(Level.INFO, String.format("Updated scoreboard for arena %s", this.arena.getName()));
    }

    public void showScoreboard() {
        this.arena.doForAllPlayers(gamePlayer -> {
            Player player = gamePlayer.getPlayer();
            if (player == null) return;

            player.setScoreboard(this.scoreboard);
        });

        this.instance.debug(Level.INFO, String.format("Displayed scoreboard for arena %s", this.arena.getName()));
    }
}
