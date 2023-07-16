package org.minejewels.jewelscobblecubes.upgrade.skilltree.menu;

import com.cryptomorin.xseries.SkullUtils;
import net.abyssdev.abysslib.builders.ItemBuilder;
import net.abyssdev.abysslib.builders.PageBuilder;
import net.abyssdev.abysslib.economy.registry.impl.DefaultEconomyRegistry;
import net.abyssdev.abysslib.menu.MenuBuilder;
import net.abyssdev.abysslib.menu.item.MenuItemBuilder;
import net.abyssdev.abysslib.menu.templates.PagedAbyssMenu;
import net.abyssdev.abysslib.placeholder.PlaceholderReplacer;
import net.abyssdev.abysslib.utils.Utils;
import net.abyssdev.abysslib.utils.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.EulerAngle;
import org.eclipse.collections.api.factory.Lists;
import org.minejewels.jewelscobblecubes.JewelsCobbleCubes;
import org.minejewels.jewelscobblecubes.cube.player.PlayerCobbleCube;
import org.minejewels.jewelscobblecubes.menus.UpgradeMenu;
import org.minejewels.jewelscobblecubes.upgrade.skilltree.SkillTree;
import org.minejewels.jewelscobblecubes.upgrade.skilltree.upgrade.SkillTreeUpgrade;
import org.minejewels.jewelscobblecubes.utils.RegionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class SkillTreeMenu extends PagedAbyssMenu<JewelsCobbleCubes> {

    private final MenuItemBuilder next, prev, current, back;
    private final ItemBuilder lockedSlots, purchasedSlots, nextUpgrade;

    private final int maxPages;

    private final SkillTree skillTree;

    public SkillTreeMenu(final JewelsCobbleCubes plugin, final SkillTree skillTree) {
        super(plugin, plugin.getUpgradesConfig(), "upgrades." + skillTree.getUpgrade().getName().toLowerCase() + ".skill-tree.");

        final FileConfiguration config = plugin.getUpgradesConfig();
        final String path = "upgrades." + skillTree.getUpgrade().getName().toLowerCase() + ".skill-tree";

        this.maxPages = config.getInt("upgrades." + skillTree.getUpgrade().getName().toLowerCase() + ".max-pages");

        this.next = new MenuItemBuilder(
                new ItemBuilder(config, path + ".action-items.next-page"),
                config.getInt(path + ".action-items.next-page.slot"));

        this.prev = new MenuItemBuilder(
                new ItemBuilder(config, path + ".action-items.previous-page"),
                config.getInt(path + ".action-items.previous-page.slot"));

        this.current = new MenuItemBuilder(
                new ItemBuilder(config, path + ".action-items.current-page"),
                config.getInt(path + ".action-items.current-page.slot"));

        this.back = new MenuItemBuilder(
                new ItemBuilder(config, path + ".action-items.back-menu"),
                config.getInt(path + ".action-items.back-menu.slot"));

        this.lockedSlots = new ItemBuilder(config, path + ".display-items.locked-slots");
        this.purchasedSlots = new ItemBuilder(config, path + ".display-items.purchased-slots");
        this.nextUpgrade = new ItemBuilder(config, path + ".display-items.next-upgrade");

        this.skillTree = skillTree;
    }

    public void open(final Player player, final int page, final PlayerCobbleCube cube) {

        final long cubeLevel = cube.getLevel(this.skillTree.getUpgrade());

        final MenuBuilder builder = this.createBase();

        final PlaceholderReplacer itemReplacer = new PlaceholderReplacer()
                .addPlaceholder("%current_level%", Utils.format(cubeLevel))
                .addPlaceholder("%max_level%", Utils.format(this.skillTree.getUpgrade().getLevels()))
                .addPlaceholder("%current_amount%", Utils.format(skillTree.getUpgrade().getAmount(cubeLevel)))
                .addPlaceholder("%page%", Utils.format(page + 1));

        final LinkedList<SkillTreeUpgrade> upgradeList = new LinkedList<>(this.skillTree.getUpgrades().values());

        final PageBuilder<SkillTreeUpgrade> pageBuilder = new PageBuilder<>(upgradeList, this.skillTree.getUpgradeSlots().size());

        builder.setItem(this.next.getSlot(), this.next.getItem().parse(itemReplacer));
        builder.setItem(this.prev.getSlot(), this.prev.getItem().parse(itemReplacer));
        builder.setItem(this.current.getSlot(), this.current.getItem().parse(itemReplacer));
        builder.setItem(this.back.getSlot(), this.back.getItem().parse(itemReplacer));

        builder.addClickEvent(this.back.getSlot(), event -> new UpgradeMenu(plugin).open(player, cube));

        builder.addClickEvent(this.next.getSlot(), event -> {
            if (page + 2 > maxPages) return;
            if (pageBuilder.hasPage(page + 1)) {
                this.open(player, page + 1, cube);
            }
        });

        builder.addClickEvent(this.prev.getSlot(), event -> {
            if (page - 1 > -1) {
                this.open(player, page - 1, cube);
            }
        });

        List<SkillTreeUpgrade> upgrades = pageBuilder.getPage(page);
        int index = 0;

        Collections.sort(upgrades, Comparator.comparingInt(SkillTreeUpgrade::getLevel));

        for (final int slot : this.skillTree.upgradeSlots) {
            if (index >= upgrades.size()) {
                break;
            }

            final SkillTreeUpgrade upgrade = upgrades.get(index);

            final PlaceholderReplacer replacer = new PlaceholderReplacer()
                    .addPlaceholder("%upgrade%", WordUtils.formatText(skillTree.getUpgrade().getName()))
                    .addPlaceholder("%cost%", Utils.format(upgrade.getCost()))
                    .addPlaceholder("%level%", Utils.format(upgrade.getLevel()))
                    .addPlaceholder("%new_level%", Utils.format(upgrade.getLevel() + 1))
                    .addPlaceholder("%max_level%", Utils.format(this.skillTree.getUpgrade().getLevels()))
                    .addPlaceholder("%increase%", Utils.format(skillTree.getUpgrade().getAmount(upgrade.getLevel() + 1) - skillTree.getUpgrade().getAmount(upgrade.getLevel())));

            if (upgrade.getLevel() <= cubeLevel) {
                builder.setItem(slot, this.purchasedSlots.parse(replacer));
            }

            if (upgrade.getLevel()-1 == cubeLevel) {
                builder.setItem(slot, this.nextUpgrade.parse(replacer));
            }

            if (upgrade.getLevel()-1 > cubeLevel) {
                builder.setItem(slot, this.lockedSlots.parse(replacer));
            }

            builder.addClickEvent(slot, event -> {

                if (upgrade.getLevel() <= cubeLevel) {
                    plugin.getMessageCache().sendMessage(player, "messages.already-purchased");
                    return;
                }

                if (upgrade.getLevel()-1 == cubeLevel) {

                    final double cost = upgrade.getCost();

                    if (!DefaultEconomyRegistry.get().getEconomy("vault").hasBalance(player, cost)) {
                        plugin.getMessageCache().sendMessage(player, "messages.not-enough");
                        return;
                    }

                    DefaultEconomyRegistry.get().getEconomy("vault").withdrawBalance(player, cost);

                    plugin.getMessageCache().sendMessage(player, "messages.level-purchased", replacer);
                    cube.addLevel(skillTree.getUpgrade(), 1);

                    this.open(player, page, cube);
                    return;
                }

                plugin.getMessageCache().sendMessage(player, "messages.level-too-high");
            });
            index++;

        }

        player.openInventory(builder.build());
    }
}
