package net.bfsr.server.network.packet.server;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.PacketOut;
import net.bfsr.server.entity.ship.Ship;
import org.joml.Vector2f;

import java.io.IOException;

@NoArgsConstructor
@Log4j2
public class PacketSpawnShip implements PacketOut {
    private int id;
    private String shipClassName;
    private Vector2f position;
    private float rot;
    private boolean isSpawned;

    public PacketSpawnShip(Ship ship) {
        this.id = ship.getId();
        this.position = ship.getPosition();
        this.rot = ship.getRotation();
        this.shipClassName = ship.getClass().getSimpleName();
        this.isSpawned = ship.isSpawned();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);
        data.writeVector2f(position);
        data.writeFloat(rot);
        data.writeStringToBuffer(shipClassName);
        data.writeBoolean(isSpawned);
    }
}