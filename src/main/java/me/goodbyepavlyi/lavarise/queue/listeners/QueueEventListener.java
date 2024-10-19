package me.goodbyepavlyi.lavarise.queue.listeners;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.arena.Arena;
import me.goodbyepavlyi.lavarise.utils.Logger;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class QueueEventListener implements Listener {
    private final LavaRiseInstance instance;

    public QueueEventListener(LavaRiseInstance instance) {
        this.instance = instance;
    }

    @EventHandler
    public void removePlayerFromQueue(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Arena arena = this.instance.getArenaManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null || arena.getState() == Arena.State.IN_GAME) return;

        arena.removePlayer(player);
        Logger.debug(String.format("Removed player %s from queue in arena %s", player.getName(), arena.getName()));
    }

    @EventHandler
    public void handleLeaveItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Arena arena = this.instance.getArenaManager().getArenaByPlayer(player.getUniqueId());
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (arena == null || arena.getState() == Arena.State.IN_GAME) return;

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (item == null) return;

        ItemMeta itemMeta = item.getItemMeta();
        ItemStack leaveItem = arena.getQueue().getLeaveItem();
        if (itemMeta == null
            || itemMeta.getDisplayName() == null
            || !itemMeta.getDisplayName().equals(leaveItem.getItemMeta().getDisplayName()))
            return;

        event.setCancelled(true);
        arena.removePlayer(player);
        Logger.debug(String.format("Player %s used leave item in arena %s, removed from queue", player.getName(), arena.getName()));
    }

    @EventHandler
    public void cancelPlayerDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Arena arena = this.instance.getArenaManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null || arena.getState() == Arena.State.IN_GAME) return;

        event.setCancelled(true);
        Logger.debug(String.format("Cancelled player damage in arena %s to player %s", arena.getName(), player.getName()));
    }

    @EventHandler
    public void cancelPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Arena arena = this.instance.getArenaManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null || arena.getState() == Arena.State.IN_GAME) return;

        event.setCancelled(true);
        Logger.debug(String.format("Cancelled player damage in arena %s to player %s", arena.getName(), player.getName()));
    }

    @EventHandler
    public void cancelInventoryItemMove(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Arena arena = this.instance.getArenaManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null || arena.getState() == Arena.State.IN_GAME) return;

        event.setCancelled(true);
        Logger.debug(String.format("Cancelled inventory move in arena %s to player %s", arena.getName(), player.getName()));
    }

    @EventHandler
    public void cancelPlayerDroppingItems(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Arena arena = this.instance.getArenaManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null || arena.getState() == Arena.State.IN_GAME) return;

        event.setCancelled(true);
        Logger.debug(String.format("Cancelled player item drop in arena %s by player %s", arena.getName(), player.getName()));
    }

    @EventHandler
    public void cancelBlockBreaking(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (player == null || block == null) return;

        Arena arena = this.instance.getArenaManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null || arena.getState() == Arena.State.IN_GAME) return;

        event.setCancelled(true);
        Logger.debug(String.format("Cancelled block break in arena %s by player %s", arena.getName(), player.getName()));
    }

    @EventHandler
    public void cancelBlockPlacing(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (player == null || block == null) return;

        Arena arena = this.instance.getArenaManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null || arena.getState() == Arena.State.IN_GAME) return;

        event.setCancelled(true);
        Logger.debug(String.format("Cancelled block place in arena %s by player %s", arena.getName(), player.getName()));
    }
}
