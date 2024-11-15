package net.bfsr.network.packet.common.entity.spawn.connectedobject;

import net.bfsr.config.ConfigData;
import net.bfsr.config.component.weapon.beam.BeamData;
import net.bfsr.damage.ConnectedObject;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;

public class BeamSlotConnectedObjectSpawnData extends WeaponSlotConnectObjectSpawnData {
    @Override
    public ConnectedObject<?> create(ConfigData configData) {
        WeaponSlotBeam weaponSlotBeam = new WeaponSlotBeam((BeamData) configData);
        weaponSlotBeam.setId(id);
        weaponSlotBeam.setLocalPosition(localPosition);
        return weaponSlotBeam;
    }
}
