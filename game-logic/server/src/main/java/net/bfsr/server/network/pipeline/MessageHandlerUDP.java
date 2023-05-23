package net.bfsr.server.network.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import net.bfsr.server.network.packet.PacketIn;

import java.net.InetSocketAddress;

@Log4j2
public class MessageHandlerUDP extends SimpleChannelInboundHandler<DefaultAddressedEnvelope<PacketIn, InetSocketAddress>> {
    @Setter
    private PlayerNetworkHandler playerNetworkHandler;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultAddressedEnvelope<PacketIn, InetSocketAddress> msg) {
        msg.content().handle(playerNetworkHandler, ctx, msg.recipient());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Error during handling UDP packet on server", cause);
        playerNetworkHandler.closeChannel("Channel exception");
    }
}