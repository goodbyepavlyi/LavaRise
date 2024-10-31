package me.goodbyepavlyi.lavarise.commands.lavarise;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.utils.CommandUtils;
import me.goodbyepavlyi.lavarise.utils.Logger;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public class LavaRisePluginCommand {
    public static boolean onCommand(LavaRiseInstance lavaRiseInstance, Player player, Command command, String label, String[] args) {
        if (!player.hasPermission(LavaRiseCommand.Permissions.ADMIN.toString())) {
            CommandUtils.sendMessage(player, lavaRiseInstance.getMessages().CommandNoPermissions());
            return true;
        }

        if (args[1].equalsIgnoreCase("world_teleport") && args.length == 6) {
            String worldName = args[2];
            int x = Integer.parseInt(args[3]);
            int y = Integer.parseInt(args[4]);
            int z = Integer.parseInt(args[5]);
            Logger.debug(String.format("PluginCommand: Teleporting player to %s at %d, %d, %d", worldName, x, y, z));

            if (lavaRiseInstance.getServer().getWorld(worldName) == null) {
                Logger.debug(String.format("PluginCommand: World %s does not exist", worldName));
                return true;
            }

            player.teleport(lavaRiseInstance.getServer().getWorld(worldName).getBlockAt(x, y, z).getLocation());
        }

        return true;
    }
}
