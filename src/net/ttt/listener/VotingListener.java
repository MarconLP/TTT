package net.ttt.listener;

import net.ttt.main.Main;
import net.ttt.voting.Voting;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class VotingListener implements Listener {

    private Main plugin;
    private Voting voting;

    public VotingListener(Main plugin) {
        this.plugin = plugin;
        voting = plugin.getVoting();
    }

    @EventHandler
    public void handleVotingMenuOpener(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
            if (e.getItem() == null) return;
            if (e.getItem().getType() != Material.PAPER) return;
            if (!e.getItem().getItemMeta().getDisplayName().contains("Mapvoting")) return;
            Player p = e.getPlayer();
            ItemStack item = p.getItemInHand();
            if (item.getItemMeta() != null) {
                if (item.getItemMeta().getDisplayName().equals(PlayerLobbyConnectionListener.VOTE_ITEM_NAME)) {
                    p.openInventory(voting.getVotingInv());
                }
            }
        }
    }

    @EventHandler
    public void handleVotingClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            if (e.getCurrentItem() == null) return;
            Player p = (Player) e.getWhoClicked();
            if (e.getInventory().getTitle().equals(Voting.VOTING_INV_TITLE)) {
                e.setCancelled(true);
                for (int i = 0; i < voting.getMaps().size(); i++) {
                    if (voting.getMaps().get(i).getMaterial() == e.getCurrentItem().getType()) {
                        voting.vote(p, i);
                        return;
                    }
                }
            }
        }
    }

}
