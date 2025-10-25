package net.bfsr.client.network.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.network.NetworkSystem;

import java.io.IOException;
import java.util.List;

@Log4j2
@RequiredArgsConstructor
public class MessageDecoderUDP extends MessageToMessageDecoder<DatagramPacket> {
    private final NetworkSystem networkSystem;

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws IOException {
        out.add(networkSystem.decodePacket(msg.content()));
    }
}