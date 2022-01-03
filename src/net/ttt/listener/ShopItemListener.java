package net.ttt.listener;

import net.ttt.gamestates.IngameState;
import net.ttt.main.Main;
import net.ttt.role.HealingStation;
import net.ttt.role.Role;
import net.ttt.role.RoleInventories;
import net.ttt.util.ItemBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static net.ttt.role.RoleInventories.*;

public class ShopItemListener implements Listener {

    private Main plugin;
    private ArrayList<Player> tempPlayers;
    private HashMap<String, Location> tntlocations, teleporterLocations, fakeChests;
    private HashMap<String, Long> cooldown;
    private ArrayList<Integer> poisonArrows, oneShotArrows;
    private static final String C4_TRIGGER_NAME = "§4C4 §bDetonater";
    private HashMap<Player, Role> compassSetting;
    private ArrayList<Location> traitorDetectorLocations;

    public ShopItemListener(Main plugin) {
        this.plugin = plugin;
        fakeChests = new HashMap<>();
        cooldown = new HashMap<>();
        tempPlayers = new ArrayList<>();
        tntlocations = new HashMap<>();
        teleporterLocations = new HashMap<>();
        compassSetting = new HashMap<>();
        poisonArrows = new ArrayList<>();
        oneShotArrows = new ArrayList<>();
        traitorDetectorLocations = new ArrayList<>();
        compassAndDetectorUpdate();
    }

    @EventHandler
    public void handleCreeperArrowShop(ProjectileHitEvent e) {
        if (e.getEntity().getType() != EntityType.ARROW) return;
        if (!(e.getEntity().getShooter() instanceof Player)) return;
        Player p = (Player) e.getEntity().getShooter();
        if (plugin.getRoleManager().getPlayerRole(p) != Role.TRAITOR) return;

        if (RoleInventories.removeMaterialItem(p, Material.MONSTER_EGG)) {
            World world = e.getEntity().getWorld();
            world.spawnEntity(e.getEntity().getLocation(), EntityType.CREEPER);
            e.getEntity().remove();
        }
    }

    @EventHandler
    public void handlePoisonArrowShot(EntityShootBowEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        //if (plugin.getRoleManager().getPlayerRole(p) != Role.TRAITOR) return;
        if (!e.getBow().hasItemMeta()) return;
        if (e.getBow().getItemMeta().getDisplayName().equals(GIFT_BOGEN) && plugin.getRoleManager().getPlayerRole(p) == Role.TRAITOR)
            poisonArrows.add(e.getProjectile().getEntityId());
        else if (e.getBow().getItemMeta().getDisplayName().equals(ONE_SHOT_BOW)) {
            oneShotArrows.add(e.getProjectile().getEntityId());
        }
    }

    @EventHandler
    public void handleArrowHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Arrow && e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if (p.getItemInHand() != null && p.getItemInHand().hasItemMeta()) {
            if (p.getItemInHand().getItemMeta().getDisplayName().equals(ARROW_SHIELD)) {
                e.setDamage(0);
                return;
            }
        }
        if (poisonArrows.contains(e.getDamager().getEntityId())) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
        } else if (oneShotArrows.contains(e.getDamager().getEntityId())) {
            e.setDamage(50);
        }
    }

    @EventHandler
    public void handleCompassTrack(PlayerInteractEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof IngameState)) return;
        if (e.getItem() == null || !e.getItem().hasItemMeta() || e.getItem().getItemMeta().getDisplayName() == null) return;

        if (e.getItem().getItemMeta().getDisplayName().equals(RADAR)) {
            if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_AIR) return;
            Inventory inv = Bukkit.createInventory(null, 3 * 9, "select");
            if (plugin.getRoleManager().getPlayerRole(e.getPlayer()) == Role.TRAITOR) {
                inv.setItem(10, new ItemBuilder(Material.WOOL, (short) 5).setDisplayName("§aInnocent").build());
                inv.setItem(12, new ItemBuilder(Material.WOOL, (short) 11).setDisplayName("§9Detective").build());
                inv.setItem(14, new ItemBuilder(Material.WOOL, (short) 14).setDisplayName("§4Traitor").build());
                inv.setItem(16, new ItemBuilder(Material.WOOL, (short) 8).setDisplayName("§7Anyone").build());
            } else {
                inv.setItem(11, new ItemBuilder(Material.WOOL, (short) 5).setDisplayName("§aInnocent").build());
                inv.setItem(13, new ItemBuilder(Material.WOOL, (short) 11).setDisplayName("§9Detective").build());
                inv.setItem(15, new ItemBuilder(Material.WOOL, (short) 8).setDisplayName("§7Anyone").build());
            }
            e.getPlayer().openInventory(inv);
        } else if (e.getItem().getItemMeta().getDisplayName().equals(RANDOM_TESTER)) {
            if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_AIR) return;
            Player p = e.getPlayer();
            p.sendMessage("started random testing...");

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    Player p = plugin.getPlayers().get(new Random().nextInt(plugin.getPlayers().size()));
                    Role role = plugin.getRoleManager().getPlayerRole(p);
                    Bukkit.broadcastMessage("player " + role.getChatColor() + p.getName() + " §ewurde vom random tester überprüft");
                }
            }, 20 * 30);

            if (p.getItemInHand().getAmount() == 1)
                p.getInventory().setItemInHand(null);
            else
                p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
        }
    }

    @EventHandler
    public void handleCompassClick(InventoryClickEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof IngameState)) return;
        if (e.getClickedInventory() == null || e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;
        if (!e.getClickedInventory().getName().equals("select")) return;
        Player p = (Player) e.getWhoClicked();
        e.setCancelled(true);
        switch (e.getCurrentItem().getItemMeta().getDisplayName()) {
            case "§aInnocent":
                compassSetting.put(p, Role.INNOCENT);
                break;
            case "§9Detective":
                compassSetting.put(p, Role.DETECTIVE);
                break;
            case "§4Traitor":
                compassSetting.put(p, Role.TRAITOR);
                break;
            default:
                compassSetting.put(p, null);
                break;
        }
        if (compassSetting.get(p) == null)
            p.sendMessage(Main.PREFIX + "now tracking anyone");
        else
            p.sendMessage(Main.PREFIX + "now tracking" + compassSetting.get(p).getChatColor() + compassSetting.get(p).getName());
        p.closeInventory();
    }

    private void compassAndDetectorUpdate() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (!(plugin.getGameStateManager().getCurrentGameState() instanceof IngameState)) return;
                for (Location b : traitorDetectorLocations) {
                    boolean traitor = false;
                    for (Entity current : b.getWorld().getNearbyEntities(b, 15, 15, 15)) {
                        if (!(current instanceof Player)) continue;
                        Player p = (Player) current;
                        if (!plugin.getPlayers().contains(p)) continue;
                        if (plugin.getRoleManager().getPlayerRole(p) == Role.TRAITOR)
                            traitor = true;
                    }
                    if (traitor) {
                        b.getWorld().getBlockAt(b).setType(Material.REDSTONE_LAMP_ON);

                        Block old = b.getWorld().getBlockAt(new Location(b.getWorld(), b.getX(), b.getY() - 1, b.getZ()));
                        Material oldType = old.getType();
                        old.setType(Material.REDSTONE_BLOCK);
                        old.setType(oldType);
                    }
                    else
                        b.getWorld().getBlockAt(b).setType(Material.REDSTONE_LAMP_OFF);
                }
                for (Player p : plugin.getPlayers()) {
                    if (!compassSetting.containsKey(p)) continue;
                    Player t = getNearestPlayer(p, compassSetting.get(p));
                    if (t != null)
                        p.setCompassTarget(t.getLocation());
                    else
                        p.setCompassTarget(p.getLocation());
                }
            }
        }, 0, 20);
    }

    private Player getNearestPlayer(Player p, Role role) {
        Player nearestPlayer = null;
        double closest = Double.MAX_VALUE;
        for (Player current : Bukkit.getOnlinePlayers()) {
            Role currentRole = plugin.getRoleManager().getPlayerRole(current);
            if (plugin.getRoleManager().getPlayerRole(p) != Role.TRAITOR)
                currentRole = Role.INNOCENT;
            double distance;
            if (p == current || !((distance = p.getLocation().distance(current.getPlayer().getLocation())) < closest)) continue;
            if ((role != null && role != currentRole)) continue;

            nearestPlayer = current;
            closest = distance;
        }
        return nearestPlayer;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockRedstoneEvent(BlockRedstoneEvent ev) {
        if (ev.getBlock().getType() == Material.REDSTONE_LAMP_ON) {
            ev.setNewCurrent(15);
        }
    }

    @EventHandler
    public void handleMobileTest(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof Player) {
            Player p = e.getPlayer();
            if (p.getItemInHand() == null || !p.getItemInHand().hasItemMeta()) return;
            if (p.getItemInHand().getItemMeta().getDisplayName().equals(MOBILER_TESTER)) {
                Role role = plugin.getRoleManager().getPlayerRole((Player) e.getRightClicked());
                p.sendMessage("Das ist ein " + role.getChatColor() + role.getName());

                if (p.getItemInHand().getAmount() == 1)
                    p.getInventory().setItemInHand(null);
                else
                    p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void handleShopPlace(BlockPlaceEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof IngameState)) return;
        Player p = e.getPlayer();
        Material type = e.getBlock().getType();
        if (type == Material.BEACON) {
            if (plugin.getRoleManager().getPlayerRole(p) != Role.DETECTIVE) return;
            e.setCancelled(false);
            new HealingStation(plugin, e.getBlock().getLocation());
        } else if (type == Material.REDSTONE_LAMP_OFF) {
            if (plugin.getRoleManager().getPlayerRole(p) != Role.DETECTIVE) return;
            traitorDetectorLocations.add(e.getBlock().getLocation());
            e.getBlock().getLocation().getWorld().getBlockAt(e.getBlock().getLocation()).setType(Material.REDSTONE_LAMP_OFF);
            e.setCancelled(false);
        } else if (type == Material.TNT) {
            if (plugin.getRoleManager().getPlayerRole(p) != Role.TRAITOR) return;
            e.setCancelled(false);
            tntlocations.put(p.getName(), e.getBlockPlaced().getLocation());
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    p.getInventory().addItem(new ItemBuilder(Material.REDSTONE_TORCH_ON).setDisplayName(C4_TRIGGER_NAME).build());
                }
            }, 1);
        } else if (type == Material.CHEST) {
            if (plugin.getRoleManager().getPlayerRole(p) != Role.TRAITOR) return;
            e.setCancelled(false);
            fakeChests.put(p.getName(), e.getBlockPlaced().getLocation());
        }
    }

    @EventHandler
    public void onShopInteract(PlayerInteractEvent e) {
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof IngameState)) return;
        Action a = e.getAction();
        if (e.getPlayer() == null) return;
        Player p = e.getPlayer();
        if (!p.getItemInHand().hasItemMeta()) return;
        switch (p.getItemInHand().getItemMeta().getDisplayName()) {
            case C4_TRIGGER_NAME:
                if (!(a == Action.RIGHT_CLICK_BLOCK || a == Action.RIGHT_CLICK_AIR)) return;
                if (!tntlocations.containsKey(p.getName())) return;
                Location loc = tntlocations.get(p.getName());
                tntlocations.remove(p.getName());
                if (p.getItemInHand().getAmount() == 1)
                    p.getInventory().setItemInHand(null);
                else
                    p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
                loc.getBlock().setType(Material.AIR);
                p.getItemInHand().setAmount(0);
                Entity tnt = p.getWorld().spawn(loc, TNTPrimed.class);
                ((TNTPrimed) tnt).setFuseTicks(0);
                break;
            case TAUSCHER:
                if (!(a == Action.RIGHT_CLICK_BLOCK || a == Action.RIGHT_CLICK_AIR)) return;
                e.setCancelled(true);
                if (p.getItemInHand().getAmount() == 1)
                    p.getInventory().setItemInHand(null);
                else
                    p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
                p.updateInventory();
                Location swapperLoc = p.getLocation();
                tempPlayers.addAll(plugin.getPlayers());
                tempPlayers.remove(p);
                Player t = tempPlayers.get(new Random().nextInt(tempPlayers.size()));
                p.teleport(t.getLocation());
                t.teleport(swapperLoc);
                p.getWorld().playSound(p.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
                t.getWorld().playSound(t.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
                break;
            case TELEPORTER:
                if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
                    if (cooldown.containsKey(p.getName())) {
                        if (cooldown.get(p.getName()) + 2 >= System.currentTimeMillis() / 1000) {
                            p.sendMessage(Main.PREFIX + "wait for cooldown");
                            return;
                        } else
                            cooldown.remove(p.getName());
                    }
                    p.getWorld().playSound(p.getLocation(), Sound.NOTE_PLING, 1, 1);
                    teleporterLocations.put(p.getName(), p.getLocation());
                    p.sendMessage(Main.PREFIX + "§7du hast eine teleport location gesetzt");
                    cooldown.put(p.getName(), System.currentTimeMillis() / 1000);
                } else if (a == Action.LEFT_CLICK_AIR || a == Action.LEFT_CLICK_BLOCK) {
                    if (teleporterLocations.containsKey(p.getName())) {
                        p.teleport(teleporterLocations.get(p.getName()));
                        teleporterLocations.remove(p.getName());
                        p.getWorld().playSound(p.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
                        p.sendMessage(Main.PREFIX + "teleportet using teleporter");
                        if (p.getItemInHand().getAmount() == 1)
                            p.getInventory().setItemInHand(null);
                        else
                            p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
                    } else
                        p.sendMessage(Main.PREFIX + "keine location gesetzt");
                }
//                if (teleporterLocations.containsKey(p.getName())) {
//                    if (p.getItemInHand().getAmount() == 1)
//                        p.getInventory().setItemInHand(null);
//                    else
//                        p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
//                    p.teleport(teleporterLocations.get(p.getName()));
//                    teleporterLocations.remove(p.getName());
//                } else
//                    teleporterLocations.put(p.getName(), p.getLocation());
                break;
            case BOOM_BODY:
                if (!(a == Action.RIGHT_CLICK_BLOCK || a == Action.RIGHT_CLICK_AIR)) return;
                Bukkit.broadcastMessage("boom body place");
            default:
                break;
        }
    }

    @EventHandler
    public void onTNTExplode(EntityExplodeEvent e) {
        e.setCancelled(true);
    }

    public HashMap<String, Location> getFakeChests() {
        return fakeChests;
    }

    public void addToCompass(Player p) {
        compassSetting.put(p, null);
    }

    // ENDERMAN_TELEPORT sound

}
