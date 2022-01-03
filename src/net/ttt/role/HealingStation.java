package net.ttt.role;

import net.ttt.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class HealingStation {

    private static final int HEALING_DELAY = 1,
            HEALING_RADIUS = 2;
    private static final double HEALING_AMOUNT = 0.5;

    private Main plugin;
    private int taskID, durability;
    private Location location;

    public HealingStation(Main plugin, Location location) {
        durability = 10;
        this.plugin = plugin;
        this.location = location;
        generateStation();
    }

    private void generateStation() {
        location.getBlock().setType(Material.BEACON);
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                durability--;

                for (Entity current : location.getWorld().getNearbyEntities(location, HEALING_RADIUS, HEALING_RADIUS, HEALING_RADIUS)) {
                    if (current instanceof Player) {
                        Player p = (Player) current;
                        if (!(p.getHealth() + HEALING_AMOUNT > 20)) {
                            p.setHealth(p.getHealth() + HEALING_AMOUNT);
                            location.getWorld().playSound(location.getBlock().getLocation(), Sound.LEVEL_UP, 1, 1);
                        }
                    }
                }
                if (durability <= 0)
                    destroyStation();
            }
        }, 0, HEALING_DELAY * 20);
    }

    public void destroyStation() {
        location.getBlock().setType(Material.AIR);
        Bukkit.getScheduler().cancelTask(taskID);
    }

}
