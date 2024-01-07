package net.bfsr.server.network.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.log4j.Log4j2;
import net.bfsr.network.packet.Packet;
import net.bfsr.server.ServerGameLogic;
import net.bfsr.server.network.NetworkSystem;

import java.net.InetSocketAddress;

@Log4j2
public class MessageHandlerUDP extends SimpleChannelInboundHandler<DefaultAddressedEnvelope<Packet, InetSocketAddress>> {
    private final NetworkSystem networkSystem = ServerGameLogic.getNetwork();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultAddressedEnvelope<Packet, InetSocketAddress> msg) {
        Packet packet = msg.content();
        long address = NetworkSystem.convertAddress(msg.recipient());
        if (packet.isAsync()) {
            networkSystem.handle(packet, networkSystem.getHandler(address), ctx,
                    msg.recipient());
        } else {
            networkSystem.getHandler(address).addPacketToQueue(packet);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Error during handling UDP packet on server", cause);
    }
}