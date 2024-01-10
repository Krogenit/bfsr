package net.bfsr.client.world.entity;

import net.bfsr.client.Core;
import net.bfsr.client.damage.DamageHandler;
import net.bfsr.config.component.weapon.beam.BeamRegistry;
import net.bfsr.config.component.weapon.gun.GunRegistry;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.entity.ship.Ship;
import net.bfsr.entity.ship.ShipFactory;
import net.bfsr.entity.ship.ShipOutfitter;
import net.bfsr.entity.ship.module.weapon.WeaponSlot;
import net.bfsr.entity.ship.module.weapon.WeaponSlotBeam;
import net.bfsr.entity.ship.module.weapon.WeaponType;
import net.bfsr.faction.Faction;
import net.bfsr.network.packet.common.entity.spawn.EntityPacketSpawnData;
import net.bfsr.network.packet.common.entity.spawn.ShipSpawnData;
import net.bfsr.world.World;
import org.dyn4j.dynamics.BodyFixture;

import java.util.List;

public class ShipSpawnLogic implements EntitySpawnLogic {
    private final Faction[] factions = Faction.values();
    private final DamageHandler damageHandler = Core.get().getDamageHandler();

    @Override
    public void spawn(EntityPacketSpawnData spawnData) {
        World world = Core.get().getWorld();
        ShipSpawnData shipSpawnData = (ShipSpawnData) spawnData;
        Ship ship = ShipFactory.get().create(world, spawnData.getEntityId(), spawnData.getPosX(), spawnData.getPosY(),
                spawnData.getSin(), spawnData.getCos(), factions[shipSpawnData.getFaction()],
                ShipRegistry.INSTANCE.get(shipSpawnData.getDataId()));
        ship.setName(shipSpawnData.getName());
        ShipOutfitter.get().outfit(ship);
        createWeaponSlots(ship, shipSpawnData.getSlotList());

        ship.setContours(shipSpawnData.getContours());
        List<BodyFixture> fixtures = shipSpawnData.getFixtures();
        if (fixtures.size() > 0) {
            ship.setFixtures(fixtures);
        }

        ship.getMask().setData(shipSpawnData.getDamageMaskBytes());

        world.add(ship, false);

        if (shipSpawnData.isSpawned()) ship.setSpawned();

        damageHandler.updateDamage(ship, 0, 0, ship.getMask().getWidth(), ship.getMask().getHeight(),
                shipSpawnData.getDamageMaskByteBuffer());
    }

    private void createWeaponSlots(Ship ship, ShipSpawnData.Slot[] slotList) {
        for (int i = 0; i < slotList.length; i++) {
            ShipSpawnData.Slot slot = slotList[i];

            WeaponSlot weaponSlot;
            if (WeaponType.get(slot.getType()) == WeaponType.BEAM) {
                weaponSlot = new WeaponSlotBeam(BeamRegistry.INSTANCE.get(slot.getDataIndex()));
            } else {
                weaponSlot = new WeaponSlot(GunRegistry.INSTANCE.get(slot.getDataIndex()));
            }

            ship.addWeaponToSlot(slot.getId(), weaponSlot);
        }
    }
}