package net.bfsr.client.network.packet.common;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.core.Core;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
public class PacketPing implements PacketIn, PacketOut {
    private long time;

    @Override
    public void read(PacketBuffer data) throws IOException {
        this.time = data.readLong();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeLong(this.time);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        Core.get().getGuiInGame().setPing(time);
    }

    @Override
    public boolean hasPriority() {
        return true;
    }
}