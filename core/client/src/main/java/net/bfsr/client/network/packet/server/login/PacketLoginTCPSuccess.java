package net.bfsr.client.network.packet.server.login;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.log4j.Log4j2;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.packet.AsyncPacketIn;

import java.io.IOException;

@Log4j2
public class PacketLoginTCPSuccess implements AsyncPacketIn {
    private byte[] digest;

    @Override
    public void read(ByteBuf data) throws IOException {
        data.readBytes(digest = new byte[data.readByte()]);
    }

    @Override
    public void processOnClientSide(ChannelHandlerContext ctx) {
        log.debug("Login success. Connecting via UDP");
        Core.get().establishUDPConnection(digest);
        digest = null;
    }
}