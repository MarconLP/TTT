package net.ttt.commands;

import net.ttt.gamestates.LobbyState;
import net.ttt.main.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartCommand implements CommandExecutor {

    private static final int START_SECONDS = 10;

    private Main plugin;

    public StartCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (p.hasPermission("ttt.start")) {
                if (plugin.getGameStateManager().getCurrentGameState() instanceof LobbyState) {
                    LobbyState lobbyState = (LobbyState) plugin.getGameStateManager().getCurrentGameState();
                    if (lobbyState.getCountdown().isRunning()) {
                        if (lobbyState.getCountdown().getSeconds() > START_SECONDS) {
                            lobbyState.getCountdown().setSeconds(START_SECONDS);
                            p.sendMessage(Main.PREFIX + "§aDu hast die Runde gestartet");
                        } else
                            p.sendMessage(Main.PREFIX + "§7Das Spiel startet bereits");
                    } else
                        p.sendMessage(Main.PREFIX + "§cEs sind nicht genug Spieler online");
                } else
                    p.sendMessage(Main.PREFIX + "§cDie Runde läuft bereits");
            } else
                p.sendMessage(Main.NOPERMS);
        }
        return false;
    }
}
