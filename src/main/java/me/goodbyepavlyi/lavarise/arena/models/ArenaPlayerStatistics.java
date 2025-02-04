package me.goodbyepavlyi.lavarise.arena.models;

import org.bukkit.configuration.ConfigurationSection;

public class ArenaPlayerStatistics {
    private int wins = 0;
    private int losses = 0;
    private int kills = 0;
    private int deaths = 0;

    public ArenaPlayerStatistics(ConfigurationSection section){
        if(section == null) return;
        this.wins = section.getInt("wins", 0);
        this.losses = section.getInt("losses", 0);
        this.kills = section.getInt("kills", 0);
        this.deaths = section.getInt("deaths", 0);
    }

    public int getWins() {
        return wins;
    }
    
    public void addWin(){
        this.wins++;
    }

    public int getLosses() {
        return losses;
    }
    
    public void addLoss(){
        this.losses++;
    }

    public int getKills() {
        return kills;
    }
    
    public void addKill(){
        this.kills++;
    }

    public int getDeaths() {
        return deaths;
    }
    
    public void addDeath(){
        this.deaths++;
    }
}
