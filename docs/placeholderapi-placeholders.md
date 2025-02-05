# LavaRise PlaceholderAPI Placeholders

## Arena Placeholders

### Arena Status
- `%lavarise_arena_status_<arena>%`
  - Returns the current state of a specific arena.
  - Possible values: `Waiting`, `Starting`, `In game`, `Ending`.
  - Custom values can be set in `messages.yml`.

### Arena Players
- `%lavarise_arena_players_<arena>%`
  - Returns the number of active players (excluding spectators) in the specified arena.

---  

## Player Statistics Placeholders

### Wins
- `%lavarise_stats_wins%`
  - Returns the number of games the player has won.

### Losses
- `%lavarise_stats_losses%`
  - Returns the number of games the player has lost.

### Kills
- `%lavarise_stats_kills%`
  - Returns the number of kills the player has achieved.

### Deaths
- `%lavarise_stats_deaths%`
  - Returns the number of times the player has died in the game.

---  

## Leaderboard Placeholders

These placeholders allow you to display top players based on their statistics.

### Top Players by Wins
- `%lavarise_top_wins_player_<position>%`
  - Returns the name of the player at the specified leaderboard position for wins.
- `%lavarise_top_wins_value_<position>%`
  - Returns the number of wins of the player at the specified position.

### Top Players by Losses
- `%lavarise_top_losses_player_<position>%`
  - Returns the name of the player at the specified leaderboard position for losses.
- `%lavarise_top_losses_value_<position>%`
  - Returns the number of losses of the player at the specified position.

### Top Players by Kills
- `%lavarise_top_kills_player_<position>%`
  - Returns the name of the player at the specified leaderboard position for kills.
- `%lavarise_top_kills_value_<position>%`
  - Returns the number of kills of the player at the specified position.

### Top Players by Deaths
- `%lavarise_top_deaths_player_<position>%`
  - Returns the name of the player at the specified leaderboard position for deaths.
- `%lavarise_top_deaths_value_<position>%`
  - Returns the number of deaths of the player at the specified position.

`<position>` should be replaced with a number

---  

## Global Placeholders

### Total Players
- `%lavarise_total_players%`
  - Returns the total number of players (excluding spectators) across all arenas.