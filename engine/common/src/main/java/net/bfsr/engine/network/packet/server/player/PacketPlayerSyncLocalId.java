package net.bfsr.engine.network.packet.server.player;

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
@PacketAnnotation(id = CommonPacketRegistry.PLAYER_SYNC_LOCAL_ID)
public class PacketPlayerSyncLocalId extends PacketScheduled {
    private int localId;

    public PacketPlayerSyncLocalId(int localId, int tick) {
        super(tick);
        this.localId = localId;
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        super.write(data);
        data.writeInt(localId);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        super.read(data, gameLogic);
        localId = data.readInt();
    }

    @Override
    public boolean canProcess(int tick) {
        return true;
    }
}
