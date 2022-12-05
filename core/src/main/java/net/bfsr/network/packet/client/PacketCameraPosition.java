package net.bfsr.network.packet.client;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.entity.ship.PlayerServer;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.ClientPacket;
import net.bfsr.network.server.NetworkManagerServer;
import net.bfsr.server.MainServer;
import net.bfsr.world.WorldServer;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketCameraPosition extends ClientPacket {

    private float x, y;

    @Override
    public void read(PacketBuffer data) throws IOException {
        x = data.readFloat();
        y = data.readFloat();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeFloat(x);
        data.writeFloat(y);
    }

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
        player.setPosition(x, y);
    }
}