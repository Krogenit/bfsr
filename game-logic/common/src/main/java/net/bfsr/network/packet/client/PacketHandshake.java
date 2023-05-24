package net.bfsr.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PacketHandshake extends PacketAdapter {
    private int version;
    private long handshakeClientTime;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeByte(version);
        data.writeLong(handshakeClientTime);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        version = data.readByte();
        handshakeClientTime = data.readLong();
    }
}