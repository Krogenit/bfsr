package net.bfsr.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.IOException;

public class PacketEncoder extends MessageToByteEncoder<Packet> {
    private final NetworkStatistics networkStatistics;

    public PacketEncoder(NetworkStatistics networkStatistics) {
        this.networkStatistics = networkStatistics;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Packet packet, ByteBuf buffer) throws IOException {
        Integer integer = (Integer) channelHandlerContext.channel().attr(NetworkManager.attrKeySendable).get().inverse().get(packet.getClass());

        if (integer == null) {
            throw new IOException("Can't serialize unregistered packet");
        } else {
            PacketBuffer packetbuffer = new PacketBuffer(buffer);
            packetbuffer.writeVarIntToBuffer(integer);
            packet.write(packetbuffer);
            this.networkStatistics.addSend(integer, packetbuffer.readableBytes());
        }
    }
}