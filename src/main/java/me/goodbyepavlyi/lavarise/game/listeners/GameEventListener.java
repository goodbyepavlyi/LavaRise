package me.goodbyepavlyi.lavarise.game.listeners;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.arena.Arena;
import me.goodbyepavlyi.lavarise.utils.Logger;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GameEventListener implements Listener {
    private final LavaRiseInstance instance;

    public GameEventListener(LavaRiseInstance instance) {
        this.instance = instance;
    }

    @EventHandler
    public void makePlayerSpectatorOnDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Arena arena = this.instance.getArenaManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null || arena.getState() != Arena.State.IN_GAME || arena.getGame().isSpectator(player)) return;

        event.setDeathMessage(null);
        arena.announceMessage(Arena.AnnouncementType.PLAYER_DEATH, player.getName());
        arena.getGame().makeSpectator(player);
        Logger.debug(String.format("Player %s died in arena %s", player.getName(), arena.getName()));
    }

    @EventHandler
    public void makePlayerSpectatorOnPlayerKill(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim && event.getDamager() instanceof Player killer)) return;
        Logger.debug(String.format("Victim: %s, Killer: %s", victim.getName(), killer.getName()));

        Arena arena = this.instance.getArenaManager().getArenaByPlayer(victim.getUniqueId());
        if (arena == null || arena.getState() != Arena.State.IN_GAME || arena.getGame().isSpectator(victim)) return;
        if (victim.getHealth() - event.getFinalDamage() > 0) return;

        event.setCancelled(true);
        arena.announceMessage(Arena.AnnouncementType.PLAYER_KILLED, victim.getName(), killer.getName());
        arena.getGame().makeSpectator(victim);
        Logger.debug(String.format("Player %s was killed by %s in arena %s", victim.getName(), killer.getName(), arena.getName()));
    }

    @EventHandler
    public void makePlayerSpectatorOnFinalDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) return; // Let the makePlayerSpectatorOnPlayerKill handle this
        if (!(event.getEntity() instanceof Player player)) return;

        Arena arena = this.instance.getArenaManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null || arena.getState() != Arena.State.IN_GAME || arena.getGame().isSpectator(player)) return;
        if (player.getHealth() - event.getFinalDamage() > 0) return;

        event.setCancelled(true);
        arena.announceMessage(Arena.AnnouncementType.PLAYER_DEATH, player.getName());
        arena.getGame().makeSpectator(player);
        Logger.debug(String.format("Player %s died on final damage in arena %s", player.getName(), arena.getName()));
    }

    @EventHandler
    public void cancelSpectatorDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Arena arena = this.instance.getArenaManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null || arena.getState() != Arena.State.IN_GAME) return;
        if (!arena.getGame().isSpectator(player)) return;

        event.setCancelled(true);
        Logger.debug(String.format("Cancelled damage for spectator %s in arena %s", player.getName(), arena.getName()));
    }

    @EventHandler
    public void cancelDamageBySpectator(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player spectator && event.getEntity() instanceof Player victim)) return;

        Arena arena = this.instance.getArenaManager().getArenaByPlayer(spectator.getUniqueId());
        if (arena == null || arena.getState() != Arena.State.IN_GAME || !arena.getGame().isSpectator(spectator)) return;

        event.setCancelled(true);
        Logger.debug(String.format("Cancelled damage between players %s and %s in arena %s because %s is a spectator", spectator.getName(), victim.getName(), arena.getName(), spectator.getName()));
    }

    @EventHandler
    public void playerDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Arena arena = this.instance.getArenaManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null || arena.getState() != Arena.State.IN_GAME) return;

        arena.removePlayer(player);
        Logger.debug(String.format("Player %s disconnected, removing them from arena %s", player.getName(), arena.getName()));
    }

    @EventHandler
    public void cancelBlockBreakingOutsideGameMap(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (block == null) return;

        Arena arena = this.instance.getArenaManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null || arena.getState() != Arena.State.IN_GAME || arena.getGame().isSpectator(player)) return;
        if (arena.getGame().getGameMap().isLocationInsideMap(block.getLocation())) return;

        event.setCancelled(true);
        Logger.debug(String.format("Cancelling block break event for player %s in arena %s (Outside game map)", player.getName(), arena.getName()));
    }

    @EventHandler
    public void cancelBlockPlacingOutsideGameMap(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (block == null) return;

        Arena arena = this.instance.getArenaManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null || arena.getState() != Arena.State.IN_GAME || arena.getGame().isSpectator(player)) return;
        if (arena.getGame().getGameMap().isLocationInsideMap(block.getLocation())) return;

        event.setCancelled(true);
        Logger.debug(String.format("Cancelling block place event for player %s in arena %s (Outside game map)", player.getName(), arena.getName()));
    }
}
