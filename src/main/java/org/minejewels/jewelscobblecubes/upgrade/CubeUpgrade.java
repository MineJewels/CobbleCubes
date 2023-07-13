package org.minejewels.jewelscobblecubes.upgrade;

import lombok.Data;

@Data
public class CubeUpgrade {

    private final String name;
    private final boolean enabled;
    private final long startingCost, startingAmount;
    private final double costIncrease, increasePerLevel;
    private final int levels;

    public double getAmount(final long level) {
        if (level <= 0 ) {
            throw new IllegalArgumentException("Invalid level: " + level);
        }

        return startingAmount + increasePerLevel * (level - 1);
    }
}
