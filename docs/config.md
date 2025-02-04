# LavaRise Configuration Documentation

## Version

- `version`: Configuration version identifier.
  - ⚠ **Do not modify this value.**

---

## Game Settings

### Grace Period and Lava Rising

- `game.gracePhaseTime`: Duration of the grace period in seconds during which players are safe from lava rise.
- `game.pvpGracePeriod`: Duration in seconds of the grace period for player-vs-player (PvP) combat at the start of the game. Set to `0` to disable the PvP grace period.
- `game.spectator`:
  - `gameMode`: Game mode for spectators.
    - Use the [Spigot API](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/GameMode.html#enum-constant-summary) for game mode names.
- `game.lavaRisingTime`: Settings for lava rise intervals during the game.
    - `default`: Default time interval for lava to rise across all levels.
    - `levels`: Optional custom intervals for specific Y-levels. If left empty, the `default` rise time will be applied for all levels.
        - `level`: The Y-level where the custom rise time should take effect.
        - `time`: Time interval in seconds for lava rise at that level.

        - Example with specific levels:
            ```yaml
            lavaRisingTime:
              default: 60
              levels:
                - level: 40
                  time: 30
                - level: 20
                  time: 15
            ```

        - Example with empty `levels` (uses `default` interval for all Y-levels):
            ```yaml
            lavaRisingTime:
              default: 60
              levels: []
            ```

### Deathmatch Damage

- `game.deathmatch.damage`:
  - `enabled`: Toggle to enable or disable deathmatch damage.
  - `delay`: Time in seconds before deathmatch damage begins.
  - `damage`: Amount of damage dealt to players during each damage interval.
  - `interval`: Interval in seconds between each instance of damage dealt to players during the deathmatch phase.

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

### Visual Effects

`visualEffects` allows configuring different effects to enhance the game's visual and audio experience for various phases: **lava rising**, **deathmatch**, and **winner announcement**. The available effect types include **sound**, **title**, and **particle**.

#### Effect Types

1. **Sound**: Configures sound effects to play during specific phases.
    - `enabled`: Toggle to enable or disable the sound.
    - `sound`: The sound type.
      - Use the [Spigot API](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html) for sound names.
    - `volume`: Volume level of the sound.
    - `pitch`: Pitch of the sound.

2. **Title**: Displays title and subtitle messages on players' screens.
    - `enabled`: Toggle to enable or disable the title display.
    - `titleMessage`: The main title message shown.
    - `subtitleMessage`: Subtitle message shown beneath the title.
    - `fadeIn`: Time in ticks for the title to fade in.
    - `stay`: Time in ticks for the title to stay on screen.
    - `fadeOut`: Time in ticks for the title to fade out.

3. **Particle**: Configures particle effects to display around players.
    - `enabled`: Toggle to enable or disable the particle effect.
    - `particle`: The type of particle to display.
      - Use the [Spigot API](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Particle.html) for particle names.
    - `count`: Number of particles to spawn.
    - `offsetX`, `offsetY`, `offsetZ`: Controls the spread of particles along each axis.
    - `speed`: Speed of the particle effect.

#### Event-Specific Configurations

- **Lava Rising Phase**
    - **Effects Used**: Sound and Title
    - Configuration:
        ```yaml
        lava:
          sound:
            enabled: true
            sound: "ENTITY_GENERIC_EXPLODE"
            volume: 1.0
            pitch: 1.0
          title:
            enabled: true
            titleMessage: "&6Lava is rising!"
            subtitleMessage: "&8Get to higher ground!"
            fadeIn: 10
            stay: 40
            fadeOut: 10
        ```

- **Deathmatch Phase**
    - **Effects Used**: Sound and Title
    - Configuration:
        ```yaml
        deathmatch:
          sound:
            enabled: true
            sound: "ENTITY_GENERIC_EXPLODE"
            volume: 1.0
            pitch: 1.0
          title:
            enabled: true
            titleMessage: "&6Death Match!"
            subtitleMessage: "&8Last player standing wins!"
            fadeIn: 10
            stay: 40
            fadeOut: 10
        ```

- **PVP Announcement**
    - **Effects Used**: Sound and Title
    - Configuration:
        ```yaml
        pvp:
          sound:
            enabled: true
            sound: "ENTITY_GENERIC_EXPLODE"
            volume: 1.0
            pitch: 1.0
          title:
            enabled: true
            titleMessage: "&6PVP is enabled!"
            subtitleMessage: "&8Fight to the death!"
            fadeIn: 10
            stay: 40
            fadeOut: 10
        ```

- **Winner Announcement**
    - **Effects Used**: Sound, Title and Particle
    - Configuration:
        ```yaml
        winner:
          sound:
            enabled: true
            sound: "ENTITY_PLAYER_LEVELUP"
            volume: 1.0
            pitch: 1.0
          title:
            enabled: true
            titleMessage: "&6You won!"
            subtitleMessage: "&8Congratulations!"
            fadeIn: 10
            stay: 40
            fadeOut: 10
          particle:
            enabled: true
            particle: "FIREWORKS_SPARK"
            count: 25
            offsetX: 0.5
            offsetY: 0.5
            offsetZ: 0.5
            speed: 0.1
        ```
      
- **Spectator Announcement**
    - **Effects Used**: Sound and Title
    - Configuration:
        ```yaml
        spectator:
          sound:
            enabled: true
            sound: "ENTITY_WITHER_DEATH"
            volume: 1.0
            pitch: 1.0
          title:
            enabled: true
            titleMessage: "&cYou lost!"
            subtitleMessage: "&8Better luck next time!"
            fadeIn: 10
            stay: 40
            fadeOut: 10
        ```

- **Allowed Commands**
  - `game.allowedCommands` allows you to specify a list of commands that players can execute during the game.
    - `enabled`: Toggle to enable or disable the execution of commands during the game.
    - `commands`: List of commands that players can execute during the game.

---

## Queue Settings

- `queue.tips`: Configuration for action bar tips
    - `enabled`: Determines if action bar tips are displayed.
    - `interval`: Countdown interval in seconds between each tip.
- `queue.countdown`: Countdown time in seconds before the game starts.
- `queue.halfFullQueueCountdown`: Configuration for countdown timing when the queue is half full.
    - `enabled`: Determines if the half-full queue countdown is active.
    - `value`: Countdown duration in seconds when the queue is half full.
- `queue.leaveItem`: Item configuration allowing players to leave the queue.
    - `material`: Material for the leave item (e.g., `"BARRIER"`).
        - Use the [Spigot API](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html) for material names.
    - `slot`: Inventory slot in which the leave item will appear.
- `queue.joinSound`: Sound effect played when players join the queue.
    - `enabled`: Toggle to enable or disable the join sound.
    - `sound`: Sound type.
        - Use the [Spigot API](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html) for sound names.

---

## Metrics Collection

- `metrics`: Enables or disables metrics collection for server performance tracking and statistics.
