package net.ttt.role;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class PointManager {

    private HashMap<String, Integer> playerPoints;

    public PointManager() {
        playerPoints = new HashMap<>();
    }

    public void setPoints(Player p, int points) {
        playerPoints.put(p.getName(), points);
    }

    public void addPoints(Player p, int points) {
        if (playerPoints.containsKey(p.getName()))
            playerPoints.put(p.getName(), playerPoints.get(p.getName()) + points);
    }

    public boolean removePoints(Player p, int points) {
        if (!playerPoints.containsKey(p.getName())) return false;
        if (playerPoints.get(p.getName()) >= points) {
            playerPoints.put(p.getName(), playerPoints.get(p.getName()) - points);
            if (playerPoints.get(p.getName()) < 0)
                setPoints(p, 0);
            return true;
        }
        return false;
    }

    public int getPoints(Player p) {
        return playerPoints.get(p.getName());
    }
}
