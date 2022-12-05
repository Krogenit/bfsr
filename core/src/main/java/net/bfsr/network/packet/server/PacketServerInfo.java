package net.bfsr.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.server.ServerPacket;
import net.bfsr.network.status.ServerStatusResponse;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
public class PacketServerInfo extends ServerPacket {

    private ServerStatusResponse serverStatus;

    @Override
    public void read(PacketBuffer data) throws IOException {
        this.serverStatus = new ServerStatusResponse(data.readInt(), data.readStringFromBuffer(320));
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(serverStatus.getPlayerCountData());
        data.writeStringToBuffer(serverStatus.getVersion());
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {

    }

    @Override
    public boolean hasPriority() {
        return true;
    }
}