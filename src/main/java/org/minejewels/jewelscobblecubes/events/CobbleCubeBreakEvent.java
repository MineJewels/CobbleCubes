package org.minejewels.jewelscobblecubes.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.minejewels.jewelscobblecubes.cube.player.PlayerCobbleCube;

@Getter
@RequiredArgsConstructor
public final class CobbleCubeBreakEvent extends Event {

    private final static HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final PlayerCobbleCube playerCobbleCube;
    private final BlockBreakEvent event;

    @Override
    public HandlerList getHandlers() {
        return CobbleCubeBreakEvent.HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return CobbleCubeBreakEvent.HANDLER_LIST;
    }

}
