package com.goodbyepavlyi.lavarise.plugin.utils;

import com.goodbyepavlyi.lavarise.plugin.LavaRise;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.util.logging.Level;

public class ConfigUtil {
    private final LavaRise instance;
    private final String path;
    private final File file;
    private final FileConfiguration config;

    public ConfigUtil(LavaRise instance, String path, boolean save) {
        this.instance = instance;
        this.path = path;
        File pluginDirectory = this.instance.getDataFolder();
        this.file = new File(pluginDirectory.getAbsolutePath() + '/' + this.path);

        if (!pluginDirectory.exists())
            pluginDirectory.mkdirs();

        if (!this.file.exists() && save)
            this.saveDefault();

        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public boolean saveDefault() {
        try (InputStream inputStream = this.instance.getResource(this.path);
            OutputStream outStream = Files.newOutputStream(this.file.toPath())) {

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1)
                outStream.write(buffer, 0, bytesRead);

            return true;
        } catch (IOException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public void save() {
        try {
            this.getConfig().save(this.file);
            instance.debug(Level.INFO, String.format("Config file saved: %s", this.file.getAbsolutePath()));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public File getFile() {
        return this.file;
    }

    public FileConfiguration getConfig() {
        return this.config;
    }
}
