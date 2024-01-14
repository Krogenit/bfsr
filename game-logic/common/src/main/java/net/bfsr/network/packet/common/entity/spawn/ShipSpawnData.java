package net.bfsr.network.packet.common.entity.spawn;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.damage.DamageMask;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.faction.Faction;
import net.bfsr.network.util.ByteBufUtils;
import org.dyn4j.geometry.decompose.SweepLine;

@Getter
@NoArgsConstructor
public class ShipSpawnData extends DamageableRigidBodySpawnData<Ship> {
    private static final SweepLine SWEEP_LINE = new SweepLine();

    private boolean isSpawned;
    private String name;
    private byte faction;

    public ShipSpawnData(Ship ship) {
        super(ship);
        this.isSpawned = ship.isSpawned();
        this.name = ship.getName();
        this.faction = (byte) ship.getFaction().ordinal();
    }

    @Override
    public void writeData(ByteBuf data) {
        data.writeBoolean(isSpawned);
        ByteBufUtils.writeString(data, name);
        data.writeByte(faction);

        super.writeData(data);
    }

    @Override
    public void readData(ByteBuf data) {
        isSpawned = data.readBoolean();
        name = ByteBufUtils.readString(data);
        faction = data.readByte();

        super.readData(data);
    }

    @Override
    protected Ship createRigidBody() {
        Ship ship = ShipFactory.get().create(posX, posY, sin, cos, Faction.get(faction), ShipRegistry.INSTANCE.get(dataId),
                new DamageMask(32, 32, null));
        ship.setName(name);
        ship.setContours(contours);
        return ship;
    }

    @Override
    public EntityPacketSpawnType getType() {
        return EntityPacketSpawnType.SHIP;
    }
}