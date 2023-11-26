package net.bfsr.network.packet.common.entity.spawn;

import clipper2.core.PathD;
import clipper2.core.PointD;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bfsr.config.entity.ship.ShipData;
import net.bfsr.config.entity.ship.ShipRegistry;
import net.bfsr.damage.DamageMask;
import net.bfsr.damage.DamageSystem;
import net.bfsr.damage.SimpleConnectedObject;
import net.bfsr.engine.Engine;
import net.bfsr.entity.wreck.ShipWreck;
import org.joml.Vector2f;

import java.nio.ByteBuffer;

@NoArgsConstructor
@Getter
public class ShipWreckSpawnData extends DamageableRigidBodySpawnData {
    private static final DamageSystem DAMAGE_SYSTEM = new DamageSystem();

    private int x, y;
    private float velocityX, velocityY;
    private float angularVelocity;
    private PathD pathD;
    private int maskWidth, maskHeight;
    private int bufferWidth, bufferHeight;
    private ByteBuffer byteBuffer;

    private int maxX, maxY;
    private PathD path;
    private DamageMask damageMask;
    private ShipWreck wreck;

    public ShipWreckSpawnData(ShipWreck wreck) {
        super(wreck);
        this.damageMask = wreck.getMask();
        this.x = damageMask.getX();
        this.y = damageMask.getY();
        this.maxX = damageMask.getMaxX();
        this.maxY = damageMask.getMaxY();
        this.path = wreck.getContours().get(0);
        Vector2f velocity = wreck.getVelocity();
        this.velocityX = velocity.x;
        this.velocityY = velocity.y;
        this.angularVelocity = wreck.getAngularVelocity();
        this.maskWidth = damageMask.getWidth();
        this.maskHeight = damageMask.getHeight();
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);

        data.writeShort(x);
        data.writeShort(y);
        data.writeShort(maxX);
        data.writeShort(maxY);

        byte[] bytes = damageMask.getData();
        int width = maxX - x + 1;
        for (int i = y; i <= maxY; i++) {
            data.writeBytes(bytes, i * damageMask.getHeight() + x, width);
        }

        data.writeShort(path.size());
        for (int i = 0; i < path.size(); i++) {
            PointD pointD = path.get(i);
            data.writeFloat((float) pointD.x);
            data.writeFloat((float) pointD.y);
        }

        data.writeFloat(velocityX);
        data.writeFloat(velocityY);
        data.writeFloat(angularVelocity);
        data.writeShort(maskWidth);
        data.writeShort(maskHeight);
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);

        ShipData shipData = ShipRegistry.INSTANCE.get(dataId);
        x = data.readShort();
        y = data.readShort();
        int maxX = data.readShort();
        int maxY = data.readShort();
        bufferWidth = maxX - x + 1;
        bufferHeight = maxY - y + 1;
        byteBuffer = Engine.renderer.createByteBuffer(bufferWidth * bufferHeight);
        data.readBytes(byteBuffer);
        byteBuffer.position(0);

        short paths = data.readShort();
        pathD = new PathD(paths);
        for (int j = 0; j < paths; j++) {
            pathD.add(new PointD(data.readFloat(), data.readFloat()));
        }

        velocityX = data.readFloat();
        velocityY = data.readFloat();
        angularVelocity = data.readFloat();
        maskWidth = data.readShort();
        maskHeight = data.readShort();

        wreck = DAMAGE_SYSTEM.createDamage(posX, posY, sin, cos, shipData.getSizeX(), shipData.getSizeY(), pathD,
                new DamageMask(maskWidth, maskHeight, null), shipData);

        if (connectedObjects.size() > 0) {
            for (int i = 0; i < connectedObjects.size(); i++) {
                SimpleConnectedObject connectedObject = ((SimpleConnectedObject) connectedObjects.get(i));
                connectedObject.setupFixtures(wreck);
                wreck.addConnectedObject(connectedObject);
            }

            wreck.addConnectedObjectFixturesToBody();
            wreck.getBody().updateMass();
        }
    }

    @Override
    public EntityPacketSpawnType getType() {
        return EntityPacketSpawnType.SHIP_WRECK;
    }
}