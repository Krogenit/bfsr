package net.bfsr.network.packet;

import lombok.NoArgsConstructor;
import net.bfsr.entity.ship.PlayerServer;
import net.bfsr.network.Packet;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.packet.server.PacketServerInfo;
import net.bfsr.network.server.NetworkManagerServer;
import net.bfsr.server.MainServer;
import net.bfsr.world.WorldServer;

import java.io.IOException;

@NoArgsConstructor
public class PacketServerQuery extends Packet {

    @Override
    public void read(PacketBuffer data) throws IOException {

    }

    @Override
    public void write(PacketBuffer data) throws IOException {

    }

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
        networkManager.scheduleOutboundPacket(new PacketServerInfo(server.getStatus()));
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {

    }

    @Override
    public boolean hasPriority() {
        return true;
    }
}