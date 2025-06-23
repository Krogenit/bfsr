package net.bfsr.network.packet.server.entity.ship;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.engine.network.packet.PacketScheduled;
import net.bfsr.engine.world.entity.RigidBody;
import net.bfsr.network.packet.PacketIdRegistry;

import java.io.IOException;

@Getter
@NoArgsConstructor
@PacketAnnotation(id = PacketIdRegistry.SHIP_SET_DESTROYING)
public class PacketShipSetDestroying extends PacketScheduled {
    private int id;

    public PacketShipSetDestroying(RigidBody obj, int tick) {
        super(tick);
        this.id = obj.getId();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        super.write(data);
        data.writeInt(id);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        super.read(data, gameLogic);
        id = data.readInt();
    }
}