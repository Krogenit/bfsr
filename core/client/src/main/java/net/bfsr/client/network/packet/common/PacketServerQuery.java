package net.bfsr.client.network.packet.common;

import lombok.NoArgsConstructor;
import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;

import java.io.IOException;

@NoArgsConstructor
public class PacketServerQuery implements PacketIn, PacketOut {
    @Override
    public void read(PacketBuffer data) throws IOException {}

    @Override
    public void write(PacketBuffer data) throws IOException {}

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {}

    @Override
    public boolean hasPriority() {
        return true;
    }
}