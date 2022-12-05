package net.bfsr.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.List;

public class PacketDecoder2 extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf buffer, List list) {
        buffer.markReaderIndex();
        byte[] abyte = new byte[3];

        for (int i = 0; i < abyte.length; ++i) {
            if (!buffer.isReadable()) {
                buffer.resetReaderIndex();
                return;
            }

            abyte[i] = buffer.readByte();

            if (abyte[i] >= 0) {
                PacketBuffer packetbuffer = new PacketBuffer(Unpooled.wrappedBuffer(abyte));

                try {
                    int j = packetbuffer.readVarIntFromBuffer();

                    if (buffer.readableBytes() >= j) {
                        list.add(buffer.readBytes(j));
                        return;
                    }

                    buffer.resetReaderIndex();
                } finally {
                    packetbuffer.release();
                }

                return;
            }
        }

        throw new CorruptedFrameException("length wider than 21-bit");
    }
}