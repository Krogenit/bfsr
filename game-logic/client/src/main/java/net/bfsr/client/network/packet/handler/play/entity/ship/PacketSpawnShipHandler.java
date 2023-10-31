package net.bfsr.client.network.packet.handler.play.entity.ship;

import io.netty.channel.ChannelHandlerContext;
import net.bfsr.client.Core;
import net.bfsr.client.network.NetworkSystem;
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
import net.bfsr.network.packet.PacketHandler;
import net.bfsr.network.packet.server.entity.ship.PacketSpawnShip;
import net.bfsr.world.World;
import org.joml.Vector2f;

import java.net.InetSocketAddress;

public class PacketSpawnShipHandler extends PacketHandler<PacketSpawnShip, NetworkSystem> {
    private final Faction[] factions = Faction.values();
    private final WeaponType[] weaponTypes = WeaponType.values();

    @Override
    public void handle(PacketSpawnShip packet, NetworkSystem networkSystem, ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        World world = Core.get().getWorld();
        if (world.getEntityById(packet.getId()) == null) {
            Vector2f position = packet.getPosition();
            Ship ship = ShipFactory.get().create(world, packet.getId(), position.x, position.y, packet.getSin(), packet.getCos(),
                    factions[packet.getFaction()], ShipRegistry.INSTANCE.get(packet.getDataIndex()));
            ship.setName(packet.getName());
            ShipOutfitter.get().outfit(ship);
            createWeaponSlots(ship, packet.getSlotList());
            world.addShip(ship);
            if (packet.isSpawned()) ship.setSpawned();
        }
    }

    private void createWeaponSlots(Ship ship, PacketSpawnShip.Slot[] slotList) {
        for (int i = 0; i < slotList.length; i++) {
            PacketSpawnShip.Slot slot = slotList[i];

            WeaponSlot weaponSlot;
            if (weaponTypes[slot.getType()] == WeaponType.BEAM) {
                weaponSlot = new WeaponSlotBeam(BeamRegistry.INSTANCE.get(slot.getDataIndex()));
            } else {
                weaponSlot = new WeaponSlot(GunRegistry.INSTANCE.get(slot.getDataIndex()));
            }

            ship.addWeaponToSlot(slot.getId(), weaponSlot);
        }
    }
}