package net.bfsr.client.network.packet.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.network.packet.AsyncPacketIn;
import net.bfsr.network.PacketOut;

public class PacketKeepAlive implements AsyncPacketIn, PacketOut {
    @Override
    public void read(ByteBuf data) {}

    @Override
    public void write(ByteBuf data) {}

    @Override
    public void processOnClientSide(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new PacketKeepAlive());
    }
}