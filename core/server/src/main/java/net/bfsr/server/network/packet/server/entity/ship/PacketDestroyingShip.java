package net.bfsr.server.network.packet.server.entity.ship;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.entity.RigidBody;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketDestroyingShip implements PacketOut {
    private int id;

    public PacketDestroyingShip(RigidBody obj) {
        this.id = obj.getId();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);
    }
}