package net.bfsr.server.network.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.bfsr.server.network.NetworkSystem;

import java.io.IOException;
import java.util.List;

@Log4j2
@RequiredArgsConstructor
public class MessageDecoderUDP extends MessageToMessageDecoder<DatagramPacket> {
    private final NetworkSystem networkSystem;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, DatagramPacket msg, List<Object> list) throws IOException {
        list.add(new DefaultAddressedEnvelope<>(networkSystem.decodePacket(msg.content()), msg.sender()));
    }
}