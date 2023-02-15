package net.bfsr.client.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.status.ServerStatusResponse;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
public class PacketServerInfo implements PacketIn {
    private ServerStatusResponse serverStatus;

    @Override
    public void read(PacketBuffer data) throws IOException {
        this.serverStatus = new ServerStatusResponse(data.readInt(), data.readStringFromBuffer(320));
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {

    }

    @Override
    public boolean hasPriority() {
        return true;
    }
}