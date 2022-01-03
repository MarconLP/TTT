package net.ttt.role;

import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import net.ttt.main.Main;
import net.ttt.util.ItemBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class RoleManager {

    private Main plugin;
    private HashMap<String, Role> playerRoles;
    private ArrayList<Player> players;
    private ArrayList<Player> playersForRole;
    private ArrayList<String> traitorPlayers, allTraitorPlayers;

    private int traitors, detectives, innocents;

    public RoleManager(Main plugin) {
        this.plugin = plugin;
        playerRoles = new HashMap<>();
        players = plugin.getPlayers();
        playersForRole = new ArrayList<>();
        traitorPlayers = new ArrayList<>();
    }

    public void calculateRoles() {
        playersForRole.addAll(players);
        int playerSize = playersForRole.size();

        int traitors = 1;
        if (playerSize >= 7) {
            if (playerSize >= 10)
                traitors = 3;
            traitors = 2;
        }

        int detectives = 1;
        if (playerSize >= 5) {
            if (playerSize >= 10) {
                detectives = 2;
            }
            detectives = 1;
        }

        innocents = playerSize - traitors - detectives;

        System.out.println("traitors " + traitors);
        System.out.println("detectives " + detectives);
        System.out.println("innocents " + innocents);

        if (plugin.getTokenManager().checkTokenUse("t")) {
            ArrayList<Player> tokenUsert = plugin.getTokenManager().getTokenUser("t");
//            if (traitors == 0) {
//                for (int i = 0; i < tokenUsert.size(); i++) {
//                    tokenUsert.get(i).sendMessage(Main.PREFIX + "§7Es gab nicht genug §cTraitor§8-§cplätze");
//                    tokenUsert.remove(tokenUsert.get(i));
//                }
//            }
            if (tokenUsert.size() > traitors) {
                int toremove = tokenUsert.size() - traitors;
                for (int i = 0; i < toremove; i++) {
                    tokenUsert.get(i).sendMessage(Main.PREFIX + "§7Es gab nicht genug §cTraitor§8-§cplätze");
                    tokenUsert.remove(tokenUsert.get(i));
                }
            }
            traitors -= tokenUsert.size();
            for (Player tokenPlayer : tokenUsert) {
                playersForRole.remove(tokenPlayer);
                playerRoles.put(tokenPlayer.getName(), Role.TRAITOR);
                traitorPlayers.add(tokenPlayer.getName());
            }
        }

        if (plugin.getTokenManager().checkTokenUse("d")) {
            ArrayList<Player> tokenUser = plugin.getTokenManager().getTokenUser("d");
            if (detectives == 0) {
                for (int i = 0; i < tokenUser.size(); i++) {
                    tokenUser.get(i).sendMessage(Main.PREFIX + "§cEs gab nicht genug §eDetective plätze");
                    tokenUser.remove(tokenUser.get(i));
                }
            }
            if (tokenUser.size() > detectives) {
                int toremove = tokenUser.size() - detectives;
                for (int i = 0; i < toremove; i++) {
                    tokenUser.get(i).sendMessage(Main.PREFIX + "§cEs gab nicht genug §eDetective plätze");
                    tokenUser.remove(tokenUser.get(i));
                }
            }
            detectives -= tokenUser.size();
            for (Player tokenPlayer : tokenUser) {
                playersForRole.remove(tokenPlayer);
                playerRoles.put(tokenPlayer.getName(), Role.DETECTIVE);
            }
        }

        Collections.shuffle(playersForRole);

        int counter = 0;
        for (int i = counter; i < traitors; i++) {
            playerRoles.put(playersForRole.get(i).getName(), Role.TRAITOR);
            traitorPlayers.add(playersForRole.get(i).getName());
        }
        counter += traitors;

        allTraitorPlayers = traitorPlayers;

        for (int i = counter; i < detectives + counter; i++)
            playerRoles.put(playersForRole.get(i).getName(), Role.DETECTIVE);

        counter += detectives;

        for (int i = counter; i < innocents + counter; i++)
            playerRoles.put(playersForRole.get(i).getName(), Role.INNOCENT);

        for (Player current : players) {
            current.getInventory().setItem(8, new ItemBuilder(Material.STICK).setDisplayName("§7Identificator").build());
            switch (getPlayerRole(current)) {
                case TRAITOR:
                    for (Player others : players) {
                        if (current == others) {
                            setFakeArmorreal(current, current.getEntityId(), Color.RED);
                        } else
                            setFakeArmor(others, current.getEntityId(), (getPlayerRole(others) != Role.TRAITOR) ? Color.GREEN : Color.RED);
                    }
                    current.getInventory().setItem(22, plugin.getRoleInventories().getTraitorItem());
                    break;
                case DETECTIVE:
                    setArmor(current, Color.BLUE);
                    current.getInventory().setItem(22, plugin.getRoleInventories().getDetectiveItem());
                    break;
                case INNOCENT:
                    setArmor(current, Color.GREEN);

                    break;
                default:
                    break;
            }
        }
    }

    public void setFakeArmor(Player p, int entityID, Color color) {
        ItemStack armor = getColoredChestPlate(color);

        final int CHESTPLATE_SLOT = 3;
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(entityID, CHESTPLATE_SLOT, CraftItemStack.asNMSCopy(armor));
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    public void setFakeArmorreal(Player p, int entityID, Color color) {
        ItemStack armor = getColoredChestPlate(color);

        final int CHESTPLATE_SLOT = 2;
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(entityID, CHESTPLATE_SLOT, CraftItemStack.asNMSCopy(armor));
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    public void setArmor(Player p, Color color) {
        p.getInventory().setChestplate(getColoredChestPlate(color));
    }

    private ItemStack getColoredChestPlate(Color color) {
        ItemStack armor = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta armorMeta = (LeatherArmorMeta) armor.getItemMeta();
        armorMeta.setColor(color);
        armorMeta.spigot().setUnbreakable(true);
        armor.setItemMeta(armorMeta);
        return armor;
    }

    public Role getPlayerRole(Player p) {
        return playerRoles.get(p.getName());
    }

    public ArrayList<String> getTraitorPlayers() {
        return traitorPlayers;
    }

    public ArrayList<String> getAllTraitorPlayers() {
        return allTraitorPlayers;
    }
}
