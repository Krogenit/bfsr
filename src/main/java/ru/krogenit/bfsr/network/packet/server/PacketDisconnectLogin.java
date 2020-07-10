package ru.krogenit.bfsr.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.krogenit.bfsr.network.PacketBuffer;
import ru.krogenit.bfsr.network.client.NetworkManagerClient;
import ru.krogenit.bfsr.network.server.ServerPacket;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketDisconnectLogin extends ServerPacket {

    private String message;

    @Override
    public void read(PacketBuffer data) throws IOException {
        this.message = data.readStringFromBuffer(32767);
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeStringToBuffer(this.message);
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