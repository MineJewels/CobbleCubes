package org.minejewels.jewelscobblecubes.cube.service;

import net.abyssdev.abysslib.patterns.service.impl.EclipseService;
import net.abyssdev.abysslib.patterns.service.type.ServiceType;
import org.minejewels.jewelscobblecubes.cube.player.PlayerCobbleCube;

public final class CachedCubeService extends EclipseService<PlayerCobbleCube> {

    public CachedCubeService() {
        super(ServiceType.SET);
    }
}
