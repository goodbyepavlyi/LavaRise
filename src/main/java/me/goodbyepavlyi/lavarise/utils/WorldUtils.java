package me.goodbyepavlyi.lavarise.utils;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class WorldUtils {
    public static World copyWorld(JavaPlugin plugin, World sourceWorld, String targetWorldName) {
        File sourceWorldFile = new File(plugin.getServer().getWorldContainer(), sourceWorld.getName());
        File targetWorldFile = new File(plugin.getServer().getWorldContainer(), targetWorldName);

        if (!sourceWorldFile.exists()) {
            Logger.severe(String.format("Source world does not exist: %s", sourceWorldFile.getName()));
            return null;
        }

        if (targetWorldFile.exists()) {
            Logger.warning(String.format("Target world already exists: %s", targetWorldName));
            return null;
        }

        try {
            copyDirectory(sourceWorldFile.toPath(), targetWorldFile.toPath());
            Logger.debug(String.format("World copied successfully from %s to %s", sourceWorldFile.getName(), targetWorldName));

            WorldCreator creator = new WorldCreator(targetWorldName);
            return plugin.getServer().createWorld(creator);
        } catch (IOException e) {
            Logger.severe(String.format("Failed to copy world %s to %s, see stack trace for details:", sourceWorld.getName(), targetWorldName));
            e.printStackTrace();
            return null;
        }
    }

    public static void deleteWorld(JavaPlugin plugin, String worldName) {
        World world = plugin.getServer().getWorld(worldName);
        if (world != null) {
            Logger.debug(String.format("Unloading world %s", worldName));
            plugin.getServer().unloadWorld(world, false);
        }

        File worldFolder = new File(plugin.getServer().getWorldContainer(), worldName);

        if (!worldFolder.exists()) {
            Logger.warning(String.format("World does not exist: %s", worldName));
            return;
        }

        try {
            deleteDirectory(worldFolder);
            Logger.debug(String.format("World deleted successfully: %s", worldName));
        } catch (Exception e) {
            Logger.severe(String.format("Failed to delete world %s, see stack trace for details:", worldName));
            e.printStackTrace();
        }
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(path -> {
            try {
                Path destination = target.resolve(source.relativize(path));
                if (destination.getFileName() != null
                    && destination.getFileName().toString().equals("uid.dat")
                    || destination.getFileName().toString().equals("session.lock")) return;

                Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                Logger.severe(String.format("Failed to copy file %s to %s, see stack trace for details:", path, target));
                e.printStackTrace();
            }
        });
    }

    private static void deleteDirectory(File file) {
        if (file.isDirectory()) {
            for (File dir : file.listFiles()) {
                deleteDirectory(dir);
            }
        }

        if (!file.delete()) {
            Logger.severe(String.format("Failed to delete file or directory: %s", file.getAbsolutePath()));
        }
    }
}
