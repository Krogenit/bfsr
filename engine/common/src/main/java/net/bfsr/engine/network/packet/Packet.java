package net.bfsr.engine.network.packet;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

public interface Packet {
    void write(ByteBuf data) throws IOException;
    void read(ByteBuf data) throws IOException;
    boolean isAsync();
    boolean canProcess(double time);
}