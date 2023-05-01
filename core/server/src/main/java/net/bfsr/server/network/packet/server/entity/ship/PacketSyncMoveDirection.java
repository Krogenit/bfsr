package net.bfsr.server.network.packet.server.entity.ship;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PacketSyncMoveDirection implements PacketOut {
    private int id;
    private int direction;
    private boolean remove;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);
        data.writeByte(direction);
        data.writeBoolean(remove);
    }
}