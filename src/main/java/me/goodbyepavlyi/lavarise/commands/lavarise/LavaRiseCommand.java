package me.goodbyepavlyi.lavarise.commands.lavarise;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.arena.Arena;
import me.goodbyepavlyi.lavarise.arena.ArenaManager;
import me.goodbyepavlyi.lavarise.arena.utils.ArenaConfig;
import me.goodbyepavlyi.lavarise.utils.CommandUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LavaRiseCommand implements CommandExecutor, TabCompleter {
    private final LavaRiseInstance instance;

    public LavaRiseCommand(LavaRiseInstance instance) {
        this.instance = instance;
    }

    public enum Permissions {
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
        if (!(commandSender instanceof Player player)) {
            CommandUtils.sendMessage(commandSender, this.instance.getMessages().COMMAND_ONLYPLAYER());
            return true;
        }

        if (args.length == 0) {
            CommandUtils.sendMessage(player, this.instance.getMessages().COMMAND_USAGE());
            return true;
        }

        if (args[0].equalsIgnoreCase("arena") && LavaRiseArenaCommand.onCommand(this.instance, player, command, label, args))
            return true;

        if (args[0].equalsIgnoreCase("join") && args.length == 2) {
            Arena arena = this.instance.getArenaManager().getArena(args[1]);
            if (arena == null) {
                CommandUtils.sendMessage(commandSender, this.instance.getMessages().COMMAND_ARENANOTFOUND());
                return true;
            }

            switch (this.instance.getArenaManager().joinArena(arena, player)) {
                case PLAYER_IN_ARENA:
                    CommandUtils.sendMessage(commandSender, this.instance.getMessages().COMMAND_JOIN_ALREADYINARENA());
                    break;

                case ARENA_IS_NOT_SETUP:
                    CommandUtils.sendMessage(commandSender, this.instance.getMessages().COMMAND_JOIN_ARENAISNTSETUP());
                    break;

                case ARENA_FULL:
                    CommandUtils.sendMessage(commandSender, this.instance.getMessages().COMMAND_JOIN_ARENAFULL());
                    break;

                case ARENA_IN_GAME:
                    CommandUtils.sendMessage(commandSender, this.instance.getMessages().COMMAND_JOIN_ARENAINGAME());
                    break;
            }

            return true;
        }

        if (args[0].equalsIgnoreCase("leave") && args.length == 1) {
            if (Objects.requireNonNull(this.instance.getArenaManager().leaveArena(player)) == ArenaManager.ArenaStateResult.PLAYER_NOT_IN_ARENA) {
                CommandUtils.sendMessage(commandSender, this.instance.getMessages().COMMAND_LEAVE_NOTINARENA());
            }

            return true;
        }

        CommandUtils.sendMessage(commandSender, this.instance.getMessages().COMMAND_USAGE());
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
                completions.addAll(this.instance.getArenaManager().getArenaList().stream().map(Arena::getName).toList());

        if (args[0].equalsIgnoreCase("arena")) {
            if (!commandSender.hasPermission(Permissions.ADMIN.toString()))
                return null;

            if (args.length == 2)
                completions.addAll(Arrays.asList("list", "create", "delete", "set"));

            if (args.length == 3 && (
                args[1].equalsIgnoreCase("delete")
                || args[1].equalsIgnoreCase("set"))
            )
                completions.addAll(this.instance.getArenaManager().getArenaList().stream().map(Arena::getName).toList());

            if (args.length == 4 && args[1].equalsIgnoreCase("set")) {
                completions.addAll(Arrays.asList("lobby", "gamearea", "minplayers", "maxplayers"));
            }

            if (args.length == 5 && args[1].equalsIgnoreCase("set")) {
                if (args[3].equalsIgnoreCase("gamearea"))
                    completions.addAll(Arrays.asList(ArenaConfig.GameArea.TOP.toString(), ArenaConfig.GameArea.BOTTOM.toString()));
            }
        }

        String currentInput = args[args.length - 1].toLowerCase();
        completions.removeIf(option -> !option.toLowerCase().startsWith(currentInput));

        return completions;
    }
}