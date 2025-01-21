package net.bfsr.network.packet.common.entity.spawn.ship;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.Modules;
import net.bfsr.entity.ship.module.engine.Engine;
import net.bfsr.entity.ship.module.engine.Engines;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.math.Direction;
import net.bfsr.network.packet.common.entity.spawn.DamageableRigidBodySpawnData;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnType;
import net.bfsr.network.util.ByteBufUtils;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

@Getter
@NoArgsConstructor
public class ShipSpawnData extends DamageableRigidBodySpawnData {
    private boolean isSpawned;
    private String name;
    private byte faction;
    private int reactorDataId;
    private int enginesDataId;
    private final EnumMap<Direction, List<EngineSpawnData>> enginesMap = new EnumMap<>(Direction.class);
    private boolean hasShield;
    private int shieldDataId;
    private int shieldRebuildingTime;
    private float shieldHp;
    private boolean shieldDead;
    private int hullDataId;
    private int armorDataId;
    private int cargoDataId;
    private int crewDataId;

    public ShipSpawnData(Ship ship) {
        super(ship);
        this.isSpawned = ship.isSpawned();
        this.name = ship.getName();
        this.faction = (byte) ship.getFaction().ordinal();
        Modules modules = ship.getModules();

        this.reactorDataId = modules.getReactor().getReactorData().getId();

        Engines engines = modules.getEngines();
        this.enginesDataId = engines.getEnginesData().getId();

        EnumMap<Direction, List<Engine>> enginesByDirection = engines.getEnginesByDirection();
        enginesByDirection.forEach((direction, engines1) -> {
            List<EngineSpawnData> engineSpawnData = new ArrayList<>(engines1.size());
            enginesMap.put(direction, engineSpawnData);
            for (int i = 0; i < engines1.size(); i++) {
                Engine engine = engines1.get(i);
                engineSpawnData.add(new EngineSpawnData(engine.getId(), engine.isDead()));
            }
        });

        Shield shield = modules.getShield();
        this.hasShield = shield != null;
        this.shieldDataId = -1;
        if (hasShield) {
            this.shieldDead = shield.isDead();
            if (!shieldDead) {
                this.shieldDataId = shield.getShieldData().getId();
                this.shieldRebuildingTime = shield.getRebuildingTime();
                this.shieldHp = shield.getShieldHp();
            }
        }

        this.hullDataId = modules.getHull().getData().getId();
        this.armorDataId = modules.getArmor().getData().getId();
        this.cargoDataId = modules.getCargo().getData().getId();
        this.crewDataId = modules.getCrew().getData().getId();
    }

    @Override
    public void writeData(ByteBuf data) {
        data.writeBoolean(isSpawned);
        ByteBufUtils.writeString(data, name);
        data.writeByte(faction);

        data.writeInt(reactorDataId);
        data.writeInt(enginesDataId);
        data.writeByte(enginesMap.size());
        enginesMap.forEach((direction, engineData) -> {
            data.writeByte(direction.ordinal());
            data.writeByte(engineData.size());
            for (int i = 0; i < engineData.size(); i++) {
                EngineSpawnData engineSpawnData = engineData.get(i);
                data.writeByte(engineSpawnData.id());
                data.writeBoolean(engineSpawnData.isDead());
            }
        });

        data.writeBoolean(hasShield);
        if (hasShield) {
            data.writeBoolean(shieldDead);
            if (!shieldDead) {
                data.writeInt(shieldDataId);
                data.writeInt(shieldRebuildingTime);
                data.writeFloat(shieldHp);
            }
        }

        data.writeInt(hullDataId);
        data.writeInt(armorDataId);
        data.writeInt(cargoDataId);
        data.writeInt(crewDataId);

        super.writeData(data);
    }

    @Override
    public void readData(ByteBuf data) {
        isSpawned = data.readBoolean();
        name = ByteBufUtils.readString(data);
        faction = data.readByte();

        reactorDataId = data.readInt();
        enginesDataId = data.readInt();
        byte count = data.readByte();
        for (int i = 0; i < count; i++) {
            Direction direction = Direction.get(data.readByte());
            byte size = data.readByte();
            List<EngineSpawnData> engineSpawnData = new ArrayList<>(size);
            enginesMap.put(direction, engineSpawnData);
            for (int j = 0; j < size; j++) {
                engineSpawnData.add(new EngineSpawnData(data.readByte(), data.readBoolean()));
            }
        }

        hasShield = data.readBoolean();
        if (hasShield) {
            shieldDead = data.readBoolean();
            if (!shieldDead) {
                shieldDataId = data.readInt();
                shieldRebuildingTime = data.readInt();
                shieldHp = data.readFloat();
            }
        }

        hullDataId = data.readInt();
        armorDataId = data.readInt();
        cargoDataId = data.readInt();
        crewDataId = data.readInt();

        super.readData(data);
    }

    @Override
    public EntityPacketSpawnType getType() {
        return EntityPacketSpawnType.SHIP;
    }
}