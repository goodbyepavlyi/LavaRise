package me.goodbyepavlyi.lavarise.game.models;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.arena.Arena;
import me.goodbyepavlyi.lavarise.arena.utils.ArenaScoreboard;
import me.goodbyepavlyi.lavarise.game.Game;

import java.util.List;

public class GameScoreboard extends ArenaScoreboard {
    private final Game game;

    public GameScoreboard(LavaRiseInstance instance, Arena arena, Game game) {
        super(instance, arena);
        this.game = game;
    }

    public void update() {
        String title = this.instance.getMessages().GameScoreboardTitle();
        List<String> boardLines = this.instance.getMessages().GameScoreboardLines(
            (int) this.getArena().getPlayers()
                .stream()
                .filter(player -> !player.isSpectator())
                .count(),
            this.game.getCurrentLavaY(),
            (System.currentTimeMillis() - this.game.getGameTime()),
            this.game.getGamePhase()
        );

        this.update(title, boardLines);
    }

    public void startScoreboardUpdates() {
        this.startScoreboardUpdateTask(this::update);
    }
}
