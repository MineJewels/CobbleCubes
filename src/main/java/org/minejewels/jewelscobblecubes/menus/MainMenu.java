package org.minejewels.jewelscobblecubes.menus;

import net.abyssdev.abysslib.builders.ItemBuilder;
import net.abyssdev.abysslib.economy.registry.impl.DefaultEconomyRegistry;
import net.abyssdev.abysslib.menu.MenuBuilder;
import net.abyssdev.abysslib.menu.item.MenuItemBuilder;
import net.abyssdev.abysslib.menu.item.MenuItemStack;
import net.abyssdev.abysslib.menu.templates.AbyssMenu;
import net.abyssdev.abysslib.placeholder.PlaceholderReplacer;
import net.abyssdev.abysslib.utils.Utils;
import net.abyssdev.abysslib.utils.WordUtils;
import net.abyssdev.me.lucko.helper.Events;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.eclipse.collections.api.factory.Lists;
import org.minejewels.jewelscobblecubes.JewelsCobbleCubes;
import org.minejewels.jewelscobblecubes.cube.CobbleCube;
import org.minejewels.jewelscobblecubes.cube.block.CobbleCubeBlock;
import org.minejewels.jewelscobblecubes.cube.player.PlayerCobbleCube;
import org.minejewels.jewelscobblecubes.events.CobbleCubeRemoveEvent;
import org.minejewels.jewelscobblecubes.events.CobbleCubeSellEvent;
import org.minejewels.jewelscobblecubes.upgrade.CubeUpgrade;
import org.minejewels.jewelscobblecubes.utils.RegionUtils;

import java.util.List;
import java.util.Map;

public class MainMenu extends AbyssMenu {

    private final JewelsCobbleCubes plugin;
    private final List<Integer> itemSlots = Lists.mutable.empty();
    private final MenuItemStack sellall;
    private final String itemName;
    private final List<String> itemLore;

    private final MenuItemBuilder reset, upgrade, autosellLocked, autosellEnabled, autosellDisabled;
    private final MenuItemStack remove;


    public MainMenu(final JewelsCobbleCubes plugin, final CobbleCube cube) {
        super(plugin.getMenuConfig(), "main-menu.");

        this.plugin = plugin;

        itemSlots.addAll(plugin.getMenuConfig().getIntegerList("main-menu.item-slots"));

        this.sellall = new MenuItemStack(
                new ItemBuilder(plugin.getMenuConfig(), "main-menu.items.sellall-item").parse(),
                plugin.getMenuConfig().getInt("main-menu.items.sellall-item.slot")
        );

        this.reset = new MenuItemBuilder(
                new ItemBuilder(plugin.getMenuConfig(), "main-menu.items.reset-item"),
                plugin.getMenuConfig().getInt("main-menu.items.reset-item.slot")
        );

        this.upgrade = new MenuItemBuilder(
                new ItemBuilder(plugin.getMenuConfig(), "main-menu.items.upgrades-menu"),
                plugin.getMenuConfig().getInt("main-menu.items.upgrades-menu.slot")
        );

        this.autosellLocked = new MenuItemBuilder(
                new ItemBuilder(plugin.getMenuConfig(), "main-menu.items.autosell.locked"),
                plugin.getMenuConfig().getInt("main-menu.items.autosell.locked.slot")
        );

        this.autosellEnabled = new MenuItemBuilder(
                new ItemBuilder(plugin.getMenuConfig(), "main-menu.items.autosell.enabled"),
                plugin.getMenuConfig().getInt("main-menu.items.autosell.enabled.slot")
        );

        this.autosellDisabled = new MenuItemBuilder(
                new ItemBuilder(plugin.getMenuConfig(), "main-menu.items.autosell.disabled"),
                plugin.getMenuConfig().getInt("main-menu.items.autosell.disabled.slot")
        );

        this.remove = new MenuItemStack(
                new ItemBuilder(plugin.getMenuConfig(), "main-menu.items.remove-cube").parse(),
                plugin.getMenuConfig().getInt("main-menu.items.remove-cube.slot")
        );

        this.itemName = plugin.getMenuConfig().getColoredString("main-menu.storage-item.name");
        this.itemLore = plugin.getMenuConfig().getColoredStringList("main-menu.storage-item.lore");
    }

    public void open(final Player player, final PlayerCobbleCube playerCube) {

        double currentValue = 0;
        long totalBlocks = 0;

        final List<CobbleCubeBlock> blocks = Lists.mutable.empty();

        for (Map.Entry<CobbleCubeBlock, Long> entry : playerCube.getBlockStorage().entrySet()) {
            currentValue += entry.getKey().getPrice() * entry.getValue();
            totalBlocks += entry.getValue();

            blocks.add(entry.getKey());
        }


        final PlaceholderReplacer replacer = new PlaceholderReplacer()
                .addPlaceholder("%autosell%", this.getStatus(playerCube))
                .addPlaceholder("%value%", Utils.format(currentValue))
                .addPlaceholder("%blocks%", Utils.format(totalBlocks));

        final MenuBuilder builder = this.createBase();

        for (final Map.Entry<CubeUpgrade, Long> upgrades : playerCube.getUpgrades().entrySet()) {
            replacer.addPlaceholder("%" + upgrades.getKey().getName().toLowerCase() + "%", Utils.format(upgrades.getKey().getAmount(upgrades.getValue())));
        }

        builder.setItem(sellall.getSlot(), sellall.getItem());
        builder.setItem(upgrade.getSlot(), upgrade.getItem());
        builder.setItem(remove.getSlot(), remove.getItem());
        builder.setItem(reset.getSlot(), reset.getItem());

        builder.addClickEvent(this.reset.getSlot(), event -> {
            if (this.plugin.getResetCooldown().contains(playerCube)) {
                this.plugin.getMessageCache().sendMessage(player, "messages.on-cooldown");
                return;
            }

            playerCube.reset();
            this.plugin.getMessageCache().sendMessage(player, "messages.cube-reset");
            this.plugin.getResetCooldown().add(playerCube);
        });

        builder.addClickEvent(this.upgrade.getSlot(), event -> new UpgradeMenu(plugin).open(player, playerCube));

        builder.addClickEvent(this.remove.getSlot(), event -> {

            plugin.getCachedCubeService().remove(playerCube);
            plugin.getCubeStorage().remove(playerCube);

            for (final Block block : RegionUtils.getBlocksWithinRegion(playerCube.getOutlineRegion())) {
                player.playEffect(block.getLocation(), Effect.SMOKE, 0);
                block.setType(Material.AIR);
            }

            plugin.getMessageCache().sendMessage(player, "messages.removed-cube");

            player.closeInventory();

            final ItemStack item = playerCube.getCobbleCube().getCubeItem().clone();

            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItem(player.getLocation(), item);
            } else {
                player.getInventory().addItem(item);
            }

            final CobbleCubeRemoveEvent removeEvent = new CobbleCubeRemoveEvent(
                    player,
                    playerCube
            );

            Events.call(removeEvent);
        });

        double finalCurrentValue = currentValue;

        builder.addClickEvent(sellall.getSlot(), event -> {

            if (playerCube.getBlockStorage().isEmpty()) return;
            if (playerCube.getStorage().isEmpty()) return;

            DefaultEconomyRegistry.get().getEconomy("vault").addBalance(player, finalCurrentValue);

            playerCube.getBlockStorage().clear();
            playerCube.getStorage().clear();

            plugin.getMessageCache().sendMessage(player, "messages.sold-cube", replacer);

            player.closeInventory();

            Events.call(new CobbleCubeSellEvent(
                    player,
                    playerCube,
                    finalCurrentValue
            ));
        });

        int index = 0;

        for (final Integer slot : this.itemSlots) {

            if (index >= playerCube.getBlockStorage().size()) {
                builder.setItem(slot, new ItemStack(Material.AIR));
                continue;
            }

            final CobbleCubeBlock block = blocks.get(index);

            final ItemStack item = new ItemStack(block.getMaterial());
            final ItemMeta itemMeta = item.getItemMeta();

            final PlaceholderReplacer itemReplacer = new PlaceholderReplacer()
                    .addPlaceholder("%name%", WordUtils.formatText(block.getMaterial().name().replace("_", " ")))
                    .addPlaceholder("%amount%", Utils.format(playerCube.getBlockStorage().get(block)))
                    .addPlaceholder("%value%", Utils.format(playerCube.getBlockStorage().get(block) * block.getPrice()));

            itemMeta.setDisplayName(itemReplacer.parse(this.itemName));
            itemMeta.setLore(itemReplacer.parse(this.itemLore));

            item.setItemMeta(itemMeta);

            builder.setItem(slot, item);

            index++;
        }

        if (!playerCube.isAutosellPurchased()) {
            builder.setItem(this.autosellLocked.getSlot(), this.autosellLocked.getItem().parse(replacer));
            builder.addClickEvent(this.autosellLocked.getSlot(), event -> {

                final long cost = this.plugin.getSettingsConfig().getLong("autosell-cost");

                if (!DefaultEconomyRegistry.get().getEconomy("vault").hasBalance(player, cost)) {
                    plugin.getMessageCache().sendMessage(player, "messages.not-enough");
                    return;
                }

                DefaultEconomyRegistry.get().getEconomy("vault").withdrawBalance(player, cost);

                plugin.getMessageCache().sendMessage(player, "messages.level-purchased", new PlaceholderReplacer()
                                .addPlaceholder("%upgrade%", "Autosell")
                               .addPlaceholder("%level%", Utils.format(1)));

                playerCube.setAutosellPurchased(true);
                playerCube.setAutosellEnabled(true);
                this.open(player, playerCube);
            });
        }

        if (playerCube.isAutosellPurchased() && !playerCube.isAutosellEnabled()) {
            builder.setItem(this.autosellDisabled.getSlot(), this.autosellDisabled.getItem().parse(replacer));
            builder.addClickEvent(this.autosellDisabled.getSlot(), event -> {
                playerCube.setAutosellEnabled(true);
                this.open(player, playerCube);
            });
        }

        if (playerCube.isAutosellPurchased() && playerCube.isAutosellEnabled()) {
            builder.setItem(this.autosellEnabled.getSlot(), this.autosellEnabled.getItem().parse(replacer));
            builder.addClickEvent(this.autosellEnabled.getSlot(), event -> {
                playerCube.setAutosellEnabled(false);
                this.open(player, playerCube);
            });
        }

        player.openInventory(builder.build(replacer));
    }

    private String getStatus(final PlayerCobbleCube cube) {
        if (!cube.isAutosellPurchased()) return "Locked";
        if (!cube.isAutosellEnabled()) return "Disabled";
        return "Enabled";
    }
}
