package com.goodbyepavlyi.lavarise.plugin.commands;

import com.goodbyepavlyi.lavarise.plugin.LavaRise;
import com.goodbyepavlyi.lavarise.plugin.arena.Arena;
import com.goodbyepavlyi.lavarise.plugin.arena.ArenaManager;
import com.goodbyepavlyi.lavarise.plugin.arena.utils.ArenaOptions;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LavaRiseCommand extends com.goodbyepavlyi.lavarise.plugin.utils.Command implements CommandExecutor, TabCompleter {
    private final LavaRise instance;

    public LavaRiseCommand(LavaRise instance) {
        this.instance = instance;
    }

    private enum Permissions {
        ADMIN("lavarise.admin");

        private final String name;

        Permissions(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        boolean senderIsPlayer = commandSender instanceof Player;

        if (args.length == 0) {
            this.sendMessage(commandSender, this.instance.getMessages().COMMAND_USAGE());
            return true;
        }

        if (args[0].equalsIgnoreCase("arena")) {
            if (!commandSender.hasPermission(Permissions.ADMIN.toString())) {
                this.sendMessage(commandSender, this.instance.getMessages().COMMAND_NOPERMISSIONS());
                return true;
            }

            if (args.length == 2) {
                if (args[1].equalsIgnoreCase("list")) {
                    String arenas = String.join(", ", this.instance.getArenaManager().getArenaList().stream().map(Arena::getName).collect(Collectors.toList()));

                    if (arenas.isEmpty()) {
                        this.sendMessage(commandSender, this.instance.getMessages().COMMAND_ARENA_LIST_EMPTY());
                        return true;
                    }

                    this.sendMessage(commandSender, this.instance.getMessages().COMMAND_ARENA_LIST_SUCCESS(arenas));
                    return true;
                }
            }

            if (args.length == 3) {
                if (args[1].equalsIgnoreCase("create")) {
                    String arenaName = args[2];

                    if (this.instance.getArenaManager().create(arenaName))
                        this.sendMessage(commandSender, this.instance.getMessages().COMMAND_ARENA_CREATE_SUCCESS(arenaName));
                    else
                        this.sendMessage(commandSender, this.instance.getMessages().COMMAND_ARENA_CREATE_FAILED(arenaName));

                    return true;
                }

                if (args[1].equalsIgnoreCase("delete")) {
                    String arenaName = args[2];

                    if (this.instance.getArenaManager().delete(arenaName))
                        this.sendMessage(commandSender, this.instance.getMessages().COMMAND_ARENA_DELETE_SUCCESS(arenaName));
                    else
                        this.sendMessage(commandSender, this.instance.getMessages().COMMAND_ARENA_DELETE_FAILED(arenaName));

                    return true;
                }
            }

            if (args.length == 4) {
                if (args[1].equalsIgnoreCase("set")) {
                    String arenaName = args[2];
                    Arena arena = this.instance.getArenaManager().getArena(arenaName);

                    if (arena == null) {
                        this.sendMessage(commandSender, this.instance.getMessages().COMMAND_ARENANOTFOUND());
                        return true;
                    }

                    String action = args[3];

                    if (action.equals("lobby")) {
                        if (!senderIsPlayer) {
                            this.sendMessage(commandSender, this.instance.getMessages().COMMAND_ONLYPLAYER());
                            return true;
                        }

                        Player player = (Player) commandSender;
                        Location lobby = player.getLocation();

                        arena.getOptions().setLobby(lobby);
                        this.sendMessage(commandSender, this.instance.getMessages().COMMAND_ARENA_SET_LOBBY_SUCCESS(lobby.getWorld().getName(), lobby.getBlockX(), lobby.getBlockY(), lobby.getBlockZ()));
                        return true;
                    }
                }
            }

            if (args.length == 5) {
                if (args[1].equalsIgnoreCase("set")) {
                    String arenaName = args[2];
                    Arena arena = this.instance.getArenaManager().getArena(arenaName);

                    if (arena == null) {
                        this.sendMessage(commandSender, this.instance.getMessages().COMMAND_ARENANOTFOUND());
                        return true;
                    }


                    String action = args[3];
                    String actionValue = args[4];

                    if (action.equalsIgnoreCase("minplayers")) {
                        try {
                            int minimumPlayers = Integer.parseInt(actionValue);
                            int maximumPlayers = arena.getOptions().getMaximumPlayers();

                            if (maximumPlayers != 0 && minimumPlayers > maximumPlayers || minimumPlayers < 0) {
                                this.sendMessage(commandSender, this.instance.getMessages().COMMAND_ARENA_SET_MINPLAYERS_LOW_VALUE());
                                return true;
                            }

                            arena.getOptions().setMinimumPlayers(minimumPlayers);
                            this.sendMessage(commandSender, this.instance.getMessages().COMMAND_ARENA_SET_MINPLAYERS_SUCCESS(minimumPlayers));
                            return true;
                        } catch (NumberFormatException numberFormatException) {
                            this.sendMessage(commandSender, this.instance.getMessages().COMMAND_EXPECTEDNUMBER());
                            return true;
                        }
                    }

                    if (action.equalsIgnoreCase("maxplayers")) {
                        try {
                            int minimumPlayers = arena.getOptions().getMinimumPlayers();
                            int maximumPlayers = Integer.parseInt(actionValue);

                            if (minimumPlayers != 0 && maximumPlayers < minimumPlayers || maximumPlayers < 0) {
                                this.sendMessage(commandSender, this.instance.getMessages().COMMAND_ARENA_SET_MAXPLAYERS_LOW_VALUE());
                                return true;
                            }

                            arena.getOptions().setMaximumPlayers(maximumPlayers);
                            this.sendMessage(commandSender, this.instance.getMessages().COMMAND_ARENA_SET_MAXPLAYERS_SUCCESS(maximumPlayers));
                            return true;
                        } catch (NumberFormatException numberFormatException) {
                            this.sendMessage(commandSender, this.instance.getMessages().COMMAND_EXPECTEDNUMBER());
                            return true;
                        }
                    }

                    if (action.equalsIgnoreCase("gamearea")) {
                        if (!senderIsPlayer) {
                            this.sendMessage(commandSender, this.instance.getMessages().COMMAND_ONLYPLAYER());
                            return true;
                        }

                        Player player = (Player) commandSender;
                        Location gameAreaLocation = player.getLocation();
                        ArenaOptions.GameArea gameArea;

                        try {
                            gameArea = ArenaOptions.GameArea.valueOf(actionValue.toUpperCase());
                        } catch (IllegalArgumentException illegalArgumentException) {
                            this.sendMessage(commandSender, this.instance.getMessages().COMMAND_USAGE());
                            return true;
                        }

                        arena.getOptions().setGameAreaLocation(gameArea, gameAreaLocation);

                        this.sendMessage(commandSender, this.instance.getMessages().COMMAND_ARENA_SET_GAMEAREA_SUCCESS(gameArea, gameAreaLocation.getWorld().getName(), gameAreaLocation.getBlockX(), gameAreaLocation.getBlockY(), gameAreaLocation.getBlockZ()));
                        return true;
                    }
                }
            }
        }

        if (args[0].equalsIgnoreCase("join") && args.length == 2) {
            if (!senderIsPlayer) {
                this.sendMessage(commandSender, this.instance.getMessages().COMMAND_ONLYPLAYER());
                return true;
            }

            Player player = (Player) commandSender;
            String arenaName = args[1];

            if (!this.instance.getArenaManager().exists(arenaName)) {
                this.sendMessage(commandSender, this.instance.getMessages().COMMAND_ARENANOTFOUND());
                return true;
            }

            Arena arena = this.instance.getArenaManager().getArena(arenaName);
            switch (this.instance.getArenaManager().join(arena, player)) {
                case PLAYER_IN_ARENA:
                    this.sendMessage(commandSender, this.instance.getMessages().COMMAND_JOIN_ALREADYINARENA());
                    break;

                case ARENA_ISNT_SETUP:
                    this.sendMessage(commandSender, this.instance.getMessages().COMMAND_JOIN_ARENAISNTSETUP());
                    break;

                case ARENA_FULL:
                    this.sendMessage(commandSender, this.instance.getMessages().COMMAND_JOIN_ARENAFULL());
                    break;

                case ARENA_IN_GAME:
                    this.sendMessage(commandSender, this.instance.getMessages().COMMAND_JOIN_ARENAINGAME());
                    break;
            }

            return true;
        }

        if (args[0].equalsIgnoreCase("leave") && args.length == 1) {
            if (!senderIsPlayer) {
                this.sendMessage(commandSender, this.instance.getMessages().COMMAND_ONLYPLAYER());
                return true;
            }

            Player player = (Player) commandSender;

            if (Objects.requireNonNull(this.instance.getArenaManager().leave(player)) == ArenaManager.Result.PLAYER_NOT_IN_ARENA) {
                this.sendMessage(commandSender, this.instance.getMessages().COMMAND_LEAVE_NOTINARENA());
            }

            return true;
        }

        this.sendMessage(commandSender, this.instance.getMessages().COMMAND_USAGE());
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 0) return null;

        if (args.length == 1)
            completions.addAll(Arrays.asList("arena", "join", "leave"));

        if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("leave"))
            if (args.length == 2)
                completions.addAll(this.instance.getArenaManager().getArenaList().stream().map(Arena::getName).collect(Collectors.toList()));

        if (args[0].equalsIgnoreCase("arena")) {
            if (!commandSender.hasPermission(Permissions.ADMIN.toString()))
                return null;

            if (args.length == 2)
                completions.addAll(Arrays.asList("list", "create", "delete", "set"));

            if (args.length == 3 && (
                args[1].equalsIgnoreCase("delete")
                || args[1].equalsIgnoreCase("set"))
            )
                completions.addAll(this.instance.getArenaManager().getArenaList().stream().map(Arena::getName).collect(Collectors.toList()));

            if (args.length == 4 && args[1].equalsIgnoreCase("set")) {
                completions.addAll(Arrays.asList("lobby", "gamearea", "minplayers", "maxplayers"));
            }

            if (args.length == 5 && args[1].equalsIgnoreCase("set")) {
                if (args[3].equalsIgnoreCase("gamearea"))
                    completions.addAll(Arrays.asList(ArenaOptions.GameArea.TOP.toString(), ArenaOptions.GameArea.BOTTOM.toString()));
            }
        }

        String currentInput = args[args.length - 1].toLowerCase();
        completions.removeIf(option -> !option.toLowerCase().startsWith(currentInput));

        return completions;
    }
}