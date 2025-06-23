package net.bfsr.engine.network.packet.server;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.CommonPacketRegistry;
import net.bfsr.engine.network.packet.PacketAdapter;
import net.bfsr.engine.network.packet.PacketAnnotation;

import java.io.IOException;

@Getter
@NoArgsConstructor
@PacketAnnotation(id = CommonPacketRegistry.SYNC_TICK)
public class PacketSyncTick extends PacketAdapter {
    private int tick;
    private double time;

    public PacketSyncTick(int tick, double time) {
        this.tick = tick;
        this.time = time;
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(tick);
        data.writeDouble(time);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        tick = data.readInt();
        time = data.readDouble();
    }
}
