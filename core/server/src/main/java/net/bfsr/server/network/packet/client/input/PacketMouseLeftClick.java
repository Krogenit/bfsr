package net.bfsr.server.network.packet.client.input;

import io.netty.buffer.ByteBuf;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.packet.PacketIn;

import java.io.IOException;

public class PacketMouseLeftClick implements PacketIn {
    @Override
    public void read(ByteBuf data) throws IOException {

    }

    @Override
    public void processOnServerSide(PlayerNetworkHandler playerNetworkHandler) {
        playerNetworkHandler.getPlayer().getPlayerInputController().mouseLeftClick();
    }
}