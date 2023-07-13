package org.minejewels.jewelscobblecubes.listeners;

import net.abyssdev.abysslib.listener.AbyssListener;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.minejewels.jewelscobblecubes.JewelsCobbleCubes;
import org.minejewels.jewelscobblecubes.cube.player.PlayerCobbleCube;
import org.minejewels.jewelscobblecubes.menus.MainMenu;
import org.minejewels.jewelsrealms.events.RealmInteractEvent;

public class InteractListener extends AbyssListener<JewelsCobbleCubes> {

    public InteractListener(JewelsCobbleCubes plugin) {
        super(plugin);
    }

    @EventHandler
    public void onInteract(final RealmInteractEvent event) {

        if (event.isCancelled()) return;

        final Player player = event.getPlayer();

        if (event.getEvent().getClickedBlock() == null) return;

        final Location location = event.getEvent().getClickedBlock().getLocation();

        if(event.getEvent().getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getEvent().getAction() != Action.RIGHT_CLICK_BLOCK) return;

        for (final PlayerCobbleCube cube : plugin.getCachedCubeService().getService()) {

            if (!cube.getOutlineRegion().isInside(location)) continue;
            if (cube.getCobbleCube().getOutlineBlock() != event.getEvent().getClickedBlock().getType()) continue;

            new MainMenu(plugin, cube.getCobbleCube()).open(player, cube);

            plugin.getMessageCache().sendMessage(
                    player,
                    "messages.management-menu-opened"
            );

            return;
        }
    }
}
