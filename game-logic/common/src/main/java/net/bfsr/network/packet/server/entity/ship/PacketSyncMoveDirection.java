package net.bfsr.network.packet.server.entity.ship;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PacketSyncMoveDirection extends PacketAdapter {
    private int id;
    private int direction;
    private boolean remove;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);
        data.writeByte(direction);
        data.writeBoolean(remove);
    }

    @Override
    public void read(ByteBuf data) {
        id = data.readInt();
        direction = data.readByte();
        remove = data.readBoolean();
    }
}