package net.bfsr.server.network.packet.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.bfsr.network.PacketOut;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.packet.AsyncPacketIn;

import java.io.IOException;
import java.net.InetSocketAddress;

public class PacketKeepAlive implements AsyncPacketIn, PacketOut {
    @Override
    public void read(ByteBuf data) throws IOException {}

    @Override
    public void write(ByteBuf data) throws IOException {}

    @Override
    public void processOnServerSide(PlayerNetworkHandler playerNetworkHandler, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {}
}