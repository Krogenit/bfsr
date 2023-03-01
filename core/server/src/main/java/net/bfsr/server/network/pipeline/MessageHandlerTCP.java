package net.bfsr.server.network.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.packet.PacketIn;

@AllArgsConstructor
@Log4j2
public class MessageHandlerTCP extends SimpleChannelInboundHandler<PacketIn> {
    private final PlayerNetworkHandler playerNetworkHandler;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PacketIn msg) {
        msg.handle(playerNetworkHandler, ctx, null);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Error during handling TCP packet", cause);
    }
}
