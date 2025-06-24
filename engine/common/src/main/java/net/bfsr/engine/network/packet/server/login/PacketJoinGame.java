package net.bfsr.engine.network.packet.server.login;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.CommonPacketRegistry;
import net.bfsr.engine.network.packet.PacketAdapter;
import net.bfsr.engine.network.packet.PacketAnnotation;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@PacketAnnotation(id = CommonPacketRegistry.JOIN_GAME)
public class PacketJoinGame extends PacketAdapter {
    private long seed;
    private int frame;
    private double serverTime;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeLong(seed);
        data.writeInt(frame);
        data.writeDouble(serverTime);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        seed = data.readLong();
        frame = data.readInt();
        serverTime = data.readDouble();
    }
}