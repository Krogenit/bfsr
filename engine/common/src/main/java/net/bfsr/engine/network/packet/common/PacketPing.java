package net.bfsr.engine.network.packet.common;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.CommonPacketRegistry;
import net.bfsr.engine.network.packet.PacketAdapter;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.engine.util.Side;

import java.io.IOException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@PacketAnnotation(id = CommonPacketRegistry.PING)
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
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        originalSentTime = data.readLong();
        side = Side.get(data.readByte());
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}