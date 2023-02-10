package net.bfsr.network.packet.server;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.core.Core;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.server.ServerPacket;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketJoinGame extends ServerPacket {

    private long seed;

    public void read(PacketBuffer data) throws IOException {
        this.seed = data.readLong();
    }

    public void write(PacketBuffer data) throws IOException {
        data.writeLong(seed);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        Core core = Core.get();
        core.setCurrentGui(null);
        core.getWorld().setSeed(seed);
    }
}