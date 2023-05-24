package net.bfsr.server.network.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.bfsr.network.packet.Packet;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.network.handler.PlayerNetworkHandler;

import java.net.InetSocketAddress;

@Log4j2
public class MessageHandlerUDP extends SimpleChannelInboundHandler<DefaultAddressedEnvelope<Packet, InetSocketAddress>> {
    @Setter
    private PlayerNetworkHandler playerNetworkHandler;
    private final NetworkSystem networkSystem = ServerGameLogic.getNetwork();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultAddressedEnvelope<Packet, InetSocketAddress> msg) {
        Packet packet = msg.content();
        if (packet.isAsync()) {
            networkSystem.handle(packet, playerNetworkHandler, ctx, msg.recipient());
        } else {
            playerNetworkHandler.addPacketToQueue(packet);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Error during handling UDP packet on server", cause);
        playerNetworkHandler.closeChannel("Channel exception");
    }
}