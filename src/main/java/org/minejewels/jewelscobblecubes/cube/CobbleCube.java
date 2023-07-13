package org.minejewels.jewelscobblecubes.cube;

import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import lombok.Setter;
import net.abyssdev.abysslib.config.AbyssConfig;
import net.abyssdev.abysslib.nbt.NBTUtils;
import net.abyssdev.abysslib.placeholder.PlaceholderReplacer;
import net.abyssdev.abysslib.utils.ConcurrentRandomCollection;
import net.abyssdev.abysslib.utils.Utils;
import net.abyssdev.abysslib.utils.file.FileUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.minejewels.jewelscobblecubes.JewelsCobbleCubes;
import org.minejewels.jewelscobblecubes.cube.block.CobbleCubeBlock;

import java.io.File;
import java.util.List;

@Getter
@Setter
public class CobbleCube {

    private final String type;

    private final File configFile;
    private final AbyssConfig config;

    private final ConcurrentRandomCollection<CobbleCubeBlock> blocks = new ConcurrentRandomCollection<>();
    private final int length, width, height;
    private final ItemStack cubeItem;
    private final Material outlineBlock;

    private final List<String> hologram;
    private final int yOffset;

    private final int blocksPerReset;
    private final boolean resetOnRestart;

    public CobbleCube(final JewelsCobbleCubes plugin, final String type) {
        this.type = type;

        this.configFile = new File(plugin.getCubesFolder(), type + ".yml");

        if (!this.configFile.exists()) {
            FileUtils.copy(FileUtils.getInputFromJar("default.yml", plugin), this.configFile);
        }

        this.config = AbyssConfig.loadAbyssConfig(this.configFile);

        for (final String key : this.config.getConfigurationSection("materials").getKeys(false)) {

            if (!XMaterial.matchXMaterial(key).isPresent()) continue;

            final CobbleCubeBlock block = new CobbleCubeBlock(
                    XMaterial.matchXMaterial(key).get().parseMaterial(),
                    this.config.getDouble("materials." + key + ".price"),
                    this.config.getDouble("materials." + key + ".generation-chance")
            );

            this.blocks.add(block.getChance(), block);
        }

        this.length = this.config.getInt("size.length");
        this.width = this.config.getInt("size.width");
        this.height = this.config.getInt("size.height");

        this.hologram = this.config.getColoredStringList("hologram.hologram");
        this.yOffset = this.config.getInt("hologram.y-offset");

        this.cubeItem = NBTUtils.get().setString(this.config.getItemBuilder("item").parse(
                new PlaceholderReplacer()
                        .addPlaceholder("%length%", Utils.format(this.length))
                        .addPlaceholder("%width%", Utils.format(this.width))
                        .addPlaceholder("%height%", Utils.format(this.height))
        ), "COBBLECUBE", this.type.toUpperCase());

        this.outlineBlock = XMaterial.matchXMaterial("generation.outline-block").orElse(XMaterial.BEDROCK).parseMaterial();

        this.blocksPerReset = this.config.getInt("settings.blocks-per-reset");

        this.resetOnRestart = this.config.getBoolean("settings.reset-on-load");
    }
}
