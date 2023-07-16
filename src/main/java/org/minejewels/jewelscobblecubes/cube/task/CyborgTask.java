package org.minejewels.jewelscobblecubes.cube.task;

import net.abyssdev.abysslib.caged.MathUtility;
import net.abyssdev.abysslib.runnable.AbyssTask;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.minejewels.jewelscobblecubes.JewelsCobbleCubes;
import org.minejewels.jewelscobblecubes.cube.player.PlayerCobbleCube;
import org.minejewels.jewelscobblecubes.upgrade.CubeUpgrade;
import org.minejewels.jewelscobblecubes.utils.RegionUtils;

import java.util.Set;

public class CyborgTask extends AbyssTask<JewelsCobbleCubes> {
    public CyborgTask(final JewelsCobbleCubes plugin) {
        super(plugin);

        this.runTaskTimerAsynchronously(plugin, 0L, 1200L);
    }

    @Override
    public void run() {

        for (final PlayerCobbleCube cube : this.plugin.getCachedCubeService()) {

            final CubeUpgrade cubeUpgrade = this.plugin.getUpgradeRegistry().get("CYBORG").get();

            if (cube.getLevel(cubeUpgrade) <= 1) continue;

            final double amount = cubeUpgrade.getAmount(cube.getLevel(cubeUpgrade));

            int index = 0;

            while (index < amount) {

                final Block block = this.randomElement(RegionUtils.getBlocksWithinRegion(cube.getCubeRegion()));

                if (block == null) continue;
                if (block.getType() == Material.AIR) return;

                cube.addDrop(block.getType());

                index++;
            }
        }

    }

    public <T> T randomElement(Set<T> set) {
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
