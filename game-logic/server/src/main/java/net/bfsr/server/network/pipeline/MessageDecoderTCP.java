package net.bfsr.server.network.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.bfsr.server.network.NetworkSystem;

import java.io.IOException;
import java.util.List;

@Log4j2
@RequiredArgsConstructor
public class MessageDecoderTCP extends ByteToMessageDecoder {
    private final NetworkSystem networkSystem;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws IOException {
        list.add(networkSystem.decodePacket(byteBuf));
    }
}