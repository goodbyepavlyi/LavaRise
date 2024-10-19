package me.goodbyepavlyi.lavarise.game.listeners;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.arena.Arena;
import me.goodbyepavlyi.lavarise.game.Game;
import me.goodbyepavlyi.lavarise.utils.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class GameGracePhaseEventListener implements Listener {
    private final LavaRiseInstance instance;

    public GameGracePhaseEventListener(LavaRiseInstance instance) {
        this.instance = instance;
    }

    @EventHandler
    public void cancelDamageBetweenPlayers(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker && event.getEntity() instanceof Player victim)) return;

        Arena arena = this.instance.getArenaManager().getArenaByPlayer(attacker.getUniqueId());
        if (arena == null || arena.getState() != Arena.State.IN_GAME || arena.getGame().getGamePhase() != Game.GamePhase.GRACE) return;

        event.setCancelled(true);
        Logger.debug(String.format("Cancelled damage between players %s and %s in arena %s", attacker.getName(), victim.getName(), arena.getName()));
    }

    @EventHandler
    public void cancelPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Arena arena = this.instance.getArenaManager().getArenaByPlayer(player.getUniqueId());
        if (arena == null || arena.getState() != Arena.State.IN_GAME || arena.getGame().getGamePhase() != Game.GamePhase.GRACE) return;

        event.setCancelled(true);
        Logger.debug(String.format("Cancelled damage for player %s in arena %s", player.getName(), arena.getName()));
    }
}

