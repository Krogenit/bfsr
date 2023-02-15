package net.bfsr.server.network.packet.client;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketBuffer;
import net.bfsr.server.network.NetworkManagerServer;
import net.bfsr.server.network.PacketIn;

@AllArgsConstructor
@NoArgsConstructor
public class PacketCameraPosition implements PacketIn {
    private float x, y;

    @Override
    public void read(PacketBuffer data) {
        x = data.readFloat();
        y = data.readFloat();
    }

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager) {
        networkManager.getPlayer().setPosition(x, y);
    }
}