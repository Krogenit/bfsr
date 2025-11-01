package net.bfsr.server.network.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import net.bfsr.engine.network.packet.Packet;
import net.bfsr.server.network.NetworkSystem;

import java.io.IOException;

@AllArgsConstructor
public class PacketEncoderTCP extends MessageToByteEncoder<Packet> {
    private final NetworkSystem networkSystem;

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) throws IOException {
        out.writeShort(networkSystem.getPacketId(msg));
        msg.write(out);
    }
}