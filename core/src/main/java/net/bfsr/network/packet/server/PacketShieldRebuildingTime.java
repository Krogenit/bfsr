package net.bfsr.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.component.shield.Shield;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.server.ServerPacket;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketShieldRebuildingTime extends ServerPacket {

    private int id;
    private int time;

    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
        time = data.readInt();
    }

    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
        data.writeInt(time);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        CollisionObject obj = Core.get().getWorld().getEntityById(id);
        if (obj != null) {
            Ship ship = (Ship) obj;
            Shield shield = ship.getShield();
            if (shield != null) shield.setRebuildingTime(time);
        }
    }
}