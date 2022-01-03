package net.ttt.voting;

import net.ttt.gamestates.LobbyState;
import net.ttt.main.Main;
import net.ttt.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class Voting {

    public static final int MAP_AMOUNT = 2;
    public static final String VOTING_INV_TITLE = "§eMapvoting";

    private Main plugin;
    private ArrayList<Map> maps;
    private ArrayList<Map> votingMaps;
    private HashMap<String, Integer> playerVotes;
    private Inventory votingInv;

    public Voting(Main plugin, ArrayList<Map> maps) {
        this.plugin = plugin;
        this.maps = maps;
        votingMaps = new ArrayList<>();
        playerVotes = new HashMap<>();

        initVotingInv();
    }

    public void initVotingInv() {
        votingInv = Bukkit.createInventory(null, 9, VOTING_INV_TITLE);
        for (Map map : maps) {
            votingInv.addItem(new ItemBuilder(map.getMaterial()).setDisplayName("§" + map.getDisplayname()).setLore("§e" + map.getVotes() + " §8- §7Votes").build());
        }
    }

    public Map getWinnerMap() {
        if (votingMaps.isEmpty()) {
            votingMaps = maps;
            Collections.shuffle(votingMaps);
        }
        Map winnerMap = votingMaps.get(0);
        for (int i = 1; i < votingMaps.size(); i++) {
            if (votingMaps.get(i).getVotes() >= winnerMap.getVotes())
                winnerMap = votingMaps.get(i);
        }
        return winnerMap;
    }

    public void vote(Player p, int votingMap) {
        if (plugin.getGameStateManager().getCurrentGameState() instanceof LobbyState) {
            if (!((LobbyState) plugin.getGameStateManager().getCurrentGameState()).getMap().equals("Voting")) {
                p.sendMessage(Main.PREFIX + "§cDie Runde startet bereits");
                p.closeInventory();
                return;
            }
        }
        if (playerVotes.containsKey(p.getName())) {
            if (playerVotes.get(p.getName()) == votingMap) {
                p.sendMessage(Main.PREFIX + "§7Du hast §cbereits §7für diese  Map abgestimmt");
                p.closeInventory();
                return;
            }
            maps.get(playerVotes.get(p.getName())).removeVote();
            playerVotes.remove(p.getName());
        }
        maps.get(votingMap).addVote();
        p.closeInventory();
        p.sendMessage(Main.PREFIX + "du hast erfolgreich für die Map" + maps.get(votingMap).getName() + " abgestimmt");
        playerVotes.put(p.getName(), votingMap);
        initVotingInv();
    }

    public HashMap<String, Integer> getPlayerVotes() {
        return playerVotes;
    }

    public Inventory getVotingInv() {
        return votingInv;
    }

    public ArrayList<Map> getMaps() {
        return maps;
    }
}
