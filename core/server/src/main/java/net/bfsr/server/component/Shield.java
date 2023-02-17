package net.bfsr.server.component;

import net.bfsr.component.shield.ShieldCommon;
import net.bfsr.config.component.ShieldConfig;
import net.bfsr.server.MainServer;
import net.bfsr.server.entity.ship.Ship;
import net.bfsr.server.network.packet.server.PacketShieldRebuild;
import net.bfsr.server.network.packet.server.PacketShieldRebuildingTime;
import net.bfsr.server.network.packet.server.PacketShieldRemove;
import net.bfsr.server.world.WorldServer;
import net.bfsr.util.TimeUtils;

public class Shield extends ShieldCommon {
    private final Ship ship;

    public Shield(Ship ship, ShieldConfig shieldConfig) {
        super(ship.getBody(), shieldConfig.getMaxShield(), shieldConfig.getShieldRegen(), shieldConfig.getRebuildTime());
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
        MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShieldRebuild(ship.getId()), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    @Override
    protected void onNoShieldDamage() {
        setRebuildingTime(0);
        MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShieldRebuildingTime(ship.getId(), 0), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }

    @Override
    public void removeShield() {
        super.removeShield();
        MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketShieldRemove(ship.getId()), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
    }
}
