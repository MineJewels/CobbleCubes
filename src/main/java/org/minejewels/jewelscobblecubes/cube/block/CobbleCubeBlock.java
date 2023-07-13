package org.minejewels.jewelscobblecubes.cube.block;

import lombok.Getter;
import org.bukkit.Material;

@Getter
public final class CobbleCubeBlock {

    private final Material material;
    private final double price, chance;

    public CobbleCubeBlock(final Material material, final double price, final double chance) {
        this.material = material;
        this.price = price;
        this.chance = chance;
    }
}
