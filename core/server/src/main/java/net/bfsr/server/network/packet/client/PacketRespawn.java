package net.bfsr.server.network.packet.client;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketBuffer;
import net.bfsr.server.PlayerManager;
import net.bfsr.server.network.NetworkManagerServer;
import net.bfsr.server.network.PacketIn;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketRespawn implements PacketIn {
    private float camPosX, camPosY;

    @Override
    public void read(PacketBuffer data) throws IOException {
        camPosX = data.readFloat();
        camPosY = data.readFloat();
    }

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager) {
        PlayerManager.getInstance().respawnPlayer(networkManager.getPlayer(), camPosX, camPosY);
    }
}