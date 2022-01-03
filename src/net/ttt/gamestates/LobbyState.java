package net.ttt.gamestates;

import net.ttt.countdowns.LobbyCountdown;
import net.ttt.main.Main;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class LobbyState extends GameState {

    public static final int MIN_PLAYERS = 2,
                            MAX_PLAYERS = 12;

    private Main plugin;
    private LobbyCountdown countdown;
    private String map;

    public LobbyState(Main plugin, GameStateManager gameStateManager) {
        this.plugin = plugin;
        countdown = new LobbyCountdown(gameStateManager);
    }

    public void updateScoreboard(Player p) {
        if (p.getScoreboard().getTeam("karmaval") != null) {
            p.getScoreboard().getTeam("karmaval").setPrefix("§e" + plugin.getKarmaManager().getPlayerKarma(p));
            p.getScoreboard().getTeam("mapval").setPrefix("§b" + WordUtils.capitalizeFully(getMap()));
            p.getScoreboard().getTeam("tokenval").setPrefix("§9" + plugin.getTokenManager().getTokens(p, "d") + " §8/ §c" + plugin.getTokenManager().getTokens(p, "t"));
        } else {
            Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
            Objective ob = board.registerNewObjective("3", "4");
            ob.setDisplaySlot(DisplaySlot.SIDEBAR);
            ob.setDisplayName("§lTESTNAMEd.NET");
            ob.getScore("§1").setScore(9);
            ob.getScore("§7Karma§8:").setScore(8);
            //ob.getScore("§e" + plugin.getKarmaManager().getPlayerKarma(p)).setScore(7);

            Team karmaval = board.registerNewTeam("karmaval");
            karmaval.addEntry("§1§3");
            karmaval.setPrefix("§e" + plugin.getKarmaManager().getPlayerKarma(p));
            ob.getScore("§1§3").setScore(7);

            ob.getScore("§2").setScore(6);
            ob.getScore("§7Map§8:").setScore(5);
            //ob.getScore("§b" + getMap()).setScore(4);

            Team mapval = board.registerNewTeam("mapval");
            mapval.addEntry("§2§4");
            mapval.setPrefix("§b" + WordUtils.capitalizeFully(getMap()));
            ob.getScore("§2§4").setScore(4);

            ob.getScore("§3").setScore(3);
            ob.getScore("§7Pässe§8:").setScore(2);
            //ob.getScore("§9" + plugin.getTokenManager().getTokens(p, "d") + " §8/ §c" + plugin.getTokenManager().getTokens(p, "t")).setScore(1);

            Team tokenval = board.registerNewTeam("tokenval");
            tokenval.addEntry("§3§5");
            tokenval.setPrefix("§9" + plugin.getTokenManager().getTokens(p, "d") + " §8/ §c" + plugin.getTokenManager().getTokens(p, "t"));
            ob.getScore("§3§5").setScore(1);

            ob.getScore("§4").setScore(0);
            p.setScoreboard(board);
        }
    }

    @Override
    public void start() {
        countdown.startIdle();
    }

    @Override
    public void stop() {
        Bukkit.broadcastMessage(Main.PREFIX + "§eDas Spiel beginnt! Sammle Waffen und rüste dich für den Kampf!");
    }

    public LobbyCountdown getCountdown() {
        return countdown;
    }

    public String getMap() {
        String map = "Voting";
        if (countdown.getWinnerMap() != null && countdown.getSeconds() <= 10)
            map = countdown.getWinnerMap().getName();
        return WordUtils.capitalizeFully(map);
    }
}