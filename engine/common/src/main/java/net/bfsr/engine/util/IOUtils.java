package net.bfsr.engine.util;

import javax.imageio.ImageIO;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class IOUtils {
    public static void writePNGGrayScale(ByteBuffer byteBuffer, int width, int height, String fileName) {
        byte[] dst = new byte[byteBuffer.remaining()];
        byteBuffer.get(dst);
        byteBuffer.position(0);
        DataBuffer buffer = new DataBufferByte(dst, dst.length);

        WritableRaster raster = Raster.createInterleavedRaster(buffer, width, height, width, 1, new int[]{0},
                null);
        ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), false, true, Transparency.OPAQUE,
                DataBuffer.TYPE_BYTE);
        BufferedImage image = new BufferedImage(cm, raster, true, null);

        try {
            ImageIO.write(image, "png", new File(fileName + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
