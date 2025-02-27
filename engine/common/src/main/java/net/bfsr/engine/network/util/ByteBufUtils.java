package net.bfsr.engine.network.util;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import net.bfsr.engine.geometry.GeometryUtils;
import org.joml.Vector2f;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

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

    public static void writePolygon(ByteBuf byteBuf, Polygon polygon) {
        CoordinateSequence coordinateSequence = polygon.getExteriorRing().getCoordinateSequence();
        int length = coordinateSequence.size() - 1;
        byteBuf.writeShort(length);
        for (int i = 0; i < length; i++) {
            Coordinate coordinate = coordinateSequence.getCoordinate(i);
            byteBuf.writeFloat((float) coordinate.x);
            byteBuf.writeFloat((float) coordinate.y);
        }

        int numInteriorRing = polygon.getNumInteriorRing();
        byteBuf.writeByte(numInteriorRing);
        for (int i = 0; i < numInteriorRing; i++) {
            coordinateSequence = polygon.getInteriorRingN(i).getCoordinateSequence();
            length = coordinateSequence.size() - 1;
            byteBuf.writeShort(length);
            for (int j = 0; j < length; j++) {
                Coordinate coordinate = coordinateSequence.getCoordinate(j);
                byteBuf.writeFloat((float) coordinate.x);
                byteBuf.writeFloat((float) coordinate.y);
            }
        }
    }

    public static Polygon readPolygon(ByteBuf byteBuf) {
        short count = byteBuf.readShort();
        Coordinate[] coordinates = new Coordinate[count + 1];
        for (int i = 0; i < count; i++) {
            coordinates[i] = new Coordinate(byteBuf.readFloat(), byteBuf.readFloat());
        }
        coordinates[count] = coordinates[0];
        LinearRing linearRing = GeometryUtils.createLinearRing(coordinates);

        byte holesCount = byteBuf.readByte();
        LinearRing[] holes = null;
        if (holesCount > 0) {
            holes = new LinearRing[holesCount];

            for (int i = 0; i < holesCount; i++) {
                count = byteBuf.readShort();
                coordinates = new Coordinate[count + 1];
                for (int j = 0; j < count; j++) {
                    coordinates[j] = new Coordinate(byteBuf.readFloat(), byteBuf.readFloat());
                }
                coordinates[count] = coordinates[0];
                holes[i] = GeometryUtils.createLinearRing(coordinates);
            }
        }

        return GeometryUtils.createPolygon(linearRing, holes);
    }
}