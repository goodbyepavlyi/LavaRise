# LavaRise Commands
Mandatory arguments are marked with `< >` brackets; optional arguments are enclosed in square brackets (`[ ]`).

- /lavarise: Main command for LavaRise.

## Admin Commands
Requires `lavarise.admin` permission.
- `/lavarise arena list` - List all LavaRise arenas.
- `/lavarise arena create <arena>` - Create a new LavaRise arena.
- `/lavarise arena delete <arena>` - Delete a LavaRise arena.
- `/lavarise arena set <arena> lobby` - Set the lobby location for a LavaRise arena.
- `/lavarise arena set <arena> gamearea <top|bottom>` - Set the top or bottom corner of the game area for a LavaRise arena.
- `/lavarise arena set <arena> minplayers <number>` - Set the minimum number of players required to start a game in a LavaRise arena.
- `/lavarise arena set <arena> maxplayers <number>` - Set the maximum number of players allowed in a LavaRise arena.
- `/lavarise arena set <arena> pvp <true|false>` - Enable or disable PvP in a LavaRise arena.
- `/lavarise arena set <arena> lavalevel <number>` - Set the maximum lava level for a LavaRise arena.

## Player Commands
- `/lavarise join <arena>` - Join a LavaRise game.
- `/lavarise leave` - Leave the current LavaRise game.