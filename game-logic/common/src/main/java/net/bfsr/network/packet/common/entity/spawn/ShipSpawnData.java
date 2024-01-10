package net.bfsr.network.packet.common.entity.spawn;

import clipper2.core.PathD;
import clipper2.core.PathsD;
import clipper2.core.PointD;
import earcut4j.Earcut;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.damage.DamageMask;
import net.bfsr.damage.DamageSystem;
import net.bfsr.engine.Engine;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.network.util.ByteBufUtils;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.decompose.SweepLine;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class ShipSpawnData extends RigidBodySpawnData {
    private Ship ship;
    private boolean isSpawned;
    private Slot[] slotList;
    private String name;
    private byte faction;

    private PathsD contours;
    private byte[] damageMaskBytes;
    private ByteBuffer damageMaskByteBuffer;
    private List<BodyFixture> fixtures;

    @AllArgsConstructor
    @Getter
    public static class Slot {
        private final byte type;
        private final int dataIndex;
        private final int id;
    }

    public ShipSpawnData(Ship ship) {
        super(ship);
        this.ship = ship;

        PathsD contours = ship.getContours();
        this.contours = new PathsD(contours.size());
        for (int i = 0; i < contours.size(); i++) {
            this.contours.add(contours.get(i));
        }

        damageMaskBytes = ship.getMask().copy();
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);
        data.writeBoolean(ship.isSpawned());

        List<WeaponSlot> weaponSlots = ship.getModules().getWeaponSlots();
        data.writeByte(weaponSlots.size());
        for (int i = 0; i < weaponSlots.size(); i++) {
            WeaponSlot weaponSlot = weaponSlots.get(i);
            data.writeByte(weaponSlot.getWeaponType().ordinal());
            data.writeShort(weaponSlot.getGunData().getId());
            data.writeInt(weaponSlot.getId());
        }

        ByteBufUtils.writeString(data, ship.getName());
        data.writeByte(ship.getFaction().ordinal());

        DamageMask damageMask = ship.getMask();
        PathsD contours = ship.getContours();
        data.writeShort(contours.size());
        for (int i = 0; i < contours.size(); i++) {
            PathD contour = contours.get(i);
            data.writeShort(contour.size());
            for (int j = 0; j < contour.size(); j++) {
                PointD pointD = contour.get(j);
                data.writeFloat((float) pointD.x);
                data.writeFloat((float) pointD.y);
            }
        }

        int maskWidth = damageMask.getWidth();
        int maskHeight = damageMask.getHeight();
        data.writeInt(maskWidth * maskHeight);
        for (int i = 0; i < maskHeight; i++) {
            data.writeBytes(damageMaskBytes, i * maskHeight, maskWidth);
        }
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);
        isSpawned = data.readBoolean();

        byte slotsCount = data.readByte();
        slotList = new Slot[slotsCount];
        for (int i = 0; i < slotsCount; i++) {
            slotList[i] = new Slot(data.readByte(), data.readShort(), data.readInt());
        }

        name = ByteBufUtils.readString(data);
        faction = data.readByte();

        short contoursSize = data.readShort();
        contours = new PathsD(contoursSize);
        for (int i = 0; i < contoursSize; i++) {
            short contourSize = data.readShort();
            PathD contour = new PathD(contourSize);
            contours.add(contour);

            for (int j = 0; j < contourSize; j++) {
                contour.add(new PointD(data.readFloat(), data.readFloat()));
            }
        }

        int maskSize = data.readInt();
        damageMaskByteBuffer = Engine.renderer.createByteBuffer(maskSize);
        data.readBytes(damageMaskByteBuffer);
        damageMaskByteBuffer.position(0);
        fixtures = new ArrayList<>(32);

        DamageSystem.decompose(contours, convex -> fixtures.add(new BodyFixture(convex)), new SweepLine(), new Earcut());
    }

    @Override
    public EntityPacketSpawnType getType() {
        return EntityPacketSpawnType.SHIP;
    }
}