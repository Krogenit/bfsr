package net.bfsr.client.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.network.PacketBuffer;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketDisconnectLogin implements PacketIn {
    private String message;

    @Override
    public void read(PacketBuffer data) throws IOException {
        this.message = data.readStringFromBuffer(32767);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        networkManager.closeChannel(message);
    }

    @Override
    public boolean hasPriority() {
        return true;
    }
}