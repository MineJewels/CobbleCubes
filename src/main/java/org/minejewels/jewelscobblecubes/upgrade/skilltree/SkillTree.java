package org.minejewels.jewelscobblecubes.upgrade.skilltree;

import lombok.Data;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.minejewels.jewelscobblecubes.JewelsCobbleCubes;
import org.minejewels.jewelscobblecubes.upgrade.CubeUpgrade;
import org.minejewels.jewelscobblecubes.upgrade.skilltree.upgrade.SkillTreeUpgrade;

import java.util.List;
import java.util.Map;

@Data
public class SkillTree {

    private final CubeUpgrade upgrade;
    public final List<Integer> upgradeSlots;
    private final Map<Integer, SkillTreeUpgrade> upgrades = Maps.mutable.empty();

    public SkillTree(final CubeUpgrade upgrade, final JewelsCobbleCubes plugin) {
        this.upgrade = upgrade;
        this.upgradeSlots = plugin.getUpgradesConfig().getIntegerList("upgrades." + upgrade.getName().toLowerCase() + ".skill-tree.upgrade-slots");
        this.cacheUpgrades();
    }

    private void cacheUpgrades() {
        long currentCost = upgrade.getStartingCost();
        double costMultiplier = upgrade.getCostIncrease();

        for (int level = 1; level <= upgrade.getLevels(); level++) {
            double cost = currentCost * (1 + costMultiplier * (level - 1));
            SkillTreeUpgrade upgrade = new SkillTreeUpgrade(level, cost);
            upgrades.put(level, upgrade);
        }
    }
}
