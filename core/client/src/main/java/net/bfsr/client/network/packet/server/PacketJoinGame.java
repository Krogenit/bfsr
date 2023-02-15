package net.bfsr.client.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.network.PacketBuffer;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketJoinGame implements PacketIn {
    private long seed;

    @Override
    public void read(PacketBuffer data) throws IOException {
        this.seed = data.readLong();
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        Core core = Core.get();
        core.setCurrentGui(null);
        core.getWorld().setSeed(seed);
    }
}