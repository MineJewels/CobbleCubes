package org.minejewels.jewelscobblecubes.menus;

import net.abyssdev.abysslib.builders.ItemBuilder;
import net.abyssdev.abysslib.menu.MenuBuilder;
import net.abyssdev.abysslib.menu.item.MenuItemBuilder;
import net.abyssdev.abysslib.menu.templates.AbyssMenu;
import net.abyssdev.abysslib.placeholder.PlaceholderReplacer;
import net.abyssdev.abysslib.utils.Utils;
import org.bukkit.entity.Player;
import org.minejewels.jewelscobblecubes.JewelsCobbleCubes;
import org.minejewels.jewelscobblecubes.cube.player.PlayerCobbleCube;
import org.minejewels.jewelscobblecubes.upgrade.CubeUpgrade;
import org.minejewels.jewelscobblecubes.upgrade.skilltree.SkillTree;
import org.minejewels.jewelscobblecubes.upgrade.skilltree.menu.SkillTreeMenu;

public class UpgradeMenu extends AbyssMenu {

    private final JewelsCobbleCubes plugin;
    private final MenuItemBuilder back;

    public UpgradeMenu(final JewelsCobbleCubes plugin) {
        super(plugin.getMenuConfig(), "upgrades-menu.");

        this.plugin = plugin;

        this.back = new MenuItemBuilder(
                new ItemBuilder(plugin.getMenuConfig(), "upgrades-menu.back-menu"),
                plugin.getMenuConfig().getInt("upgrades-menu.back-menu.slot"));
    }

    public void open(final Player player, final PlayerCobbleCube cobbleCube) {

        final MenuBuilder builder = this.createBase();

        builder.setItem(this.back.getSlot(), this.back.getItem().parse());
        builder.addClickEvent(this.back.getSlot(), event -> new MainMenu(plugin, cobbleCube.getCobbleCube()).open(player, cobbleCube));

        for (final String key : this.plugin.getMenuConfig().getSectionKeys("upgrades-menu.upgrades")) {

            final CubeUpgrade upgrade = this.plugin.getUpgradeRegistry().get(key.toUpperCase()).get();
            final SkillTree skillTree = new SkillTree(upgrade, plugin);

            final PlaceholderReplacer replacer = new PlaceholderReplacer()
                    .addPlaceholder("%" + key.toLowerCase() + "_level%", Utils.format(cobbleCube.getLevel(upgrade)))
                    .addPlaceholder("%" + key.toLowerCase() + "_max_level%", Utils.format(upgrade.getLevels()))
                    .addPlaceholder("%" + key.toLowerCase() + "_max_amount%", Utils.format(upgrade.getStartingAmount() + (upgrade.getIncreasePerLevel() * (upgrade.getLevels() - 1))))
                    .addPlaceholder("%" + key.toLowerCase() + "_amount%", Utils.format(upgrade.getAmount(cobbleCube.getLevel(upgrade))));

            builder.setItem(
                    plugin.getMenuConfig().getInt("upgrades-menu.upgrades." + upgrade.getName().toLowerCase() + ".slot"),
                    plugin.getMenuConfig().getItemBuilder("upgrades-menu.upgrades." + upgrade.getName().toLowerCase()).parse(replacer)
            );

            builder.addClickEvent(plugin.getMenuConfig().getInt("upgrades-menu.upgrades." + upgrade.getName().toLowerCase() + ".slot"), event -> {
                player.closeInventory();

                new SkillTreeMenu(plugin, skillTree).open(player, 0, cobbleCube);
            });
        }

        player.openInventory(builder.build());
    }
}
