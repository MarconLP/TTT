package net.ttt.listener;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.ttt.gamestates.IngameState;
import net.ttt.gamestates.LobbyState;
import net.ttt.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SpectatorListener implements Listener {

    private Main plugin;

    public SpectatorListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (plugin.getGameStateManager().getCurrentGameState() instanceof LobbyState) return;
        e.setJoinMessage(null);
        if (plugin.getGameStateManager().getCurrentGameState() instanceof IngameState) {
            ((IngameState) plugin.getGameStateManager().getCurrentGameState()).addSpec(e.getPlayer());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (plugin.getGameStateManager().getCurrentGameState() instanceof IngameState) {
            IngameState ingameState = (IngameState) plugin.getGameStateManager().getCurrentGameState();
            if (!(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR)) return;
            if (e.getPlayer() == null) return;
            Player p = e.getPlayer();
            if (ingameState.getSpectators().contains(p)) {
                e.setCancelled(true);
                if (p.getItemInHand() == null || p.getItemInHand().getType() == null || p.getItemInHand().getItemMeta() == null)
                    return;
                ItemStack item = p.getItemInHand();
                if (item.getItemMeta().getDisplayName() == null) return;
                if (item.getItemMeta().getDisplayName().equals("§eTeleporter")) {
                    e.setCancelled(true);
                    Inventory inv = Bukkit.createInventory(null, 3 * 9, "§eTeleporter");
                    if (plugin.getGameStateManager().getCurrentGameState() instanceof IngameState) {
                        for (Player current : plugin.getPlayers())
                            inv.addItem(plugin.getSkullBuilder().HeadItemDisplay(current, current.getDisplayName()));
                    }
                    p.openInventory(inv);
                } else if (item.getItemMeta().getDisplayName().equals("lobby")) {
                    e.setCancelled(true);
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("Connect");
                    out.writeUTF("lobby");
                    p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                } else if (item.getItemMeta().getDisplayName().equals("§5Teleporter")) {

                }
            }
        }
    }
}
