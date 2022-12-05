package net.bfsr.network.packet.server;

import lombok.NoArgsConstructor;
import net.bfsr.client.particle.ParticleWreck;
import net.bfsr.core.Core;
import net.bfsr.entity.CollisionObject;
import net.bfsr.entity.ship.Ship;
import net.bfsr.network.PacketBuffer;
import net.bfsr.network.client.NetworkManagerClient;
import net.bfsr.network.server.ServerPacket;
import net.bfsr.world.WorldClient;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.io.IOException;

@NoArgsConstructor
public class PacketSpawnParticle extends ServerPacket {

    private int id, destroyedShipId;
    private int textureOffset;
    private boolean isFire, isLight, isFireExplosion, isShipWreck;
    private float rot, sizeVelocity, alphaVelocity, rotationSpeed, wreckLifeTime, wreckMaxLifeTime;
    private Vector2f pos, velocity, size;
    private Vector4f color;

    public PacketSpawnParticle(ParticleWreck p) {
        this.id = p.getId();
        this.destroyedShipId = p.getDestroyedShipId();
        this.textureOffset = p.getTextureOffset();
        this.isShipWreck = p.isShipWreck();
        this.wreckLifeTime = p.getWreckLifeTime();
        this.wreckMaxLifeTime = p.getMaxWreckLifeTime();
        this.isFire = p.isFire();
        this.isLight = p.isLight();
        this.isFireExplosion = p.isFireExplosion();
//		this.sizeVelocity = p.getSizeVelocity();
        this.alphaVelocity = p.getAlphaVelocity();
        this.rotationSpeed = p.getRotationSpeed();
//		this.maxAlpha = p.getMaxAlpha();
        this.velocity = p.getVelocity();
        this.size = p.getScale();
        this.pos = p.getPosition();
        this.color = p.getColor();
        this.rot = p.getRotation();
    }

    @Override
    public void read(PacketBuffer data) throws IOException {
        id = data.readInt();

        textureOffset = data.readInt();
        isShipWreck = data.readBoolean();

        if (isShipWreck) {
            wreckLifeTime = data.readFloat();
            wreckMaxLifeTime = data.readFloat();
            destroyedShipId = data.readInt();
        } else {
            isFire = data.readBoolean();
            isLight = data.readBoolean();
            isFireExplosion = data.readBoolean();
        }

        pos = data.readVector2f();
        velocity = data.readVector2f();
        rot = data.readFloat();
        rotationSpeed = data.readFloat();
        size = data.readVector2f();
        sizeVelocity = data.readFloat();
        color = data.readVector4f();
        alphaVelocity = data.readFloat();
    }

    @Override
    public void write(PacketBuffer data) throws IOException {
        data.writeInt(id);

        data.writeInt(textureOffset);
        data.writeBoolean(isShipWreck);

        if (isShipWreck) {
            data.writeFloat(wreckLifeTime);
            data.writeFloat(wreckMaxLifeTime);
            data.writeInt(destroyedShipId);
        } else {
            data.writeBoolean(isFire);
            data.writeBoolean(isLight);
            data.writeBoolean(isFireExplosion);
        }

        data.writeVector2f(pos);
        data.writeVector2f(velocity);
        data.writeFloat(rot);
        data.writeFloat(rotationSpeed);
        data.writeVector2f(size);
        data.writeFloat(sizeVelocity);
        data.writeVector4f(color);
        data.writeFloat(alphaVelocity);
    }

    @Override
    public void processOnClientSide(NetworkManagerClient networkManager) {
        WorldClient world = Core.getCore().getWorld();
        if (world.getEntityById(id) == null) {
            if (isShipWreck) {
                CollisionObject obj = world.getEntityById(destroyedShipId);
                if (obj instanceof Ship) {
                    new ParticleWreck(textureOffset, (Ship) obj, pos, velocity,
                            rot, rotationSpeed, size, sizeVelocity,
                            color, alphaVelocity, id, wreckMaxLifeTime);
                }
            } else {
                new ParticleWreck(textureOffset, isLight, isFire, isFireExplosion,
                        pos, velocity, rot, rotationSpeed,
                        size, sizeVelocity, color, alphaVelocity, id);
            }
        }
    }
}