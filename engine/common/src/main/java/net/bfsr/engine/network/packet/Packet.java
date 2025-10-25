package net.bfsr.engine.network.packet;

import io.netty.buffer.ByteBuf;
import net.bfsr.engine.logic.GameLogic;

import java.io.IOException;

public interface Packet {
    void write(ByteBuf data) throws IOException;
    void read(ByteBuf data, GameLogic gameLogic) throws IOException;
    boolean isAsync();
    boolean canProcess(int frame);
}