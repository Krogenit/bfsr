package net.bfsr.client.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketHandshake implements PacketOut {
    private int version;
    private long handshakeClientTime;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeByte(version);
        data.writeLong(handshakeClientTime);
    }
}
