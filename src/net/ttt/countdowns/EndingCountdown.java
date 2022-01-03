package net.ttt.countdowns;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.ttt.gamestates.EndingState;
import net.ttt.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class EndingCountdown extends Countdown {

    private static final int ENDING_SECONDS = 10;

    private Main plugin;
    private int seconds;

    public EndingCountdown(Main plugin) {
        this.plugin = plugin;
        seconds = ENDING_SECONDS;
    }

    @Override
    public void start() {
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                switch (seconds) {
                    case 10: case 5: case 4: case 3: case 2:
                        Bukkit.broadcastMessage(Main.PREFIX + "§cDer Server startet in §e" + seconds + " §cSekunden neu");
                        break;
                    case 1:
                        Bukkit.broadcastMessage(Main.PREFIX + "§cDer Server startet in §e" + seconds + " §cSekunde neu");
                        break;
                    case 0:
                        for (Player current : Bukkit.getOnlinePlayers()) {
                            ByteArrayDataOutput out = ByteStreams.newDataOutput();
                            out.writeUTF("Connect");
                            out.writeUTF("lobby");
                            current.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                        }
                        plugin.getGameStateManager().getCurrentGameState().stop();
                        stop();

                        break;
                    default:
                        break;
                }
                if (plugin.getGameStateManager().getCurrentGameState() instanceof EndingState) {
                    for (Player current : Bukkit.getOnlinePlayers()) {
                        current.setLevel(seconds);
                        current.setExp((1f / 10) *seconds);
                    }
                }
                seconds--;
            }
        }, 0, 20);
    }

    @Override
    public void stop() {
        Bukkit.getScheduler().cancelTask(taskID);
    }
}
