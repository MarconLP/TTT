package net.ttt.gamestates;

import net.ttt.countdowns.EndingCountdown;
import net.ttt.main.Main;
import org.bukkit.Bukkit;

public class EndingState extends GameState {

    private Main plugin;
    private EndingCountdown endingCountdown;

    public EndingState(Main plugin) {
        this.plugin = plugin;
        endingCountdown = new EndingCountdown(plugin);
    }

    @Override
    public void start() {
        endingCountdown.start();
    }

    @Override
    public void stop() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                Bukkit.shutdown();
            }
        }, 20);
    }
}