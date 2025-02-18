package net.bfsr.network.packet.server.player;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.common.PacketScheduled;

import java.io.IOException;

@NoArgsConstructor
@Getter
public class PacketSetPlayerShip extends PacketScheduled {
    private int id;
    private float x;
    private float y;

    public PacketSetPlayerShip(Ship ship, double timestamp) {
        super(timestamp);
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
    public void read(ByteBuf data) throws IOException {
        super.read(data);
        id = data.readInt();
        x = data.readFloat();
        y = data.readFloat();
    }
}