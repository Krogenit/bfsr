package net.bfsr.network.packet.server.entity.ship;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.engine.network.packet.PacketScheduled;
import net.bfsr.network.packet.PacketIdRegistry;

import java.io.IOException;

@Getter
@NoArgsConstructor
@PacketAnnotation(id = PacketIdRegistry.SHIP_SYNC_MOVE_DIRECTION)
public class PacketShipSyncMoveDirection extends PacketScheduled {
    private int id;
    private int direction;
    private boolean remove;

    public PacketShipSyncMoveDirection(int id, int direction, boolean remove, int tick) {
        super(tick);
        this.id = id;
        this.direction = direction;
        this.remove = remove;
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        super.write(data);
        data.writeInt(id);
        data.writeByte(direction);
        data.writeBoolean(remove);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        super.read(data, gameLogic);
        id = data.readInt();
        direction = data.readByte();
        remove = data.readBoolean();
    }
}