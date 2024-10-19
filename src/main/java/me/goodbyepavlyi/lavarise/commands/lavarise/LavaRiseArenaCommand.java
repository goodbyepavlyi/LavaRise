package me.goodbyepavlyi.lavarise.commands.lavarise;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.arena.Arena;
import me.goodbyepavlyi.lavarise.arena.utils.ArenaConfig;
import me.goodbyepavlyi.lavarise.utils.CommandUtils;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class LavaRiseArenaCommand {
    public static boolean onCommand(LavaRiseInstance lavaRiseInstance, Player player, Command command, String label, String[] args) {
        if (!player.hasPermission(LavaRiseCommand.Permissions.ADMIN.toString())) {
            CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().COMMAND_NOPERMISSIONS());
            return true;
        }

        if (args.length == 2 && args[1].equalsIgnoreCase("list")) {
            if (lavaRiseInstance.getArenaManager().getArenaList().isEmpty()) {
                CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().COMMAND_ARENA_LIST_EMPTY());
                return true;
            }

            CommandUtils.sendMessage(player,
                lavaRiseInstance.getMessages().COMMAND_ARENA_LIST_SUCCESS(
                    lavaRiseInstance.getArenaManager().getArenaList().stream().map(Arena::getName).collect(Collectors.joining(", "))
                )
            );

            return true;
        }

        if (args.length == 3) {
            String arenaName = args[2];

            if (args[1].equalsIgnoreCase("create")) {
                String userMessage = lavaRiseInstance.getArenaManager().createArena(arenaName)
                    ? lavaRiseInstance.getMessages().COMMAND_ARENA_CREATE_SUCCESS(arenaName)
                    : lavaRiseInstance.getMessages().COMMAND_ARENA_CREATE_FAILED(arenaName);

                CommandUtils.sendMessage(player, userMessage);
                return true;
            }

            if (args[1].equalsIgnoreCase("delete")) {
                String userMessage = lavaRiseInstance.getArenaManager().removeArena(arenaName)
                    ? lavaRiseInstance.getMessages().COMMAND_ARENA_DELETE_SUCCESS(arenaName)
                    : lavaRiseInstance.getMessages().COMMAND_ARENA_DELETE_FAILED(arenaName);

                CommandUtils.sendMessage(player, userMessage);
                return true;
            }
        }

        if (args.length == 4 || args.length == 5 && args[1].equalsIgnoreCase("set")) {
            String arenaName = args[2];
            Arena arena = lavaRiseInstance.getArenaManager().getArena(arenaName);

            if (arena == null) {
                CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().COMMAND_ARENANOTFOUND());
                return true;
            }

            String action = args[3];
            if (args.length == 4 && action.equalsIgnoreCase("lobby")) {
                arena.getConfig().setLobby(player.getLocation());
                CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().COMMAND_ARENA_SET_LOBBY_SUCCESS(player.getLocation()));
                return true;
            }

            if (args.length == 5) {
                String actionValue = args[4];

                if (action.equalsIgnoreCase("minplayers")) {
                    try {
                        int minimumPlayers = Integer.parseInt(actionValue);
                        int maximumPlayers = arena.getConfig().getMaximumPlayers();

                        if (minimumPlayers <= 0 || (maximumPlayers > 0 && minimumPlayers > maximumPlayers)) {
                            CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().COMMAND_ARENA_SET_MINPLAYERS_LOW_VALUE());
                            return true;
                        }

                        arena.getConfig().setMinimumPlayers(minimumPlayers);
                        CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().COMMAND_ARENA_SET_MINPLAYERS_SUCCESS(minimumPlayers));
                    } catch (NumberFormatException numberFormatException) {
                        CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().COMMAND_EXPECTEDNUMBER());
                    }

                    return true;
                }

                if (action.equalsIgnoreCase("maxplayers")) {
                    try {
                        int minimumPlayers = arena.getConfig().getMinimumPlayers();
                        int maximumPlayers = Integer.parseInt(actionValue);

                        if (maximumPlayers <= 0 || (minimumPlayers > 0 && maximumPlayers < minimumPlayers)) {
                            CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().COMMAND_ARENA_SET_MAXPLAYERS_LOW_VALUE());
                            return true;
                        }

                        arena.getConfig().setMaximumPlayers(maximumPlayers);
                        CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().COMMAND_ARENA_SET_MAXPLAYERS_SUCCESS(maximumPlayers));
                    } catch (NumberFormatException numberFormatException) {
                        CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().COMMAND_EXPECTEDNUMBER());
                    }

                    return true;
                }

                if (action.equalsIgnoreCase("gamearea")) {
                    try {
                        ArenaConfig.GameArea gameArea = ArenaConfig.GameArea.valueOf(actionValue.toUpperCase());
                        arena.getConfig().setGameAreaLocation(gameArea, player.getLocation());
                        CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().COMMAND_ARENA_SET_GAMEAREA_SUCCESS(gameArea, player.getLocation()));
                    } catch (IllegalArgumentException illegalArgumentException) {
                        CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().COMMAND_INVALID_GAMEAREA());
                    }

                    return true;
                }
            }
        }

        return false;
    }
}