package com.goodbyepavlyi.lavarise.plugin.game;

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
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.logging.Level;

public class GameEvents implements Listener {
    private final LavaRise instance;

    public GameEvents(LavaRise instance) {
        this.instance = instance;
    }

    @EventHandler
    public void playerDamageToPlayer(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player && event.getEntity() instanceof Player)) return;

        Player damager = (Player) event.getDamager();
        Arena arena = this.instance.getArenaManager().getArena(damager.getUniqueId());

        if (arena == null) return;
        if (!arena.getState().equals(Arena.State.IN_GAME)) return;
        if (!arena.getGame().getGamePhase().equals(Game.GamePhase.GRACE)) return;

        event.setCancelled(true);

        this.instance.debug(Level.INFO, String.format("Player %s damaged player %s in arena %s (Cancelled)", damager.getName(), event.getEntity().getName(), arena.getName()));
    }

    @EventHandler
    public void playerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Arena arena = this.instance.getArenaManager().getArena(player.getUniqueId());

        if (arena == null) return;
        if (!arena.getState().equals(Arena.State.IN_GAME)) return;

        event.setDeathMessage(null);

        Game game = arena.getGame();
        game.makeSpectator(player);

        this.instance.debug(Level.INFO, String.format("Player %s died in arena %s", player.getName(), arena.getName()));
    }

    @EventHandler
    public void playerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Arena arena = this.instance.getArenaManager().getArena(player.getUniqueId());

        if (arena == null) return;
        if (!arena.getState().equals(Arena.State.IN_GAME)) return;
        if (player.getHealth() - event.getFinalDamage() > 0) return;

        event.setCancelled(true);

        Game game = arena.getGame();
        arena.announce(Arena.Announce.PLAYER_DEATH, player.getName());
        game.makeSpectator(player);

        this.instance.debug(Level.INFO, String.format("Player %s died in arena %s (Cancelled)", player.getName(), arena.getName()));
    }

    @EventHandler
    public void gracePeriodNoDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Arena arena = this.instance.getArenaManager().getArena(player.getUniqueId());

        if (arena == null) return;
        if (!arena.getState().equals(Arena.State.IN_GAME)) return;
        if (!arena.getGame().getGamePhase().equals(Game.GamePhase.GRACE)) return;

        event.setCancelled(true);

        this.instance.debug(Level.INFO, String.format("Player %s received damage during grace period in arena %s (Cancelled)", player.getName(), arena.getName()));
    }

    @EventHandler
    public void playerDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Arena arena = this.instance.getArenaManager().getArena(player.getUniqueId());

        if (arena == null || !arena.getState().equals(Arena.State.IN_GAME)) return;

        Game game = arena.getGame();
        game.makeSpectator(player);

        this.instance.debug(Level.INFO, String.format("Player %s disconnected in arena %s", player.getName(), arena.getName()));
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Arena arena = this.instance.getArenaManager().getArena(player.getUniqueId());
        Block block = event.getBlock();

        if (block == null) return;
        if (arena == null || !arena.getState().equals(Arena.State.IN_GAME)) return;

        Game game = arena.getGame();
        if (game.getGameMap().isLocationInsideMap(block.getLocation())) return;

        event.setCancelled(true);

        this.instance.debug(Level.INFO, String.format("Block break event cancelled for player %s in arena %s", player.getName(), arena.getName()));
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Arena arena = this.instance.getArenaManager().getArena(player.getUniqueId());
        Block block = event.getBlock();

        if (block == null) return;
        if (arena == null || !arena.getState().equals(Arena.State.IN_GAME)) return;

        Game game = arena.getGame();
        if (game.getGameMap().isLocationInsideMap(block.getLocation())) return;

        event.setCancelled(true);

        this.instance.debug(Level.INFO, String.format("Block place event cancelled for player %s in arena %s", player.getName(), arena.getName()));
    }
}

