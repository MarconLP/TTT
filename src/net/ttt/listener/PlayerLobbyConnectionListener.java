package net.ttt.listener;

import net.ttt.countdowns.LobbyCountdown;
import net.ttt.gamestates.IngameState;
import net.ttt.gamestates.LobbyState;
import net.ttt.main.Main;
import net.ttt.util.ConfigLocationUtil;
import net.ttt.util.ItemBuilder;
import net.ttt.voting.Voting;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerLobbyConnectionListener implements Listener {

    public static final String VOTE_ITEM_NAME = "§eMapvoting";
    public static final String SETTINGS_ITEM_NAME = "§bSettings";

    private Main plugin;
    private ItemStack voteItem;
    private ItemStack settingsItem;

    public PlayerLobbyConnectionListener(Main plugin) {
        this.plugin = plugin;
        voteItem = new ItemBuilder(Material.PAPER).setDisplayName(VOTE_ITEM_NAME).build();
        settingsItem = new ItemBuilder(Material.REDSTONE_COMPARATOR).setDisplayName(SETTINGS_ITEM_NAME).build();
    }

    @EventHandler
    public void handlePlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        plugin.getKarmaManager().loadKarma(p);
        plugin.getStatsManager().loadStats(p);
        if (plugin.getGameStateManager().getCurrentGameState() instanceof LobbyState) {
            if (!((LobbyState) plugin.getGameStateManager().getCurrentGameState()).getCountdown().isRunning()) {
                p.setLevel(60);
                p.setExp(1f);
            }
            p.removePotionEffect(PotionEffectType.INVISIBILITY);
            plugin.getPlayers().add(p);
            e.setJoinMessage(Main.PREFIX + p.getDisplayName() + " §7hat das Spiel betreten.");

            p.getInventory().clear();
            p.getInventory().setHelmet(null);
            p.getInventory().setChestplate(null);
            p.getInventory().setItem(0, voteItem);
            p.getInventory().setItem(4, settingsItem);
            p.setAllowFlight(false);
            for (Player current : Bukkit.getOnlinePlayers()) {
                current.showPlayer(p);
                p.showPlayer(current);
            }

            ConfigLocationUtil locationUtil = new ConfigLocationUtil(plugin, "wartelobby");
            if (locationUtil.loadLocation() != null) {
                p.teleport(locationUtil.loadLocation());
            } else {
                Bukkit.getConsoleSender().sendMessage("§cERROR: No lobby location set");
            }

            Location lobbyloc = new Location(Bukkit.getWorld("world"), 984.5, 4, -529.5, 90, 0);
            p.teleport(lobbyloc);
            LobbyState lobbyState = (LobbyState) plugin.getGameStateManager().getCurrentGameState();
            LobbyCountdown countdown = lobbyState.getCountdown();
            if (plugin.getPlayers().size() >= LobbyState.MIN_PLAYERS)
                if (!countdown.isRunning()) {
                    countdown.stopIdle();
                    countdown.start();
                }

            for (Player current : Bukkit.getOnlinePlayers())
                lobbyState.updateScoreboard(current);
        }
    }

    @EventHandler
    public void handlePlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        plugin.getKarmaManager().saveKarma(p);
        plugin.getStatsManager().saveStats(p);

        if (plugin.getGameStateManager().getCurrentGameState() instanceof LobbyState) {
            plugin.getPlayers().remove(p);
            e.setQuitMessage(Main.PREFIX + p.getDisplayName() + " §7hat das Spiel verlassen.");

            LobbyState lobbyState = (LobbyState) plugin.getGameStateManager().getCurrentGameState();
            LobbyCountdown countdown = lobbyState.getCountdown();
            if (plugin.getPlayers().size() < LobbyState.MIN_PLAYERS) {
                if (countdown.isRunning()) {
                    countdown.stop();
                    countdown.startIdle();
                }
            }

            Voting voting = plugin.getVoting();
            if (voting.getPlayerVotes().containsKey(p.getName())) {
                voting.getMaps().get(voting.getPlayerVotes().get(p.getName())).removeVote();
                voting.getPlayerVotes().remove(p.getName());
                voting.initVotingInv();
            }
            for (Player current : Bukkit.getOnlinePlayers())
                lobbyState.updateScoreboard(current);
        }
    }

}
