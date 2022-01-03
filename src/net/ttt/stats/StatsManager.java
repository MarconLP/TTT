package net.ttt.stats;

import net.ttt.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;

public class StatsManager {

    private Main plugin;
    private HashMap<UUID, HashMap<String, Integer>> playerStats;
    private HashMap<UUID, Integer> playerStatsRequests;
    private String date;

    public StatsManager(Main plugin) {
        this.plugin = plugin;
        playerStats = new HashMap<>();
        this.date = LocalDate.now().toString();
        clearTempStats();
    }

    public void loadStats(Player p) {
        playerStats.put(p.getUniqueId(), new HashMap<String, Integer>() {{
            put("Kills", 0);
            put("Deaths", 0);
            put("Plays", 0);
            put("Wins", 0);
            put("failKills", 0);
        }});
    }

    public void saveStats(Player p) {
        if (!playerStats.containsKey(p.getUniqueId())) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getMySQL().update("UPDATE " + plugin.getMySQL().getStatsTable() + " SET Kills='" +
                        (playerStats.get(p.getUniqueId()).get("Kills") + plugin.getMySQL().get("Kills", plugin.getMySQL().getStatsTable(), "UUID", p.getUniqueId().toString())) + "' WHERE UUID='" + p.getUniqueId() + "'");
                plugin.getMySQL().update("UPDATE " + plugin.getMySQL().getStatsTable() + " SET Deaths='" +
                        (playerStats.get(p.getUniqueId()).get("Deaths") + plugin.getMySQL().get("Deaths", plugin.getMySQL().getStatsTable(), "UUID", p.getUniqueId().toString())) + "' WHERE UUID='" + p.getUniqueId() + "'");
                plugin.getMySQL().update("UPDATE " + plugin.getMySQL().getStatsTable() + " SET Plays='" +
                        (playerStats.get(p.getUniqueId()).get("Plays") + plugin.getMySQL().get("Plays", plugin.getMySQL().getStatsTable(), "UUID", p.getUniqueId().toString())) + "' WHERE UUID='" + p.getUniqueId() + "'");
                plugin.getMySQL().update("UPDATE " + plugin.getMySQL().getStatsTable() + " SET Wins='" +
                        (playerStats.get(p.getUniqueId()).get("Wins") + plugin.getMySQL().get("Wins", plugin.getMySQL().getStatsTable(), "UUID", p.getUniqueId().toString())) + "' WHERE UUID='" + p.getUniqueId() + "'");
                plugin.getMySQL().update("UPDATE " + plugin.getMySQL().getStatsTable() + " SET failKills='" +
                        (playerStats.get(p.getUniqueId()).get("failKills") + plugin.getMySQL().get("failKills", plugin.getMySQL().getStatsTable(), "UUID", p.getUniqueId().toString())) + "' WHERE UUID='" + p.getUniqueId() + "'");
                saveTempStats(p);
            }
        });
    }

    private void saveTempStats(Player p) {

        boolean availableRecord = false;
        try {
            ResultSet rs = plugin.getMySQL().query("SELECT * FROM " + plugin.getMySQL().getTempStatsTable() + " WHERE UUID='" + p.getUniqueId().toString() + "' AND DATE='" + date + "'");
            if (rs.next()) {
                availableRecord = rs.getString("UUID") != null;
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        if (!availableRecord) {
            plugin.getMySQL().update("INSERT INTO `" + plugin.getMySQL().getTempStatsTable() + "` (`UUID`, `Name`, `Kills`, `Deaths`, `Plays`, `Wins`, `failKills`, `date`) VALUES ('" + p.getUniqueId() + "', '" + p.getName() + "', '0', '0', '0', '0', '0', '" + date + "');");
        }

        plugin.getMySQL().update("UPDATE " + plugin.getMySQL().getTempStatsTable() + " SET Kills='" + (playerStats.get(p.getUniqueId()).get("Kills") + plugin.getMySQL().get("Kills", plugin.getMySQL().getTempStatsTable(), "UUID", p.getUniqueId().toString())) + "' WHERE UUID='" + p.getUniqueId() + "' AND DATE='" + date + "'");
        plugin.getMySQL().update("UPDATE " + plugin.getMySQL().getTempStatsTable() + " SET Deaths='" + (playerStats.get(p.getUniqueId()).get("Deaths") + plugin.getMySQL().get("Deaths", plugin.getMySQL().getTempStatsTable(), "UUID", p.getUniqueId().toString())) + "' WHERE UUID='" + p.getUniqueId() + "' AND DATE='" + date + "'");
        plugin.getMySQL().update("UPDATE " + plugin.getMySQL().getTempStatsTable() + " SET Plays='" + (playerStats.get(p.getUniqueId()).get("Plays") + plugin.getMySQL().get("Plays", plugin.getMySQL().getTempStatsTable(), "UUID", p.getUniqueId().toString())) + "' WHERE UUID='" + p.getUniqueId() + "' AND DATE='" + date + "'");
        plugin.getMySQL().update("UPDATE " + plugin.getMySQL().getTempStatsTable() + " SET Wins='" + (playerStats.get(p.getUniqueId()).get("Wins") + plugin.getMySQL().get("Wins", plugin.getMySQL().getTempStatsTable(), "UUID", p.getUniqueId().toString())) + "' WHERE UUID='" + p.getUniqueId() + "' AND DATE='" + date + "'");
        plugin.getMySQL().update("UPDATE " + plugin.getMySQL().getTempStatsTable() + " SET failKills='" + (playerStats.get(p.getUniqueId()).get("failKills") + plugin.getMySQL().get("failKills", plugin.getMySQL().getTempStatsTable(), "UUID", p.getUniqueId().toString())) + "' WHERE UUID='" + p.getUniqueId() + "' AND DATE='" + date + "'");

        playerStats.remove(p.getUniqueId());

    }

    public void addStats(String type, Player p) {

        HashMap<String, Integer> playerStat = playerStats.get(p.getUniqueId());

        playerStat.put(type, (playerStat.get(type) + 1));

        playerStats.put(p.getUniqueId(), playerStat);

    }

    public void requestStats(Player p, String t, boolean temp) {
        Bukkit.broadcastMessage(t);
        ResultSet rs = null;
        if (temp) {
            Bukkit.broadcastMessage("true");
            rs = plugin.getMySQL().query("SELECT sum(Kills), sum(Deaths), sum(Plays), sum(Wins), sum(failKills) FROM ttt_stats_temp WHERE Name='" + t + "' AND DATE>'" + LocalDate.now().minusMonths(1) + "';");
        } else {
            rs = plugin.getMySQL().query("SELECT sum(Kills), sum(Deaths), sum(Plays), sum(Wins), sum(failKills) FROM ttt_stats WHERE Name='" + t + "';");
        }
        try {
            rs.next();
            p.sendMessage(rs.getString(1));
            p.sendMessage(rs.getString(2));
            p.sendMessage(rs.getString(3));
            p.sendMessage(rs.getString(4));
            p.sendMessage(rs.getString(5));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearTempStats() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getMySQL().update("DELETE FROM " + plugin.getMySQL().getTempStatsTable() + " WHERE DATE<'" + LocalDate.now().minusMonths(1) + "'");
            }
        });
    }
}
