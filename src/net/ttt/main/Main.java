package net.ttt.main;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.ttt.commands.SetupCommand;
import net.ttt.commands.StartCommand;
import net.ttt.commands.StatsCommand;
import net.ttt.commands.StatsallCommand;
import net.ttt.gamestates.GameState;
import net.ttt.gamestates.GameStateManager;
import net.ttt.listener.*;
import net.ttt.role.*;
import net.ttt.util.MySQL;
import net.ttt.util.SkullBuilder;
import net.ttt.stats.StatsManager;
import net.ttt.voting.Map;
import net.ttt.voting.Voting;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class Main extends JavaPlugin {

    public static final String PREFIX = "§f[§4TTT§f] §r",
                               NOPERMS = PREFIX + "§cYou don't have permission to do that.";

    private static Main plugin;

    private GameStateManager gameStateManager;
    private ArrayList<Player> players;
    private ArrayList<Map> maps;
    private Voting voting;
    private RoleManager roleManager;
    private GameProtectionListener gameProtectionListener;
    private RoleInventories roleInventories;
    private MySQL mySQL;
    private KarmaManager karmaManager;
    private TokenManager tokenManager;
    private SkullBuilder skullBuilder;
    private ShopItemListener shopItemListener;
    private ProtocolManager protocolManager;
    private StatsManager statsManager;

    @Override
    public void onEnable() {
        plugin = this;

        protocolManager = ProtocolLibrary.getProtocolManager();
        gameStateManager = new GameStateManager(this);
        players = new ArrayList<>();

        gameStateManager.setGameState(GameState.LOBBY_STATE);

        init(Bukkit.getPluginManager());
    }

    private void init(PluginManager pluginManager) {
        initVoting();

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        mySQL = new MySQL(this);
        statsManager = new StatsManager(this);
        karmaManager = new KarmaManager(this);
        tokenManager = new TokenManager(this);
        roleManager = new RoleManager(this);
        gameProtectionListener = new GameProtectionListener(this);
        roleInventories = new RoleInventories(this);
        skullBuilder = new SkullBuilder(this);
        shopItemListener = new ShopItemListener(this);

        mySQL.connect();
        mySQL.createTables();
        statsManager.clearTempStats();

        getCommand("setup").setExecutor(new SetupCommand(this));
        getCommand("start").setExecutor(new StartCommand(this));
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("statsall").setExecutor(new StatsallCommand(this));

        pluginManager.registerEvents(new PlayerLobbyConnectionListener(this), this);
        pluginManager.registerEvents(new VotingListener(this), this);
        pluginManager.registerEvents(new GameProgressListener(this), this);
        pluginManager.registerEvents(gameProtectionListener, this);
        pluginManager.registerEvents(new ChatListener(this), this);
        pluginManager.registerEvents(new ChestListener(this), this);
        pluginManager.registerEvents(new TesterListener(this), this);
        pluginManager.registerEvents(roleInventories, this);
        pluginManager.registerEvents(shopItemListener, this);
        pluginManager.registerEvents(new TokenListener(this), this);
        pluginManager.registerEvents(new SpectatorListener(this), this);
    }

    private void initVoting() {
        maps = new ArrayList<>();
        if (getConfig().getConfigurationSection("Arenas") == null) {
            voting = null;
            return;
        }
        for (String current : getConfig().getConfigurationSection("Arenas").getKeys(false)) {
            Map map = new Map(this, current);
            if (map.playable()) {
                maps.add(map);
            } else
                Bukkit.getConsoleSender().sendMessage(Main.PREFIX + "§4Die map " + map.getName() + " §4ist nicht eingerichtet.");
        }
        if (maps.size() >= Voting.MAP_AMOUNT)
            voting = new Voting(this, maps);
        else {
            Bukkit.getConsoleSender().sendMessage(Main.PREFIX + "§cEs sind nicht genug Maps eingerichtet §7[§c" + maps.size() + "§8/§a" + Voting.MAP_AMOUNT + "§7]");
            voting = null;
        }
    }

    @Override
    public void onDisable() {
        mySQL.close();
    }

    public GameStateManager getGameStateManager() {
        return gameStateManager;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public Voting getVoting() {
        return voting;
    }

    public ArrayList<Map> getMaps() {
        return maps;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public GameProtectionListener getGameProtectionListener() {
        return gameProtectionListener;
    }

    public RoleInventories getRoleInventories() {
        return roleInventories;
    }

    public MySQL getMySQL() {
        return mySQL;
    }

    public KarmaManager getKarmaManager() {
        return karmaManager;
    }

    public TokenManager getTokenManager() {
        return tokenManager;
    }

    public SkullBuilder getSkullBuilder() {
        return skullBuilder;
    }

    public ShopItemListener getShopItemListener() {
        return shopItemListener;
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }
}
