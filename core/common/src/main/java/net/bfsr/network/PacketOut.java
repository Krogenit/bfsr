package net.bfsr.network;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

public interface PacketOut extends Packet {
    void write(ByteBuf data) throws IOException;
}
