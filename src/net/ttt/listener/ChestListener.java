package net.ttt.listener;

import net.ttt.gamestates.IngameState;
import net.ttt.main.Main;
import net.ttt.role.Role;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Random;

public class ChestListener implements Listener {

    private Main plugin;
    private ItemStack woodenSword, stoneSword, ironSword, bow, arrows;

    public ChestListener(Main plugin) {
        this.plugin = plugin;

        woodenSword = new ItemStack(Material.WOOD_SWORD);
        stoneSword = new ItemStack(Material.STONE_SWORD);
        ironSword = new ItemStack(Material.IRON_SWORD);
        bow = new ItemStack(Material.BOW);
        arrows = new ItemStack(Material.ARROW, 32);
    }

    @EventHandler
    public void handleChestClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock().getType() != Material.CHEST) return;
        e.setCancelled(true);
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof IngameState)) return;

        Player p = e.getPlayer();

        // Check FAKE Chest
        if (!plugin.getShopItemListener().getFakeChests().isEmpty()) {
            if (plugin.getShopItemListener().getFakeChests().containsValue(e.getClickedBlock().getLocation())) {
                if (plugin.getRoleManager().getPlayerRole(p) != Role.TRAITOR) {
                    e.getClickedBlock().setType(Material.AIR);
                    Entity fakeChestTNT = p.getWorld().spawn(e.getClickedBlock().getLocation(), TNTPrimed.class);
                    ((TNTPrimed) fakeChestTNT).setFuseTicks(0);
                    return;
                }
            }
        }

        ArrayList<ItemStack> items = new ArrayList<>();
        if (!p.getInventory().contains(woodenSword)) {
            items.add(woodenSword);
        }
        if (!p.getInventory().contains(stoneSword)) {
            items.add(stoneSword);
        }
        if (!p.getInventory().contains(bow) && !p.getInventory().contains(Material.ARROW)) {
            items.add(bow);
        }
        if (items.isEmpty()) {
            return;
        }

        ItemStack item = items.get(new Random().nextInt(items.size()));
        if (item.equals(bow)) {
            p.getInventory().addItem(bow);
            p.getInventory().addItem(arrows);
        } else
            p.getInventory().addItem(item);
        e.getClickedBlock().setType(Material.AIR);
        e.getClickedBlock().getLocation().getWorld().playSound(e.getClickedBlock().getLocation(), Sound.CHEST_OPEN, 1, 1);

        p.updateInventory();
    }

    @EventHandler
    public void openEnderChest(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock().getType() != Material.ENDER_CHEST) return;
        e.setCancelled(true);
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof IngameState)) return;

        IngameState ingameState = (IngameState) plugin.getGameStateManager().getCurrentGameState();
        Player p = e.getPlayer();
        if (!(ingameState.isGrace())) {
            p.getWorld().playSound(e.getClickedBlock().getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
            if (p.getInventory().contains(ironSword)) return;
            p.getInventory().addItem(ironSword);
            e.getClickedBlock().setType(Material.AIR);
        } else
            p.sendMessage(Main.PREFIX + "§cDu kannst dieses Item erst nach Ende der Schutzzeit aufnehmen");
    }
}
