package net.bfsr.client.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.EnumConnectionState;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.world.WorldClient;
import net.bfsr.network.PacketBuffer;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketLoginSuccess implements PacketIn {
    private String playerName;

    @Override
    public void read(PacketBuffer data) throws IOException {
        this.playerName = data.readStringFromBuffer(32767);
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