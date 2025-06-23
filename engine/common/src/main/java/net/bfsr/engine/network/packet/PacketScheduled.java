package net.bfsr.engine.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;

import java.io.IOException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class PacketScheduled extends PacketAdapter {
    private int tick;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(tick);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        tick = data.readInt();
    }

    @Override
    public boolean canProcess(int tick) {
        return tick >= this.tick;
    }
}