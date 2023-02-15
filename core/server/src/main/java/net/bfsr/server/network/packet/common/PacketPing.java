package net.bfsr.server.network.packet.common;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;
import net.bfsr.server.network.NetworkManagerServer;
import net.bfsr.server.network.PacketIn;

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
    public void processOnServerSide(NetworkManagerServer networkManager) {
        this.time = System.currentTimeMillis() - time;
        networkManager.scheduleOutboundPacket(new PacketPing(time));
    }

    @Override
    public boolean hasPriority() {
        return true;
    }
}