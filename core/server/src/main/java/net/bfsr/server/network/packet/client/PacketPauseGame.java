package net.bfsr.server.network.packet.client;

import net.bfsr.network.PacketBuffer;
import net.bfsr.server.network.NetworkManagerServer;
import net.bfsr.server.network.PacketIn;

import java.io.IOException;

public class PacketPauseGame implements PacketIn {
    @Override
    public void read(PacketBuffer data) throws IOException {

    }

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager) {
        if (networkManager.getServer().isSinglePlayer()) {
            networkManager.getServer().setPause(!networkManager.getServer().isPause());
        }
    }
}
