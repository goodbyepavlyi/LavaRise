package me.goodbyepavlyi.lavarise.game.listeners;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.arena.Arena;
import me.goodbyepavlyi.lavarise.utils.Logger;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GameEventListener implements Listener {
    private final LavaRiseInstance instance;

    public GameEventListener(LavaRiseInstance instance) {
        this.instance = instance;
    }

    @EventHandler
    public void cancelDisallowedCommands(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        Arena arena = this.instance.getArenaManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null) return;
        if(!this.instance.getConfiguration().GameAllowedCommandsEnabled()) return;

        String command = event.getMessage().toLowerCase();
        if(this.instance.getConfiguration().GameAllowedCommands().contains(command)) return;

        Logger.debug(String.format("Player %s tried to execute disallowed command %s", player.getName(), command));
        player.sendMessage(this.instance.getMessages().CommandNoPermissions());
        event.setCancelled(true);
    }
    
    @EventHandler
    public void makePlayerSpectatorOnDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Arena arena = this.instance.getArenaManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null || arena.getState() != Arena.State.IN_GAME || arena.getGame().isSpectator(player)) return;

        event.setDeathMessage(null);
        arena.announceMessage(Arena.AnnouncementType.PLAYER_DEATH, player.getName());
        arena.getGame().makeSpectator(player);
        arena.getPlayer(player.getUniqueId()).getStatistics().addDeath();
        arena.getPlayer(player.getUniqueId()).getStatistics().addLoss();
        Logger.debug(String.format("Player %s died in arena %s", player.getName(), arena.getName()));
    }

    @EventHandler
    public void makePlayerSpectatorOnPlayerKill(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim && event.getDamager() instanceof Player killer)) return;

        Arena arena = this.instance.getArenaManager().getArenaByPlayer(victim.getUniqueId());
        if (arena == null || arena.getState() != Arena.State.IN_GAME || arena.getGame().isSpectator(victim)) return;
        if (victim.getHealth() - event.getFinalDamage() > 0) return;

        event.setCancelled(true);
        arena.announceMessage(Arena.AnnouncementType.PLAYER_KILLED, victim.getName(), killer.getName());
        arena.getGame().makeSpectator(victim);
        arena.getPlayer(killer.getUniqueId()).getStatistics().addKill();
        arena.getPlayer(victim.getUniqueId()).getStatistics().addDeath();
        arena.getPlayer(victim.getUniqueId()).getStatistics().addLoss();
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
        arena.getPlayer(player.getUniqueId()).getStatistics().addDeath();
        arena.getPlayer(player.getUniqueId()).getStatistics().addLoss();
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

        arena.getPlayer(player.getUniqueId()).getStatistics().addLoss();
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

    @EventHandler
    public void cancelPlayerPVP(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim && event.getDamager() instanceof Player killer)) return;

        Arena arena = this.instance.getArenaManager().getArenaByPlayer(victim.getUniqueId());
        if (arena == null || arena.getState() != Arena.State.IN_GAME || arena.getGame().isPVPEnabled) return;

        event.setCancelled(true);
        Logger.debug(String.format("Cancelled PVP between players %s and %s in arena %s", killer.getName(), victim.getName(), arena.getName()));
    }

    @EventHandler
    public void blockMoveOutsideGameMap(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Arena arena = this.instance.getArenaManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null || arena.getState() != Arena.State.IN_GAME) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) return;
        if (arena.getGame().getGameMap().isLocationInsideMap(to)) return;

        from.setYaw(to.getYaw());
        from.setPitch(to.getPitch());
        event.setTo(from);
        Logger.debug(String.format("Cancelled player %s move event in arena %s (Outside game map)", player.getName(), arena.getName()));
    }

    @EventHandler
    public void cancelSpectatorItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Arena arena = this.instance.getArenaManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null || arena.getState() != Arena.State.IN_GAME || !arena.getGame().isSpectator(player)) return;

        event.setCancelled(true);
        Logger.debug(String.format("Cancelled item pickup event for spectator %s in arena %s", player.getName(), arena.getName()));
    }
    
    @EventHandler
    public void cancelSpectatorFoodHunger(FoodLevelChangeEvent event){
        if (!(event.getEntity() instanceof Player player)) return;

        Arena arena = this.instance.getArenaManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null || arena.getState() != Arena.State.IN_GAME || !arena.getGame().isSpectator(player)) return;

        event.setCancelled(true);
        Logger.debug(String.format("Cancelled food level change event for spectator %s in arena %s", player.getName(), arena.getName()));
    }
}
