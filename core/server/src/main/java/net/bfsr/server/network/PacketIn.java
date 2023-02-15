package net.bfsr.server.network;

import net.bfsr.network.Packet;
import net.bfsr.network.PacketBuffer;

import java.io.IOException;

public interface PacketIn extends Packet {
    void read(PacketBuffer data) throws IOException;
    void processOnServerSide(NetworkManagerServer networkManager);
}
