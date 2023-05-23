package net.bfsr.server.network.packet.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketOut;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.packet.AsyncPacketIn;

import java.io.IOException;
import java.net.InetSocketAddress;

@NoArgsConstructor
@AllArgsConstructor
public class PacketPing implements AsyncPacketIn, PacketOut {
    private long clientDeltaTime;

    @Override
    public void read(ByteBuf data) throws IOException {
        clientDeltaTime = data.readLong();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeLong(clientDeltaTime);
    }

    @Override
    public void processOnServerSide(PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        ctx.writeAndFlush(new PacketPing(System.nanoTime() - (playerNetworkHandler.getHandshakeClientTime() + clientDeltaTime)));
    }
}