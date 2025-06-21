package net.bfsr.engine.network.packet.server;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.CommonPacketRegistry;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.engine.network.packet.PacketScheduled;

import java.io.IOException;

@Getter
@NoArgsConstructor
@PacketAnnotation(id = CommonPacketRegistry.SYNC_TICK)
public class PacketSyncTick extends PacketScheduled {
    private int tick;

    public PacketSyncTick(int tick, double timestamp) {
        super(timestamp);
        this.tick = tick;
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        super.write(data);
        data.writeInt(tick);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        super.read(data, gameLogic);
        tick = data.readInt();
    }

    @Override
    public boolean canProcess(double time) {
        return true;
    }
}
