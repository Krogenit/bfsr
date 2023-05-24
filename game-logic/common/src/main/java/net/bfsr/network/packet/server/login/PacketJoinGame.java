package net.bfsr.network.packet.server.login;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PacketJoinGame extends PacketAdapter {
    private long seed;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeLong(seed);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        this.seed = data.readLong();
    }
}