package net.bfsr.server.network.packet.client.input;

import io.netty.buffer.ByteBuf;
import net.bfsr.math.Direction;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.packet.PacketIn;

import java.io.IOException;

public class PacketShipMove implements PacketIn {
    private Direction direction;

    @Override
    public void read(ByteBuf data) throws IOException {
        direction = Direction.values()[data.readByte()];
    }

    @Override
    public void processOnServerSide(PlayerNetworkHandler playerNetworkHandler) {
        playerNetworkHandler.getPlayer().getPlayerInputController().move(direction);
    }
}