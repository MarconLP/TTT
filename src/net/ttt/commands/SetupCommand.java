package net.ttt.commands;

import net.ttt.gamestates.LobbyState;
import net.ttt.main.Main;
import net.ttt.role.Tester;
import net.ttt.util.ConfigLocationUtil;
import net.ttt.util.TesterSetup;
import net.ttt.voting.Map;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetupCommand implements CommandExecutor {

    private Main plugin;

    public SetupCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (p.hasPermission("ttt.setup")) {
                if (args.length == 0) {
                    p.sendMessage(Main.PREFIX + "§7Bitte nutze /setup help");
                } else {
                    if (args[0].equalsIgnoreCase("lobby")) {
                        if (args.length == 1) {
                            new ConfigLocationUtil(plugin, p.getLocation(), "wartelobby").saveLocation();
                            p.sendMessage("die lobby wurde gesetzt");
                        } else
                            p.sendMessage(Main.PREFIX + "§7pls use /setup lobby");
                    } else if (args[0].equalsIgnoreCase("create")) {
                        if (args.length == 5) {
                            Map map = new Map(plugin, args[1]);
                            if (!map.exists()) {
                                map.create(args[2], Material.valueOf(args[3].toUpperCase()), args[4].replace("&", ""));
                                p.sendMessage(Main.PREFIX + "§adie map wurde erstellt");
                            } else
                                p.sendMessage(Main.PREFIX + "§cmap gibt es schon");
                        } else
                            p.sendMessage(Main.PREFIX + "§cbenutz /setup create <NAME> <Builder> <Material> <Displayname>");
                    } else if (args[0].equalsIgnoreCase("set")) {
                        if (args.length == 3) {
                            Map map = new Map(plugin, args[1].toUpperCase());
                            if (map.exists()) {
                                try {
                                    int spawnNumber = Integer.parseInt(args[2]);
                                    if (spawnNumber > 0 && spawnNumber <= LobbyState.MAX_PLAYERS) {
                                        map.setSpawnLocation(spawnNumber, p.getLocation());
                                        p.sendMessage(Main.PREFIX + "§adu hast erfolgreich den spawn " + spawnNumber + "§a auf der map " + map.getName() + "gesetzt");
                                    } else
                                        p.sendMessage(Main.PREFIX + "§cspawn number ungültig");
                                } catch (NumberFormatException e) {
                                    if (args[2].equalsIgnoreCase("spec")) {
                                        map.setSpectatorLocation(p.getLocation());
                                        p.sendMessage(Main.PREFIX + "§a du hast spec spawn gesetzt");
                                    } else
                                        p.sendMessage(Main.PREFIX + "§cuse /setup set <name> <1-" + LobbyState.MAX_PLAYERS + "/spec>");
                                }
                            } else
                                p.sendMessage(Main.PREFIX + "§cdie map existiert nicht");
                        } else
                            p.sendMessage(Main.PREFIX + "§cuse /setup set <name> <1-" + LobbyState.MAX_PLAYERS + "/spec>");
                    } else if (args[0].equalsIgnoreCase("tester")) {
                        if (args.length == 2) {
                            Map map = new Map(plugin, args[1].toUpperCase());
                            if (map.exists()) {
                                new TesterSetup(p, map, plugin);
                            } else
                                p.sendMessage(Main.PREFIX + "§cDiese Map existiert nicht!");
                        }
                    } else {
                        p.sendMessage(Main.PREFIX + "§eUsage: /setup lobby -- setze lobby");
                        p.sendMessage(Main.PREFIX + "§eUsage: /setup create <name> <Builder> <Material> <Displayname>");
                        p.sendMessage(Main.PREFIX + "§eUsage: /setup set <name> <1-" + LobbyState.MAX_PLAYERS + "/spec>");
                        p.sendMessage(Main.PREFIX + "§eUsage: /setup tester <name>");
                    }
                }
            } else {
                p.sendMessage(Main.NOPERMS);
            }
        }
        return false;
    }
}
