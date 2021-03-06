package net.ttt.gamestates;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.ttt.countdowns.GameCountdown;
import net.ttt.countdowns.RoleCountdown;
import net.ttt.main.Main;
import net.ttt.role.Role;
import net.ttt.util.ItemBuilder;
import net.ttt.voting.Map;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collections;

import static net.ttt.role.Role.*;

public class IngameState extends GameState {

    private Main plugin;
    private Map map;
    private ArrayList<Player> players, spectators;
    private RoleCountdown roleCountdown;
    private GameCountdown gameCountdown;
    private ItemStack navigatorItem = new ItemBuilder(Material.COMPASS).setDisplayName("┬žeTeleporter").build();
    private ItemStack nextroundItem = new ItemBuilder(Material.REDSTONE_COMPARATOR).setDisplayName("round").build();
    private ItemStack lobbyItem = new ItemBuilder(Material.WOOD_AXE).setDisplayName("lobby").build();
    private boolean grace;
    public boolean timeout;
    private Role winningRole;

    private Scoreboard specBoard;
    private Team specTeam;

    public IngameState(Main plugin) {
        this.plugin = plugin;
        initSpecTeam();
        roleCountdown = new RoleCountdown(plugin);
        gameCountdown = new GameCountdown(plugin);
        spectators = new ArrayList<>();
    }

    @Override
    public void start() {
        grace = true;

        Collections.shuffle(plugin.getPlayers());
        players = plugin.getPlayers();

//        for (Player current : plugin.getPlayers()) {
//            plugin.getSkullBuilder().HeadItemDisplay(current);
//        }

        map = plugin.getVoting().getWinnerMap();
        map.load();
        for (int i = 0; i < players.size(); i++) {
            players.get(i).teleport(map.getSpawnLocations()[i]);
        }

        for (Player current : players) {
            current.setHealth(20);
            current.setFoodLevel(20);
            current.setLevel(0);
            current.getInventory().clear();
            plugin.getRoleInventories().getPointManager().setPoints(current, 0);
            updateScoreboard(current);
            plugin.getStatsManager().addStats("Plays", current);
        }

        roleCountdown.start();
    }

    public void updateScoreboard(Player p) {
        if (p.getScoreboard().getTeam("ttokenval") != null) {
            Scoreboard board = p.getScoreboard();

            if (board.getTeam("ttokenval") != null)
                board.getTeam("ttokenval").setPrefix("┬ž4" + plugin.getRoleInventories().getPointManager().getPoints(p));

            if (board.getTeam("dtokenval") != null)
                board.getTeam("dtokenval").setPrefix("┬ž9" + plugin.getRoleInventories().getPointManager().getPoints(p));

            board.getTeam("karmaval").setPrefix("┬že" + plugin.getKarmaManager().getPlayerKarma(p));
            board.getTeam("playersval").setPrefix("┬žc" + plugin.getPlayers().size());
            board.getTeam("mapval").setPrefix("┬žb" + WordUtils.capitalizeFully(map.getName()));

            if (board.getTeam("gtimeval") != null)
                board.getTeam("gtimeval").setPrefix("┬ža" + formatTime(roleCountdown.getSeconds()));
            if (board.getTeam("timeval") != null)
                board.getTeam("timeval").setPrefix("┬ža" + formatTime(gameCountdown.getSeconds()));

            if (!isGrace()) {
                board.resetScores("┬ž5┬žc");
                board.resetScores("┬ž7Map┬ž8:");
                board.resetScores("┬ž5┬ž8");
                board.resetScores("┬ž4");
                board.resetScores("┬ž7Schutzzeit┬ž8:");
                board.resetScores("┬ž6┬ž9");

                Role role = plugin.getRoleManager().getPlayerRole(p);
                Objective obj = board.getObjective("1");
                if (role == TRAITOR) {
                    obj.getScore("┬ž7T-Points┬ž8:").setScore(13);
                    obj.getScore("┬ž1┬ž4").setScore(12);
                    obj.getScore("┬ž9┬ž1").setScore(11);
                } else if (role == DETECTIVE) {
                    obj.getScore("┬ž7D-Points┬ž8:").setScore(13);
                    obj.getScore("┬ž2┬ž5").setScore(12);
                    obj.getScore("┬ž9┬ž1").setScore(11);
                }
                obj.getScore("┬ž4").setScore(2);
                obj.getScore("┬ž7Ende┬ž8:").setScore(1);
                obj.getScore("┬ž7┬ž0").setScore(0);
            }
        } else {
            Role role = plugin.getRoleManager().getPlayerRole(p);
            Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
            Objective ob = board.registerNewObjective("1", "2");
            ob.setDisplaySlot(DisplaySlot.SIDEBAR);

            Team ttokenval = board.registerNewTeam("ttokenval");
            ttokenval.addEntry("┬ž1┬ž4");
            ttokenval.setPrefix("┬ž4" + plugin.getRoleInventories().getPointManager().getPoints(p));

            Team dtokenval = board.registerNewTeam("dtokenval");
            dtokenval.addEntry("┬ž2┬ž5");
            dtokenval.setPrefix("┬ž9" + plugin.getRoleInventories().getPointManager().getPoints(p));

            Team karmaval = board.registerNewTeam("karmaval");
            karmaval.addEntry("┬ž3┬ž6");
            karmaval.setPrefix("┬že" + plugin.getKarmaManager().getPlayerKarma(p));

            Team playersval = board.registerNewTeam("playersval");
            playersval.addEntry("┬ž4┬ž7");
            playersval.setPrefix("┬žc" + plugin.getPlayers().size());

            Team mapval = board.registerNewTeam("mapval");
            mapval.addEntry("┬ž5┬ž8");
            mapval.setPrefix("┬žb" + WordUtils.capitalizeFully(map.getDisplayname()));

            Team gtimeval = board.registerNewTeam("gtimeval");
            gtimeval.addEntry("┬ž6┬ž9");
            gtimeval.setPrefix("┬ža" + formatTime(roleCountdown.getSeconds()));

            Team timeval = board.registerNewTeam("timeval");
            timeval.addEntry("┬ž7┬ž0");
            timeval.setPrefix("┬ža" + formatTime(gameCountdown.getSeconds()));

            ob.setDisplayName("┬žlTESTNAME.NET");
            ob.getScore("┬ž1").setScore(14);
            if (!isGrace() && role != INNOCENT) {
                if (role == TRAITOR) {
                    ob.getScore("┬ž7T-Points┬ž8:").setScore(13);
                    //ob.getScore("┬ž4" + plugin.getRoleInventories().getPointManager().getPoints(p)).setScore(12);

                    ob.getScore("┬ž1┬ž4").setScore(12);

                    ob.getScore("┬ž9┬ž1").setScore(11);
                } else if (role == DETECTIVE) {
                    ob.getScore("┬ž7D-Points┬ž8:").setScore(13);
                    //ob.getScore("┬ž3" + plugin.getRoleInventories().getPointManager().getPoints(p)).setScore(12);

                    ob.getScore("┬ž2┬ž5").setScore(12);

                    ob.getScore("┬ž9┬ž1").setScore(11);
                }
            }
            ob.getScore("┬ž7Karma┬ž8:").setScore(10);
            //ob.getScore("┬že" + plugin.getKarmaManager().getPlayerKarma(p)).setScore(9);

            ob.getScore("┬ž3┬ž6").setScore(9);

            ob.getScore("┬ž2").setScore(8);
            ob.getScore("┬ž7Spieler┬ž8:").setScore(7);
            //ob.getScore("┬ž2" + plugin.getPlayers().size()).setScore(6);

            ob.getScore("┬ž4┬ž7").setScore(6);

            if (isGrace()) {
                ob.getScore("┬ž5┬žc").setScore(5);
                ob.getScore("┬ž7Map┬ž8:").setScore(4);
                //ob.getScore("┬žb" + map.getName()).setScore(3);

                ob.getScore("┬ž5┬ž8").setScore(3);

                ob.getScore("┬ž4").setScore(2);
                ob.getScore("┬ž7Schutzzeit┬ž8:").setScore(1);
                //ob.getScore("┬žc" + formatTime(roleCountdown.getSeconds())).setScore(0);

                ob.getScore("┬ž6┬ž9").setScore(0);

            } else {
                ob.getScore("┬ž4").setScore(2);
                ob.getScore("┬ž7Ende┬ž8:").setScore(1);
                //ob.getScore("┬žc" + formatTime(gameCountdown.getSeconds())).setScore(0);

                ob.getScore("┬ž7┬ž0").setScore(0);

            }

            /*
             * Grace----
             * karma
             * spieler
             * map
             * schutzzeit
             *
             * Special-Role
             * points
             * karma
             * spieler
             * ende
             *
             * Inno
             * karma
             * spieler
             * ende
             *
             * S - Points
             * Karma
             * Spieler
             * G - Map
             * End
             * */

            p.setScoreboard(board);
        }
    }

    public void checkGameEnding() {
        if (plugin.getRoleManager().getTraitorPlayers().size() <= 0) {
            winningRole = INNOCENT;
            plugin.getGameStateManager().setGameState(ENDING_STATE);
            for (Player current : plugin.getPlayers()) {
                plugin.getStatsManager().addStats("Wins", current);
            }
        } else if (plugin.getRoleManager().getTraitorPlayers().size() == plugin.getPlayers().size()) {
            for (String current : plugin.getRoleManager().getTraitorPlayers()) {
                plugin.getStatsManager().addStats("Wins", Bukkit.getPlayer(current));
            }
            winningRole = Role.TRAITOR;
            plugin.getGameStateManager().setGameState(ENDING_STATE);
        } else if (timeout) {

        }
    }

    public void addSpec(Player p) {
        spectators.add(p);
        p.setAllowFlight(true);
        p.getInventory().clear();
        p.spigot().setCollidesWithEntities(false);
        for (Player current : Bukkit.getOnlinePlayers())
            current.hidePlayer(p);
        for (Player current : spectators) {
            current.showPlayer(p);
            p.showPlayer(current);
        }
        specTeam(p);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                p.getInventory().setItem(0, navigatorItem);
                p.getInventory().setItem(4, nextroundItem);
                p.getInventory().setItem(8, lobbyItem);
            }
        }, 1);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (Player current : players) {
                    if (plugin.getRoleManager().getPlayerRole(current) == TRAITOR)
                        plugin.getRoleManager().setFakeArmor(p, current.getEntityId(), (plugin.getRoleManager().getPlayerRole(p) != Role.TRAITOR) ? Color.GREEN : Color.RED);
                }
            }
        }, 5);
    }

    private void specTeam(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15));
        specTeam.addEntry(p.getName());

        for (Player current : spectators)
            current.setScoreboard(specBoard);
    }

    private void initSpecTeam() {
        specBoard = Bukkit.getScoreboardManager().getNewScoreboard();
        specTeam = specBoard.registerNewTeam("team");
        specTeam.setCanSeeFriendlyInvisibles(true);

        plugin.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_EFFECT) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        event.setCancelled(true);
                    }
                }
        );
    }

    @Override
    public void stop() {
        if (winningRole == Role.TRAITOR) {
            Bukkit.broadcastMessage(Main.PREFIX + "┬ž7Die ┬žcTraitor ┬ž7haben alle ┬žaInnocents ┬ž7und ┬ž9Detectives ┬ž7eliminiert");
            Bukkit.broadcastMessage(Main.PREFIX + "┬ž7Die Traitor waren: ┬žc" + String.join("┬ž7, ┬žc", plugin.getRoleManager().getAllTraitorPlayers()));
        } else if (winningRole == INNOCENT) {
            Bukkit.broadcastMessage(Main.PREFIX + "┬ž7Alle ┬žcTraitor ┬ž7wurden eliminiert");
            Bukkit.broadcastMessage(Main.PREFIX + "┬ž7Die Traitor waren: ┬žc" + String.join("┬ž7, ┬žc", plugin.getRoleManager().getAllTraitorPlayers()));
        } else
            Bukkit.broadcastMessage(String.valueOf(winningRole));
        for (Player current : players) {
            plugin.getKarmaManager().saveKarma(current);
            plugin.getStatsManager().saveStats(current);
        }
    }

    private String formatTime(int seconds) {
        int minAndSec = seconds%3600;
        int min = minAndSec/60;
        int sec = minAndSec%60;

        return (min > 9 ?  min : "0" + min) + ":" + (sec > 9 ?  sec : "0" + sec);
    }

    public void setGrace(boolean grace) {
        this.grace = grace;
    }

    public boolean isGrace() {
        return grace;
    }

    public ArrayList<Player> getSpectators() {
        return spectators;
    }

    public Map getMap() {
        return map;
    }

    public GameCountdown getGameCountdown() {
        return gameCountdown;
    }
}