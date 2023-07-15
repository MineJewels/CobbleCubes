package org.minejewels.jewelscobblecubes.cube.player;

import com.cryptomorin.xseries.XMaterial;
import com.lewdev.probabilitylib.ProbabilityCollection;
import lombok.Getter;
import lombok.Setter;
import net.abyssdev.abysslib.caged.MathUtility;
import net.abyssdev.abysslib.location.LocationSerializer;
import net.abyssdev.abysslib.utils.Region;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.minejewels.jewelscobblecubes.JewelsCobbleCubes;
import org.minejewels.jewelscobblecubes.cube.CobbleCube;
import org.minejewels.jewelscobblecubes.cube.block.CobbleCubeBlock;
import org.minejewels.jewelscobblecubes.upgrade.CubeUpgrade;
import org.minejewels.jewelsrealms.JewelsRealms;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
public final class PlayerCobbleCube {

    private final String location;
    private final String cobbleCubeType;

    private final Map<Material, Long> storage = Maps.mutable.empty();
    private final Set<String> brokenLocations = Sets.mutable.empty();

    private final Map<String, Long> serializedUpgrades = Maps.mutable.empty();

    private boolean autosellPurchased;
    private boolean autosellEnabled;

    private transient Map<CobbleCubeBlock, Long> blockStorage = Maps.mutable.empty();

    private transient Location bukkitLocation;
    private transient CobbleCube cobbleCube;
    private transient Region outlineRegion, cubeRegion;
    private transient Map<CubeUpgrade, Long> upgrades = Maps.mutable.empty();

    public PlayerCobbleCube(final String location, final String cobbleCubeType, final JewelsCobbleCubes plugin) {
        this.location = location;
        this.cobbleCubeType = cobbleCubeType;

        this.bukkitLocation = LocationSerializer.deserialize(location);
        this.cobbleCube = plugin.getCubeRegistry().get(cobbleCubeType).get();

        this.autosellPurchased = false;
        this.autosellEnabled = false;

        final Location end = bukkitLocation.clone().add(
                cobbleCube.getLength(),
                cobbleCube.getHeight(),
                cobbleCube.getWidth()
        );

        this.outlineRegion = new Region(bukkitLocation, end);
        this.cubeRegion = new Region(bukkitLocation, end).shrink(1);

        for (final CubeUpgrade upgrade : plugin.getUpgradeRegistry()) {
            this.serializedUpgrades.put(upgrade.getName().toUpperCase(), 1L);
            this.upgrades.put(upgrade, 1L);
        }

        final Vector cubeSize = new Vector(cobbleCube.getLength(), cobbleCube.getHeight(), cobbleCube.getWidth());
        final Vector topOffset = cubeSize.clone().multiply(0.5).setY(cubeSize.getY());
    }

    public void deserialize(final JewelsCobbleCubes plugin) {
        this.bukkitLocation = LocationSerializer.deserialize(location);
        this.cobbleCube = plugin.getCubeRegistry().get(cobbleCubeType).get();

        final Location end = bukkitLocation.clone().add(
                cobbleCube.getLength(),
                cobbleCube.getHeight(),
                cobbleCube.getWidth()
        );

        this.outlineRegion = new Region(bukkitLocation, end);
        this.cubeRegion = new Region(bukkitLocation, end).shrink(1);

        this.blockStorage = Maps.mutable.empty();

        for (final Map.Entry<Material, Long> entry : this.storage.entrySet()) {

            final Optional<CobbleCubeBlock> optionalBlock = this.cobbleCube.getBlocks().getList()
                    .stream()
                    .filter(cobbleCubeBlock -> cobbleCubeBlock.getMaterial() == entry.getKey())
                    .findFirst();

            if (!optionalBlock.isPresent()) continue;

            final CobbleCubeBlock block = optionalBlock.get();

            this.blockStorage.put(block, entry.getValue());
        }

        if (!this.brokenLocations.isEmpty() && this.cobbleCube.isResetOnRestart()) {

            for (final String brokenLocation : this.brokenLocations) {
                LocationSerializer.deserialize(brokenLocation).getBlock().setType(this.cobbleCube.getBlocks().next().getMaterial());
            }

            this.brokenLocations.clear();
        }

        this.upgrades = Maps.mutable.empty();

        for (final Map.Entry<String, Long> upgrades : this.serializedUpgrades.entrySet()) {
            this.upgrades.put(
                    plugin.getUpgradeRegistry().get(upgrades.getKey().toUpperCase()).get(),
                    upgrades.getValue()
            );
        }

        final Vector cubeSize = new Vector(cobbleCube.getLength(), cobbleCube.getHeight(), cobbleCube.getWidth());
        final Vector topOffset = cubeSize.clone().multiply(0.5).setY(cubeSize.getY());
    }

    public void addDrop(final Material material) {

        final Optional<CobbleCubeBlock> optionalBlock = this.cobbleCube.getBlocks().getList()
                .stream()
                .filter(cobbleCubeBlock -> cobbleCubeBlock.getMaterial() == material)
                .findFirst();

        if (!optionalBlock.isPresent()) return;

        final CobbleCubeBlock block = optionalBlock.get();

        this.blockStorage.put(block, this.blockStorage.getOrDefault(block, 0L) + 1);
        this.storage.put(block.getMaterial(), this.storage.getOrDefault(block.getMaterial(), 0L) + 1);
    }

    public long getLevel(final CubeUpgrade upgrade) {
        return this.upgrades.get(upgrade);
    }

    public void addLevel(final CubeUpgrade upgrade, final long levels) {
        this.serializedUpgrades.put(upgrade.getName().toUpperCase(), this.getLevel(upgrade) + levels);
        this.upgrades.put(upgrade, this.getLevel(upgrade) + levels);
    }

    public double getCost(final Material material) {
        final Optional<CobbleCubeBlock> optionalBlock = this.cobbleCube.getBlocks().getList()
                .stream()
                .filter(cobbleCubeBlock -> cobbleCubeBlock.getMaterial() == material)
                .findFirst();

        if (!optionalBlock.isPresent()) return 0;

        final CobbleCubeBlock block = optionalBlock.get();

        return block.getPrice();
    }

    public void reset(final JewelsCobbleCubes plugin) {
        if (JewelsRealms.get().getRealmUtils().getMembersOnRealm(JewelsRealms.get().getRealmUtils().getRealm(this.getBukkitLocation().getWorld())).isEmpty()) return;
        if (this.getBrokenLocations().isEmpty()) return;

        final ProbabilityCollection<XMaterial> probabilityCollection = new ProbabilityCollection<>();

        for (Map.Entry<Double, CobbleCubeBlock> entry : cobbleCube.getBlocks().getMap().entrySet()) {
            probabilityCollection.add(XMaterial.matchXMaterial(entry.getValue().getMaterial()), (int) Math.round(entry.getKey()));
        }

        final Collection<Location> cachedLocations = Sets.mutable.empty();

        for (final String brokenLocation : this.brokenLocations) {
            cachedLocations.add(LocationSerializer.deserialize(brokenLocation));
        }

        plugin.getBlockHandler().setRandomBlocks(
                cachedLocations,
                probabilityCollection
        );

        this.brokenLocations.clear();
    }


    private  <T> T randomElement(Set<T> set) {
        int size = set.size();
        int item = MathUtility.getRandomNumber(0, size);
        int i = 0;
        for (T obj : set) {
            if (i == item) {
                return obj;
            }
            i++;
        }
        return null;
    }
}
