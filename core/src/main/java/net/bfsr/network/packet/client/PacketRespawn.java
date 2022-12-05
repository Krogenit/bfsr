package net.bfsr.network.packet.client;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.entity.ship.PlayerServer;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.ClientPacket;
import net.bfsr.network.server.NetworkManagerServer;
import net.bfsr.server.MainServer;
import net.bfsr.server.PlayerManager;
import net.bfsr.world.WorldServer;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketRespawn extends ClientPacket {

    private float camPosX, camPosY;

    @Override
    public void read(PacketBuffer data) throws IOException {
        camPosX = data.readFloat();
        camPosY = data.readFloat();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeFloat(camPosX);
        data.writeFloat(camPosY);
    }

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager, MainServer server, WorldServer world, PlayerServer player) {
        PlayerManager.getInstance().respawnPlayer(player, camPosX, camPosY);
    }
}