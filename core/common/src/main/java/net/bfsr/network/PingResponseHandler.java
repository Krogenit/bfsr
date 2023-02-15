package net.bfsr.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class PingResponseHandler extends ChannelInboundHandlerAdapter {

    public void channelRead(ChannelHandlerContext ctx, Object object) {
        ByteBuf buffer = (ByteBuf) object;
        buffer.markReaderIndex();

        try {
            buffer.readUnsignedByte();
        } catch (RuntimeException ignored) {
            ;
        } finally {
            buffer.resetReaderIndex();
            ctx.channel().pipeline().remove("legacy_query");
            ctx.fireChannelRead(object);
        }
    }
}