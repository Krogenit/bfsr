package net.bfsr.network.packet.client;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PacketHandshake extends PacketAdapter {
    private long handshakeClientTime;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeLong(handshakeClientTime);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        handshakeClientTime = data.readLong();
    }
}