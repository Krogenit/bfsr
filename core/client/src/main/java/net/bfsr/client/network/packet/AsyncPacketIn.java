package net.bfsr.client.network.packet;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.network.NetworkSystem;

public interface AsyncPacketIn extends PacketIn {
    @Override
    default void processOnClientSide() {

    }

    void processOnClientSide(ChannelHandlerContext ctx);

    @Override
    default void handle(NetworkSystem networkSystem, ChannelHandlerContext ctx) {
        processOnClientSide(ctx);
    }
}
