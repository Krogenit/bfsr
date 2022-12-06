package net.bfsr.network.packet.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.PlayerServer;
import net.bfsr.entity.ship.Ship;
import net.bfsr.math.Direction;
import net.bfsr.network.Packet;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.server.NetworkManagerServer;
import net.bfsr.server.MainServer;
import net.bfsr.world.WorldServer;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PacketShipEngine extends Packet {

    private int id;
    private int dir;

    @Override
    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();
        dir = data.readInt();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
        data.writeInt(dir);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        Direction direction = Direction.values()[dir];
        CollisionObject obj = Core.getCore().getWorld().getEntityById(id);
        if (obj instanceof Ship) {
            ((Ship) obj).setMoveDirection(direction);
        }
    }

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
        server.getNetworkSystem().sendPacketToAllExcept(new PacketShipEngine(id, dir), player);
    }
}