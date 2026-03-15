package net.bfsr.server.network.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.AllArgsConstructor;
import net.bfsr.engine.network.packet.Packet;
import net.bfsr.server.network.NetworkSystem;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

@AllArgsConstructor
public class PacketEncoderUDP extends MessageToMessageEncoder<AddressedEnvelope<Packet, InetSocketAddress>> {
    private final NetworkSystem networkSystem;

    @Override
    protected void encode(ChannelHandlerContext ctx, AddressedEnvelope<Packet, InetSocketAddress> msg, List<Object> out)
            throws IOException {
        Packet packet = msg.content();
        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeShort(networkSystem.getPacketId(packet));
        packet.write(buffer);
        out.add(new DatagramPacket(buffer, msg.recipient(), msg.sender()));
    }
}