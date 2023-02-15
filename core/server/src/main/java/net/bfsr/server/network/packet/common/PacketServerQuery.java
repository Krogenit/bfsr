package net.bfsr.server.network.packet.common;

import lombok.NoArgsConstructor;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;
import net.bfsr.server.network.NetworkManagerServer;
import net.bfsr.server.network.PacketIn;
import net.bfsr.server.network.packet.server.PacketServerInfo;

import java.io.IOException;

@NoArgsConstructor
public class PacketServerQuery implements PacketIn, PacketOut {
    @Override
    public void read(PacketBuffer data) throws IOException {}

    @Override
    public void write(PacketBuffer data) throws IOException {}

    @Override
    public void processOnServerSide(NetworkManagerServer networkManager) {
        networkManager.scheduleOutboundPacket(new PacketServerInfo(networkManager.getServer().getStatus()));
    }

    @Override
    public boolean hasPriority() {
        return true;
    }
}