package net.bfsr.server.dto.converter;

import net.bfsr.server.dto.ShipModel;
import net.bfsr.server.entity.ship.Ship;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import org.mapstruct.TargetType;

import java.lang.reflect.InvocationTargetException;

@Mapper(uses = WeaponConverter.class)
public interface ShipConverter {
    @Mapping(target = "className", expression = "java(ship.getClass().getName())")
    @Mapping(target = "weapons", source = "weaponSlots")
    ShipModel to(Ship ship);

    @Mapping(target = "weaponSlots", source = "weapons")
    Ship from(ShipModel shipModel);

    @ObjectFactory
    default <T extends Ship> T to(ShipModel shipModel, @TargetType Class<T> entityClass) {
        try {
            return (T) Class.forName(shipModel.className()).getConstructor().newInstance();
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}