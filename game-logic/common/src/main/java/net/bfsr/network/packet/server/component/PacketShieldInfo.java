package net.bfsr.network.packet.server.component;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@NoArgsConstructor
@Getter
public class PacketShieldInfo extends PacketAdapter {
    private int id;
    private float shieldValue;

    public PacketShieldInfo(Ship ship) {
        this.id = ship.getId();
        this.shieldValue = ship.getModules().getShield().getShield();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);
        data.writeFloat(shieldValue);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
        shieldValue = data.readFloat();
    }
}