package net.bfsr.server.network.packet.server.login;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@AllArgsConstructor
public class PacketLoginTCPSuccess implements PacketOut {
    private byte[] digest;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeByte(digest.length);
        data.writeBytes(digest);
    }
}