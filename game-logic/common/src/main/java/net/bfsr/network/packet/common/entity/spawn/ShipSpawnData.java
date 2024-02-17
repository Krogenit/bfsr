package net.bfsr.network.packet.common.entity.spawn;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.config.component.armor.ArmorPlateRegistry;
import net.bfsr.config.component.cargo.CargoRegistry;
import net.bfsr.config.component.crew.CrewRegistry;
import net.bfsr.config.component.engine.EngineRegistry;
import net.bfsr.config.component.hull.HullRegistry;
import net.bfsr.config.component.reactor.ReactorRegistry;
import net.bfsr.config.component.shield.ShieldRegistry;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.damage.DamageMask;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.entity.ship.module.Modules;
import net.bfsr.entity.ship.module.armor.Armor;
import net.bfsr.entity.ship.module.cargo.Cargo;
import net.bfsr.entity.ship.module.crew.Crew;
import net.bfsr.entity.ship.module.engine.Engine;
import net.bfsr.entity.ship.module.engine.Engines;
import net.bfsr.entity.ship.module.hull.Hull;
import net.bfsr.entity.ship.module.reactor.Reactor;
import net.bfsr.entity.ship.module.shield.Shield;
import net.bfsr.faction.Faction;
import net.bfsr.logic.LogicType;
import net.bfsr.math.Direction;
import net.bfsr.network.util.ByteBufUtils;
import org.dyn4j.geometry.decompose.SweepLine;

import java.util.EnumMap;
import java.util.List;

@Getter
@NoArgsConstructor
public class ShipSpawnData extends DamageableRigidBodySpawnData<Ship> {
    private static final SweepLine SWEEP_LINE = new SweepLine();

    private boolean isSpawned;
    private String name;
    private byte faction;
    private int reactorDataId;
    private int enginesDataId;
    private final EnumMap<Direction, BooleanArrayList> enginesMap = new EnumMap<>(Direction.class);
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
            BooleanArrayList dataList = new BooleanArrayList(engines1.size());
            enginesMap.put(direction, dataList);
            for (int i = 0; i < engines1.size(); i++) {
                Engine engine = engines1.get(i);
                dataList.add(engine.isDead());
            }
        });

        Shield shield = modules.getShield();
        this.hasShield = shield != null;
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
                data.writeBoolean(engineData.getBoolean(i));
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
            BooleanArrayList booleans = new BooleanArrayList(size);
            enginesMap.put(direction, booleans);
            for (int j = 0; j < size; j++) {
                booleans.add(data.readBoolean());
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
    protected Ship createRigidBody() {
        Ship ship = ShipFactory.get().create(posX, posY, sin, cos, Faction.get(faction), ShipRegistry.INSTANCE.get(dataId),
                new DamageMask(32, 32, null));
        ship.setName(name);
        ship.setPolygon(polygon);
        return ship;
    }

    @Override
    public EntityPacketSpawnType getType() {
        return EntityPacketSpawnType.SHIP;
    }

    public void outfit(Ship ship) {
        ship.setReactor(new Reactor(ReactorRegistry.INSTANCE.get(reactorDataId),
                ship.getConfigData().getReactorPolygon()));
        Engines engines = new Engines(EngineRegistry.INSTANCE.get(enginesDataId), ship);
        ship.setEngine(engines);

        enginesMap.forEach((direction, booleans) -> {
            List<Engine> engineList = engines.getEngines(direction);
            for (int i = 0; i < engineList.size(); i++) {
                if (booleans.getBoolean(i))
                    engineList.get(i).setDead();
            }
        });

        ship.setHull(new Hull(HullRegistry.INSTANCE.get(hullDataId), ship));
        ship.setArmor(new Armor(ArmorPlateRegistry.INSTANCE.get(armorDataId), ship));
        ship.setCrew(new Crew(CrewRegistry.INSTANCE.get(crewDataId)));
        ship.setCargo(new Cargo(CargoRegistry.INSTANCE.get(cargoDataId)));
        if (hasShield) {
            Shield shield = new Shield(ShieldRegistry.INSTANCE.get(shieldDataId), ship.getConfigData().getShieldPolygon(),
                    ship.getWorld().getGameLogic().getLogic(LogicType.SHIELD_UPDATE.ordinal()));
            ship.setShield(shield);
            if (shieldDead) {
                shield.setDead();
            } else {
                shield.setShieldHp(shieldHp);
                shield.setRebuildingTime(shieldRebuildingTime);
            }
        }
    }
}