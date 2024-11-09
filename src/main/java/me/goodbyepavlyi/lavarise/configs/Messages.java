package me.goodbyepavlyi.lavarise.configs;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.arena.Arena;
import me.goodbyepavlyi.lavarise.arena.utils.ArenaConfig;
import me.goodbyepavlyi.lavarise.game.Game;
import me.goodbyepavlyi.lavarise.utils.ChatUtils;
import me.goodbyepavlyi.lavarise.utils.Logger;
import me.goodbyepavlyi.lavarise.utils.YamlConfig;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Messages extends YamlConfig {
    private final int CONFIG_VERSION = 3;

    public Messages(LavaRiseInstance instance) {
        super(instance, "messages.yml", true);
        this.migrateConfigVersion();
    }

    public void migrateConfigVersion() {
        Logger.debug(String.format("Checking config file version: %s", this.getFile().getName()));
        if (this.getConfigVersion() == this.CONFIG_VERSION) return;

        Logger.warning(String.format("The config file %s is outdated. Migrating to version %d...", this.getFile().getName(), this.CONFIG_VERSION));

        FileConfiguration resourceConfig = this.getResourceConfig();
        if (resourceConfig == null) return;

        for (String key : resourceConfig.getKeys(true)) {
            if (this.getConfig().contains(key)) continue;
            Logger.debug(String.format("Migrating config key: %s", key));
            this.getConfig().set(key, resourceConfig.get(key));
        }

        this.setConfigVersion(this.CONFIG_VERSION);
        Logger.info(String.format("Config file %s migrated to version %d", this.getFile().getName(), this.CONFIG_VERSION));
        this.save();
    }

    public String getString(String path) {
        return ChatUtils.color(this.getConfig().getString(path));
    }

    public List<String> getList(String path) {
        return ChatUtils.color(this.getConfig().getStringList(path));
    }

    private String getLastColorCode(String text) {
        StringBuilder colorCodes = new StringBuilder();
        Matcher matcher = Pattern.compile("(?i)(?:&[0-9A-FK-OR])+").matcher(text);

        while (matcher.find()) {
            colorCodes.setLength(0);
            colorCodes.append(matcher.group());
        }

        return colorCodes.toString();
    }

    private void addLocationComponent(String line, String placeholder, Location location, Player player, TextComponent textComponent) {
        String[] split = line.split(placeholder);
        textComponent.setText(ChatUtils.color(split[0]));

        String colorCode = getLastColorCode(split[0]);
        if (location != null) {
            TextComponent locationComponent = new TextComponent(ChatUtils.color(
                String.format("%s&n%s, %d, %d, %d", colorCode, location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ()))
            );

            locationComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                String.format("/lavarise _plugin world_teleport %s %d %d %d", location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ())));

            textComponent.addExtra(locationComponent);
        } else {
            textComponent.addExtra(new TextComponent(ChatUtils.color(colorCode + "N/A")));
        }

        if (split.length > 1) {
            textComponent.addExtra(ChatUtils.color(split[1]));
        }
    }

    public String getGamePhaseMessage(Game.GamePhase gamePhase) {
        return switch (gamePhase) {
            case GRACE -> this.getString("game.phase.grace");
            case LAVA -> this.getString("game.phase.lava");
            case DEATHMATCH -> this.getString("game.phase.deathmatch");
        };
    }

    public List<String> CommandUsage() {
        return this.getList("command.usage");
    }

    public String CommandNoPermissions() {
        return this.getString("command.noPermissions");
    }

    public String CommandOnlyPlayer() {
        return this.getString("command.onlyPlayer");
    }

    public String CommandArenaNotFound() {
        return this.getString("command.arenaNotFound");
    }

    public String CommandExpectedNumber() {
        return this.getString("command.expectedNumber");
    }

    public String CommandArenaListEmpty() {
        return this.getString("command.arena.list.empty");
    }

    public String CommandArenaListSuccess(String arenas) {
        return this.getString("command.arena.list.success")
                .replaceAll("%arenas%", arenas);
    }

    public String CommandArenaCreateSuccess(String arenaName) {
        return this.getString("command.arena.create.success")
                .replaceAll("%arenaName%", arenaName);
    }

    public String CommandArenaCreateFailed(String arenaName) {
        return this.getString("command.arena.create.failed")
                .replaceAll("%arenaName%", arenaName);
    }

    public String CommandArenaDeleteSuccess(String arenaName) {
        return this.getString("command.arena.delete.success")
                .replaceAll("%arenaName%", arenaName);
    }

    public String CommandArenaDeleteFailed(String arenaName) {
        return this.getString("command.arena.delete.failed")
                .replaceAll("%arenaName%", arenaName);
    }

    public String CommandArenaSetMinPlayersSuccess(int minimumPlayers) {
        return this.getString("command.arena.set.minplayers.success")
                .replaceAll("%minimumPlayers%", String.valueOf(minimumPlayers));
    }

    public String CommandArenaSetMinPlayersLowValue() {
        return this.getString("command.arena.set.minplayers.lowValue");
    }

    public String CommandArenaSetMaxPlayersSuccess(int maximumPlayers) {
        return this.getString("command.arena.set.maxplayers.success")
                .replaceAll("%maximumPlayers%", String.valueOf(maximumPlayers));
    }

    public String CommandArenaSetMaxPlayersLowValue() {
        return this.getString("command.arena.set.maxplayers.lowValue");
    }

    public String CommandArenaSetLobbySuccess(Location location) {
        return this.getString("command.arena.set.lobby.success")
                .replaceAll("%worldName%", location.getWorld().getName())
                .replaceAll("%blockX%", String.valueOf(location.getBlockX()))
                .replaceAll("%blockY%", String.valueOf(location.getBlockY()))
                .replaceAll("%blockZ%", String.valueOf(location.getBlockZ()));
    }

    public String CommandArenaSetGameAreaInvalidValue() {
        return this.getString("command.arena.set.gamearea.invalidValue")
                .replaceAll("%types%", Arrays.stream(ArenaConfig.GameArea.values())
                        .map(ArenaConfig.GameArea::toString)
                        .collect(Collectors.joining(", ")));
    }

    public String CommandArenaSetGameAreaSuccess(ArenaConfig.GameArea gameArea, Location location) {
        return this.getString("command.arena.set.gamearea.success")
                .replaceAll("%type%", gameArea.toString())
                .replaceAll("%worldName%", location.getWorld().getName())
                .replaceAll("%blockX%", String.valueOf(location.getBlockX()))
                .replaceAll("%blockY%", String.valueOf(location.getBlockY()))
                .replaceAll("%blockZ%", String.valueOf(location.getBlockZ()));
    }

    public String CommandArenaSetPvpInvalidValue() {
        return this.getString("command.arena.set.pvp.invalidValue")
                .replaceAll("%types%", "true, false");
    }

    public String CommandArenaSetPvpSuccess(boolean pvp) {
        return this.getString("command.arena.set.pvp.success")
                .replaceAll("%type%", pvp ? "true" : "false");
    }

    public String CommandArenaSetLavalevelNoGameArea() {
        return this.getString("command.arena.set.lavalevel.noGameArea");
    }

    public String CommandArenaSetLavalevelOutOfRange(int minY, int maxY) {
        return this.getString("command.arena.set.lavalevel.outOfRange")
                .replaceAll("%minY%", String.valueOf(minY))
                .replaceAll("%maxY%", String.valueOf(maxY));
    }

    public String CommandArenaSetLavalevelSuccess(int lavaLevel) {
        return this.getString("command.arena.set.lavalevel.success")
                .replaceAll("%lavaLevel%", String.valueOf(lavaLevel));
    }

    public List<TextComponent> CommandArenaInfoSuccess(Player player, Arena arena) {
        List<TextComponent> message = new ArrayList<>();

        for (String line : this.getConfig().getStringList("command.arena.info.success")) {
            line = line.replace("%arenaName%", arena.getName())
                    .replace("%minimumPlayers%", String.valueOf(arena.getConfig().getMinimumPlayers()))
                    .replace("%maximumPlayers%", String.valueOf(arena.getConfig().getMaximumPlayers()))
                    .replace("%lavaLevel%", String.valueOf(arena.getConfig().getLavaLevel()))
                    .replace("%pvp%", String.valueOf(arena.getConfig().getPVP()));

            TextComponent textComponent = new TextComponent(ChatUtils.color(line));
            if (line.contains("%lobby%")) {
                addLocationComponent(line, "%lobby%", arena.getConfig().getLobby(), player, textComponent);
            } else if (line.contains("%gameAreaBottom%")) {
                addLocationComponent(line, "%gameAreaBottom%", arena.getConfig().getGameArea(ArenaConfig.GameArea.BOTTOM), player, textComponent);
            } else if (line.contains("%gameAreaTop%")) {
                addLocationComponent(line, "%gameAreaTop%", arena.getConfig().getGameArea(ArenaConfig.GameArea.TOP), player, textComponent);
            }

            message.add(textComponent);
        }

        return message;
    }

    public String CommandJoinAlreadyInArena() {
        return this.getString("command.join.alreadyInArena");
    }

    public String CommandJoinArenaIsntSetup() {
        return this.getString("command.join.arenaIsntSetup");
    }

    public String CommandJoinArenaFull() {
        return this.getString("command.join.arenaFull");
    }

    public String CommandJoinArenaInGame() {
        return this.getString("command.join.arenaInGame");
    }

    public String CommandLeaveNotInArena() {
        return this.getString("command.leave.notInArena");
    }

    public String QueuePlayerJoin(String playerName, int playerCount, int maximumPlayers) {
        return this.getString("queue.playerJoin")
                .replaceAll("%playerName%", playerName)
                .replaceAll("%playerCount%", String.valueOf(playerCount))
                .replaceAll("%maximumPlayers%", String.valueOf(maximumPlayers));
    }

    public String QueuePlayerLeave(String playerName, int playerCount, int maximumPlayers) {
        return this.getString("queue.playerLeave")
                .replaceAll("%playerName%", playerName)
                .replaceAll("%playerCount%", String.valueOf(playerCount))
                .replaceAll("%maximumPlayers%", String.valueOf(maximumPlayers));
    }

    public String QueueCancelled() {
        return this.getString("queue.cancelled");
    }

    public String QueueGameStartingIn(int timeLeft) {
        return this.getString("queue.gameStartingIn")
                .replaceAll("%timeLeft%", String.valueOf(timeLeft));
    }

    public String QueueItemsLeaveName() {
        return this.getString("queue.items.leave.name");
    }

    public List<String> QueueItemsLeaveLore() {
        return this.getList("queue.items.leave.lore");
    }

    public String QueueScoreboardTitle() {
        return this.getString("queue.scoreboard.title");
    }

    public String QueueScoreboardDurationWaiting() {
        return this.getString("queue.scoreboard.durationWaiting");
    }

    public List<String> QueueScoreboardLines(String duration, int currentPlayers, int maximumPlayers) {
        return this.getList("queue.scoreboard.lines")
                .stream()
                .map(line -> line
                        .replaceAll("%duration%", duration)
                        .replaceAll("%currentPlayers%", String.valueOf(currentPlayers))
                        .replaceAll("%maximumPlayers%", String.valueOf(maximumPlayers))
                )
                .collect(Collectors.toList());
    }

    public String GameScoreboardTitle() {
        return this.getString("game.scoreboard.title");
    }

    public List<String> GameScoreboardLines(int playersLeft, int lavaY, long gameTime, String gamePhase) {
        String gameTimeFormatted = new SimpleDateFormat("mm:ss").format(new Date(gameTime));

        return this.getList("game.scoreboard.lines")
                .stream()
                .map(line -> line
                    .replaceAll("%playersLeft%", String.valueOf(playersLeft))
                    .replaceAll("%lavaY%", String.valueOf(lavaY))
                    .replaceAll("%gameTime%", gameTimeFormatted)
                    .replaceAll("%event%", gamePhase)
                )
                .collect(Collectors.toList());
    }

    public String GameEventsGameStart() {
        return this.getString("game.events.gameStart");
    }

    public String GameEventsGameEnded() {
        return this.getString("game.events.gameEnded");
    }

    public String GameEventsPlayerDeath(String playerName) {
        return this.getString("game.events.playerDeath")
                .replaceAll("%playerName%", playerName);
    }

    public String GameEventsPlayerKilled(String playerName, String killerName) {
        return this.getString("game.events.playerKilled")
                .replaceAll("%playerName%", playerName)
                .replaceAll("%killerName%", killerName);
    }

    public String GameEventsLavaPhaseStart() {
        return this.getString("game.events.lavaPhaseStart");
    }

    public String GameEventsLavaPhaseEnd() {
        return this.getString("game.events.lavaPhaseEnd");
    }

    public String GameEventsPvpEnabled() {
        return this.getString("game.events.pvpEnabled");
    }
}
