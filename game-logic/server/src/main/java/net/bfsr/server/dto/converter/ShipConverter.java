package net.bfsr.server.dto.converter;

import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.server.dto.ShipModel;
import net.bfsr.server.dto.factory.ShipFactory;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(uses = {ModulesConverter.class, ShipFactory.class})
public abstract class ShipConverter {
    @Mapping(target = "name", expression = "java(ship.getConfigData().getFileName())")
    public abstract ShipModel to(Ship ship);

    @Mappings({@Mapping(target = "dead", ignore = true),
            @Mapping(target = "id", ignore = true), @Mapping(target = "upFixture", ignore = true),
            @Mapping(target = "shield", ignore = true), @Mapping(target = "linearVelocity", ignore = true),
            @Mapping(target = "faction", ignore = true), @Mapping(target = "cargo", ignore = true),
            @Mapping(target = "name", ignore = true), @Mapping(target = "controlledByPlayer", ignore = true),
            @Mapping(target = "target", ignore = true), @Mapping(target = "owner", ignore = true),
            @Mapping(target = "fixtures", ignore = true), @Mapping(target = "moveDirections", ignore = true),
            @Mapping(target = "polygon", ignore = true),
            @Mapping(target = "armor", ignore = true), @Mapping(target = "configData", ignore = true),
            @Mapping(target = "engine", ignore = true), @Mapping(target = "health", ignore = true),
            @Mapping(target = "hull", ignore = true), @Mapping(target = "reactor", ignore = true),
            @Mapping(target = "connectedObjects", ignore = true), @Mapping(target = "collisionTimer", ignore = true),
            @Mapping(target = "updateRunnable", ignore = true), @Mapping(target = "damageMask", ignore = true),
            @Mapping(target = "angularVelocity", ignore = true), @Mapping(target = "correctionHandler", ignore = true),
            @Mapping(target = "crew", ignore = true), @Mapping(target = "lastAttacker", ignore = true),
            @Mapping(target = "ai", ignore = true), @Mapping(target = "shipData", ignore = true),
            @Mapping(target = "body", ignore = true), @Mapping(target = "jumpPosition", ignore = true),
            @Mapping(target = "shipEventBus", ignore = true), @Mapping(target = "world", ignore = true)
    })
    public abstract Ship from(ShipModel shipModel);

    @AfterMapping
    void initWeapons(ShipModel shipModel, @MappingTarget Ship ship) {
        List<WeaponSlot> weaponSlots = ship.getModules().getWeaponSlots();
        for (int i = 0; i < weaponSlots.size(); i++) {
            weaponSlots.get(i).init(i, ship);
        }
    }
}