package net.bfsr.network.packet.client;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.PlayerServer;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.ClientPacket;
import net.bfsr.network.server.NetworkManagerServer;
import net.bfsr.server.MainServer;
import net.bfsr.world.WorldServer;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketShipControl extends ClientPacket {

    private int id;
    private boolean control;

    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
        control = data.readBoolean();
    }

    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
        data.writeBoolean(control);
    }

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
        CollisionObject obj = world.getEntityById(id);
        if (obj != null) {
            Ship ship = (Ship) obj;
            ship.setControlledByPlayer(control);
        }
    }
}