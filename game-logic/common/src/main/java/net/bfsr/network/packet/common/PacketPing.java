package net.bfsr.network.packet.common;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PacketPing extends PacketAdapter {
    private long time;

    @Override
    public void read(ByteBuf data) throws IOException {
        this.time = data.readLong();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeLong(time);
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}