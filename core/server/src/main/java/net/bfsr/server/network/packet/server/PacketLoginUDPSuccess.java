package net.bfsr.server.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.bfsr.network.PacketOut;

import java.io.IOException;

public class PacketLoginUDPSuccess implements PacketOut {
    @Override
    public void write(ByteBuf data) throws IOException {}
}