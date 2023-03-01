package net.bfsr.server.network.packet.server;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketOut;
import net.bfsr.server.entity.CollisionObject;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketDestroyingShip implements PacketOut {
    private int id;

    public PacketDestroyingShip(CollisionObject obj) {
        this.id = obj.getId();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);
    }
}