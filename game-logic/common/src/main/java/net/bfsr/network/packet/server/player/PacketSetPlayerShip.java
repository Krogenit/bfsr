package net.bfsr.network.packet.server.player;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.engine.logic.GameLogic;
import net.bfsr.engine.network.packet.PacketAnnotation;
import net.bfsr.engine.network.packet.PacketScheduled;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.PacketIdRegistry;

import java.io.IOException;

@NoArgsConstructor
@Getter
@PacketAnnotation(id = PacketIdRegistry.PLAYER_SET_SHIP)
public class PacketSetPlayerShip extends PacketScheduled {
    private int id;
    private float x;
    private float y;

    public PacketSetPlayerShip(Ship ship, int tick) {
        super(tick);
        this.id = ship.getId();
        this.x = ship.getX();
        this.y = ship.getY();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        super.write(data);
        data.writeInt(id);
        data.writeFloat(x);
        data.writeFloat(y);
    }

    @Override
    public void read(ByteBuf data, GameLogic gameLogic) throws IOException {
        super.read(data, gameLogic);
        id = data.readInt();
        x = data.readFloat();
        y = data.readFloat();
    }
}