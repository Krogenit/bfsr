package net.bfsr.server.network.packet.client;

import io.netty.buffer.ByteBuf;
import net.bfsr.server.network.ConnectionState;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.packet.PacketIn;

import java.io.IOException;

public class PacketHandshake implements PacketIn {
    private int version;
    private long handshakeClientTime;

    @Override
    public void read(ByteBuf data) throws IOException {
        version = data.readByte();
        handshakeClientTime = data.readLong();
    }

    @Override
    public void processOnServerSide(PlayerNetworkHandler playerNetworkHandler) {
        playerNetworkHandler.setConnectionState(ConnectionState.LOGIN);
        playerNetworkHandler.setHandshakeClientTime(handshakeClientTime);
        playerNetworkHandler.setLoginStartTime(System.currentTimeMillis());
    }
}
