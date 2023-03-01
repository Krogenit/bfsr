package net.bfsr.client.network.packet.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.packet.AsyncPacketIn;
import net.bfsr.client.world.WorldClient;
import net.bfsr.server.network.ConnectionState;

import java.io.IOException;

public class PacketLoginUDPSuccess implements AsyncPacketIn {
    @Override
    public void read(ByteBuf data) throws IOException {}

    public void processOnClientSide(ChannelHandlerContext ctx) {
        Core.get().getNetworkSystem().setConnectionState(ConnectionState.PLAY);
        Core.get().addFutureTask(() -> Core.get().setWorld(new WorldClient()));
    }
}