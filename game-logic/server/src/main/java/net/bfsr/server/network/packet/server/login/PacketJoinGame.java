package net.bfsr.server.network.packet.server.login;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketOut;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.packet.PacketIn;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketJoinGame implements PacketOut, PacketIn {
    private long seed;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeLong(seed);
    }

    @Override
    public void read(ByteBuf data) throws IOException {

    }

    @Override
    public void processOnServerSide(PlayerNetworkHandler playerNetworkHandler) {
        playerNetworkHandler.joinGame();
    }
}