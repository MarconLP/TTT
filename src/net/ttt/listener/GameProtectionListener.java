package net.ttt.listener;

import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;
import net.ttt.gamestates.IngameState;
import net.ttt.gamestates.LobbyState;
import net.ttt.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.ArrayList;
import java.util.HashMap;

public class GameProtectionListener implements Listener {

    private Main plugin;
    private HashMap<Player, Location> playerDeathLoc;

    public GameProtectionListener(Main plugin) {
        this.plugin = plugin;
        playerDeathLoc = new HashMap<>();

        for (World current : Bukkit.getWorlds()) {
            for (Entity currente : current.getEntities())
                if (!(currente instanceof Player))
                    currente.remove();
        }
    }

    @EventHandler
    public void handlePlayerBuild(BlockPlaceEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void handlePlayerBreak(BlockBreakEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void handleExplosion(EntityExplodeEvent e) {
        e.blockList().clear();
    }

    @EventHandler
    public void onServerPing(ServerListPingEvent e) {
        if (plugin.getGameStateManager().getCurrentGameState() instanceof LobbyState) {
            e.setMotd(((LobbyState) plugin.getGameStateManager().getCurrentGameState()).getMap());
            e.setMaxPlayers(LobbyState.MAX_PLAYERS);
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e) {
        e.setCancelled(e.toWeatherState());
    }

    @EventHandler
    public void handleFallDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            if (!(e.getCause() == EntityDamageEvent.DamageCause.FALL)) return;
            if (!(plugin.getGameStateManager().getCurrentGameState() instanceof IngameState)) {
                e.setCancelled(true);
                return;
            } else if (((IngameState) plugin.getGameStateManager().getCurrentGameState()).isGrace()) {
                e.setCancelled(true);
                return;
            }
            e.setCancelled(false);
        } else if (e.getEntity() instanceof Zombie) {
            if (e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
                    e.getCause() == EntityDamageEvent.DamageCause.LAVA ||
                    e.getCause() == EntityDamageEvent.DamageCause.FIRE) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity() instanceof Creeper) {
            e.setDroppedExp(0);
            e.getDrops().clear();
        }
    }

    @EventHandler
    public void handlePlayerDrop(PlayerDropItemEvent e) {
        if (plugin.getGameStateManager().getCurrentGameState() instanceof IngameState) {
            Material material = e.getItemDrop().getItemStack().getType();
            if (material == Material.LEATHER_CHESTPLATE || material == Material.STICK || material == Material.CHEST) {
                e.setCancelled(true);
            } else
                e.setCancelled(false);
        } else
            e.setCancelled(true);
    }

    @EventHandler
    public void handleInvClick(InventoryClickEvent e) {
        if (plugin.getGameStateManager().getCurrentGameState() instanceof IngameState) {
            if (!(e.getWhoClicked() instanceof Player)) return;
            if (e.getCurrentItem() == null) return;
            if (e.getCurrentItem().getType() == Material.LEATHER_CHESTPLATE || e.getCurrentItem().getType() == Material.CHEST) {
                e.setCancelled(true);
            }
        } else
            e.setCancelled(true);
    }

    @EventHandler
    public void handleZombieBurn(EntityCombustEvent e) {
        if (e.getEntityType().equals(EntityType.ZOMBIE)){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void handleFoodLevelChange(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void handleCreatureSpawnEvent(CreatureSpawnEvent e) {
        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL)
            e.setCancelled(true);
    }

    @EventHandler
    public void handleBedEnter(PlayerBedEnterEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void handleDamage(EntityDamageByEntityEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof IngameState)) {
            e.setCancelled(true);
            return;
        }

        IngameState ingameState = (IngameState) plugin.getGameStateManager().getCurrentGameState();
        if (ingameState.isGrace())
            e.setCancelled(true);
        if (!(e.getDamager() instanceof Player)) return;
        Player p = (Player) e.getDamager();
        if (ingameState.getSpectators().contains(p))
            e.setCancelled(true);
    }

    @EventHandler
    public void handlePlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        playerDeathLoc.put(p, p.getLocation());
        PacketPlayInClientCommand packet = new PacketPlayInClientCommand(PacketPlayInClientCommand.EnumClientCommand.PERFORM_RESPAWN);
        ((CraftPlayer) p).getHandle().playerConnection.a(packet);
        p.teleport(playerDeathLoc.get(p));
    }
}
