package net.bfsr.client.network;

import com.google.common.collect.BiMap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.network.NetworkStatistics;
import net.bfsr.network.PacketBuffer;

import java.io.IOException;
import java.util.List;

@Log4j2
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
            PacketIn packet = generatePacket(channelHandlerContext.channel().attr(NetworkManager.attrKeyReceivable).get(), j);

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

    /**
     * Returns a packet instance, given the params: BiMap<int, (Packet) Class> and (int) id
     */
    private PacketIn generatePacket(BiMap biMap, int id) {
        try {
            Class<?> oclass = (Class<?>) biMap.get(id);
            return oclass == null ? null : (PacketIn) oclass.getConstructor().newInstance();
        } catch (Exception e) {
            log.error("Couldn't create packet {}", id, e);
            return null;
        }
    }
}