package org.minejewels.jewelscobblecubes.listeners;

import net.abyssdev.abysslib.economy.registry.impl.DefaultEconomyRegistry;
import net.abyssdev.abysslib.fawe.FaweUtils;
import net.abyssdev.abysslib.listener.AbyssListener;
import net.abyssdev.abysslib.location.LocationSerializer;
import net.abyssdev.me.lucko.helper.Events;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.minejewels.jewelscobblecubes.JewelsCobbleCubes;
import org.minejewels.jewelscobblecubes.cube.block.CobbleCubeBlock;
import org.minejewels.jewelscobblecubes.cube.player.PlayerCobbleCube;
import org.minejewels.jewelscobblecubes.events.CobbleCubeBreakEvent;
import org.minejewels.jewelscobblecubes.events.CobbleCubePlaceEvent;
import org.minejewels.jewelscobblecubes.upgrade.CubeUpgrade;
import org.minejewels.jewelsrealms.JewelsRealms;
import org.minejewels.jewelsrealms.events.RealmBreakEvent;

import java.util.Map;
import java.util.Optional;

public class BreakListener extends AbyssListener<JewelsCobbleCubes> {

    public BreakListener(final JewelsCobbleCubes plugin) {
        super(plugin);
    }

    @EventHandler
    public void onBreak(final RealmBreakEvent event) {

        final Player player = event.getEvent().getPlayer();
        final Block block = event.getEvent().getBlock();
        final Location location = block.getLocation();

        if (event.isCancelled()) return;

        final Optional<PlayerCobbleCube> optionalCube = this.plugin.getCachedCubeService().getService()
                .stream()
                .filter(cube -> cube.getCubeRegion().isInside(location))
                .findFirst();

        if (!optionalCube.isPresent()) return;

        final PlayerCobbleCube cube = optionalCube.get();

        event.getEvent().setDropItems(false);
        event.getEvent().setExpToDrop(0);

        cube.getBrokenLocations().add(LocationSerializer.serialize(location));

        if (cube.isAutosellEnabled()) {
            DefaultEconomyRegistry.get().getEconomy("vault").addBalance(player, cube.getCost(block.getType()));

            final CobbleCubeBreakEvent breakEvent = new CobbleCubeBreakEvent(
                    player,
                    cube,
                    event.getEvent()
            );

            Events.call(breakEvent);

            if (breakEvent.isCancelled()) {
                event.setCancelled(true);
            }

            return;
        }

        final CubeUpgrade storageUpgrade = this.plugin.getUpgradeRegistry().get("STORAGE").get();

        final double maxStorage = storageUpgrade.getAmount(cube.getLevel(storageUpgrade));

        long totalBlocks = 0;

        for (Map.Entry<CobbleCubeBlock, Long> entry : cube.getBlockStorage().entrySet()) {
            totalBlocks += entry.getValue();
        }

        if (totalBlocks >= maxStorage) {
            plugin.getMessageCache().sendMessage(player, "messages.max-storage");
            event.setCancelled(true);
            return;
        }

        final CobbleCubeBreakEvent breakEvent = new CobbleCubeBreakEvent(
                player,
                cube,
                event.getEvent()
        );

        Events.call(breakEvent);

        if (breakEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        cube.addDrop(block.getType());
    }
}
