package net.bfsr.client.network.packet.server.entity.ship;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.entity.GameObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.Direction;

@Getter
public class PacketSyncMoveDirection implements PacketIn {
    private int id;
    private int dir;
    private boolean remove;

    @Override
    public void read(ByteBuf data) {
        id = data.readInt();
        dir = data.readByte();
        remove = data.readBoolean();
    }

    @Override
    public void processOnClientSide() {
        Direction direction = Direction.values()[dir];
        GameObject obj = Core.get().getWorld().getEntityById(id);
        if (obj instanceof Ship ship) {
            if (remove) {
                ship.removeMoveDirection(direction);
            } else {
                ship.addMoveDirection(direction);
            }
        }
    }
}