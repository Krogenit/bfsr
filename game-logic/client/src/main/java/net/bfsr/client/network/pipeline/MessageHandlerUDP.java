package net.bfsr.client.network.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.TimeoutException;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.client.network.packet.PacketIn;
import org.jetbrains.annotations.NotNull;

@Log4j2
@AllArgsConstructor
public class MessageHandlerUDP extends SimpleChannelInboundHandler<PacketIn> {
    private final NetworkSystem networkSystem;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PacketIn msg) {
        msg.handle(networkSystem, ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String reason;

        if (cause instanceof TimeoutException) {
            reason = "timeout";
        } else {
            reason = "other";
            log.error("Error during handling UDP packet on client", cause);
        }

        networkSystem.closeChannels();
        networkSystem.onDisconnect(reason);
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) {
        if (networkSystem.isChannelOpen()) {
            networkSystem.closeChannels();
            networkSystem.onDisconnect("UDPChannelInactive");
        }
    }
}