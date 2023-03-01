package net.bfsr.server.network.packet.server;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketJoinGame implements PacketOut {
    private long seed;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeLong(seed);
    }
}