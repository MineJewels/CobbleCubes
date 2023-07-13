package org.minejewels.jewelscobblecubes.cube.storage;

import net.abyssdev.abysslib.storage.json.SingleKeyedJsonStorage;
import net.abyssdev.abysslib.utils.file.Files;
import org.minejewels.jewelscobblecubes.JewelsCobbleCubes;
import org.minejewels.jewelscobblecubes.cube.player.PlayerCobbleCube;

public final class CubeStorage extends SingleKeyedJsonStorage<PlayerCobbleCube> {

    public CubeStorage(final JewelsCobbleCubes plugin) {
        super(Files.file("data.json", plugin), PlayerCobbleCube.class);
    }
}
