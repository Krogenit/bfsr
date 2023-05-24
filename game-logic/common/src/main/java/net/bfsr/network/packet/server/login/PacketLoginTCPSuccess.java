package net.bfsr.network.packet.server.login;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@Log4j2
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PacketLoginTCPSuccess extends PacketAdapter {
    private byte[] digest;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeByte(digest.length);
        data.writeBytes(digest);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        data.readBytes(digest = new byte[data.readByte()]);
    }

    public void clearDigest() {
        digest = null;
    }
}