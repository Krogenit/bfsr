package net.bfsr.network;

import java.io.IOException;

public interface PacketOut extends Packet {
    void write(PacketBuffer data) throws IOException;
}
