package net.ttt.util;

import net.ttt.main.Main;
import net.ttt.voting.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class TesterSetup implements Listener {

    private Main plugin;
    private Player p;
    private Map map;
    private int phase;
    private boolean finished;


    private Block[] borderBlocks, groundBlocks, lampsBlocks;
    private Block button, trap;
    private Location testerInLocation, testerOutLocation;

    public TesterSetup(Player p, Map map, Main plugin) {
        this.plugin = plugin;
        this.map = map;
        this.p = p;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.phase = 1;
        finished = false;

        p.sendMessage("started testersetup");
        p.sendMessage("click 3 border blocks, 2 lamp blocks, 9 groundblocks");
        p.sendMessage("click tester button, click trap button");
        p.sendMessage("sneak at tester in location, sneak at tester out location");

        borderBlocks = new Block[3];
        groundBlocks = new Block[9];
        lampsBlocks = new Block[2];
    }

    @EventHandler
    public void handleBlockBreak(BlockBreakEvent e) {
        if (!e.getPlayer().getName().equals(p.getName())) return;
        if (finished) return;
        switch (phase) {
            case 1: case 2: case 3:
                if (e.getBlock().getType() == Material.GLASS) {
                    borderBlocks[phase - 1] = e.getBlock();
                    phase++;
                }
                break;
            case 4: case 5:
                if (e.getBlock().getType() == Material.STAINED_GLASS) {
                    lampsBlocks[phase-4] = e.getBlock();
                    phase++;
                }
                break;
            case 6: case 7: case 8: case 9: case 10: case 11: case 12: case 13: case 14:
                if (e.getBlock().getType() == Material.IRON_BLOCK) {
                    groundBlocks[phase - 6] = e.getBlock();
                    phase++;
                }
                break;
            case 15: case 16:
                if (e.getBlock().getType() == Material.WOOD_BUTTON) {
                    if (phase == 15) {
                        button = e.getBlock();
                        phase++;
                    }
                }
                if (e.getBlock().getType() == Material.STONE_BUTTON) {
                    if (phase == 16) {
                        trap = e.getBlock();
                        phase++;
                    }
                }
                break;
        }
    }

    @EventHandler
    public void handlePlayerSneakEvent(PlayerToggleSneakEvent e) {
        if (!e.getPlayer().getName().equals(p.getName())) return;
        if (finished) return;
        if (phase == 17) {
            testerInLocation = p.getLocation();
            phase++;
        } else if (phase == 18) {
            phase++;
        } else if (phase == 19) {
            testerOutLocation = p.getLocation();
            finishSetup();
        }
    }

    private void finishSetup() {
        p.sendMessage(Main.PREFIX + "§aDer Tester wurde erfolgreich eingestellt");
        finished = true;
        for (int i = 0; i < borderBlocks.length; i++)
            new ConfigLocationUtil(plugin, borderBlocks[i].getLocation(), "Arenas." + map.getName() + ".Tester.Borderblocks." + i).saveBlockLocation();
        for (int i = 0; i < lampsBlocks.length; i++)
            new ConfigLocationUtil(plugin, lampsBlocks[i].getLocation(), "Arenas." + map.getName() + ".Tester.Lampsblocks." + i).saveBlockLocation();
        for (int i = 0; i < groundBlocks.length; i++)
            new ConfigLocationUtil(plugin, groundBlocks[i].getLocation(), "Arenas." + map.getName() + ".Tester.Groundblocks." + i).saveBlockLocation();
        new ConfigLocationUtil(plugin, button.getLocation(), "Arenas." + map.getName() + ".Tester.Button.test").saveBlockLocation();
        new ConfigLocationUtil(plugin, trap.getLocation(), "Arenas." + map.getName() + ".Tester.Button.trap").saveBlockLocation();
        new ConfigLocationUtil(plugin, testerInLocation, "Arenas." + map.getName() + ".Tester.Location.in").saveLocation();
        new ConfigLocationUtil(plugin, testerOutLocation, "Arenas." + map.getName() + ".Tester.Location.out").saveLocation();
    }
}
