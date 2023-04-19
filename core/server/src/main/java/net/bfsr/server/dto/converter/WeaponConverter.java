package net.bfsr.server.dto.converter;

import net.bfsr.server.component.weapon.WeaponSlot;
import net.bfsr.server.dto.WeaponModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import org.mapstruct.TargetType;

import java.lang.reflect.InvocationTargetException;

@Mapper
public interface WeaponConverter {
    @Mapping(target = "className", expression = "java(weaponSlot.getClass().getName())")
    WeaponModel to(WeaponSlot weaponSlot);

    WeaponSlot from(WeaponModel weaponModel);

    @ObjectFactory
    default <T extends WeaponSlot> T to(WeaponModel weaponModel, @TargetType Class<T> entityClass) {
        try {
            return (T) Class.forName(weaponModel.className()).getConstructor().newInstance();
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}