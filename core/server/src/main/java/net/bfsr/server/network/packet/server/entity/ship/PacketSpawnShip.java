package net.bfsr.server.network.packet.server.entity.ship;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.bfsr.network.PacketOut;
import net.bfsr.network.util.ByteBufUtils;
import net.bfsr.server.component.weapon.WeaponSlot;
import net.bfsr.server.entity.ship.Ship;
import org.joml.Vector2f;

import java.io.IOException;
import java.util.List;

@NoArgsConstructor
@Log4j2
public class PacketSpawnShip implements PacketOut {
    private Vector2f position;
    private float rot;
    private Ship ship;

    public PacketSpawnShip(Ship ship) {
        this.ship = ship;
        this.position = ship.getPosition();
        this.rot = ship.getRotation();
    }

    @Override
    public void write(ByteBuf data) throws IOException {
        data.writeInt(ship.getId());
        ByteBufUtils.writeVector(data, position);
        data.writeFloat(rot);
        ByteBufUtils.writeString(data, ship.getClass().getSimpleName());
        data.writeBoolean(ship.isSpawned());

        List<WeaponSlot> weaponSlots = ship.getWeaponSlots();
        data.writeByte(weaponSlots.size());
        for (int i = 0; i < weaponSlots.size(); i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            ByteBufUtils.writeString(data, weaponSlot.getClass().getSimpleName());
            data.writeInt(weaponSlot.getId());
        }

        ByteBufUtils.writeString(data, ship.getName());
        data.writeByte(ship.getFaction().ordinal());
    }
}