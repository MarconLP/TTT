package net.ttt.role;

import net.ttt.main.Main;
import net.ttt.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class RoleInventories implements Listener {

    public static final String TRAITOR_SHOP_ITEM = "§rShop",
            DETECTIVE_SHOP_ITEM = "§rShop",
            TRAITOR_SHOP_TITLE = TRAITOR_SHOP_ITEM,
            DETECTIVE_SHOP_TITLE = RoleInventories.DETECTIVE_SHOP_ITEM,

            INSTANT_KNIFE = "§eInstant§8-§eKnife",
            C4 = "§4C4",
            TAUSCHER = "§5Tauscher",
            TRAP_SPOOFER = "§aTrap§8-§aSpoofer",
            TELEPORTER = "§5Teleporter",
            BOOM_BODY = "§9Boom§8-§9Body",
            RADAR = "§9Kompass",
            FAKE_CHEST = "§eFake§8-§eChest",
            FLARE_GUN = "§eFlare§8-§eGun",
            GIFT_BOGEN = "§2Gift§8-§2Bogen",
            TRAITOR_CHECKER = "§bTraitor§8-§bDetector",
            ONE_SHOT_BOW = "§cOne§8-§cShot§8-§cBogen",
            MOBILER_TESTER = "§eMobiler§8-§eTester",
            SUPER_IDENTIFICATOR = "§5Super§8-§5Identificator",
            RANDOM_TESTER = "§7Random§8-§7Tester",
            ARROW_SHIELD = "§eShield",
            CREEPER_ARROWS = "§aCreeper§8-§aArrows",
            INNOCENT_TICKET = "§aInnocent§8-§aTicket",
            HEAL_STATION = "§aHealstation";

    private Main plugin;
    private ItemStack traitorItem, detectiveItem;
    private Inventory traitorShop, detectiveShop;
    private PointManager pointManager;

    private ArrayList<Player> trapSpoofPlayers;

    public RoleInventories(Main plugin) {
        this.plugin = plugin;
        traitorItem = new ItemBuilder(Material.CHEST).setDisplayName(RoleInventories.TRAITOR_SHOP_ITEM).build();
        detectiveItem = new ItemBuilder(Material.CHEST).setDisplayName(RoleInventories.DETECTIVE_SHOP_ITEM).build();

        traitorShop = Bukkit.createInventory(null, 9*3, TRAITOR_SHOP_TITLE);
        detectiveShop = Bukkit.createInventory(null, 9*3, DETECTIVE_SHOP_TITLE);

        pointManager = new PointManager();

        trapSpoofPlayers = new ArrayList<>();

        fillInv();
    }

    @EventHandler
    public void handleShopBuy(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();

        ItemStack item = e.getCurrentItem();
        if (item == null) return;
        if (item.getItemMeta() == null) return;
        if (item.getItemMeta().getDisplayName() == null) return;

        Role role = plugin.getRoleManager().getPlayerRole(p);
        if (e.getInventory().getTitle().equals(RoleInventories.TRAITOR_SHOP_TITLE) || e.getInventory().getTitle().equals(RoleInventories.DETECTIVE_SHOP_TITLE)) {
            if (role == Role.TRAITOR) {
                e.setCancelled(true);
                if (item.getItemMeta().getLore() == null) return;
                switch (item.getItemMeta().getDisplayName()) {
                    case CREEPER_ARROWS:
                        if (!removePoints(p, 2)) return;
                        p.getInventory().addItem(new ItemBuilder(Material.MONSTER_EGG, 3, (short) 50).setDisplayName(RoleInventories.CREEPER_ARROWS).build());
                        p.closeInventory();
                        break;
                    case INNOCENT_TICKET:
                        if (!removePoints(p, 5)) return;
                        p.getInventory().addItem(new ItemBuilder(Material.STAINED_GLASS, (short) 5).setDisplayName(INNOCENT_TICKET).build());
                        p.closeInventory();
                        break;
                    case INSTANT_KNIFE:
                        if (!removePoints(p, 5)) return;
                        p.getInventory().addItem(new ItemBuilder(Material.DIAMOND_SWORD).setDisplayName(INSTANT_KNIFE).setDurability((short) 1561).build());
                        p.closeInventory();
                        break;
                    case TRAP_SPOOFER:
                        if (!removePoints(p, 1)) return;
                        if (trapSpoofPlayers.contains(p)) {
                            trapSpoofPlayers.remove(p);
                            p.sendMessage(Main.PREFIX + "§7Du hast die §eFalle §7für dich nun wieder §aaktiviert");
                        }
                        else {
                            trapSpoofPlayers.add(p);
                            p.sendMessage(Main.PREFIX + "§7Du hast die §eFalle §7für dich nun §cdeaktiviert");
                        }
                        p.closeInventory();
                        break;
                    case C4:
                        if (!removePoints(p, 3)) return;
                        p.getInventory().addItem(new ItemBuilder(Material.TNT).setDisplayName(C4).build());
                        p.closeInventory();
                        break;
                    case TELEPORTER:
                        if (!removePoints(p, 3)) return;
                        p.getInventory().addItem(new ItemBuilder(Material.ENDER_PORTAL_FRAME).setDisplayName(TELEPORTER).build());
                        p.closeInventory();
                        break;
                    case BOOM_BODY:
                        if (!removePoints(p, 3)) return;
                        p.getInventory().addItem(new ItemBuilder(Material.MONSTER_EGG, (short) 54).setDisplayName(BOOM_BODY).build());
                        p.closeInventory();
                        break;
                    case FAKE_CHEST:
                        if (!removePoints(p, 2)) return;
                        p.getInventory().addItem(new ItemBuilder(Material.CHEST).setDisplayName(FAKE_CHEST).build());
                        p.closeInventory();
                        break;
                    case FLARE_GUN:
                        if (!removePoints(p, 2)) return;
                        p.getInventory().addItem(new ItemBuilder(Material.BOW).setDisplayName(FLARE_GUN).addEntchant(Enchantment.ARROW_FIRE, 1).setDurability((short) 379).build());
                        p.closeInventory();
                        break;
                    case TAUSCHER:
                        if (!removePoints(p, 3)) return;
                        p.getInventory().addItem(new ItemBuilder(Material.ENDER_PEARL).setDisplayName(TAUSCHER).build());
                        p.closeInventory();
                        break;
                    case RADAR:
                        if (!removePoints(p, 2)) return;
                        p.getInventory().addItem(new ItemBuilder(Material.COMPASS).setDisplayName(RADAR).build());
                        plugin.getShopItemListener().addToCompass(p);
                        p.closeInventory();
                        break;
                    case GIFT_BOGEN:
                        if (!removePoints(p, 3)) return;
                        p.getInventory().addItem(new ItemBuilder(Material.BOW).setDisplayName(GIFT_BOGEN).build());
                        p.closeInventory();
                        break;
                    default:
                        break;
                }
            } else if (role == Role.DETECTIVE) {
                e.setCancelled(true);
                switch (item.getItemMeta().getDisplayName()) {
                    case HEAL_STATION:
                        if (!removePoints(p, 2)) return;
                        p.getInventory().addItem(new ItemBuilder(Material.BEACON).setDisplayName(RoleInventories.HEAL_STATION).build());
                        p.closeInventory();
                        break;
                    case RADAR:
                        if (!removePoints(p, 2)) return;
                        p.getInventory().addItem(new ItemBuilder(Material.COMPASS).setDisplayName(RADAR).build());
                        plugin.getShopItemListener().addToCompass(p);
                        p.closeInventory();
                        break;
                    case ARROW_SHIELD:
                        if (!removePoints(p, 2)) return;
                        p.getInventory().addItem(new ItemBuilder(Material.BANNER).setDisplayName(ARROW_SHIELD).build());
                        p.closeInventory();
                        break;
                    case TAUSCHER:
                        if (!removePoints(p, 3)) return;
                        p.getInventory().addItem(new ItemBuilder(Material.ENDER_PEARL).setDisplayName(TAUSCHER).build());
                        p.closeInventory();
                        break;
                    case TELEPORTER:
                        if (!removePoints(p, 3)) return;
                        p.getInventory().addItem(new ItemBuilder(Material.ENDER_PORTAL_FRAME).setDisplayName(TELEPORTER).build());
                        p.closeInventory();
                        break;
                    case SUPER_IDENTIFICATOR:
                        if (!removePoints(p, 2)) return;
                        p.getInventory().addItem(new ItemBuilder(Material.STICK).addEntchant(Enchantment.LUCK, 1).hideEnchantmencht().setDisplayName(SUPER_IDENTIFICATOR).build());
                        p.closeInventory();
                        break;
                    case TRAITOR_CHECKER:
                        if (!removePoints(p, 4)) return;
                        p.getInventory().addItem(new ItemBuilder(Material.REDSTONE_LAMP_OFF).setDisplayName(TRAITOR_CHECKER).build());
                        p.closeInventory();
                        break;
                    case ONE_SHOT_BOW:
                        if (!removePoints(p, 4)) return;
                        p.getInventory().addItem(new ItemBuilder(Material.BOW).setDisplayName(ONE_SHOT_BOW).build());
                        p.closeInventory();
                        break;
                    case RANDOM_TESTER:
                        if (!removePoints(p, 4)) return;
                        p.getInventory().addItem(new ItemBuilder(Material.IRON_INGOT).setDisplayName(RANDOM_TESTER).build());
                        p.closeInventory();
                        break;
                    case MOBILER_TESTER:
                        if (!removePoints(p, 4)) return;
                        p.getInventory().addItem(new ItemBuilder(Material.WOOD_AXE).setDisplayName(MOBILER_TESTER).build());
                        p.closeInventory();
                        break;
                    default:
                        break;
                }
            }
        }
        if (e.getCurrentItem().getType() == Material.CHEST && e.getCurrentItem().getItemMeta().getLore() == null) {
            if (role == Role.TRAITOR) {
                p.openInventory(traitorShop);
            } else if (role == Role.DETECTIVE) {
                p.openInventory(detectiveShop);
            }
        }
    }

    private boolean removePoints(Player p, int points) {
        if (pointManager.removePoints(p, points)) {
            return true;
        }
        p.sendMessage(Main.PREFIX + "do you have enough money??");
        return false;
    }

    private void fillInv() {
        traitorShop.setItem(0, new ItemBuilder(Material.MONSTER_EGG, (short) 50).setDisplayName(CREEPER_ARROWS).setLore("§7Schieße Pfeile und spawne Creeper an ", "§7der Stelle, an der die Pfeile aufkommen").build());
        traitorShop.setItem(1, new ItemBuilder(Material.WOOD_BUTTON).setDisplayName(TRAP_SPOOFER).setLore("§7Durch den Kauf kannst du die Falle für ", "§7dich deaktivieren, und erneut aktivieren").build());
        traitorShop.setItem(2, new ItemBuilder(Material.TNT).setDisplayName(C4).setLore("§7Platziere eine Bombe, die du später ", "§7per Fernzünder auslösen kannst").build());
        traitorShop.setItem(3, new ItemBuilder(Material.ENDER_PORTAL_FRAME).setDisplayName(TELEPORTER).setLore("§7Setze eine Position und teleportiere ", "§7dich einmalig dort hin").build());
        traitorShop.setItem(4, new ItemBuilder(Material.DIAMOND_SWORD).setDisplayName(INSTANT_KNIFE).setLore("§7Töte Gegner mit nur einem Schwerthieb").build());
        traitorShop.setItem(5, new ItemBuilder(Material.MONSTER_EGG, (short) 54).setDisplayName(BOOM_BODY).setLore("§7Erschaffe eine Leiche welche explodiert, ", "§7sobald sie von einem Spieler identifiziert wird").build());
        traitorShop.setItem(6, new ItemBuilder(Material.CHEST).setDisplayName(FAKE_CHEST).setLore("§7Wenn du diese Truhe platzierst und ein ", "§7Innocent sie öffnet, wird sie explodieren").build());
        traitorShop.setItem(7, new ItemBuilder(Material.BOW).setDisplayName(FLARE_GUN).addEntchant(Enchantment.ARROW_FIRE, 1).hideEnchantmencht().setLore("§7Ein Bogen mit nur sechs Pfeilen, ", "§7die Spieler in flammen setzten").build());
        traitorShop.setItem(8, new ItemBuilder(Material.STAINED_GLASS, (short) 5).setDisplayName(INNOCENT_TICKET).setLore("§7Wenn du dieses Item nutzt wirst du beim ", "§7nächsten Traitor-Test mit einer 75% ", "§7Chance als Innocent markiert").build());
        traitorShop.setItem(9, new ItemBuilder(Material.ENDER_PEARL).setDisplayName(TAUSCHER).setLore("§7Tausche mit einem zufälligen Spieler den Platz").build());
        traitorShop.setItem(10, new ItemBuilder(Material.COMPASS).setDisplayName(RADAR).setLore("§7Finde Spieler in deiner Nähe").build());
        traitorShop.setItem(11, new ItemBuilder(Material.BOW).setDisplayName(GIFT_BOGEN).setLore("§7Mit diesem Bogen kannst du deine Gegner vergiften.").build());

        detectiveShop.setItem(0, new ItemBuilder(Material.BEACON).setDisplayName(HEAL_STATION).setLore("§7Spieler, die im Umfeld des ", "§7Leuchtfeuers stehen, werden geheilt").build());
        detectiveShop.setItem(1, new ItemBuilder(Material.REDSTONE_LAMP_OFF).setDisplayName(TRAITOR_CHECKER).setLore("§7Der Detector schlägt Alarm, sobald sich ein ", "§7Traitor im Umkreis von 15 Blöcken befindet").build());
        detectiveShop.setItem(2, new ItemBuilder(Material.BOW).setDisplayName(ONE_SHOT_BOW).setLore("§7Mit diesem Bogen kannst du genau einen ", "§7Pfeil abschießen, der immer tötlich ist").build());
        detectiveShop.setItem(3, new ItemBuilder(Material.ENDER_PEARL).setDisplayName(TAUSCHER).setLore("§7Tausche mit einem zufälligen Spieler den Platz").build());
        detectiveShop.setItem(4, new ItemBuilder(Material.ENDER_PORTAL_FRAME).setDisplayName(TELEPORTER).setLore("§7Setze eine Position und teleportiere ", "§7dich einmalig dort hin").build());
        detectiveShop.setItem(5, new ItemBuilder(Material.COMPASS).setDisplayName(RADAR).setLore("§7Finde Spieler in deiner Nähe").build());
        detectiveShop.setItem(6, new ItemBuilder(Material.STICK).setDisplayName(SUPER_IDENTIFICATOR).setLore("§7Mit dem Super-Identificator, ", "§7erhälst du mehr Infos").build());
        detectiveShop.setItem(7, new ItemBuilder(Material.BANNER).setDisplayName(ARROW_SHIELD).setLore("§7Solange du es in der Hand hältst, erleidest ", "§7du durch Pfeile keinen Schaden mehr").build());
        detectiveShop.setItem(8, new ItemBuilder(Material.IRON_INGOT).setDisplayName(RANDOM_TESTER).setLore("§7Testet einen zufälligen Spieler 30 ", "§7Sekunden nach Benutzung des Items").build());
        detectiveShop.setItem(9, new ItemBuilder(Material.WOOD_AXE).setDisplayName(MOBILER_TESTER).setLore("§7Schlage einen Spieler, um ", "§7seine Rolle herauszufinden").build());
    }

    public static boolean removeMaterialItem(Player p, Material material) {
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            if (p.getInventory().getContents() != null && p.getInventory().getContents()[i] != null) {
                if (p.getInventory().getContents()[i].getType() == material) {
                    if (p.getInventory().getContents()[i].getAmount() <= 1)
                        p.getInventory().clear(i);
                    else
                        p.getInventory().getContents()[i].setAmount(p.getInventory().getContents()[i].getAmount() - 1);
                    p.updateInventory();
                    return true;
                }
            }
        }
        return false;
    }

    public ItemStack getTraitorItem() {
        return traitorItem;
    }

    public ItemStack getDetectiveItem() {
        return detectiveItem;
    }

    public Inventory getTraitorShop() {
        return traitorShop;
    }

    public Inventory getDetectiveShop() {
        return detectiveShop;
    }

    public PointManager getPointManager() {
        return pointManager;
    }

    public ArrayList<Player> getTrapSpoofPlayers() {
        return trapSpoofPlayers;
    }
}
