package net.bfsr.network.packet.server.component;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.engine.network.packet.PacketScheduled;
import net.bfsr.network.packet.PacketIdRegistry;

import java.io.IOException;

@Getter
@NoArgsConstructor
@PacketAnnotation(id = PacketIdRegistry.SHIELD_REBUILDING_TIME)
public class PacketShieldRebuildingTime extends PacketScheduled {
    private int id;
    private int time;

    public PacketShieldRebuildingTime(int id, int time, int frame) {
        super(frame);
        this.id = id;
        this.time = time;
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        super.write(data);
        data.writeInt(id);
        data.writeInt(time);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        super.read(data, gameLogic);
        id = data.readInt();
        time = data.readInt();
    }
}