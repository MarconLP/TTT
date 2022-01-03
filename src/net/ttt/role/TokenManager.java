package net.ttt.role;

import net.ttt.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class TokenManager {

    private final Main plugin;
    private HashMap<Player, Integer> cachedDetectiveTokens;
    private HashMap<Player, Integer> cachedTraitorTokens;
    private ArrayList<Player> playersUsedDetectiveTokens;
    private ArrayList<Player> playersUsedTraitorTokens;
    private int usedDetectiveTokens = 0;
    private int usedTraitorTokens = 0;

    public TokenManager(Main plugin) {
        this.plugin = plugin;
        cachedDetectiveTokens = new HashMap<>();
        cachedTraitorTokens = new HashMap<>();
        playersUsedDetectiveTokens = new ArrayList<>();
        playersUsedTraitorTokens = new ArrayList<>();
    }

    public int getTokens(Player p, String role) {
        if (role.equals("d")) {
            if (!cachedDetectiveTokens.containsKey(p)) {
                cachedDetectiveTokens.put(p, plugin.getMySQL().get("DetectiveTokens", plugin.getMySQL().getTable(), "UUID", String.valueOf(p.getUniqueId())));
            }
            return cachedDetectiveTokens.get(p);
        } else if (role.equals("t")) {
            if (!cachedTraitorTokens.containsKey(p)) {
                cachedTraitorTokens.put(p, plugin.getMySQL().get("TraitorTokens", plugin.getMySQL().getTable(), "UUID", String.valueOf(p.getUniqueId())));
            }
            return cachedTraitorTokens.get(p);
        }
        return 0;
    }

    private void clearCache(Player p, String role) {
        if (role.equals("d")) {
            cachedDetectiveTokens.remove(p);
        } else if (role.equals("t")) {
            cachedTraitorTokens.remove(p);
        }
    }

    public boolean removeTokens(Player p, String role) {
        if (role.equals("d")) {
            int tokens = plugin.getMySQL().get("DetectiveTokens", plugin.getMySQL().getTable(), "UUID", String.valueOf(p.getUniqueId()));
            if (!(tokens <= 0)) {
                plugin.getMySQL().update("UPDATE " + plugin.getMySQL().getTable() + " SET DetectiveTokens='" + (tokens - 1) + "' WHERE UUID='" + p.getUniqueId() + "'");
                return true;
            }
            return false;
        } else if (role.equals("t")) {
            int tokens = plugin.getMySQL().get("TraitorTokens", plugin.getMySQL().getTable(), "UUID", String.valueOf(p.getUniqueId()));
            if (!(tokens <= 0)) {
                plugin.getMySQL().update("UPDATE " + plugin.getMySQL().getTable() + " SET TraitorTokens='" + (tokens - 1) + "' WHERE UUID='" + p.getUniqueId() + "'");
                return true;
            }
            return false;
        }
        return false;
    }

    public boolean addToken(Player p, String role) {
        if (role.equals("d")) {
            int tokens = plugin.getMySQL().get("DetectiveTokens", plugin.getMySQL().getTable(), "UUID", String.valueOf(p.getUniqueId()));
            plugin.getMySQL().update("UPDATE " + plugin.getMySQL().getTable() + " SET DetectiveTokens='" + (tokens + 1) + "' WHERE UUID='" + p.getUniqueId() + "'");
            return true;
        } else if (role.equals("t")) {
            int tokens = plugin.getMySQL().get("TraitorTokens", plugin.getMySQL().getTable(), "UUID", String.valueOf(p.getUniqueId()));
            plugin.getMySQL().update("UPDATE " + plugin.getMySQL().getTable() + " SET TraitorTokens='" + (tokens + 1) + "' WHERE UUID='" + p.getUniqueId() + "'");
            return true;
        }
        return false;
    }

    public void useToken(Player p, String role) {
        if (!playersUsedTraitorTokens.contains(p) && !playersUsedDetectiveTokens.contains(p)) {
            if (role.equals("d")) {
                if (!(getTokens(p, "d") <= 0)) {
                    if (usedDetectiveTokens <= 0 && Bukkit.getOnlinePlayers().size() >= 5 || usedDetectiveTokens >= 1 && Bukkit.getOnlinePlayers().size() >= 10) {
                            if (usedDetectiveTokens < 2) {
                                if (removeTokens(p, "d")) {
                                    usedDetectiveTokens++;
                                    playersUsedDetectiveTokens.add(p);
                                    clearCache(p, "d");
                                    p.sendMessage(Main.PREFIX + "§7Du hast einen §9Detective8-§9Pass §7eingelöst");
                                } else
                                    p.sendMessage(Main.PREFIX + "§cDu hast keinen §eDetective-Pass");
                            } else
                                p.sendMessage(Main.PREFIX + "§cEs wurde bereits die maximale anzahl an pässen eingelöst");
                        } else
                            p.sendMessage(Main.PREFIX + "§cnicht genug spieler für  detective");
                } else
                    p.sendMessage(Main.PREFIX + "§cDu hast keinen §eDetective-Pass");
            } else if (role.equals("t")) {
                if (!(getTokens(p, "t") <= 0)) {
                    if (usedTraitorTokens >= 1 && Bukkit.getOnlinePlayers().size() >= 7 || usedTraitorTokens <= 0) {
                        if (usedTraitorTokens < 2) {
                            if (removeTokens(p, "t")) {
                                usedTraitorTokens++;
                                playersUsedTraitorTokens.add(p);
                                clearCache(p, "t");
                                p.sendMessage(Main.PREFIX + "§7Du hast einen §cTraitor§8-§cPass §7eingelöst");
                            } else
                                p.sendMessage(Main.PREFIX + "§cDu hast keinen §eTraitor-Pass");
                        } else
                            p.sendMessage(Main.PREFIX + "§cEs wurde bereits die maximale anzahl an pässen eingelöst");
                    } else
                        p.sendMessage(Main.PREFIX + "§cnicht genug spieler für += 2 traitor");
                } else
                    p.sendMessage(Main.PREFIX + "§cDu hast keinen §eTraitor-Pass");
            }
        } else
            p.sendMessage(Main.PREFIX + "§cDu hast schon einen Pass eingelöst");
    }

    public ArrayList<Player> getTokenUser(String role) {
        if (role.equals("d")) {
            return playersUsedDetectiveTokens;
        } else if (role.equals("t")) {
            return playersUsedTraitorTokens;
        }
        return null;
    }

    public boolean checkTokenUse(String role) {
        if (role.equals("d")) {
            if (!playersUsedDetectiveTokens.isEmpty())
                return true;
        } else if (role.equals("t")) {
            if (!playersUsedTraitorTokens.isEmpty())
                return true;
        }
        return false;
    }

    public void returnTokenAfterLeave(Player p) {
        if (playersUsedTraitorTokens.contains(p)) {
            playersUsedTraitorTokens.remove(p);
            addToken(p, "t");
            usedTraitorTokens--;
        } else if (playersUsedDetectiveTokens.contains(p)) {
            playersUsedDetectiveTokens.remove(p);
            addToken(p, "d");
            usedDetectiveTokens--;
        }
    }

}

