package net.ttt.commands;

import net.ttt.main.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {

    private Main plugin;

    public StatsCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length >= 1) {
                plugin.getStatsManager().requestStats(p, args[0], true);
            } else {
                plugin.getStatsManager().requestStats(p, p.getName(), true);
            }
        }
        return false;
    }
}
