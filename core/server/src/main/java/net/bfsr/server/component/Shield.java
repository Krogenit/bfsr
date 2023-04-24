package net.bfsr.server.component;

import net.bfsr.component.shield.ShieldCommon;
import net.bfsr.config.component.ShieldData;
import net.bfsr.server.core.Server;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.network.packet.server.component.PacketShieldRebuild;
import net.bfsr.server.network.packet.server.component.PacketShieldRebuildingTime;
import net.bfsr.server.network.packet.server.component.PacketShieldRemove;
import net.bfsr.server.world.WorldServer;
import net.bfsr.util.TimeUtils;

public class Shield extends ShieldCommon {
    private final Ship ship;

    public Shield(Ship ship, ShieldData shieldData) {
        super(ship.getBody(), shieldData.getMaxShield(), shieldData.getShieldRegen(), shieldData.getRebuildTime());
        this.ship = ship;
    }

    @Override
    public void update() {
        if (alive && shield <= 0) {
            removeShield();
        }

        super.update();

        if (rebuildingTime < timeToRebuild) {
            rebuildingTime += 60.0f * TimeUtils.UPDATE_DELTA_TIME;

            if (rebuildingTime >= timeToRebuild) {
                rebuildShield();
            }
        }
    }

    @Override
    public void rebuildShield() {
        super.rebuildShield();
        Server.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketShieldRebuild(ship.getId()), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    @Override
    protected void onNoShieldDamage() {
        setRebuildingTime(0);
        Server.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketShieldRebuildingTime(ship.getId(), 0), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    @Override
    public void removeShield() {
        super.removeShield();
        Server.getInstance().getNetworkSystem().sendUDPPacketToAllNearby(new PacketShieldRemove(ship.getId()), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }
}