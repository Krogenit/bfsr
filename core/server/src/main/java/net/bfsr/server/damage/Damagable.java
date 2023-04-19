package net.bfsr.server.damage;

import clipper2.core.PathsD;
import net.bfsr.damage.DamagableCommon;
import net.bfsr.server.core.Server;
import net.bfsr.server.network.packet.server.entity.wreck.PacketSyncDamage;
import net.bfsr.server.world.WorldServer;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.joml.Vector2f;

import java.util.List;

public interface Damagable extends DamagableCommon {
    PathsD getContours();
    DamageMask getMask();
    Body getBody();
    Vector2f getScale();
    List<BodyFixture> getFixturesToAdd();
    boolean isDead();
    void destroy();
    void setupFixture(BodyFixture bodyFixture);
    default void sync() {
        Server.getInstance().getNetworkSystem().sendTCPPacketToAllNearby(new PacketSyncDamage(this), getX(), getY(), WorldServer.PACKET_UPDATE_DISTANCE);
    }
    void sendSpawnPacket();
    float getX();
    float getY();
    float getSin();
    float getCos();
    int getTextureIndex();
}