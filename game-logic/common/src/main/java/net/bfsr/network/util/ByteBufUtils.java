package net.bfsr.network.util;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import org.joml.Vector2f;

public final class ByteBufUtils {
    public static void writeString(ByteBuf byteBuf, String string) {
        byte[] bytes = string.getBytes(CharsetUtil.UTF_8);
        byteBuf.writeShort(bytes.length).writeBytes(bytes);
    }

    public static String readString(ByteBuf byteBuf) {
        return byteBuf.readCharSequence(byteBuf.readShort(), CharsetUtil.UTF_8).toString();
    }

    public static void writeVector(ByteBuf byteBuf, Vector2f vector) {
        byteBuf.writeFloat(vector.x);
        byteBuf.writeFloat(vector.y);
    }

    public static void readVector(ByteBuf byteBuf, Vector2f vector) {
        vector.set(byteBuf.readFloat(), byteBuf.readFloat());
    }
}
