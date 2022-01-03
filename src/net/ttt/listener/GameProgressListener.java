package net.ttt.listener;

import net.minecraft.server.v1_8_R3.EntityZombie;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import net.ttt.gamestates.IngameState;
import net.ttt.main.Main;
import net.ttt.role.Role;
import net.ttt.role.RoleManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import static net.ttt.role.Role.*;

public class GameProgressListener implements Listener {

    private Main plugin;
    private RoleManager roleManager;
    private HashMap<Integer, Player> zombiePlayers;
    private HashMap<Integer, Player> zombiePlayersKiller;
    private HashMap<Integer, Long> zombiePlayersDeathTime;

    public GameProgressListener(Main plugin) {
        this.plugin = plugin;
        roleManager = plugin.getRoleManager();
        zombiePlayers = new HashMap<>();
        zombiePlayersKiller = new HashMap<>();
        zombiePlayersDeathTime = new HashMap<>();
    }

    @EventHandler
    public void handlePlayerDamage(EntityDamageByEntityEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof IngameState)) return;
        if (!(e.getEntity() instanceof Player)) {
            e.setCancelled(true);
            return;
        }
        Player t = (Player) e.getEntity();
        if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION &&
                ((IngameState) plugin.getGameStateManager().getCurrentGameState()).getSpectators().contains(t) ||
                e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION &&
                        plugin.getRoleManager().getPlayerRole(t) == TRAITOR) return;
        if (!(e.getDamager() instanceof Player)) return;
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getDamager();
        Role pRole = roleManager.getPlayerRole(p), tRole = roleManager.getPlayerRole(t);

        if (pRole == Role.DETECTIVE && tRole == Role.DETECTIVE)
            e.setDamage(0);
        if (pRole == TRAITOR && tRole == TRAITOR)
            e.setDamage(0);
        if (pRole == TRAITOR)
            if (p.getItemInHand().getType() == Material.DIAMOND_SWORD)
                e.setDamage(40);
    }

    @EventHandler
    public void handlePlayerDeath(PlayerDeathEvent e) {
        e.setDeathMessage(null);
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof IngameState)) return;
        IngameState ingameState = (IngameState) plugin.getGameStateManager().getCurrentGameState();
        Player t = e.getEntity();
        ingameState.addSpec(t);
        t.getInventory().setChestplate(null);
        t.getInventory().setHelmet(null);
        plugin.getStatsManager().addStats("Deaths", t);
        if (t.getKiller() != null) {
            Player p = t.getKiller();
            Role pRole = roleManager.getPlayerRole(p), tRole = roleManager.getPlayerRole(t);

            plugin.getStatsManager().addStats("Kills", p);

            switch (pRole) {
                case TRAITOR:
                    if (tRole == TRAITOR) {
                        p.sendMessage(Main.PREFIX + "§7Du hast einen " + tRole.getChatColor() + tRole.getName() + " getötet");
                        p.sendMessage(Main.PREFIX + "§c- §e20 Karma §7erhalten");
                        plugin.getKarmaManager().removeKarma(p, 50);
                        plugin.getStatsManager().addStats("failKills", p);
                    } else {
                        p.sendMessage(Main.PREFIX + "§7Du hast einen " + tRole.getChatColor() + tRole.getName() + " getötet");
                        p.sendMessage(Main.PREFIX + "§a+ §e20 Karma §7erhalten");
                        plugin.getKarmaManager().addKarma(p, 10);
                        if (tRole == DETECTIVE) {
                            plugin.getRoleInventories().getPointManager().addPoints(p, 3);
                        } else if (tRole == INNOCENT) {
                            plugin.getRoleInventories().getPointManager().addPoints(p, 1);
                        }
                    }
                    break;
                case INNOCENT: case DETECTIVE:
                    if (tRole == Role.TRAITOR) {
                        p.sendMessage(Main.PREFIX + "§7Du hast einen " + tRole.getChatColor() + tRole.getName() + " getötet");
                        p.sendMessage(Main.PREFIX + "§a+ §e20 Karma §7erhalten");
                        plugin.getRoleInventories().getPointManager().addPoints(p, 2);
                        plugin.getKarmaManager().addKarma(p, 20);
                    } else if (tRole == Role.INNOCENT) {
                        p.sendMessage(Main.PREFIX + "§7Du hast einen " + tRole.getChatColor() + tRole.getName() + " getötet");
                        p.sendMessage(Main.PREFIX + "§c- §e20 Karma §7erhalten");
                        plugin.getKarmaManager().removeKarma(p, 20);
                        plugin.getStatsManager().addStats("failKills", p);
                    } else if (tRole == Role.DETECTIVE) {
                        p.sendMessage(Main.PREFIX + "§7Du hast einen " + tRole.getChatColor() + tRole.getName() + " getötet");
                        p.sendMessage(Main.PREFIX + "§c- §e20 Karma §7erhalten");
                        plugin.getKarmaManager().removeKarma(p, 50);
                        plugin.getStatsManager().addStats("failKills", p);
                    }
                    break;
                default:
                    break;
            }

            t.sendMessage(Main.PREFIX + "§7Du wurdest von " + pRole.getChatColor() + p.getName() + "§7 getötet");
            if (tRole == TRAITOR) plugin.getRoleManager().getTraitorPlayers().remove(t.getName());

        } else {
            t.sendMessage(Main.PREFIX + "§7Du bist §cgestorben");

            if (plugin.getRoleManager().getPlayerRole(t) == TRAITOR)
                plugin.getRoleManager().getTraitorPlayers().remove(t.getName());

        }

        createZombie(t, t.getLocation(), t.getKiller());

        plugin.getPlayers().remove(t);
        ingameState.checkGameEnding();

        for (Player current : Bukkit.getOnlinePlayers()) {
            ingameState.updateScoreboard(current);
        }
        e.getDrops().clear();
    }

    private void createZombie(Player p, Location loc, Player killer) {
        loc = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), 0);
        Zombie z = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);

        zombiePlayers.put(z.getEntityId(), p);
        zombiePlayersKiller.put(z.getEntityId(), killer);
        zombiePlayersDeathTime.put(z.getEntityId(), System.currentTimeMillis());

        Bukkit.broadcastMessage(String.valueOf(z.getEntityId()));

        z.setCustomName("UNDEFINED");

        z.setCustomNameVisible(true);
        z.getEquipment().clear();
        z.setBaby(false);
        z.setVillager(false);
        z.setCanPickupItems(false);
        z.setHealth(20);

//        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity)z).getHandle();
//        NBTTagCompound tag = nmsEntity.getNBTTag();
//        if (tag == null) {
//            tag = new NBTTagCompound();
//        }
//        nmsEntity.c(tag);
//        tag.setInt("NoAI", 1);
//        nmsEntity.f(tag);

        EntityZombie ez = (EntityZombie) ((CraftEntity) z).getHandle();

        List goalB = (List)getPrivateField("b", PathfinderGoalSelector.class, ez.goalSelector); goalB.clear();
        List goalC = (List)getPrivateField("c", PathfinderGoalSelector.class, ez.goalSelector); goalC.clear();
        List targetB = (List)getPrivateField("b", PathfinderGoalSelector.class, ez.targetSelector); targetB.clear();
        List targetC = (List)getPrivateField("c", PathfinderGoalSelector.class, ez.targetSelector); targetC.clear();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getSkullBuilder().getCachedHeadValueByUUID(p.getUniqueId().toString());
            }
        });

        z.getEquipment().setHelmet(plugin.getSkullBuilder().getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDIzZWFlZmJkNTgxMTU5Mzg0Mjc0Y2RiYmQ1NzZjZWQ4MmViNzI0MjNmMmVhODg3MTI0ZjllZDMzYTY4NzJjIn19fQ=="));
    }

    private Object getPrivateField(String fieldName, Class clazz, Object object) {
        Field field;
        Object o = null;
        try {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            o = field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return o;
    }

    @EventHandler
    public void handleZombieScan(PlayerInteractEntityEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof IngameState)) return;
        if (!(e.getRightClicked() instanceof Zombie)) return;
        Player p = e.getPlayer();
        if (!plugin.getPlayers().contains(p)) return;
        if (p.getItemInHand() != null && p.getItemInHand().getType() != null && p.getItemInHand().getType() == Material.STICK) {

            LivingEntity z = (LivingEntity) e.getRightClicked();
            Player t = zombiePlayers.get(z.getEntityId());
            Role role = plugin.getRoleManager().getPlayerRole(t);
            if (!p.getItemInHand().getEnchantments().isEmpty()) {
                p.sendMessage("super identificator");

                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        if (zombiePlayersKiller.get(z.getEntityId()) == null) {
                            p.sendMessage("Er ist einfach so gestorben");
                        } else
                            p.sendMessage("Der killer war " + zombiePlayersKiller.get(z.getEntityId()).getName());
                    }
                }, 100);
            }
            if (z.getCustomName().equals("UNDEFINED")) {
                if (plugin.getRoleManager().getPlayerRole(p) == DETECTIVE) {

                    z.setCustomName(role.getChatColor() + t.getName());
                    if (t.isOnline())
                        z.getEquipment().setHelmet(plugin.getSkullBuilder().HeadItemDisplay(t, "0"));
                    else
                        z.getEquipment().setHelmet(plugin.getSkullBuilder().getCustomTextureHead(plugin.getSkullBuilder().getCachedHeadValueByUUID(t.getUniqueId().toString())));

                    p.sendMessage("SCANNED RIGHT NOW");
                } else {

                    if ((zombiePlayersDeathTime.get(z.getEntityId()) + 40000) > System.currentTimeMillis()) {
                        p.sendMessage(Main.PREFIX + "scanning...");
                        return;
                    }

                    z.setCustomName(role.getChatColor() + t.getName());
                    z.getEquipment().setHelmet(plugin.getSkullBuilder().getCustomTextureHead(plugin.getSkullBuilder().getCachedHeadValueByUUID(t.getUniqueId().toString())));
                    p.sendMessage("wait for scannign / -NWowN-");
                    plugin.getRoleInventories().getPointManager().addPoints(p,
                            Math.round((float) plugin.getRoleInventories().getPointManager().getPoints(t) / 2));
                    p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1, 1);
                }
            } else {
                String name = null;
                if (z.getCustomName().contains("§c")) {
                    name = z.getCustomName().replace("§c", "");

                } else if (z.getCustomName().contains("§a")) {
                    name = z.getCustomName().replace("§a", "");

                } else if (z.getCustomName().contains("§1")) {
                    name = z.getCustomName().replace("§1", "");
                }

                p.sendMessage(Main.PREFIX + "das ist §e" + name);
                p.sendMessage(Main.PREFIX + "er war ein " + role.getChatColor() + role.getName());
            }
        }
    }

    @EventHandler
    public void handlePlayerQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        if ((plugin.getGameStateManager().getCurrentGameState() instanceof IngameState)) {
            createZombie(e.getPlayer(), e.getPlayer().getLocation(), null);
            Player p = e.getPlayer();
            if (plugin.getPlayers().contains(p)) {
                IngameState ingameState = (IngameState) plugin.getGameStateManager().getCurrentGameState();
                plugin.getPlayers().remove(p);
                //e.setQuitMessage(Main.PREFIX + "§7Der spieler §e" + e.getPlayer().getName() + "§7hat das spiel verlassen!");
                ingameState.checkGameEnding();
            }
        }
    }
}
