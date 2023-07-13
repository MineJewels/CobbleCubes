package org.minejewels.jewelscobblecubes.cube.task;

import net.abyssdev.abysslib.caged.MathUtility;
import net.abyssdev.abysslib.location.LocationSerializer;
import net.abyssdev.abysslib.runnable.AbyssTask;
import org.minejewels.jewelscobblecubes.JewelsCobbleCubes;
import org.minejewels.jewelsrealms.JewelsRealms;

import java.util.Set;

public class CubeTask extends AbyssTask<JewelsCobbleCubes> {

    public CubeTask(JewelsCobbleCubes plugin) {
        super(plugin, 0, 40, false);
    }

    @Override
    public void run() {

        plugin.getCachedCubeService().iterate(cube -> {

            if (JewelsRealms.get().getRealmUtils().getMembersOnRealm(JewelsRealms.get().getRealmUtils().getRealm(cube.getBukkitLocation().getWorld())).isEmpty()) return;
            if (cube.getBrokenLocations().isEmpty()) return;

            for (int i = 0; i < cube.getCobbleCube().getBlocksPerReset(); i++) {

                final String location = this.randomElement(cube.getBrokenLocations());

                if (location == null) continue;

                LocationSerializer.deserialize(location).getBlock().setType(cube.getCobbleCube().getBlocks().next().getMaterial());

                cube.getBrokenLocations().remove(location);
            }
        });
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
