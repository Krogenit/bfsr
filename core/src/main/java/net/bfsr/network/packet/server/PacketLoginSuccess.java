package net.bfsr.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.core.Core;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.server.ServerPacket;
import net.bfsr.network.status.EnumConnectionState;
import net.bfsr.world.WorldClient;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketLoginSuccess extends ServerPacket {

    private String playerName;

    @Override
    public void read(PacketBuffer data) throws IOException {
        this.playerName = data.readStringFromBuffer(32767);
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeStringToBuffer(this.playerName);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        networkManager.setConnectionState(EnumConnectionState.PLAY);
        Core.get().addFutureTask(() -> Core.get().setWorld(new WorldClient()));
    }

    @Override
    public boolean hasPriority() {
        return true;
    }
}