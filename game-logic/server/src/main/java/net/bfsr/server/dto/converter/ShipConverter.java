package net.bfsr.server.dto.converter;

import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.damage.DamageMask;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.server.dto.ShipModel;
import org.mapstruct.*;

import java.util.List;

@Mapper(uses = ModulesConverter.class)
public abstract class ShipConverter {
    @Mapping(target = "name", expression = "java(ship.getConfigData().getFileName())")
    public abstract ShipModel to(Ship ship);

    @Mappings({@Mapping(target = "dead", ignore = true),
            @Mapping(target = "id", ignore = true), @Mapping(target = "upFixture", ignore = true),
            @Mapping(target = "velocity", ignore = true), @Mapping(target = "shield", ignore = true),
            @Mapping(target = "faction", ignore = true), @Mapping(target = "cargo", ignore = true),
            @Mapping(target = "name", ignore = true), @Mapping(target = "controlledByPlayer", ignore = true),
            @Mapping(target = "target", ignore = true), @Mapping(target = "owner", ignore = true),
            @Mapping(target = "fixtures", ignore = true), @Mapping(target = "moveDirections", ignore = true),
            @Mapping(target = "contours", ignore = true), @Mapping(target = "fixturesToAdd", ignore = true),
            @Mapping(target = "armor", ignore = true), @Mapping(target = "configData", ignore = true),
            @Mapping(target = "engine", ignore = true), @Mapping(target = "health", ignore = true),
            @Mapping(target = "hull", ignore = true), @Mapping(target = "reactor", ignore = true),
            @Mapping(target = "registryId", ignore = true), @Mapping(target = "fixturesToRemove", ignore = true),
            @Mapping(target = "connectedObjects", ignore = true),
            @Mapping(target = "chronologicalDataProcessor", ignore = true),
            @Mapping(target = "positionCalculator", ignore = true)
    })
    public abstract Ship from(ShipModel shipModel);

    @AfterMapping
    void initWeapons(ShipModel shipModel, @MappingTarget Ship ship) {
        List<WeaponSlot> weaponSlots = ship.getModules().getWeaponSlots();
        for (int i = 0; i < weaponSlots.size(); i++) {
            weaponSlots.get(i).init(i, ship);
        }
    }

    @ObjectFactory
    public <T extends Ship> T to(ShipModel shipModel, @TargetType Class<T> entityClass) {
        return (T) new Ship(ShipRegistry.INSTANCE.get(shipModel.name()), new DamageMask(32, 32));
    }
}