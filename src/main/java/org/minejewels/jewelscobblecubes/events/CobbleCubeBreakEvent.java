package org.minejewels.jewelscobblecubes.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.minejewels.jewelscobblecubes.cube.player.PlayerCobbleCube;
import org.minejewels.jewelsrealms.events.RealmBreakEvent;

@Getter
@Setter
@AllArgsConstructor
public final class CobbleCubeBreakEvent extends Event implements Cancellable {

    private final static HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final PlayerCobbleCube playerCobbleCube;
    private final BlockBreakEvent event;
    private boolean cancelled;

    public CobbleCubeBreakEvent(final Player player, final PlayerCobbleCube cube, final BlockBreakEvent event) {
        this.player = player;
        this.playerCobbleCube = cube;
        this.event = event;
    }

    @Override
    public HandlerList getHandlers() {
        return CobbleCubeBreakEvent.HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return CobbleCubeBreakEvent.HANDLER_LIST;
    }

}
