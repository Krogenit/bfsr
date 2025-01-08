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
    private long otherSideHandleTime;
    private Side side;

    private long roundTripTime;

    public PacketPing(long originalSentTime, long otherSideHandleTime, Side side) {
        this.originalSentTime = originalSentTime;
        this.otherSideHandleTime = otherSideHandleTime;
        this.side = side;
    }

    public PacketPing(Side side) {
        this.originalSentTime = System.nanoTime();
        this.side = side;
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeLong(originalSentTime);
        data.writeLong(otherSideHandleTime);
        data.writeByte(side.ordinal());
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        originalSentTime = data.readLong();
        roundTripTime = System.nanoTime() - originalSentTime;
        otherSideHandleTime = data.readLong();
        side = Side.get(data.readByte());
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}