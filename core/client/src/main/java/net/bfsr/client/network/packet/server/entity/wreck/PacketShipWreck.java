package net.bfsr.client.network.packet.server.entity.wreck;

import clipper2.core.PathD;
import clipper2.core.PointD;
import io.netty.buffer.ByteBuf;
import net.bfsr.client.core.Core;
import net.bfsr.client.entity.wreck.ShipWreckDamagable;
import net.bfsr.client.network.packet.PacketIn;
import net.bfsr.client.renderer.texture.DamageMaskTexture;
import net.bfsr.client.renderer.texture.Texture;
import net.bfsr.client.renderer.texture.TextureLoader;
import net.bfsr.client.util.DamageUtils;
import net.bfsr.texture.TextureRegister;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

public class PacketShipWreck implements PacketIn {
    private ShipWreckDamagable shipWreckDamagable;
    private int x, y, width, height;
    private ByteBuffer byteBuffer;

    @Override
    public void read(ByteBuf data) throws IOException {
        int id = data.readInt();
        float posX = data.readFloat();
        float posY = data.readFloat();
        float sin = data.readFloat();
        float cos = data.readFloat();
        float scaleX = data.readFloat();
        float scaleY = data.readFloat();
        short textureIndex = data.readShort();
        x = data.readShort();
        y = data.readShort();
        short maxX = data.readShort();
        short maxY = data.readShort();
        width = maxX - x + 1;
        height = maxY - y + 1;
        int size = width * height;
        byteBuffer = BufferUtils.createByteBuffer(size);
        byte[] bytes = new byte[size];
        data.readBytes(bytes, 0, size);
        byteBuffer.put(bytes, 0, size);
        byteBuffer.flip();

        short paths = data.readShort();
        PathD pathD = new PathD(paths);
        for (int j = 0; j < paths; j++) {
            pathD.add(new PointD(data.readFloat(), data.readFloat()));
        }

        float velocityX = data.readFloat();
        float velocityY = data.readFloat();
        float angularVelocity = data.readFloat();

        TextureRegister textureRegister = TextureRegister.values()[textureIndex];
        if (TextureLoader.isLoaded(textureRegister)) {
            Texture texture = TextureLoader.getTexture(textureRegister);

            DamageMaskTexture damageMaskTexture = new DamageMaskTexture(texture.getWidth(), texture.getHeight(), BufferUtils.createByteBuffer(texture.getWidth() * texture.getHeight()));
            shipWreckDamagable = DamageUtils.createDamage(id, posX, posY, sin, cos, scaleX, scaleY, pathD, damageMaskTexture, texture);

            if (shipWreckDamagable != null) {
                shipWreckDamagable.getBody().setLinearVelocity(velocityX, velocityY);
                shipWreckDamagable.getBody().setAngularVelocity(angularVelocity);
            }
        }
    }

    @Override
    public void processOnClientSide() {
        if (shipWreckDamagable != null) {
            DamageMaskTexture maskTexture = shipWreckDamagable.getMaskTexture();
            maskTexture.createEmpty();
            maskTexture.upload(x, y, width, height, byteBuffer);
            Core.get().getWorld().addDamage(shipWreckDamagable);
        }
    }
}