package net.ttt.role;

import net.ttt.main.Main;
import net.ttt.util.ConfigLocationUtil;
import net.ttt.voting.Map;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Tester {

    private static final int TESTING_CHECK_TIME = 4,
            TESTING_RELEASE_TIME = 2,
            TESTING_COOLDOWN = 5;

    private Main plugin;
    private Map map;

    private Block[] borderBlocks, groundBlocks, lampsBlocks;
    private Block button, trap;
    private Location testerInLocation, testerOutLocation;
    private boolean inUse, trapUsed;
    private int remainingCooldownTime, taskID;
    private World world;

    public Tester(Map map, Main plugin) {
        this.plugin = plugin;
        this.map = map;

        borderBlocks = new Block[3];
        groundBlocks = new Block[9];
        lampsBlocks = new Block[2];
    }

    public void test(Player p) {
        Role role = plugin.getRoleManager().getPlayerRole(p);
        if (inUse) {
            p.sendMessage(Main.PREFIX + "§cDer Tester wird bereits genutzt");
            return;
        }

        if (remainingCooldownTime != 0) {
            p.sendMessage(Main.PREFIX + "§cDer Tester ist wieder in " + remainingCooldownTime + " Sekunden bereit");
            return;
        }

        if (role == Role.TRAITOR) {
            if (RoleInventories.removeMaterialItem(p, Material.STAINED_GLASS)) {
                p.sendMessage(Main.PREFIX + "zu 75% wirst du als innocent angezeigt.");
                if (Math.random() <= 0.75D)
                    role = Role.INNOCENT;
            }
        }

        for (Player current : Bukkit.getOnlinePlayers()) {
            Role currentRole = plugin.getRoleManager().getPlayerRole(current);
            if (role == Role.TRAITOR) {
                if (currentRole == Role.TRAITOR)
                    current.sendMessage(Main.PREFIX + role.getChatColor() + p.getName() + " §7hat den Tester betreten");
                else
                current.sendMessage(Main.PREFIX + ChatColor.GREEN + p.getName() + " §7hat den Tester betreten");
            }
            else
                current.sendMessage(Main.PREFIX + role.getChatColor() + p.getName() + " §7hat den Tester betreten");
        }

        p.teleport(testerInLocation);
        inUse = true;
        p.getWorld().playSound(borderBlocks[1].getLocation(), Sound.PISTON_EXTEND, 1, 1);

        for (Block current : borderBlocks) {
            world.getBlockAt(current.getLocation()).setType(Material.GLASS);
            world.getBlockAt(current.getLocation().getBlockX(), current.getLocation().getBlockY() - 1, current.getLocation().getBlockZ()).setType(Material.PISTON_BASE);
        }
        for (Entity current : world.getNearbyEntities(button.getLocation(), 3, 1, 2)) {
            if (current instanceof Player)
                if (!((Player) current).getUniqueId().equals(p.getUniqueId()))
                    ((Player) current).teleport(testerOutLocation);
        }

        Role endRole = role;
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                endTesting(endRole, p);
            }
        }, TESTING_CHECK_TIME * 20);
    }

    private void endTesting(Role role, Player p) {
        DyeColor color;
        if (role == Role.DETECTIVE) {
            color = DyeColor.BLUE;
        } else if (role == Role.TRAITOR) {
            color = DyeColor.RED;
        } else {
            color = DyeColor.LIME;
        }
        for (Block current : lampsBlocks)
            setColoredGlass(current.getLocation(), color);
        p.getWorld().playSound(borderBlocks[1].getLocation(), Sound.NOTE_PLING, 1, 1);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                p.getWorld().playSound(borderBlocks[1].getLocation(), Sound.PISTON_RETRACT, 1, 1);
                resetTester();
            }
        }, TESTING_RELEASE_TIME * 20);
    }

    public void trapTester(Player p) {
        if (plugin.getRoleManager().getPlayerRole(p) == Role.TRAITOR) {
            if (!trapUsed) {
                trapUsed = true;
                world.playSound(borderBlocks[1].getLocation(), Sound.BAT_TAKEOFF, 1, 1);
                for (Block current : groundBlocks) {
                    world.getBlockAt(current.getLocation()).setType(Material.AIR);
                }
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        for (Block current : groundBlocks) {
                            world.getBlockAt(current.getLocation()).setType(Material.IRON_BLOCK);
                        }
                        world.playSound(groundBlocks[4].getLocation(), Sound.DIG_WOOD, 5, 5);
                    }
                }, 5 * 20);
            } else
                p.sendMessage(Main.PREFIX + "trap used");
        } else
            p.sendMessage(Main.PREFIX + "nur traitors");
    }

    public void load() {
        for (int i = 0; i < borderBlocks.length; i++)
            borderBlocks[i] = new ConfigLocationUtil(plugin, "Arenas." + map.getName() + ".Tester.Borderblocks." + i).loadBlockLocation();
        for (int i = 0; i < lampsBlocks.length; i++)
            lampsBlocks[i] = new ConfigLocationUtil(plugin, "Arenas." + map.getName() + ".Tester.Lampsblocks." + i).loadBlockLocation();
        for (int i = 0; i < groundBlocks.length; i++)
            groundBlocks[i] = new ConfigLocationUtil(plugin, "Arenas." + map.getName() + ".Tester.Groundblocks." + i).loadBlockLocation();
        button = new ConfigLocationUtil(plugin, "Arenas." + map.getName() + ".Tester.Button.test").loadBlockLocation();
        trap = new ConfigLocationUtil(plugin, "Arenas." + map.getName() + ".Tester.Button.trap").loadBlockLocation();
        testerInLocation = new ConfigLocationUtil(plugin, "Arenas." + map.getName() + ".Tester.Location.in").loadLocation();
        testerOutLocation = new ConfigLocationUtil(plugin, "Arenas." + map.getName() + ".Tester.Location.out").loadLocation();

        world = map.getSpawnLocations()[1].getWorld();
        resetTester();
    }

    private void resetTester() {
        for (Block current : borderBlocks) {
            world.getBlockAt(current.getLocation()).setType(Material.AIR);
            world.getBlockAt(current.getLocation().getBlockX(), current.getLocation().getBlockY() - 1, current.getLocation().getBlockZ()).setType(Material.GLASS);
            world.getBlockAt(current.getLocation().getBlockX(), current.getLocation().getBlockY() - 2, current.getLocation().getBlockZ()).setType(Material.PISTON_BASE);
        }
        for (Block current : lampsBlocks)
            setColoredGlass(current.getLocation(), DyeColor.WHITE);
        remainingCooldownTime = TESTING_COOLDOWN;
        inUse = false;
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (remainingCooldownTime != 0)
                    remainingCooldownTime--;
                else
                    Bukkit.getScheduler().cancelTask(taskID);
            }
        }, 0, 20);
    }

    @SuppressWarnings("deprecation")
    private void setColoredGlass(Location location, DyeColor dyeColor) {
        Block block = world.getBlockAt(location);
        block.setType(Material.STAINED_GLASS);
        block.setData(dyeColor.getData());
    }

    public boolean exists() {
        return plugin.getConfig().getString("Arenas." + map.getName() + ".Tester.Location.in") != null;
    }

    public Block getButton() {
        return button;
    }

    public Block getTrap() {
        return trap;
    }
}
