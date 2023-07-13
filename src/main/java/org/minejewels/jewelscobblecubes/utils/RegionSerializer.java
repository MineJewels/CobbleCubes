package org.minejewels.jewelscobblecubes.utils;

import lombok.experimental.UtilityClass;
import net.abyssdev.abysslib.utils.Region;
import org.bukkit.Bukkit;

@UtilityClass
public class RegionSerializer {

    public String serialize(final Region region) {
        return region.getWorld().getName()
                + ";" + region.getMinX()
                + ";" + region.getMinY()
                + ";" + region.getMinZ()
                + ";" + region.getMaxX()
                + ";" + region.getMaxY()
                + ";" + region.getMaxZ();
    }

    public Region deserialize(final String serializedRegion) {
        final String[] data = serializedRegion.split(";");

        return new Region(
                Bukkit.getWorld(data[0]),
                Integer.parseInt(data[1]),
                Integer.parseInt(data[2]),
                Integer.parseInt(data[3]),
                Integer.parseInt(data[4]),
                Integer.parseInt(data[5]),
                Integer.parseInt(data[6])
        );
    }
}

