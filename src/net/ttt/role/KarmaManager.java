package net.ttt.role;

import net.ttt.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class KarmaManager {

    private Main plugin;
    private HashMap<UUID, Integer> playerKarma;
    private ArrayList<UUID> editedKarma;
    private ArrayList<UUID> karmaPassChecked;

    public KarmaManager(Main plugin) {
        this.plugin = plugin;
        playerKarma = new HashMap<>();
        editedKarma = new ArrayList<>();
        karmaPassChecked = new ArrayList<>();
    }

    public void loadKarma(Player p) {
        if (plugin.getMySQL().existsPlayer(p.getUniqueId())) {
            int karma = plugin.getMySQL().get("Karma", plugin.getMySQL().getTable(), "UUID", p.getUniqueId().toString());
            checkKarmaToken(p, karma);
            playerKarma.put(p.getUniqueId(), karma);
        } else {
            plugin.getMySQL().registerPlayer(p);
            plugin.getMySQL().update("UPDATE " + plugin.getMySQL().getTable() + " SET Karma='" + 0 + "'");
            playerKarma.put(p.getUniqueId(), 0);
        }
    }

    public void saveKarma(Player p) {
        if (editedKarma.contains(p.getUniqueId())) {
            plugin.getKarmaManager().removeEdited(p);

            plugin.getMySQL().update("UPDATE " + plugin.getMySQL().getTable() + " SET Karma='" + playerKarma.get(p.getUniqueId()) + "' WHERE UUID='" + p.getUniqueId() + "'");
        }
    }

    private void checkKarmaToken(Player p, int karma) {
        if (!karmaPassChecked.contains(p)) {
            int karmaPass = plugin.getMySQL().get("KarmaPass", plugin.getMySQL().getTable(), "UUID", p.getUniqueId().toString());
            if ((karmaPass + 500) <= karma) {
                p.sendMessage(Main.PREFIX + "§aDu hast einen §eTraitor-Pass §aerhalten");
                p.sendMessage(Main.PREFIX + "§aDu hast einen §eDetective-Pass §aerhalten");
                plugin.getMySQL().update("UPDATE " + plugin.getMySQL().getTable() + " SET KarmaPass='" + (karmaPass + 500) + "' WHERE UUID='" + p.getUniqueId() + "'");
            }
        }
    }

    private void addEdited(Player p) {
        if (editedKarma.contains(p.getUniqueId())) return;
        editedKarma.add(p.getUniqueId());
    }

    private void removeEdited(Player p) {
        if (!editedKarma.contains(p.getUniqueId())) return;
        editedKarma.remove(p.getUniqueId());
    }

    public void addKarma(Player p, int amount) {
        addEdited(p);
        playerKarma.put(p.getUniqueId(), playerKarma.get(p.getUniqueId()) + amount);
    }

    public void removeKarma(Player p, int amount) {
        addEdited(p);
        playerKarma.put(p.getUniqueId(), playerKarma.get(p.getUniqueId()) - amount);
    }

    public int getPlayerKarma(Player p) {
        return playerKarma.get(p.getUniqueId());
    }
}
