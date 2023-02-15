package net.bfsr.client.network.packet.common;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;

@NoArgsConstructor
@AllArgsConstructor
public class PacketKeepAlive implements PacketIn, PacketOut {
    private int time;

    @Override
    public void read(PacketBuffer data) {
        this.time = data.readInt();
    }

    @Override
    public void write(PacketBuffer data) {
        data.writeInt(this.time);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        networkManager.scheduleOutboundPacket(new PacketKeepAlive(time));
    }

    @Override
    public boolean hasPriority() {
        return true;
    }
}