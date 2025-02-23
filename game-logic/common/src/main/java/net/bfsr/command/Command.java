package net.bfsr.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.client.PacketCommand;

@Getter
@RequiredArgsConstructor
public enum Command {
    SPAWN_SHIP(false),
    SPAWN_PARTICLE(false),
    DESTROY_SHIP(true),
    REMOVE_SHIELD(true),
    DISABLE_SHIELD(true),
    ADD_SHIELD(true),
    ADD_AI(true),
    REMOVE_AI(true),
    DESTROY_ONE_HULL_CELL(true);

    private final boolean shipCommand;

    public PacketCommand createShipPacketCommand(Command command, Ship ship) {
        return new PacketCommand(command, ship.getId());
    }
}
