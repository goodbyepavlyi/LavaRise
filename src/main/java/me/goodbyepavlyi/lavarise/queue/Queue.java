package me.goodbyepavlyi.lavarise.queue;

import me.goodbyepavlyi.lavarise.LavaRiseInstance;
import me.goodbyepavlyi.lavarise.arena.Arena;
import me.goodbyepavlyi.lavarise.arena.models.ArenaPlayer;
import me.goodbyepavlyi.lavarise.utils.Logger;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Queue {
    private static ItemStack leaveItem;
    private static int HalfFullQueueCountdown;

    private final Arena arena;
    private final LavaRiseInstance instance;
    private final QueueScoreboard queueScoreboard;
    private int countdown;
    private BukkitTask countdownTask;

    public Queue(Arena arena) {
        this.arena = arena;
        this.instance = arena.getArenaManager().getInstance();
        this.queueScoreboard = new QueueScoreboard(instance, arena, this);

        HalfFullQueueCountdown = this.instance.getConfiguration().QueueHalfFullQueueCountdownValue();
    }

    public int getCountdown() {
        return this.countdown;
    }

    public ItemStack getLeaveItem() {
        if (leaveItem != null) return leaveItem;

        leaveItem = new ItemStack(Material.BARRIER);
        ItemMeta itemMeta = leaveItem.getItemMeta();
        itemMeta.setDisplayName(instance.getMessages().QUEUE_ITEMS_LEAVE_NAME());
        leaveItem.setItemMeta(itemMeta);
        return leaveItem;
    }

    public boolean hasEnoughPlayersToStart() {
        boolean result = this.arena.getPlayers().size() >= this.arena.getConfig().getMinimumPlayers();

        Logger.debug(String.format("canStart - Arena %s, Current Players: %s, Minimum Players: %s, State: %s, Result: %s",
                this.arena.getName(), this.arena.getPlayers().size(), this.arena.getConfig().getMinimumPlayers(), this.arena.getState(), result));
        return result;
    }

    public boolean isFull() {
        return this.arena.getPlayers().size() >= this.arena.getConfig().getMaximumPlayers();
    }

    public void addPlayer(Player player) {
        ArenaPlayer arenaPlayer = new ArenaPlayer(player);
        arenaPlayer.saveData();

        Logger.debug(String.format("Player %s added to the queue in arena %s", player.getName(), arena.getName()));
        this.arena.getPlayers().add(arenaPlayer);
        this.arena.announceMessage(Arena.AnnouncementType.QUEUE_PLAYERJOIN, player.getName());
        this.queueScoreboard.update();

        player.teleport(this.arena.getConfig().getLobby(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.getInventory().setArmorContents(null);
        player.getInventory().clear();
        player.getInventory().setItem(8, this.getLeaveItem());
        this.arena.delayAction(player, p -> p.setFireTicks(0));

        // Start the countdown if the minimum players are met
        if (this.hasEnoughPlayersToStart() && this.arena.getState() == Arena.State.WAITING) {
            this.arena.setState(Arena.State.STARTING);
            this.startCountdown();
        }

        if (this.instance.getConfiguration().QueueHalfFullQueueCountdownEnabled() &&
                this.countdown > HalfFullQueueCountdown
                && this.arena.getPlayers().size() == this.arena.getConfig().getMaximumPlayers() / 2
                && this.arena.getState() == Arena.State.STARTING) {
            Logger.debug(String.format("Arena %s is half full, shortening countdown to %s", this.arena.getName(), HalfFullQueueCountdown));
            this.countdown = HalfFullQueueCountdown;
            arena.announceMessage(Arena.AnnouncementType.QUEUE_GAME_STARTING_IN, String.valueOf(this.countdown));
        }
    }

    public void removePlayer(Player player) {
        ArenaPlayer arenaPlayer = this.arena.getPlayer(player.getUniqueId());

        Logger.debug(String.format("Player %s removed from the queue in arena %s", player.getName(), arena.getName()));
        this.arena.announceMessage(Arena.AnnouncementType.QUEUE_PLAYERLEAVE, player.getName());
        this.arena.getPlayers().remove(arenaPlayer);

        if (!this.hasEnoughPlayersToStart()) {
            this.arena.setState(Arena.State.WAITING);
            this.arena.announceMessage(Arena.AnnouncementType.QUEUE_CANCELLED);
            this.stopCountdown();
        }

        this.queueScoreboard.update();
    }

    public void startCountdown() {
        this.queueScoreboard.startScoreboardUpdates();
        this.countdown = this.instance.getConfiguration().QueueCountdown();

        this.countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (countdown <= 0) {
                    stopCountdown();
                    arena.announceMessage(Arena.AnnouncementType.GAME_START);
                    arena.getGame().start();
                    return;
                }

                if (countdown % 15 == 0 || (countdown <= 3 && countdown != HalfFullQueueCountdown))
                    arena.announceMessage(Arena.AnnouncementType.QUEUE_GAME_STARTING_IN, String.valueOf(countdown));

                countdown--;
            }
        }.runTaskTimer(this.instance, 0L, 20L);

        this.arena.getTasks().add(this.countdownTask);
        Logger.debug(String.format("Countdown started in arena %s", arena.getName()));
    }

    public void stopCountdown() {
        this.queueScoreboard.stopScoreboardUpdateTask();

        if (this.countdownTask == null) return;
        this.countdownTask.cancel();
        this.arena.getTasks().remove(this.countdownTask);
        this.countdownTask = null;
        this.countdown = 0;

        Logger.debug(String.format("Countdown stopped in arena %s", arena.getName()));
    }
}
