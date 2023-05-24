package net.bfsr.network.packet.server.component;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.PacketAdapter;

import java.io.IOException;

@NoArgsConstructor
@Getter
public class PacketHullInfo extends PacketAdapter {
    private int id;
    private float hull;

    public PacketHullInfo(Ship ship) {
        this.id = ship.getId();
        this.hull = ship.getHull().getHull();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(id);
        data.writeFloat(hull);
    }

    @Override
    public void read(ByteBuf data) throws IOException {
        id = data.readInt();
        hull = data.readFloat();
    }
}