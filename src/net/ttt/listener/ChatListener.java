package net.ttt.listener;

import net.ttt.gamestates.IngameState;
import net.ttt.main.Main;
import net.ttt.role.Role;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private Main plugin;

    public ChatListener(Main plugin) {
        this.plugin = plugin;
    }
/*
    @EventHandler
    public void handleDefaultChat(AsyncPlayerChatEvent e) {
        if (plugin.getGameStateManager().getCurrentGameState() instanceof IngameState) return;
        e.setFormat(getChatFormat(ChatColor.GOLD, e.getPlayer()) + e.getMessage());
    } // */

    @EventHandler(priority = EventPriority.HIGH)
    public void handleChat(AsyncPlayerChatEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof IngameState)) return;
        IngameState ingameState = (IngameState) plugin.getGameStateManager().getCurrentGameState();
        Player p = e.getPlayer();
        if (ingameState.isGrace()) return;
        if (ingameState.getSpectators().contains(p)) {
            e.setCancelled(true);
            for (Player current : ingameState.getSpectators())
                current.sendMessage("§7[§c✖§7] §7" + p.getName() + "§8: §r" + e.getMessage());
            return;
        }
        Role pRole = plugin.getRoleManager().getPlayerRole(p);
        if (pRole == Role.DETECTIVE || pRole == Role.INNOCENT) {
            e.setFormat(getChatFormat(pRole.getChatColor(), p) + e.getMessage());
            return;
        }
        if (pRole == Role.TRAITOR) {
            e.setCancelled(true);
            for (Player current : Bukkit.getOnlinePlayers()) {
                Role currentRole = plugin.getRoleManager().getPlayerRole(current);
                if (currentRole == Role.TRAITOR)
                    current.sendMessage(getChatFormat(Role.TRAITOR.getChatColor(), p) + e.getMessage());
                else
                    current.sendMessage(getChatFormat(Role.INNOCENT.getChatColor(), p) + e.getMessage());
            }
        }
    }

    private String getChatFormat(ChatColor color, Player p) {
        return color + p.getName() + "§8: §r";
    }

}
