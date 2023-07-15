package org.minejewels.jewelscobblecubes.listeners;

import com.cryptomorin.xseries.XMaterial;
import com.lewdev.probabilitylib.ProbabilityCollection;
import net.abyssdev.abysslib.listener.AbyssListener;
import net.abyssdev.abysslib.location.LocationSerializer;
import net.abyssdev.abysslib.nbt.NBTUtils;
import net.abyssdev.abysslib.placeholder.PlaceholderReplacer;
import net.abyssdev.abysslib.utils.Region;
import net.abyssdev.abysslib.utils.Utils;
import net.abyssdev.me.lucko.helper.Events;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.minejewels.jewelscobblecubes.JewelsCobbleCubes;
import org.minejewels.jewelscobblecubes.cube.CobbleCube;
import org.minejewels.jewelscobblecubes.cube.block.CobbleCubeBlock;
import org.minejewels.jewelscobblecubes.cube.player.PlayerCobbleCube;
import org.minejewels.jewelscobblecubes.events.CobbleCubePlaceEvent;
import org.minejewels.jewelscobblecubes.utils.RegionUtils;
import org.minejewels.jewelsrealms.events.RealmPlaceEvent;

import java.util.Collection;
import java.util.Map;

public final class PlaceListener extends AbyssListener<JewelsCobbleCubes> {

    public PlaceListener(final JewelsCobbleCubes plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlace(final RealmPlaceEvent event) {

        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final ItemStack item = player.getItemInHand();
        final Location location = event.getEvent().getBlockPlaced().getLocation();
        final Vector direction = player.getLocation().getDirection();
        final double distance = 1.5; // Adjust this value as needed

        // Calculate the spawn location based on the player's direction
        final Location spawnLocation = location.add(direction.multiply(distance));

        spawnLocation.setYaw(player.getLocation().getYaw());
        spawnLocation.add(0, 1, 0);

        if (!NBTUtils.get().contains(item, "COBBLECUBE")) {
            return;
        }

        final String cobbleCubeType = NBTUtils.get().getString(item, "COBBLECUBE").toLowerCase();

        CobbleCube cobbleCube = this.plugin.getCubeRegistry().get(cobbleCubeType).get();

        event.getEvent().getBlock().setType(cobbleCube.getOutlineBlock());
        event.setCancelled(true);

        final Location end = spawnLocation.clone().add(
                cobbleCube.getLength(),
                cobbleCube.getHeight(),
                cobbleCube.getWidth()
        );

        final Region newRegion = new Region(spawnLocation, end);

        final Region shrunkRegion = new Region(spawnLocation, end).shrink(1);

        for (Block block : RegionUtils.getBlocksWithinRegion(newRegion)) {

            if (block.getType() == Material.AIR) continue;
            if (block.getLocation() == spawnLocation) continue;

            this.plugin.getMessageCache().sendMessage(player, "messages.blocks-in-way", new PlaceholderReplacer()
                    .addPlaceholder("%x%", String.valueOf(block.getX()))
                    .addPlaceholder("%y%", String.valueOf(block.getY()))
                    .addPlaceholder("%z%", String.valueOf(block.getZ())));

            return;
        }

        final Collection<Location> blockLocations = RegionUtils.getLocationsWithinRegion(shrunkRegion);
        final ProbabilityCollection<XMaterial> probabilityCollection = new ProbabilityCollection<>();

        for (Map.Entry<Double, CobbleCubeBlock> entry : cobbleCube.getBlocks().getMap().entrySet()) {
            probabilityCollection.add(XMaterial.matchXMaterial(entry.getValue().getMaterial()), (int) Math.round(entry.getKey()));
        }

        this.plugin.getBlockHandler().setRandomBlocks(
                blockLocations,
                probabilityCollection
        );

        this.plugin.getBlockHandler().setBlocksFast(RegionUtils.getEdgesOfRegion(newRegion), XMaterial.matchXMaterial(cobbleCube.getOutlineBlock()));

        this.plugin.getMessageCache().sendMessage(player, "messages.placed-cube");

        final PlayerCobbleCube playerCobbleCube = new PlayerCobbleCube(
                LocationSerializer.serialize(spawnLocation),
                cobbleCubeType,
                this.plugin
        );

        this.plugin.getCachedCubeService().add(playerCobbleCube);
        this.plugin.getCubeStorage().save(playerCobbleCube);

        Utils.removeItemsFromHand(player, 1, false);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run(){
                spawnLocation.getBlock().setType(Material.BEDROCK);
            }
        }, 1L);

        final CobbleCubePlaceEvent placeEvent = new CobbleCubePlaceEvent(
                player,
                playerCobbleCube
        );

        Events.call(placeEvent);
    }

    @EventHandler
    public void onCobblePlace(final RealmPlaceEvent event) {

        final Player player = event.getPlayer();
        final Location location = event.getEvent().getBlock().getLocation();

        plugin.getCachedCubeService().iterate(cube -> {

            if (!cube.getCubeRegion().isInside(location)) return;
            if (!cube.getOutlineRegion().isInside(location)) return;

            plugin.getMessageCache().sendMessage(player, "messages.cannot-interact");

            event.setCancelled(true);
        });
    }
}

