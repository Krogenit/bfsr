package net.bfsr.component.hull;

import net.bfsr.entity.ship.Ship;
import net.bfsr.network.packet.server.PacketDestroingShip;
import net.bfsr.server.MainServer;
import net.bfsr.world.WorldServer;

public class Hull {

    private float hull;
    private final float maxHull;
    private final float regenHull;
    private final Ship ship;

    public Hull(float maxHull, float regenHull, Ship ship) {
        this.hull = maxHull;
        this.maxHull = maxHull;
        this.regenHull = regenHull;
        this.ship = ship;
    }

    public void update(double delta) {
        regenHull(delta);
    }

    private void regenHull(double delta) {
        if (hull < 0) {
            hull = 0;
        }

        if (hull < maxHull) {
            hull += (regenHull + ship.getCrew().getCrewRegen()) * delta;
        } else if (hull > maxHull) {
            hull = maxHull;
        }
    }

    public void damage(float reducedHullDamage) {
        this.hull -= reducedHullDamage;

        if (hull <= 0) {
            if (!ship.getWorld().isRemote()) {
                ship.setDestroing();
                MainServer.getServer().getNetworkSystem().sendPacketToAllNearby(new PacketDestroingShip(ship), ship.getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
            }
        }
    }

    public void setHull(float hull) {
        this.hull = hull;
    }

    public float getHull() {
        return hull;
    }

    public float getMaxHull() {
        return maxHull;
    }
}
