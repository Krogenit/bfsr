package net.bfsr.server.network.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class MessageDecoderUDP extends MessageToMessageDecoder<DatagramPacket> {
    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) {
        out.add(new DefaultAddressedEnvelope<>(PacketDecodeUtils.decodePacket(msg.content()), msg.sender()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Error during decoding UDP packet", cause);
    }
}
