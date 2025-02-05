package me.goodbyepavlyi.lavarise.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.arena.Arena;
import me.goodbyepavlyi.lavarise.configs.ArenaPlayerStatisticsConfig;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {
    private final LavaRiseInstance instance;

    public PlaceholderAPIExpansion(LavaRiseInstance instance) {
        this.instance = instance;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "lavarise";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", instance.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return instance.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    private Optional<Arena> getArenaByParam(String params) {
        String[] split = params.split("_", 3);
        if (split.length < 3) return Optional.empty();

        return Optional.ofNullable(instance.getArenaManager().getArena(split[2]));
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.startsWith("arena_status_")) {
            return this.getArenaByParam(params).map(arena -> this.instance.getMessages().PlaceholderApiArenaState(arena.getState())).orElse(null);
        }

        if (params.startsWith("arena_players_")) {
            return this.getArenaByParam(params)
                .map(arena -> String.valueOf(arena.getPlayersExceptSpectators().size()))
                .orElse(null);
        }
        
        if(params.startsWith("stats_")){
            String[] split = params.split("_", 2);
            if (split.length < 2) return null;
            switch (split[1]) {
                case "wins":
                    return String.valueOf(this.instance.getArenaManager().getPlayerStatistics(player.getUniqueId()).getWins());
                case "losses":
                    return String.valueOf(this.instance.getArenaManager().getPlayerStatistics(player.getUniqueId()).getLosses());
                case "kills":
                    return String.valueOf(this.instance.getArenaManager().getPlayerStatistics(player.getUniqueId()).getKills());
                case "deaths":
                    return String.valueOf(this.instance.getArenaManager().getPlayerStatistics(player.getUniqueId()).getDeaths());
            }
        }
        
        if(params.startsWith("top_")){
            String[] split = params.split("_", 4);
            if (split.length < 4) return null;
            
            String valueType = split[2]; // Options: "player", "value"
            
            int position;
            try{
                position = Integer.parseInt(split[3]);
            }catch(NumberFormatException ex){
                return null;
            }

            ArenaPlayerStatisticsConfig.StatisticsType type = switch (split[1]) {
                case "wins" -> ArenaPlayerStatisticsConfig.StatisticsType.WINS;
                case "losses" -> ArenaPlayerStatisticsConfig.StatisticsType.LOSSES;
                case "kills" -> ArenaPlayerStatisticsConfig.StatisticsType.KILLS;
                case "deaths" -> ArenaPlayerStatisticsConfig.StatisticsType.DEATHS;
                default -> null;
            };
            if(type == null) return null;

            Map.Entry<UUID, Integer> stats = this.instance.getArenaManager().getPlayerStatisticsConfig().getTopPlayerByPosition(type, position);
            if(stats == null) return this.instance.getMessages().PlaceholderApiNullValue();
            
            if(valueType.equals("value")) return stats.getValue().toString();
            if(valueType.equals("player")) return this.instance.getServer().getOfflinePlayer(stats.getKey()).getName();
        }

        if ("total_players".equals(params)) {
            return String.valueOf(this.instance.getArenaManager().getArenaList()
                .stream()
                .mapToInt(arena -> arena.getPlayersExceptSpectators().size())
                .sum());
        }

        return null;
    }
}
