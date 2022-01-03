package net.ttt.listener;

import net.ttt.gamestates.IngameState;
import net.ttt.main.Main;
import net.ttt.role.Tester;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class TesterListener implements Listener {

    private Main plugin;

    public TesterListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handleTesterClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block clicked = e.getClickedBlock();
        if (!(plugin.getGameStateManager().getCurrentGameState() instanceof IngameState)) return;
        IngameState ingameState = (IngameState) plugin.getGameStateManager().getCurrentGameState();
        if (ingameState.isGrace()) return;

        Tester tester = ingameState.getMap().getTester();
        if (tester.getButton() == null) return;
        if (clicked.getType() == Material.WOOD_BUTTON) {
            if (tester.getButton().getLocation().equals(clicked.getLocation()))
                tester.test(e.getPlayer());
        } else if (clicked.getType() == Material.STONE_BUTTON) {
            if (tester.getTrap().getLocation().equals(clicked.getLocation()))
                if (plugin.getRoleInventories().getTrapSpoofPlayers().contains(e.getPlayer())) {
                    e.getPlayer().sendMessage(Main.PREFIX + "trap disabled");
                    return;
                }
                tester.trapTester(e.getPlayer());
        }
    }

}
