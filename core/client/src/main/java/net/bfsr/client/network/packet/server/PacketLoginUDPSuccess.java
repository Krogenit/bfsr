package net.bfsr.client.network.packet.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.network.packet.AsyncPacketIn;

import java.io.IOException;

public class PacketLoginUDPSuccess implements AsyncPacketIn {
    @Override
    public void read(ByteBuf data) throws IOException {}

    @Override
    public void processOnClientSide(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new PacketJoinGame());
    }
}