package net.ttt.countdowns;

import net.ttt.gamestates.IngameState;
import net.ttt.main.Main;
import net.ttt.role.Role;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class RoleCountdown extends Countdown {

    private Main plugin;
    private int seconds = 5;

    public RoleCountdown(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                switch (seconds) {
                    case 30: case 15: case 10: case 5: case 4: case 3: case 2:
                        Bukkit.broadcastMessage(Main.PREFIX + "§7Die Schutzzeit endet in §e" + seconds + " §7Sekunden.");
                        break;
                    case 1:
                        Bukkit.broadcastMessage(Main.PREFIX + "§7Die Schutzzeit endet in §e" + seconds + " §7Sekunde.");
                        break;
                    case 0:
                        stop();
                        IngameState ingameState = (IngameState) plugin.getGameStateManager().getCurrentGameState();
                        ingameState.setGrace(false);

                        plugin.getRoleManager().calculateRoles();

                        ArrayList<String> traitorPlayers = plugin.getRoleManager().getTraitorPlayers();
                        for (Player current : plugin.getPlayers()) {
                            Role playerRole = plugin.getRoleManager().getPlayerRole(current);
                            current.sendMessage(Main.PREFIX + "§7Du bist " + playerRole.getChatColor() + playerRole.getName());
                            current.setDisplayName(playerRole.getChatColor() + current.getName());
                            if (playerRole == Role.TRAITOR) {
                                plugin.getRoleInventories().getPointManager().setPoints(current, 50);
                                current.sendMessage(Main.PREFIX + "§7Töte alle §aInnocents §7und §9Detectives");
                                current.sendMessage(Main.PREFIX + "§7Öffne den §aShop §7mit §e/shop");
                                current.sendMessage(Main.PREFIX + "§7Die Traitor sind§8: §c" + String.join("§7, §c", traitorPlayers));
                            } else if (playerRole == Role.DETECTIVE) {
                                plugin.getRoleInventories().getPointManager().setPoints(current, 50);
                                current.sendMessage(Main.PREFIX + "§7Finde und töte die §cTraitor");
                                current.sendMessage(Main.PREFIX + "§7Öffne den §aShop §7mit §e/shop");
                            } else if (playerRole == Role.INNOCENT) {
                                plugin.getRoleInventories().getPointManager().setPoints(current, 0);
                                current.sendMessage(Main.PREFIX + "§7Hilf dem §9Detective §7die §cTraitor §7zu finden");
                            }
                        }
                        ((IngameState) plugin.getGameStateManager().getCurrentGameState()).getGameCountdown().start();
                        break;
                    default:
                        break;
                }
                if (plugin.getGameStateManager().getCurrentGameState() instanceof IngameState) {
                    for (Player current : plugin.getPlayers())
                        ((IngameState) plugin.getGameStateManager().getCurrentGameState()).updateScoreboard(current);
                }
                seconds--;
            }
        }, 0, 20);
    }

    @Override
    public void stop() {
        Bukkit.getScheduler().cancelTask(taskID);
    }

    public int getSeconds() {
        return seconds;
    }
}
