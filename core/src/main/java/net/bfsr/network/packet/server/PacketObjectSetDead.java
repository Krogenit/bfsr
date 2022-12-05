package net.bfsr.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.server.ServerPacket;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketObjectSetDead extends ServerPacket {

    private int id;

    public PacketObjectSetDead(CollisionObject obj) {
        this.id = obj.getId();
    }

    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
    }

    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        CollisionObject obj = Core.getCore().getWorld().getEntityById(id);
        if (obj != null) {
            obj.setDead(true);
        }
    }
}