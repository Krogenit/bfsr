package net.bfsr.server.network.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.bfsr.network.packet.Packet;
import net.bfsr.server.network.NetworkSystem;
import net.bfsr.server.network.handler.PlayerNetworkHandler;
import org.jetbrains.annotations.NotNull;

import java.net.SocketException;

@Log4j2
@RequiredArgsConstructor
public class MessageHandlerTCP extends SimpleChannelInboundHandler<Packet> {
    private final PlayerNetworkHandler playerNetworkHandler;
    private final NetworkSystem networkSystem;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet msg) {
        if (msg.isAsync()) {
            networkSystem.handle(msg, playerNetworkHandler, ctx);
        } else {
            playerNetworkHandler.addPacketToQueue(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (shouldLogException(ctx, cause)) {
            log.error("Error during handling TCP packet on server", cause);
        }

        playerNetworkHandler.closeChannel("Channel exception");
    }

    private boolean shouldLogException(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof SocketException socketException && socketException.getMessage().equals("Connection reset")) {
            log.debug("{} from {}", cause.getMessage(), ctx.channel().remoteAddress());
            return false;
        }

        return true;
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) {
        playerNetworkHandler.closeChannel("Channel inactive");
    }
}