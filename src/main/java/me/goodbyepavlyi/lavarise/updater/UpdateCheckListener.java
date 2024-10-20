package me.goodbyepavlyi.lavarise.updater;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateCheckListener implements Listener {
    private final UpdateChecker instance;

    public UpdateCheckListener(UpdateChecker instance) {
        this.instance = instance;
    }

    @EventHandler
    public void notifyOnJoin(PlayerJoinEvent event) {
        if (!instance.isCheckedAtLeastOnce() || instance.isUsingLatestVersion()) return;

        Player player = event.getPlayer();
        if ((player.isOp() && instance.isNotifyOps()) || (instance.getNotifyPermission() != null && player.hasPermission(instance.getNotifyPermission()))) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                String.format("&8[&6LavaRise&8] &8A new version of &6LavaRise&8 is available! &8(&6%s&8 -> &6%s&8)",
                    instance.getCurrentVersion(), instance.getLatestVersion())
            ));
        }
    }
}
