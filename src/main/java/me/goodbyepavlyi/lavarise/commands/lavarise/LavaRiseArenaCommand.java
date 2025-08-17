package me.goodbyepavlyi.lavarise.commands.lavarise;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.arena.Arena;
import me.goodbyepavlyi.lavarise.arena.utils.ArenaConfig;
import me.goodbyepavlyi.lavarise.utils.CommandUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class LavaRiseArenaCommand {
    public static boolean onCommand(LavaRiseInstance lavaRiseInstance, Player player, Command command, String label, String[] args) {
        if (!player.hasPermission(LavaRiseCommand.Permissions.ADMIN.toString())) {
            CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().CommandNoPermissions());
            return true;
        }

        if(args.length == 2 && args[1].equalsIgnoreCase("toggleJoin")){
            boolean joinEnabled = !lavaRiseInstance.getConfiguration().GameJoinEnabled();
            lavaRiseInstance.getConfiguration().SetGameJoinEnabled(joinEnabled);
            CommandUtils.sendMessage(player, String.format("&8[&6&lLavaRise&8] Join toggled to &6%s", joinEnabled));
            return true;
        }

        if (args.length == 2 && args[1].equalsIgnoreCase("list")) {
            if (lavaRiseInstance.getArenaManager().getArenaList().isEmpty()) {
                CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().CommandArenaListEmpty());
                return true;
            }

            CommandUtils.sendMessage(player,
                lavaRiseInstance.getMessages().CommandArenaListSuccess(
                    lavaRiseInstance.getArenaManager().getArenaList().stream().map(Arena::getName).collect(Collectors.joining(", "))
                )
            );

            return true;
        }

        if (args.length == 3) {
            String arenaName = args[2];

            if (args[1].equalsIgnoreCase("create")) {
                String userMessage = lavaRiseInstance.getArenaManager().createArena(arenaName)
                    ? lavaRiseInstance.getMessages().CommandArenaCreateSuccess(arenaName)
                    : lavaRiseInstance.getMessages().CommandArenaCreateFailed(arenaName);

                CommandUtils.sendMessage(player, userMessage);
                return true;
            }

            if (args[1].equalsIgnoreCase("delete")) {
                String userMessage = lavaRiseInstance.getArenaManager().removeArena(arenaName)
                    ? lavaRiseInstance.getMessages().CommandArenaDeleteSuccess(arenaName)
                    : lavaRiseInstance.getMessages().CommandArenaDeleteFailed(arenaName);

                CommandUtils.sendMessage(player, userMessage);
                return true;
            }

            if (args[1].equalsIgnoreCase("info")) {
                Arena arena = lavaRiseInstance.getArenaManager().getArena(arenaName);
                if (arena == null) {
                    CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().CommandArenaNotFound());
                    return true;
                }

                CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().CommandArenaInfoSuccess(player, arena));
                return true;
            }
        }

        if (args.length >= 4 && (args.length <= 5 && args[1].equalsIgnoreCase("set"))) {
            String arenaName = args[2];
            Arena arena = lavaRiseInstance.getArenaManager().getArena(arenaName);

            if (arena == null) {
                CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().CommandArenaNotFound());
                return true;
            }

            String action = args[3];
            if (action.equalsIgnoreCase("lobby")) {
                arena.getConfig().setLobby(player.getLocation());
                CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().CommandArenaSetLobbySuccess(player.getLocation()));
                return true;
            }

            if (args.length == 5) {
                String actionValue = args[4];
                switch (action) {
                    case "minplayers":
                        setMinimumPlayers(lavaRiseInstance, player, arena, actionValue);
                        return true;
                    case "maxplayers":
                        setMaximumPlayers(lavaRiseInstance, player, arena, actionValue);
                        return true;
                    case "gamearea":
                        setGameArea(lavaRiseInstance, player, arena, actionValue);
                        return true;
                    case "pvp":
                        setPVP(lavaRiseInstance, player, arena, actionValue);
                        return true;
                    case "lavalevel":
                        setLavaLevel(lavaRiseInstance, player, arena, actionValue);
                        return true;
                }
            }
        }

        return false;
    }

    private static void setMinimumPlayers(LavaRiseInstance lavaRiseInstance, Player player, Arena arena, String actionValue) {
        try {
            int minimumPlayers = Integer.parseInt(actionValue);
            int maximumPlayers = arena.getConfig().getMaximumPlayers();

            if (minimumPlayers <= 0 || (maximumPlayers > 0 && minimumPlayers > maximumPlayers)) {
                CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().CommandArenaSetMinPlayersLowValue());
            }

            arena.getConfig().setMinimumPlayers(minimumPlayers);
            CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().CommandArenaSetMinPlayersSuccess(minimumPlayers));
        } catch (NumberFormatException e) {
            CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().CommandExpectedNumber());
        }
    }

    private static void setMaximumPlayers(LavaRiseInstance lavaRiseInstance, Player player, Arena arena, String actionValue) {
        try {
            int maximumPlayers = Integer.parseInt(actionValue);
            int minimumPlayers = arena.getConfig().getMinimumPlayers();

            if (maximumPlayers <= 0 || (minimumPlayers > 0 && maximumPlayers < minimumPlayers)) {
                CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().CommandArenaSetMaxPlayersLowValue());
            }

            arena.getConfig().setMaximumPlayers(maximumPlayers);
            CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().CommandArenaSetMaxPlayersSuccess(maximumPlayers));
        } catch (NumberFormatException e) {
            CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().CommandExpectedNumber());
        }
    }

    private static void setGameArea(LavaRiseInstance lavaRiseInstance, Player player, Arena arena, String actionValue) {
        try {
            ArenaConfig.GameArea gameArea = ArenaConfig.GameArea.valueOf(actionValue.toUpperCase());
            arena.getConfig().setGameAreaLocation(gameArea, player.getLocation());
            CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().CommandArenaSetGameAreaSuccess(gameArea, player.getLocation()));
        } catch (IllegalArgumentException e) {
            CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().CommandArenaSetGameAreaInvalidValue());
        }
    }

    private static void setPVP(LavaRiseInstance lavaRiseInstance, Player player, Arena arena, String actionValue) {
        if (!actionValue.equalsIgnoreCase("true") && !actionValue.equalsIgnoreCase("false")) {
            CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().CommandArenaSetPvpInvalidValue());
            return;
        }

        boolean pvp = Boolean.parseBoolean(actionValue);
        arena.getConfig().setPVP(pvp);
        CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().CommandArenaSetPvpSuccess(pvp));
    }

    private static void setLavaLevel(LavaRiseInstance lavaRiseInstance, Player player, Arena arena, String actionValue) {
        try {
            int lavaLevel = Integer.parseInt(actionValue);
            Location gameAreaTop = arena.getConfig().getGameArea(ArenaConfig.GameArea.TOP);
            Location gameAreaBottom = arena.getConfig().getGameArea(ArenaConfig.GameArea.BOTTOM);
            if (gameAreaTop == null || gameAreaBottom == null) {
                CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().CommandArenaSetLavalevelNoGameArea());
                return;
            }

            if (lavaLevel < gameAreaBottom.getBlockY() || lavaLevel > gameAreaTop.getBlockY()) {
                CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().CommandArenaSetLavalevelOutOfRange(gameAreaBottom.getBlockY(), gameAreaTop.getBlockY()));
                return;
            }

            arena.getConfig().setLavaLevel(lavaLevel);
            CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().CommandArenaSetLavalevelSuccess(lavaLevel));
        } catch (NumberFormatException e) {
            CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().CommandExpectedNumber());
        }
    }
}