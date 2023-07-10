package com.goodbyepavlyi.lavarise.plugin.queue;

import com.goodbyepavlyi.lavarise.plugin.LavaRise;
import com.goodbyepavlyi.lavarise.plugin.arena.Arena;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

import java.util.logging.Level;

public class QueueEvents implements Listener {
    private final LavaRise instance;

    public QueueEvents(LavaRise instance) {
        this.instance = instance;
    }

    @EventHandler
    public void playerDamageToEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player damager = (Player) event.getDamager();
        Arena arena = this.instance.getArenaManager().getArena(damager.getUniqueId());

        if (arena == null || !(arena.getState().equals(Arena.State.WAITING) || arena.getState().equals(Arena.State.STARTING))) return;

        event.setCancelled(true);
        this.instance.debug(Level.INFO, String.format("Cancelled player damage to entity in arena %s by player %s", arena.getName(), damager.getName()));
    }

    @EventHandler
    public void playerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Arena arena = this.instance.getArenaManager().getArena(player.getUniqueId());

        if (arena == null || !(arena.getState().equals(Arena.State.WAITING) || arena.getState().equals(Arena.State.STARTING))) return;

        event.setCancelled(true);
        this.instance.debug(Level.INFO, String.format("Cancelled player damage in arena %s to player %s", arena.getName(), player.getName()));
    }

    @EventHandler
    public void disableInventoryMove(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Arena arena = this.instance.getArenaManager().getArena(player.getUniqueId());

        if (arena == null || !(arena.getState().equals(Arena.State.WAITING) || arena.getState().equals(Arena.State.STARTING))) return;

        event.setCancelled(true);
        this.instance.debug(Level.INFO, String.format("Cancelled inventory move in arena %s by player %s", arena.getName(), player.getName()));
    }

    @EventHandler
    public void playerDamage(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Arena arena = this.instance.getArenaManager().getArena(player.getUniqueId());

        if (arena == null || !(arena.getState().equals(Arena.State.WAITING) || arena.getState().equals(Arena.State.STARTING))) return;

        event.setCancelled(true);
        this.instance.debug(Level.INFO, String.format("Cancelled player item drop in arena %s by player %s", arena.getName(), player.getName()));
    }

    @EventHandler
    public void handleLeaveItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Arena arena = this.instance.getArenaManager().getArena(player.getUniqueId());

        if (arena == null || !(arena.getState().equals(Arena.State.WAITING) || arena.getState().equals(Arena.State.STARTING))) return;

        ItemStack item = event.getPlayer().getItemInHand();
        if (item == null) return;

        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null || !itemMeta.getDisplayName().equals(this.instance.getMessages().QUEUE_ITEMS_LEAVE_NAME())) return;

        arena.getQueue().removePlayer(player);
        event.setCancelled(true);
        this.instance.debug(Level.INFO, String.format("Cancelled leave item interaction in arena %s by player %s", arena.getName(), player.getName()));
    }

    @EventHandler
    public void playerDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Arena arena = this.instance.getArenaManager().getArena(player.getUniqueId());

        if (arena == null || !(arena.getState().equals(Arena.State.WAITING) || arena.getState().equals(Arena.State.STARTING))) return;

        arena.getQueue().removePlayer(player);
        this.instance.debug(Level.INFO, String.format("Player %s removed from the queue in arena %s", player.getName(), arena.getName()));
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Arena arena = this.instance.getArenaManager().getArena(player.getUniqueId());
        Block block = event.getBlock();

        if (block == null) return;
        if (arena == null || !(arena.getState().equals(Arena.State.WAITING) || arena.getState().equals(Arena.State.STARTING))) return;

        event.setCancelled(true);
        this.instance.debug(Level.INFO, String.format("Cancelled block break by player %s in arena %s", player.getName(), arena.getName()));
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Arena arena = this.instance.getArenaManager().getArena(player.getUniqueId());
        Block block = event.getBlock();

        if (block == null) return;
        if (arena == null || !(arena.getState().equals(Arena.State.WAITING) || arena.getState().equals(Arena.State.STARTING))) return;

        event.setCancelled(true);
        this.instance.debug(Level.INFO, String.format("Cancelled block place by player %s in arena %s", player.getName(), arena.getName()));
    }
}
