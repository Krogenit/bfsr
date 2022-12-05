package net.bfsr.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.IOException;
import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {
    private final NetworkStatistics statistics;

    public PacketDecoder(NetworkStatistics statistics) {
        this.statistics = statistics;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List list) throws IOException {
        int i = byteBuf.readableBytes();

        if (i != 0) {
            PacketBuffer packetbuffer = new PacketBuffer(byteBuf);
            int j = packetbuffer.readVarIntFromBuffer();
            Packet packet = Packet.generatePacket(channelHandlerContext.channel().attr(NetworkManager.attrKeyReceivable).get(), j);

            if (packet == null) {
                throw new IOException("Bad packet id " + j);
            } else {
                packet.read(packetbuffer);

                if (packetbuffer.readableBytes() > 0) {
                    throw new IOException("Packet was larger than I expected, found " + packetbuffer.readableBytes() + " bytes extra whilst reading packet " + j);
                } else {
                    list.add(packet);
                    this.statistics.addReceive(j, i);
                }
            }
        }
    }
}