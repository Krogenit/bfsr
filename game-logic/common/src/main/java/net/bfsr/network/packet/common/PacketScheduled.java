package net.bfsr.network.packet.common;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class PacketScheduled extends PacketAdapter {
    private double timestamp;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeDouble(timestamp);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        timestamp = data.readDouble();
    }

    @Override
    public boolean canProcess(double time) {
        return time >= timestamp;
    }
}