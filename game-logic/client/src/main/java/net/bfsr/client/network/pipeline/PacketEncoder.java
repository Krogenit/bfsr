package net.bfsr.client.network.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import net.bfsr.client.network.NetworkSystem;
import net.bfsr.network.packet.Packet;

import java.io.IOException;

@AllArgsConstructor
public class PacketEncoder extends MessageToByteEncoder<Packet> {
    private final NetworkSystem networkSystem;

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) throws IOException {
        out.writeByte(networkSystem.getPacketId(msg));
        msg.write(out);
    }
}