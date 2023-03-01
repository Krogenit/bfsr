package net.bfsr.client.network.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.TimeoutException;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.client.network.packet.PacketIn;

@Log4j2
@AllArgsConstructor
public class MessageHandlerTCP extends SimpleChannelInboundHandler<PacketIn> {
    private final NetworkSystem networkSystem;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        networkSystem.closeChannels();
        networkSystem.onDisconnect("TCPChannelInactive");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String reason;

        if (cause instanceof TimeoutException) {
            reason = "timeout";
        } else {
            reason = "other";
            cause.printStackTrace();
        }

        networkSystem.closeChannels();
        networkSystem.onDisconnect(reason);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PacketIn msg) {
        msg.handle(networkSystem, ctx);
    }
}
