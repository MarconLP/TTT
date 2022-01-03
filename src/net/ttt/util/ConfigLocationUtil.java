package net.ttt.util;

import net.ttt.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigLocationUtil {

    private Main plugin;
    private Location location;
    private String root;

    public ConfigLocationUtil(Main plugin, Location location, String root) {
        this.plugin = plugin;
        this.location = location;
        this.root = root;
    }

    public ConfigLocationUtil(Main plugin, String root) {
        this(plugin, null, root);
    }

    public void saveBlockLocation() {
        FileConfiguration config = plugin.getConfig();
//        config.set(root + ".World", location.getWorld().getName());
//        config.set(root + ".X", location.getBlockX());
//        config.set(root + ".Y", location.getBlockY());
//        config.set(root + ".Z", location.getBlockZ());

        config.set(root, location.getWorld().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ());
        plugin.saveConfig();
    }

    public void saveLocation() {
        FileConfiguration config = plugin.getConfig();
//        config.set(root + ".World", location.getWorld().getName());
//        config.set(root + ".X", location.getX());
//        config.set(root + ".Y", location.getY());
//        config.set(root + ".Z", location.getZ());
//        config.set(root + ".Yaw", location.getYaw());
//        config.set(root + ".Pitch", location.getPitch());

        config.set(root, location.getWorld().getName() + ":" + location.getX() + ":" + location.getY() + ":" +
                location.getZ() + ":" + location.getYaw() + ":" + location.getPitch());
        plugin.saveConfig();
    }

    public Block loadBlockLocation() {
        FileConfiguration config = plugin.getConfig();
        if (config.contains(root)) {
//            World world = Bukkit.getWorld(config.getString(root + ".World"));
//            int x = plugin.getConfig().getInt(root + ".X"),
//                    y = plugin.getConfig().getInt(root + ".Y"),
//                    z = plugin.getConfig().getInt(root + ".Z");
//            return new Location(world, x, y, z).getBlock();

            String s = config.getString(root);
            String[] l = s.split(":");

            return new Location(Bukkit.getWorld(l[0]), Integer.parseInt(l[1]), Integer.parseInt(l[2]), Integer.parseInt(l[3])).getBlock();
        }
        return null;
    }

    public Location loadLocation() {
        FileConfiguration config = plugin.getConfig();
        if (config.contains(root)) {
//            World world = Bukkit.getWorld(config.getString(root + ".World"));
//            double x = config.getDouble(root + ".X"),
//                    y = config.getDouble(root + ".Y"),
//                    z = config.getDouble(root + ".Z");
//            float yaw = (float) config.getDouble(root + ".Yaw"),
//                    pitch = (float) config.getDouble(root + ".Pitch");
//            return new Location(world, x, y, z, yaw, pitch);

            String s = config.getString(root);
            String[] l = s.split(":");

            return new Location(Bukkit.getWorld(l[0]), Double.parseDouble(l[1]), Double.parseDouble(l[2]), Double.parseDouble(l[3]), Float.parseFloat(l[4]), Float.parseFloat(l[5]));
        } else {
            return null;
        }
    }
}
