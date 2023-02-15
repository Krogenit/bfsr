package net.bfsr.server.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.entity.CollisionObject;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketDestroingShip implements PacketOut {
    private int id;

    public PacketDestroingShip(CollisionObject obj) {
        this.id = obj.getId();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
    }
}