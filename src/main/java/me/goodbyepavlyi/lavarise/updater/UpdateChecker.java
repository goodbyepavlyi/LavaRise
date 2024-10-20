package me.goodbyepavlyi.lavarise.updater;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {
    private static final String SPIGOT_UPDATE_API = "https://api.spigotmc.org/simple/0.2/index.php?action=getResource&id=%s";
    private static final ThrowingFunction<BufferedReader, String, IOException> SPIGOT_MAPPER = reader -> new Gson().fromJson(reader, JsonObject.class).get("current_version").getAsString();

    private final JavaPlugin plugin;
    private final String apiLink;
    private final String currentVersion;

    private String latestVersion;
    private boolean checkedAtLeastOnce;
    private int taskId;

    private boolean notifyOps = true;
    private String notifyPermission = null;

    public UpdateChecker(JavaPlugin plugin, String resourceId) {
        if (plugin == null)
            throw new IllegalStateException("Plugin has not been set.");

        this.plugin = plugin;
        this.apiLink = String.format(SPIGOT_UPDATE_API, resourceId);
        this.currentVersion = plugin.getDescription().getVersion().trim();

        this.plugin.getServer().getPluginManager().registerEvents(new UpdateCheckListener(this), this.plugin);
    }

    public UpdateChecker checkEveryXHours(double hours) {
        double seconds = hours * 60 * 60;
        long ticks = ((int) seconds) * 20L;
        this.stopTask();
        if (ticks > 0) this.taskId = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, this::checkNow, ticks, ticks);
        else this.taskId = -1;

        return this;
    }

    public void stopTask() {
        if (this.taskId != -1) this.plugin.getServer().getScheduler().cancelTask(this.taskId);
        this.taskId = -1;
    }

    public UpdateChecker checkNow() {
        this.checkedAtLeastOnce = true;

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
                HttpURLConnection httpConnection = (HttpURLConnection) new URL(this.apiLink).openConnection();

                try (InputStreamReader input = new InputStreamReader(httpConnection.getInputStream());
                     BufferedReader reader = new BufferedReader(input)) {
                    this.latestVersion = SPIGOT_MAPPER.apply(reader);
                }
            } catch (IOException exception) {
                this.plugin.getLogger().warning("Could not check for updates: " + exception.getMessage());
            }

            if (!isUsingLatestVersion())
                this.plugin.getLogger().warning(String.format("There is a newer version available: %s, you are running version: %s", this.latestVersion, this.currentVersion));
        });

        return this;
    }

    public boolean isUsingLatestVersion() {
        return !isOtherVersionNewer(this.currentVersion, this.latestVersion);
    }

    private boolean isOtherVersionNewer(String myVersion, String otherVersion) {
        String[] myParts = myVersion.split("\\.");
        String[] otherParts = otherVersion.split("\\.");

        for (int i = 0; i < Math.max(myParts.length, otherParts.length); i++) {
            String myPart = i < myParts.length ? myParts[i] : "0"; // default to 0 if myVersion is shorter
            String otherPart = i < otherParts.length ? otherParts[i] : "0"; // default to 0 if otherVersion is shorter

            int comparison = compareVersionPart(myPart, otherPart);
            if (comparison < 0) return true; // otherVersion is newer
            else if (comparison > 0) return false; // myVersion is newer
        }

        return false; // versions are equal
    }

    private int compareVersionPart(String part1, String part2) {
        return isNumeric(part1) && isNumeric(part2)
            ? Integer.compare(Integer.parseInt(part1), Integer.parseInt(part2))
            : part1.compareTo(part2); // compare lexicographically
    }

    private boolean isNumeric(String str) {
        return str.matches("\\d+");
    }

    public String getLatestVersion() {
        return this.latestVersion;
    }

    public String getNotifyPermission() {
        return this.notifyPermission;
    }

    public String getCurrentVersion() {
        return this.currentVersion;
    }

    public boolean isCheckedAtLeastOnce() {
        return this.checkedAtLeastOnce;
    }

    public boolean isNotifyOps() {
        return this.notifyOps;
    }

    public UpdateChecker setNotifyOps(boolean notifyOps) {
        this.notifyOps = notifyOps;
        return this;
    }

    public UpdateChecker setNotifyByPermissionOnJoin(String permission) {
        this.notifyPermission = permission;
        return this;
    }
}
