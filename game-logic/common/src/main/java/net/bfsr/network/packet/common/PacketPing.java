package net.bfsr.network.packet.common;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.util.Side;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PacketPing extends PacketAdapter {
    private long originalSentTime;
    private Side side;

    public PacketPing(Side side) {
        this.originalSentTime = System.nanoTime();
        this.side = side;
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeLong(originalSentTime);
        data.writeByte(side.ordinal());
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        originalSentTime = data.readLong();
        side = Side.get(data.readByte());
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}