package net.bfsr.engine.network.pipeline.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class LengthPrepender extends MessageToMessageEncoder<ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        int length = msg.readableBytes();
        if (length >= 65536) throw new IllegalArgumentException("length does not fit into a short integer: " + length);
        out.add(ctx.alloc().buffer(2).writeShort((short) length));
        out.add(msg.retain());
    }
}