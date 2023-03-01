package net.bfsr.client.network.packet.common;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.ship.Ship;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.entity.GameObject;
import net.bfsr.math.Direction;
import net.bfsr.network.PacketOut;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PacketShipEngine implements PacketIn, PacketOut {
    private int id;
    private int dir;

    @Override
    public void read(ByteBuf data) {
        id = data.readInt();
        dir = data.readInt();
    }

    @Override
    public void write(ByteBuf data) {
        data.writeInt(id);
        data.writeInt(dir);
    }

    @Override
    public void processOnClientSide() {
        Direction direction = Direction.values()[dir];
        GameObject obj = Core.get().getWorld().getEntityById(id);
        if (obj instanceof Ship ship) {
            ship.setMoveDirection(direction);
        }
    }
}