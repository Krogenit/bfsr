/*
 * Copyright (C) 2018  Magnus Bull
 *
 *  This file is part of dds-lwjgl.
 *
 *  dds-lwjgl is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  dds-lwjgl is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with dds-lwjgl.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.bfsr.client.render.texture.dds;

import org.lwjgl.opengl.EXTTextureCompressionS3TC;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple class for loading DirectDraw Surface (*.dds) files for LWJGL. DDS files have many variants and so this parser only supports the following:<br/>
 * <ul>
 * <li>Standard 124 byte headers (not extended D3D headers).</li>
 * <li>Compressed textures using DXT1, DXT3 and DXT5 formats.</li>
 * <li>Reads 2D textures with and without mipmaps (though they are discarded).</li>
 * <li>Reads Cubemap textures without mipmaps. Cubemaps with mipmaps appear offset.</li>
 * <li>Does not support volume maps.</li>
 * <li>Does not support legacy formats.</li>
 * </ul>
 *
 * @author Magnus Bull
 */
public class DDSFile {
    private boolean printDebug;

    /** A 32-bit representation of the character sequence "DDS " which is the magic word for DDS files. */
    private static final int DDS_MAGIC = 0x20534444;

    /** Stores the magic word for the binary document read */
    private int dwMagic;

    /** The header information for this DDS document */
    private DDSHeader header;

    /** Arrays of bytes that contain the main surface image data */
    private List<ByteBuffer> bdata;

    /** Arrays of bytes that contain the secondary surface data, like mipmap levels */
    private List<ByteBuffer> bdata2;

    /** The calculated size of the image */
    private int imageSize;

    /** The compression format for the current DDS document */
    private int dxtFormat;

    /**
     * Loads a DDS file from the given file.
     *
     * @param in
     * @throws IOException
     * @throws FileNotFoundException
     */
    public DDSFile(InputStream in) {
        this.loadFile(in);
    }

    public void clear() {
        if (bdata != null) {
            int size = bdata.size();
            for (int i = 0; i < size; i++) {
                ByteBuffer buffer = bdata.get(i);
                buffer.clear();
            }

            bdata.clear();
            bdata = null;
        }

        if (bdata2 != null) {
            int size = bdata2.size();
            for (int i = 0; i < size; i++) {
                ByteBuffer buffer = bdata2.get(i);
                buffer.clear();
            }

            bdata2.clear();
            bdata2 = null;
        }
    }

    /**
     * Loads a DDS file.
     *
     * @param fis
     * @throws IOException
     */
    private void loadFile(InputStream fis) {
        bdata = new ArrayList<>();
        bdata2 = new ArrayList<>(); //TODO: Not properly implemented yet.

        try {
            int totalByteCount = fis.available();
            if (printDebug) System.out.println("Total bytes: " + totalByteCount);

            byte[] bMagic = new byte[4];
            fis.read(bMagic);
            dwMagic = newByteBuffer(bMagic).getInt();

            if (dwMagic != DDS_MAGIC) {
                System.err.println("Wrong magic word! This is not a DDS file.");
                fis.close();
                return;
            }

            byte[] bHeader = new byte[124];
            fis.read(bHeader);
            header = new DDSHeader(newByteBuffer(bHeader), printDebug);

            int blockSize = 16;
            if ("DXT1".equalsIgnoreCase(header.ddspf.sFourCC)) {
                dxtFormat = EXTTextureCompressionS3TC.GL_COMPRESSED_RGB_S3TC_DXT1_EXT;
                blockSize = 8;
            } else if ("DXT3".equalsIgnoreCase(header.ddspf.sFourCC)) {
                dxtFormat = EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT3_EXT;
            } else if ("DXT5".equalsIgnoreCase(header.ddspf.sFourCC)) {
                dxtFormat = EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
            } else if ("DX10".equalsIgnoreCase(header.ddspf.sFourCC)) {
                System.err.println("Uses DX10 extended header, which is not supported!");
                fis.close();
                return;
            } else {
                System.err.println("Surface format unknown or not supported: " + header.ddspf.sFourCC);
            }

            int surfaceCount;
            totalByteCount -= 128;

            if (header.hasCaps2CubeMap) {
                surfaceCount = 6;
            } else {
                surfaceCount = 1;
            }

            imageSize = calculatePitch(blockSize);

            int size = header.dwPitchOrLinearSize;

            if (printDebug) System.out.println("Calculated pitch: " + imageSize);
            if (printDebug) System.out.println("Included PitchOrLinearSize: " + header.dwPitchOrLinearSize);
            if (printDebug) System.out.println("Mipmap count: " + header.dwMipMapCount);

            for (int i = 0; i < surfaceCount; i++) {
                byte[] bytes = new byte[size];

                if (printDebug) System.out.println("Getting main surface " + i + ". Bytes: " + bytes.length);

                fis.read(bytes);
                totalByteCount -= bytes.length;
                bdata.add(newByteBuffer(bytes));

                if (header.hasFlagMipMapCount) {
                    int size2 = Math.max(size / 4, blockSize);
                    int maxMipMaps = Math.min(5, header.dwMipMapCount - 1);
                    for (int j = 0; j < maxMipMaps; j++) {
                        byte[] bytes2 = new byte[size2];

                        if (printDebug) System.out.println("Getting secondary surface " + j + ". Bytes: " + bytes2.length);

                        fis.read(bytes2);
                        totalByteCount -= bytes2.length;
                        bdata2.add(newByteBuffer(bytes2));
                        size2 = Math.max(size2 / 4, blockSize);
                    }
                }
            }

            if (printDebug) System.out.printf("Remaining bytes: %d (%d)%n", fis.available(), totalByteCount);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int calculatePitch(int blockSize) {
        return Math.max(1, ((header.dwWidth + 3) / 4)) * blockSize;
    }

    /** Creates a new ByteBuffer and stores the data within it before returning it. */
    private static ByteBuffer newByteBuffer(byte[] data) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(data.length).order(ByteOrder.nativeOrder());
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    public int getWidth() {
        return header.dwWidth;
    }

    public int getHeight() {
        return header.dwHeight;
    }

    /**
     * Gets the main surface data buffer.
     */
    public ByteBuffer getBuffer() {
        return getMainBuffer();
    }

    /**
     * Gets the main surface data buffer - usually the first full-sized image.
     */
    private ByteBuffer getMainBuffer() {
        return bdata.get(0);
    }

    public int getMipMapCount() {
        return Math.min(this.header.dwMipMapCount - 1, this.bdata2.size());
    }

    /**
     * Gets a specific level from the amount of mipmaps. If specified outside the range of available mipmaps, the closest one is returned.
     */
    public ByteBuffer getMipMapLevel(int level) {
        level = Math.min(Math.min(header.dwMipMapCount - 1, level), Math.max(level, 0));
        return this.bdata2.get(level);
    }

    public int getDXTFormat() {
        return dxtFormat;
    }
}
