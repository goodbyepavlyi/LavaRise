package me.goodbyepavlyi.lavarise.arena.utils;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.arena.Arena;
import me.goodbyepavlyi.lavarise.utils.Logger;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;

public class ArenaScoreboard {
    public final LavaRiseInstance instance;
    private final Arena arena;
    private Scoreboard scoreboard;
    private Objective scoreboardObjective;
    private BukkitTask scoreboardUpdateTask;

    public ArenaScoreboard(LavaRiseInstance instance, Arena arena) {
        this.instance = instance;
        this.arena = arena;

        this.create();
    }

    public Arena getArena() {
        return this.arena;
    }

    private void create() {
        this.scoreboard = this.instance.getServer().getScoreboardManager().getNewScoreboard();
        if (this.scoreboard == null) {
            Logger.severe("Failed to initialize scoreboard");
            return;
        }

        String arenaName = this.arena.getName().length() >= 16 ? this.arena.getName().substring(0, 16) : this.arena.getName();
        this.scoreboardObjective = this.scoreboard.registerNewObjective(arenaName, "dummy");
        this.scoreboardObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void clear() {
        for (String entry : this.scoreboard.getEntries()) this.scoreboard.resetScores(entry);
    }

    public void update(String title, List<String> boardLines) {
        this.clear();
        this.scoreboardObjective.setDisplayName(title);

        for (int index = boardLines.size() - 1; index >= 0; index--) {
            String line = boardLines.get(index).length() >= 40 ? boardLines.get(index).substring(0, 40) : boardLines.get(index);
            this.scoreboardObjective.getScore(line).setScore(boardLines.size() - index);
        }

        this.show();
        Logger.debug(String.format("Scoreboard updated for arena %s", this.getArena().getName()));
    }

    public void show() {
        this.arena.doForAllPlayers(player -> player.setScoreboard(this.scoreboard));
    }

    public void startScoreboardUpdateTask(Runnable updateMethod) {
        if (this.scoreboardUpdateTask != null) return;

        this.scoreboardUpdateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateMethod.run();
            }
        }.runTaskTimer(this.instance, 0L, 20L);

        this.getArena().getTasks().add(this.scoreboardUpdateTask);
        Logger.debug(String.format("Scoreboard periodic update started for arena: %s", this.getArena().getName()));
    }

    public void stopScoreboardUpdateTask() {
        if (this.scoreboardUpdateTask == null) return;

        this.scoreboardUpdateTask.cancel();
        this.getArena().getTasks().remove(this.scoreboardUpdateTask);
        this.scoreboardUpdateTask = null;
        Logger.debug(String.format("Scoreboard periodic update stopped for arena: %s", this.getArena().getName()));
    }
}
