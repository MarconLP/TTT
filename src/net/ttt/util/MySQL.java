package net.ttt.util;

import net.ttt.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.UUID;

public class MySQL {

    private Main plugin;
    private Connection con;
    private String host, database, username, password, table, statsTable, tempStatsTable;

    public MySQL(Main plugin) {
        this.plugin = plugin;
        this.host = "localhost";
        this.database = "testwerk";
        this.username = "netz";
        this.password = "123";
        this.table = "ttt";
        this.statsTable = "ttt_stats";
        this.tempStatsTable = "ttt_stats_temp";
    }

    public void connect() {
        try {
            con = DriverManager.getConnection("jdbc:mysql://" + host + ":3306/" + database + "?autoReconnect=true", username, password);
            System.out.println("[MySQL] Successfully connected to database");
        } catch (SQLException e) {
            System.out.println("[MySQL] Failed to connect to database");
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (con != null) {
                con.close();
                System.out.println("[MySQL] Successfully disconnected from database");
            }
        } catch (SQLException e) {
            System.out.println("[MySQL] Failed to disconnect from database");
            e.printStackTrace();
        }
    }

    public void update(String qry) {
        try {
            Statement st = con.createStatement();
            st.executeUpdate(qry);
            st.close();
        } catch (SQLException e) {
            connect();
            e.printStackTrace();
        }
    }

    public ResultSet query(String qry) {
        ResultSet rs = null;
        try {
            Statement st = con.createStatement();
            rs = st.executeQuery(qry);
        } catch (SQLException e) {
            connect();
            e.printStackTrace();
        }
        return rs;
    }

    public int get(String select, String table, String where, String what) {
        ResultSet rs = plugin.getMySQL().query("SELECT " + select + " FROM " + table + " WHERE " + where + "='" + what + "'");
        try {
            if (rs.next()) {
                return rs.getInt(select);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 4031;
        }
        return 403_2;
    }

    public boolean existsPlayer(UUID uuid) {
        try {
            ResultSet rs = plugin.getMySQL().query("SELECT * FROM " + table + " WHERE UUID= '" + uuid + "'");
            if (rs.next()) {
                return rs.getString("UUID") != null;
            }
            rs.close();
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void registerPlayer(Player p) {
        if (!existsPlayer(p.getUniqueId())) {
            plugin.getMySQL().update("INSERT INTO `" + table + "` (`UUID`, `Name`, `TraitorTokens`, `DetectiveTokens`, `Karma`, `KarmaPass`) VALUES ('" + p.getUniqueId() + "', '" + p.getName() + "', '0', '0', '0', '0');");
            plugin.getMySQL().update("INSERT INTO `" + statsTable + "` (`UUID`, `Name`, `Kills`, `Deaths`, `Plays`, `Wins`, `failKills`) VALUES ('" + p.getUniqueId() + "', '" + p.getName() + "', '0', '0', '0', '0', '0');");
        }
    }

    public void createTables() {
        plugin.getMySQL().update("CREATE TABLE IF NOT EXISTS ttt(UUID VARCHAR(64), Name VARCHAR(16), TraitorTokens int, DetectiveTokens int, Karma int, KarmaPass int)");
        plugin.getMySQL().update("CREATE TABLE IF NOT EXISTS ttt_stats(UUID VARCHAR(64), Name VARCHAR(16), Kills int, Deaths int, Plays int, Wins int, failKills int)");
        plugin.getMySQL().update("CREATE TABLE IF NOT EXISTS ttt_stats_temp(UUID VARCHAR(64), Name VARCHAR(16), Kills int, Deaths int, Plays int, Wins int, failKills int, date DATE)");
    }

    public String getTable() {
        return table;
    }

    public String getStatsTable() {
        return statsTable;
    }

    public String getTempStatsTable() {
        return tempStatsTable;
    }
}
