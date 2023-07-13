package org.minejewels.jewelscobblecubes;

import com.google.common.io.Files;
import lombok.Getter;
import net.abyssdev.abysslib.config.AbyssConfig;
import net.abyssdev.abysslib.patterns.registry.Registry;
import net.abyssdev.abysslib.patterns.service.Service;
import net.abyssdev.abysslib.plugin.AbyssPlugin;
import net.abyssdev.abysslib.storage.SingleKeyedStorage;
import net.abyssdev.abysslib.text.MessageCache;
import org.checkerframework.checker.units.qual.C;
import org.minejewels.jewelscobblecubes.commands.CobbleCubeCommand;
import org.minejewels.jewelscobblecubes.commands.sub.CobbleCubeGiveCommand;
import org.minejewels.jewelscobblecubes.cube.CobbleCube;
import org.minejewels.jewelscobblecubes.cube.player.PlayerCobbleCube;
import org.minejewels.jewelscobblecubes.cube.registry.CubeRegistry;
import org.minejewels.jewelscobblecubes.cube.service.CachedCubeService;
import org.minejewels.jewelscobblecubes.cube.storage.CubeStorage;
import org.minejewels.jewelscobblecubes.cube.task.CubeTask;
import org.minejewels.jewelscobblecubes.listeners.BreakListener;
import org.minejewels.jewelscobblecubes.listeners.InteractListener;
import org.minejewels.jewelscobblecubes.listeners.PlaceListener;
import org.minejewels.jewelscobblecubes.upgrade.CubeUpgrade;
import org.minejewels.jewelscobblecubes.upgrade.registry.UpgradeRegistry;

import java.io.File;

@Getter
public final class JewelsCobbleCubes extends AbyssPlugin {

    private static JewelsCobbleCubes api;

    private final AbyssConfig settingsConfig = this.getAbyssConfig("settings");
    private final AbyssConfig upgradesConfig = this.getAbyssConfig("upgrades");
    private final AbyssConfig langConfig = this.getAbyssConfig("lang");
    private final AbyssConfig menuConfig = this.getAbyssConfig("menus");

    private final MessageCache messageCache = new MessageCache(this.getConfig("lang"));

    private final Registry<String, CubeUpgrade> upgradeRegistry = new UpgradeRegistry();
    private final Registry<String, CobbleCube> cubeRegistry = new CubeRegistry();

    private final Service<PlayerCobbleCube> cachedCubeService = new CachedCubeService();

    private final SingleKeyedStorage<PlayerCobbleCube> cubeStorage = new CubeStorage(this);

    private final CobbleCubeCommand cobbleCubeCommand = new CobbleCubeCommand(this);

    private File cubesFolder;

    @Override
    public void onEnable() {
        JewelsCobbleCubes.api = this;

        this.loadMessages(this.messageCache, this.getConfig("lang"));

        this.cubesFolder = new File(this.getDataFolder(), "cubes");

        if (!this.cubesFolder.exists()) {
            this.cubesFolder.mkdir();
        }

        this.loadUpgrades();
        this.loadCubes();

        this.cubeStorage.cache().iterate(cube -> {
            cube.deserialize(this);
            this.cachedCubeService.add(cube);
        });

        this.loadCommands();

        new CubeTask(this);

        new PlaceListener(this);
        new InteractListener(this);
        new BreakListener(this);
    }

    @Override
    public void onDisable() {
        this.cubeStorage.write();
    }

    public static JewelsCobbleCubes get() {
        return JewelsCobbleCubes.api;
    }

    private void loadCommands() {
        this.cobbleCubeCommand.register();
        this.cobbleCubeCommand.register(
                new CobbleCubeGiveCommand(this)
        );
    }

    private void loadUpgrades() {
        for (final String upgrades : this.upgradesConfig.getSectionKeys("upgrades")) {
            final CubeUpgrade upgrade = new CubeUpgrade(
                    upgrades.toUpperCase(),
                    this.upgradesConfig.getBoolean("upgrades." + upgrades + ".enabled"),
                    this.upgradesConfig.getLong("upgrades." + upgrades + ".base-cost"),
                    this.upgradesConfig.getLong("upgrades." + upgrades + ".base-amount"),
                    this.upgradesConfig.getDouble("upgrades." + upgrades + ".cost-increase"),
                    this.upgradesConfig.getDouble("upgrades." + upgrades + ".increase-per-level"),
                    this.upgradesConfig.getInt("upgrades." + upgrades + ".levels")
            );

            if (upgrade.isEnabled()) {
                this.upgradeRegistry.register(upgrades.toUpperCase(), upgrade);
            }
        }
    }

    private void loadCubes() {
        for (final File file : this.cubesFolder.listFiles()) {
            final String name = Files.getNameWithoutExtension(file.getName());
            this.cubeRegistry.register(name, new CobbleCube(this, name));
        }

        if (this.cubeRegistry.values().isEmpty()) {
            this.cubeRegistry.register("default", new CobbleCube(this, "default"));
        }
    }
}
