package net.bfsr.network.packet.server.effect;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.network.packet.PacketScheduled;

import java.io.IOException;

@Getter
@NoArgsConstructor
public class PacketHullCellDestroy extends PacketScheduled {
    private int entityId;
    private short cellX, cellY;

    public PacketHullCellDestroy(int entityId, int cellX, int cellY, double timestamp) {
        super(timestamp);
        this.entityId = entityId;
        this.cellX = (short) cellX;
        this.cellY = (short) cellY;
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        super.write(data);
        data.writeInt(entityId);
        data.writeShort(cellX);
        data.writeShort(cellY);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        super.read(data);
        entityId = data.readInt();
        cellX = data.readShort();
        cellY = data.readShort();
    }
}
