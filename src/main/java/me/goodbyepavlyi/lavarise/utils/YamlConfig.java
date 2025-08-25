package me.goodbyepavlyi.lavarise.utils;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.logging.Level;

public class YamlConfig {
    private static File pluginDirectory;
    private final LavaRiseInstance instance;
    private final String path;
    private final File file;
    private FileConfiguration config;

    public YamlConfig(LavaRiseInstance instance, String path, boolean copyFromResources) {
        this.instance = instance;
        this.path = path;

        if (pluginDirectory == null)
            pluginDirectory = this.instance.getDataFolder();

        if (!pluginDirectory.exists() && !pluginDirectory.mkdirs()) {
            Logger.log(Level.SEVERE, String.format("Failed to create plugin directory: %s", pluginDirectory.getAbsolutePath()));
            throw new RuntimeException(String.format("Failed to create plugin directory: %s", pluginDirectory.getAbsolutePath()));
        }

        this.file = new File(pluginDirectory, this.path);
        Logger.debug(String.format("Loading config file: %s", this.file.getAbsolutePath()));
        if (!this.file.exists() && copyFromResources)
            this.copyFromResources();

        this.load();
    }

    public void load(){
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public LavaRiseInstance getInstance() {
        return this.instance;
    }

    public File getFile() {
        return this.file;
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public FileConfiguration getResourceConfig() {
        try (InputStream inputStream = this.getInstance().getResource(this.path)) {
            if (inputStream == null) {
                Logger.severe(String.format("Failed to load default config file: %s", this.path));
                return null;
            }

            return YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream));
        } catch (IOException e) {
            Logger.severe(String.format("Error while reading default config file: %s", e.getMessage()));
            return null;
        }
    }

    public int getConfigVersion() {
        return this.config.getInt("version", -1);
    }

    public void setConfigVersion(int version) {
        this.config.set("version", version);
    }

    public void copyFromResources() {
        try (InputStream inputStream = this.instance.getResource(this.path);
            OutputStream outStream = Files.newOutputStream(this.file.toPath())) {

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1)
                outStream.write(buffer, 0, bytesRead);

            Logger.debug(String.format("Default config file copied: %s", this.file.getAbsolutePath()));
        } catch (IOException exception) {
            Logger.log(Level.SEVERE, String.format("Failed to save default config file: %s", this.file.getAbsolutePath()));
        }
    }

    public void save() {
        try {
            this.config.save(this.file);
            Logger.debug(String.format("Config file saved: %s", this.file.getAbsolutePath()));
        } catch (IOException ioException) {
            Logger.log(Level.SEVERE, String.format("Failed to save config file: %s", this.file.getAbsolutePath()));
        }
    }
}
