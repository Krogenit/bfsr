package net.bfsr.server.network.packet.client;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.entity.GameObject;
import net.bfsr.network.PacketBuffer;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.network.NetworkManagerServer;
import net.bfsr.server.network.PacketIn;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketShipControl implements PacketIn {
    private int id;
    private boolean control;

    @Override
    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
        control = data.readBoolean();
    }

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager) {
        GameObject obj = networkManager.getWorld().getEntityById(id);
        if (obj instanceof Ship ship) {
            ship.setControlledByPlayer(control);
        }
    }
}