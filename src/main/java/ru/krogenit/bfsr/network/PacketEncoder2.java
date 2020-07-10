package ru.krogenit.bfsr.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import ru.krogenit.bfsr.network.PacketBuffer;

public class PacketEncoder2 extends MessageToByteEncoder<ByteBuf> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf packetBuffer, ByteBuf byteBuf) {
        int i = packetBuffer.readableBytes();
        int j = PacketBuffer.getVarIntSize(i);

        if (j > 3) {
            throw new IllegalArgumentException("unable to fit " + i + " into " + 3);
        } else {
            PacketBuffer packetbuffer = new PacketBuffer(byteBuf);
            packetbuffer.ensureWritable(j + i);
            packetbuffer.writeVarIntToBuffer(i);
            packetbuffer.writeBytes(packetBuffer, packetBuffer.readerIndex(), i);
        }
    }
}