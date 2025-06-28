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
    private int frame;

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(frame);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        frame = data.readInt();
    }

    @Override
    public boolean canProcess(int frame) {
        return frame >= this.frame;
    }
}