package net.ttt.listener;

import net.ttt.main.Main;
import net.ttt.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

public class TokenListener implements Listener {

    private Main plugin;
    private final String GUI_NAME = "§bSettings";

    public TokenListener(Main plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player p) {
        Inventory inv = Bukkit.createInventory(null, 3*9, GUI_NAME);

        inv.setItem(11, new ItemBuilder(Material.WOOL, (short) 11).setDisplayName("§9Detectvie§8-§9Pass")
                .setLore("§7Du hast derzeit §9" + plugin.getTokenManager().getTokens(p, "d") + " §9Detective§8-§9Pässe").build());
        inv.setItem(15, new ItemBuilder(Material.WOOL, (short) 14).setDisplayName("§cTraitor§8-§cPass")
                .setLore("§7Du hast derzeit §c" + plugin.getTokenManager().getTokens(p, "t") + " §cTraitor§8-§cPässe").build());

        p.openInventory(inv);
    }

    @EventHandler
    public void handleTokenGUIOpener(PlayerInteractEvent e) {
        if (!(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR)) return;
        if (e.getItem() == null) return;
        if (e.getItem().getType() != Material.REDSTONE_COMPARATOR) return;
        if (e.getItem().getItemMeta().getDisplayName().contains(PlayerLobbyConnectionListener.SETTINGS_ITEM_NAME)) {
            openGUI(e.getPlayer());
        }
    }

    @EventHandler
    public void handleTokenGUIClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getClickedInventory() == null) return;
        if (!e.getClickedInventory().getTitle().equals(GUI_NAME)) return;
        Player p = (Player) e.getWhoClicked();
        e.setCancelled(true);
        if (e.getSlot() == 11) {
            plugin.getTokenManager().useToken(p, "d");
            p.closeInventory();
        } else if (e.getSlot() == 15) {
            plugin.getTokenManager().useToken(p, "t");
            p.closeInventory();
        }
    }

}
