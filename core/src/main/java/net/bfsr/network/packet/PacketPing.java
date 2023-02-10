package net.bfsr.network.packet;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.core.Core;
import net.bfsr.entity.ship.PlayerServer;
import net.bfsr.network.Packet;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.server.NetworkManagerServer;
import net.bfsr.server.MainServer;
import net.bfsr.world.WorldServer;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
public class PacketPing extends Packet {
    private long time;

    @Override
    public void read(PacketBuffer data) throws IOException {
        this.time = data.readLong();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeLong(this.time);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        Core.get().getGuiInGame().setPing(time);
    }

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
        this.time = System.currentTimeMillis() - time;
        networkManager.scheduleOutboundPacket(new PacketPing(time));
    }

    @Override
    public boolean hasPriority() {
        return true;
    }
}