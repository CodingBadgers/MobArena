package com.garbagemule.MobArena;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.garbagemule.MobArena.framework.Arena;

public class ScoreboardManager {
    private static final String DISPLAY_NAME = ChatColor.GREEN + "Kills       " + ChatColor.AQUA + "Wave ";
    private static final String DEAD_TEAM_NAME = "dead";
    
    private Arena arena;
    private Scoreboard scoreboard;
    private Objective kills;
    private Objective lobby;
    private HashMap<Player, String> teamPlayer = new HashMap<Player, String>();
    private HashMap<Player, String> lobbyTeamPlayer = new HashMap<Player, String>();
    private Team deadTeam;
    private String deadTeamPrefix;
    
    /**
     * Create a new scoreboard for the given arena.
     * @param arena an arena
     */
    ScoreboardManager(Arena arena, String deadTeamPrefix) {
        this.arena = arena;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.deadTeamPrefix = deadTeamPrefix;
    }
    
    /**
     * Add a player to the scoreboard by setting the player's scoreboard
     * and giving him an initial to-be-reset non-zero score.
     * @param player a player
     */
    void addPlayer(Player player, String teamName, ChatColor teamColor) {
        /* Set the player's scoreboard and give them an initial non-zero
         * score. This is necessary due to either Minecraft or Bukkit
         * not wanting to show non-zero scores initially. */
    	
    	Team team = scoreboard.getTeam(teamName);
    	if (team == null) {
    		team = scoreboard.registerNewTeam(teamName);
    		team.setPrefix(teamColor + "[" + teamName + "] " + ChatColor.WHITE);
    	}    
    	team.addPlayer(player);
    	teamPlayer.put(player, teamName);

    	player.setScoreboard(scoreboard);
        kills.getScore(player).setScore(8);
    }
    
    /**
     * Kill a player and put the on the dead team.
     * @param player a player
     */
    void killPlayer(Player player) {
    	
    	if (!teamPlayer.containsKey(player))
    		return;
    	
    	final String teamName = teamPlayer.get(player);    	
    	Team team = scoreboard.getTeam(teamName);
    	if (team != null) {
    		team.removePlayer(player);
    		teamPlayer.remove(player);
    	}
    	
    	deadTeam.addPlayer(player);
    	teamPlayer.put(player, DEAD_TEAM_NAME);
    	
    }
    
    /**
     * Remove a player from the scoreboard by setting the player's scoreboard
     * to the main server scoreboard.
     * @param player a player
     */
    void removePlayer(Player player) {
    	
    	player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    	
    	if (lobbyTeamPlayer.containsKey(player))
    	{
    		final String teamName = lobbyTeamPlayer.get(player);    	
        	Team team = scoreboard.getTeam(teamName);
        	if (team != null) {
        		team.removePlayer(player);
        		lobbyTeamPlayer.remove(player);
        	}
    	}
    	
    	if (teamPlayer.containsKey(player))
    	{
	    	final String teamName = teamPlayer.get(player);   	
	    	if (!teamName.equalsIgnoreCase(DEAD_TEAM_NAME))
	    		killPlayer(player);
    	}
    }

    /**
     * Add a kill to the player's score. Called when a player kills a mob.
     * @param player a player
     */
    void addKill(Player player) {
        Score score = kills.getScore(player);
        score.setScore(score.getScore() + 1);
    }
    
    /**
     * Update the scoreboard to display the given wave number.
     * @param wave a wave number
     */
    void updateWave(int wave) {
        kills.setDisplayName(DISPLAY_NAME + wave);
    }
    
    /**
     * Initialize the scoreboard by resetting the kills objective and
     * setting all player scores to 0.
     */
    void initialize() {
        /* Initialization involves first unregistering the kill counter if
         * it was already registered, and then setting it back up.
         * It is necessary to delay the reset of the player scores, and the
         * reset is necessary because of non-zero crappiness. */
        resetKills();
        arena.scheduleTask(new Runnable() {
            public void run() {
                for (Player p : arena.getPlayersInArena()) {
                    kills.getScore(p).setScore(0);
                }
            }
        }, 1);
    }
    
    /**
     * Reset the ingame scoreboard
     */
    private void resetKills() {
        if (kills != null) {
            kills.unregister();
        }
        kills = scoreboard.registerNewObjective("kills", "ma-kills");
        kills.setDisplaySlot(DisplaySlot.SIDEBAR);
        updateWave(0);
        
        if (deadTeam != null) {
        	deadTeam.unregister();
        }
        deadTeam = scoreboard.registerNewTeam(DEAD_TEAM_NAME);
        deadTeam.setPrefix(deadTeamPrefix);
        
        teamPlayer.clear();
        lobbyTeamPlayer.clear();
    }
    
    /**
     * Setup the scoreboard for lobby use
     */
	void setupForLobby(Arena arena) {
		if (lobby != null) {
			lobby.unregister();
		}
		lobby = scoreboard.registerNewObjective("lobby", "ma-lobby");
		lobby.setDisplaySlot(DisplaySlot.SIDEBAR);
		lobby.setDisplayName(arena.arenaName());
		
		teamPlayer.clear();
        lobbyTeamPlayer.clear();
	}
    
    /**
     * Add a player to the lobby sign
     */
	void addLobbyPlayer(Arena arena, Player player) {
		player.setScoreboard(scoreboard);
		lobby.getScore(player).setScore(1);
		
		final int noofPlayers = arena.getPlayersInLobby().size();
		lobby.setDisplayName(arena.arenaName() + "    " + noofPlayers + (noofPlayers == 1 ? " player" : " players"));
	}
	
	/**
     * Color a player green when they are ready
     */
	void readyLobbyPlayer(Player p) {
		
	}
	
	/**
     * Set a players team based upon player class
     */
	void setLobbyPlayerClass(ArenaPlayer player) {
		
		// Remove from old team
		if (lobbyTeamPlayer.containsKey(player.getPlayer())) {
			Team team = scoreboard.getTeam(lobbyTeamPlayer.get(player.getPlayer()));
			if (team != null) {
				team.removePlayer(player.getPlayer());
			}
		}
		
		// Add to new team
		final String teamName = player.getArenaClass().getConfigName();
		Team team = scoreboard.getTeam(teamName);
    	if (team == null) {
    		team = scoreboard.registerNewTeam(teamName);
    		team.setPrefix(player.getArenaClass().getColor() + "[" + teamName + "] " + ChatColor.WHITE);
    	}    
    	team.addPlayer(player.getPlayer());
    	lobbyTeamPlayer.put(player.getPlayer(), teamName);
	}

    static class NullScoreboardManager extends ScoreboardManager {
        NullScoreboardManager(Arena arena) {
            super(arena, ChatColor.STRIKETHROUGH + "");
        }
        void addPlayer(Player player) {}
        void removePlayer(Player player) {}
        void killPlayer(Player player) {}
        void addKill(Player player) {}
        void updateWave(int wave) {}
        void initialize() {}
        void addLobbyPlayer(Player p) {}
        void readyLobbyPlayer(Player p) {}
        void setLobbyPlayerClass(ArenaPlayer p) {}
    }
}
