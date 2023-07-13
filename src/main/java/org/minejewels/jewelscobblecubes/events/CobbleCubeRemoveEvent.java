package org.minejewels.jewelscobblecubes.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.minejewels.jewelscobblecubes.cube.player.PlayerCobbleCube;

@Getter
@RequiredArgsConstructor
public final class CobbleCubeRemoveEvent extends Event {

    private final static HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final PlayerCobbleCube cube;

    @Override
    public HandlerList getHandlers() {
        return CobbleCubeRemoveEvent.HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return CobbleCubeRemoveEvent.HANDLER_LIST;
    }

}
