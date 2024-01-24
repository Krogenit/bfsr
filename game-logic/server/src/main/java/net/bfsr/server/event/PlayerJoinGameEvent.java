package net.bfsr.server.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.engine.event.Event;
import net.bfsr.server.player.Player;

@Getter
@RequiredArgsConstructor
public class PlayerJoinGameEvent extends Event {
    private final Player player;
}