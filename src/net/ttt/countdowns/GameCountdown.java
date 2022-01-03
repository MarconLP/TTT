package net.ttt.countdowns;

import net.ttt.gamestates.IngameState;
import net.ttt.main.Main;
import net.ttt.role.Role;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import static net.ttt.role.Role.INNOCENT;
import static net.ttt.role.Role.TRAITOR;

public class GameCountdown extends Countdown {

    private Main plugin;
    private int taskID2;
    private int seconds = 900;

    public GameCountdown(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (seconds == 0) {
                    if (plugin.getGameStateManager().getCurrentGameState() instanceof IngameState) {
                        ((IngameState) plugin.getGameStateManager().getCurrentGameState()).timeout = true;
                        ((IngameState) plugin.getGameStateManager().getCurrentGameState()).checkGameEnding();
                    }
                }
                seconds--;
                if (plugin.getGameStateManager().getCurrentGameState() instanceof IngameState) {
                    for (Player current : Bukkit.getOnlinePlayers())
                        ((IngameState) plugin.getGameStateManager().getCurrentGameState()).updateScoreboard(current);
                }
            }
        }, 0, 20);

        taskID2 = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (Player current : plugin.getPlayers()) {
                    if (plugin.getRoleManager().getPlayerRole(current) == TRAITOR)
                        plugin.getRoleManager().setFakeArmorreal(current, current.getEntityId(), Color.RED);
                }
            }
        }, 20, 100);
    }

    @Override
    public void stop() {
        Bukkit.getScheduler().cancelTask(taskID);
        Bukkit.getScheduler().cancelTask(taskID2);
    }

    public int getSeconds() {
        return seconds;
    }
}
