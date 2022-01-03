package net.ttt.countdowns;

import net.ttt.gamestates.GameState;
import net.ttt.gamestates.GameStateManager;
import net.ttt.gamestates.LobbyState;
import net.ttt.main.Main;
import net.ttt.voting.Map;
import net.ttt.voting.Voting;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;

public class LobbyCountdown extends Countdown {

    private Main plugin;

    private static final int COUNTDOWN_TIME = 60, IDLE_TIME = 15;

    private GameStateManager gameStateManager;

    private int seconds;
    private boolean isRunning;
    private int idleID;
    private boolean isIdling;
    private Map winnerMap;

    public LobbyCountdown(GameStateManager gameStateManager) {
        this.gameStateManager = gameStateManager;
        seconds = COUNTDOWN_TIME;
    }

    @Override
    public void start() {
        isRunning = true;
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(gameStateManager.getPlugin(), new Runnable() {
            @Override
            public void run() {
                switch (seconds) {
                    case 60: case 30: case 10: case 5: case 4: case 3: case 2:
                        Bukkit.broadcastMessage(Main.PREFIX + "§7Die Runde beginnt in §e" + seconds + " §7Sekunden");

                        if (seconds == 10) {
                            Voting voting = gameStateManager.getPlugin().getVoting();
                            Map winningMap;
                            if (voting != null) {
                                winningMap = voting.getWinnerMap();
                            } else {
                                ArrayList<Map> maps = gameStateManager.getPlugin().getMaps();
                                Collections.shuffle(maps);
                                winningMap = maps.get(0);
                            }
                            winnerMap = winningMap;
                            Bukkit.broadcastMessage(Main.PREFIX + "§7Die Map §e" + winningMap.getName() + " §7(§e" + winningMap.getVotes() + " Votes§7) wurde §aausgewählt");
                        }
                        for (Player current : Bukkit.getOnlinePlayers())
                            current.playSound(current.getLocation(), Sound.NOTE_PIANO, 1, 1);
                        break;
                    case 1:
                        Bukkit.broadcastMessage(Main.PREFIX + "§7Die Runde beginnt in §e" + seconds + " §7Sekunde");
                        for (Player current : Bukkit.getOnlinePlayers())
                            current.playSound(current.getLocation(), Sound.NOTE_PIANO, 1, 1);
                        break;
                    case 0:
                        gameStateManager.setGameState(GameState.INGAME_STATE);
                        stop();
                        for (Player current : Bukkit.getOnlinePlayers()) {
                            current.setLevel(0);
                            current.setExp(0f);
                        }
                        break;

                    default:
                        break;
                }
                if (gameStateManager.getCurrentGameState() instanceof LobbyState) {
                    for (Player current : Bukkit.getOnlinePlayers()) {
                        ((LobbyState) gameStateManager.getCurrentGameState()).updateScoreboard(current);
                        current.setLevel(seconds);
                        current.setExp((1f / 60) * seconds);
                    }
                }

                seconds--;
            }
        }, 0, 20);
    }

    @Override
    public void stop() {
        if (isRunning) {
            Bukkit.getScheduler().cancelTask(taskID);
            isRunning = false;
            seconds = COUNTDOWN_TIME;
            if (gameStateManager.getCurrentGameState() instanceof LobbyState) {
                for (Player current : Bukkit.getOnlinePlayers()) {
                    ((LobbyState) gameStateManager.getCurrentGameState()).updateScoreboard(current);
                    current.setExp(1f);
                    current.setLevel(seconds);
                }
            }
        }
    }

    public void startIdle() {
        isIdling = true;
        idleID = Bukkit.getScheduler().scheduleSyncRepeatingTask(gameStateManager.getPlugin(), new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(Main.PREFIX + "§cWarten auf weitere Spieler");
            }
        }, 0, 20 * IDLE_TIME);
    }

    public void stopIdle() {
        if (isIdling) {
            Bukkit.getScheduler().cancelTask(idleID);
            isIdling = false;
        }
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public Map getWinnerMap() {
        return winnerMap;
    }
}