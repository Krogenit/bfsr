package net.bfsr.network.packet.server.entity.ship;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.network.packet.common.PacketScheduled;

import java.io.IOException;

@Getter
@NoArgsConstructor
public class PacketSyncMoveDirection extends PacketScheduled {
    private int id;
    private int direction;
    private boolean remove;

    public PacketSyncMoveDirection(int id, int direction, boolean remove, double timestamp) {
        super(timestamp);
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
    public void read(ByteBuf data) throws IOException {
        super.read(data);
        id = data.readInt();
        direction = data.readByte();
        remove = data.readBoolean();
    }
}