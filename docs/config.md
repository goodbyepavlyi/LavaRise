# LavaRise Configuration Documentation

## Version

- `version`: Configuration version identifier.

---

## Game Settings

### Grace Period and Lava Rising

- `game.gracePhaseTime`: Duration of the grace period in seconds during which players are safe from lava rise.
- `game.lavaRisingTime`: Time interval in seconds for lava to rise during the game.

### Spectator and End Game Settings

- `game.spectatorSpawnYLavaOffset`: Vertical offset above the lava level where spectators spawn.
- `game.endGameDelay`: Time delay in seconds before the game concludes once conditions are met.

### Game Items

- `game.items`: List of items that players receive at the start of the game, allowing for custom attributes.
    - `material`: Material of the item (e.g., `"IRON_AXE"`, `"IRON_PICKAXE"`).
    - `amount`: Quantity of the item given to players.
    - `name`: Custom name displayed for the item in-game.
    - `lore`: List of strings displayed as lore/description when hovering over the item.
    - `enchantments`:
      - Enchantments applied to the item, specified as `"ENCHANTMENT_NAME:LEVEL"` (e.g., `"EFFICIENCY:2"`).
      - Use the enchantment names and levels from the [Spigot API](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/enchantments/Enchantment.html).

### End Game Commands

- `game.commands`: Commands executed at the end of the game for winners, losers, and all players.
    - `winner`: Commands executed for the winner; use `%player%` placeholder to target the winning player.
    - `losers`: Commands executed for the players who lost; use `%player%` placeholder for targeting.
    - `players`: Commands executed for all players (both winners and losers); use `%player%` placeholder.

---

## Queue Settings

- `queue.countdown`: Countdown time in seconds before the game starts.
- `queue.halfFullQueueCountdown`: Configuration for countdown timing when the queue is half full.
    - `enabled`: Determines if the half-full queue countdown is active.
    - `value`: Countdown duration in seconds when the queue is half full.
- `queue.leaveItem`: Item configuration allowing players to leave the queue.
    - `material`: 
      - Material for the leave item (e.g., `"BARRIER"`).
      - Use the [Spigot API](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html) for material names.
    - `slot`: Inventory slot in which the leave item will appear.

---

## Metrics Collection

- `metrics`: Enables or disables metrics collection for server performance tracking and statistics.