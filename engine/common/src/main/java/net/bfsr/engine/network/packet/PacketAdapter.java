package net.bfsr.engine.network.packet;

import io.netty.buffer.ByteBuf;
import net.bfsr.engine.logic.GameLogic;

import java.io.IOException;

public class PacketAdapter implements Packet {
    @Override
    public void write(ByteBuf data) throws IOException {}

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {}

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean canProcess(double time) {
        return true;
    }
}