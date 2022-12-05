package net.bfsr.network.packet.common;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
public class PacketKeepAlive extends Packet {

    private int time;

    @Override
    public void read(PacketBuffer data) throws IOException {
        this.time = data.readInt();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(this.time);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        networkManager.scheduleOutboundPacket(new PacketKeepAlive(time));
    }

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
        if (time == networkManager.getCurrentTimeInt()) {
            int var2 = (int) (System.nanoTime() / 1000000L - networkManager.getCurrentTime());
            player.setPing((player.getPing() * 3 + var2) / 4);
        }
    }

    @Override
    public boolean hasPriority() {
        return true;
    }
}