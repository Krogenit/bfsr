package net.bfsr.client.network.packet;

import net.bfsr.client.network.NetworkManagerClient;
import net.bfsr.network.Packet;
import net.bfsr.network.PacketBuffer;

import java.io.IOException;

public interface PacketIn extends Packet {
    void read(PacketBuffer data) throws IOException;
    void processOnClientSide(NetworkManagerClient networkManager);
}