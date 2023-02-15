package net.bfsr.server.entity.wreck;

import net.bfsr.entity.ship.ShipCommon;
import net.bfsr.entity.wreck.ShipWreckCommon;
import net.bfsr.server.MainServer;
import net.bfsr.server.network.packet.server.PacketRemoveObject;
import net.bfsr.server.world.WorldServer;
import org.dyn4j.geometry.Vector2;

public class ShipWreck extends ShipWreckCommon {
    public ShipWreck init(int id, int wreckIndex, ShipCommon ship, float x, float y, float velocityX, float velocityY, float rotation, float angularVelocity, float scaleX, float scaleY,
                          float r, float g, float b, float a, float lifeTime) {
        super.init(id, wreckIndex, ship, x, y, velocityX, velocityY, rotation, angularVelocity, scaleX, scaleY, r, g, b, a, lifeTime);
        return this;
    }

    @Override
    protected void addParticle() {
        ((WorldServer) world).addWreck(this);
    }

    @Override
    protected void updateLifeTime() {
        super.updateLifeTime();
        if (lifeTime <= 0) {
            MainServer.getInstance().getNetworkSystem().sendPacketToAllNearby(new PacketRemoveObject(this), getPosition(), WorldServer.PACKET_SPAWN_DISTANCE);
            setDead(true);
        }
    }

    @Override
    protected void onHullDamage() {
        if (hull <= 0) {
            destroy();
        }
    }

    @Override
    protected void destroy() {
        super.destroy();
        if (color.w > 0.01f) {
            Vector2 worldPos = body.getLocalCenter().add(position.x, position.y);
            WreckSpawner.spawnDamageDebris((WorldServer) world, random.nextInt(3), (float) worldPos.x, (float) worldPos.y, velocity.x, velocity.y, 1.0f);
            WreckSpawner.spawnDamageWrecks((WorldServer) world, random.nextInt(2), (float) worldPos.x, (float) worldPos.y, velocity.x, velocity.y);
        }
    }
}
