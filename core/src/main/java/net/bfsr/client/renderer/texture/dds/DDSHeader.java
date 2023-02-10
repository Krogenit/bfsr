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

package net.bfsr.client.renderer.texture.dds;

import java.io.IOException;
import java.nio.ByteBuffer;

class DDSHeader {

    /* Flags */
    private static final int DDSD_CAPS = 0x000001;
    private static final int DDSD_HEIGHT = 0x000002;
    private static final int DDSD_WIDTH = 0x000004;
    private static final int DDSD_PITCH = 0x000008;
    private static final int DDSD_PIXELFORMAT = 0x001000;
    private static final int DDSD_MIPMAPCOUNT = 0x020000;
    private static final int DDSD_LINEARSIZE = 0x080000;
    private static final int DDSD_DEPTH = 0x800000;

    private static final int DDSCAPS_COMPLEX = 0x8;
    private static final int DDSCAPS_MIPMAP = 0x400000;
    private static final int DDSCAPS_TEXTURE = 0x1000;

    private static final int DDSCAPS2_CUBEMAP = 0x200;
    private static final int DDSCAPS2_CUBEMAP_POSITIVEX = 0x400;
    private static final int DDSCAPS2_CUBEMAP_NEGATIVEX = 0x800;
    private static final int DDSCAPS2_CUBEMAP_POSITIVEY = 0x1000;
    private static final int DDSCAPS2_CUBEMAP_NEGATIVEY = 0x2000;
    private static final int DDSCAPS2_CUBEMAP_POSITIVEZ = 0x4000;
    private static final int DDSCAPS2_CUBEMAP_NEGATIVEZ = 0x8000;
    private static final int DDSCAPS2_VOLUME = 0x200000;

    /** Size of header in bytes */
    private int dwSize;

    /** hasFlags to indicate which members contain valid data */
    private int dwFlags;

    /** Height in pixels of surface */
    int dwHeight;

    /** Width in pixels of surface */
    int dwWidth;

    /** The pitch or number of bytes per scan line in an uncompressed texture */
    int dwPitchOrLinearSize;

    /** Depth of a volume texture in pixels, otherwise unused */
    private int dwDepth;

    /** Number of mipmap levels, otherwise unused */
    int dwMipMapCount;

    /** Unused */
    private final int[] dwReserved = new int[11];

    /** The pixel format */
    DDSPixelFormat ddspf;

    /** Specifies the complexity of the surfaces stored */
    private int dwCaps;

    /** Additional details about the surfaces stored */
    private int dwCaps2;

    /** Unused */
    private int dwCaps3;

    /** Unused */
    private int dwCaps4;

    /** Unused */
    private int dwReserved2;

    boolean hasFlagMipMapCount;
    private boolean hasFlagCaps;
    private boolean hasFlagHeight;
    private boolean hasFlagWidth;
    private boolean hasFlagPitch;
    private boolean hasFlagPixelFormat;
    private boolean hasFlagLinearSize;
    private boolean hasFlagDepth;

    private boolean hasCapsComplex;
    private boolean hasCapsMipMap;
    private boolean hasCapsTexture;

    boolean hasCaps2CubeMap;
    private boolean hasCaps2CubeMapPX;
    private boolean hasCaps2CubeMapNX;
    private boolean hasCaps2CubeMapPY;
    private boolean hasCaps2CubeMapNY;
    private boolean hasCaps2CubeMapPZ;
    private boolean hasCaps2CubeMapNZ;
    private boolean hasCaps2Volume;

    DDSHeader(ByteBuffer header, boolean printDebug) throws IOException {
        if (header.capacity() != 124) {
            if (printDebug) System.err.println("Header is not 124 bytes!");
            return;
        }

        dwSize = header.getInt();
        dwFlags = header.getInt();
        dwHeight = header.getInt();
        dwWidth = header.getInt();
        dwPitchOrLinearSize = header.getInt();
        dwDepth = header.getInt();
        dwMipMapCount = header.getInt();

        // Unused bytes
        for (int i = 0; i < dwReserved.length; i++) dwReserved[i] = header.getInt();

        ddspf = new DDSPixelFormat(header, printDebug);

        dwCaps = header.getInt();
        dwCaps2 = header.getInt();

        // Unused bytes
        dwCaps3 = header.getInt();
        dwCaps4 = header.getInt();
        dwReserved2 = header.getInt();

        /* Flags */
        hasFlagCaps = (dwFlags & DDSD_CAPS) == DDSD_CAPS;
        hasFlagHeight = (dwFlags & DDSD_HEIGHT) == DDSD_HEIGHT;
        hasFlagWidth = (dwFlags & DDSD_WIDTH) == DDSD_WIDTH;
        hasFlagPitch = (dwFlags & DDSD_PITCH) == DDSD_PITCH;
        hasFlagPixelFormat = (dwFlags & DDSD_PIXELFORMAT) == DDSD_PIXELFORMAT;
        hasFlagMipMapCount = (dwFlags & DDSD_MIPMAPCOUNT) == DDSD_MIPMAPCOUNT;
        hasFlagLinearSize = (dwFlags & DDSD_LINEARSIZE) == DDSD_LINEARSIZE;
        hasFlagDepth = (dwFlags & DDSD_DEPTH) == DDSD_DEPTH;

        /* Caps */
        hasCapsComplex = (dwCaps & DDSCAPS_COMPLEX) == DDSCAPS_COMPLEX;
        hasCapsMipMap = (dwCaps & DDSCAPS_MIPMAP) == DDSCAPS_MIPMAP;
        hasCapsTexture = (dwCaps & DDSCAPS_TEXTURE) == DDSCAPS_TEXTURE;

        /* Caps2 */
        hasCaps2CubeMap = (dwCaps2 & DDSCAPS2_CUBEMAP) == DDSCAPS2_CUBEMAP;
        hasCaps2CubeMapPX = (dwCaps2 & DDSCAPS2_CUBEMAP_POSITIVEX) == DDSCAPS2_CUBEMAP_POSITIVEX;
        hasCaps2CubeMapNX = (dwCaps2 & DDSCAPS2_CUBEMAP_NEGATIVEX) == DDSCAPS2_CUBEMAP_NEGATIVEX;
        hasCaps2CubeMapPY = (dwCaps2 & DDSCAPS2_CUBEMAP_POSITIVEY) == DDSCAPS2_CUBEMAP_POSITIVEY;
        hasCaps2CubeMapNY = (dwCaps2 & DDSCAPS2_CUBEMAP_NEGATIVEY) == DDSCAPS2_CUBEMAP_NEGATIVEY;
        hasCaps2CubeMapPZ = (dwCaps2 & DDSCAPS2_CUBEMAP_POSITIVEZ) == DDSCAPS2_CUBEMAP_POSITIVEZ;
        hasCaps2CubeMapNZ = (dwCaps2 & DDSCAPS2_CUBEMAP_NEGATIVEZ) == DDSCAPS2_CUBEMAP_NEGATIVEZ;
        hasCaps2Volume = (dwCaps2 & DDSCAPS2_VOLUME) == DDSCAPS2_VOLUME;

        // Do some error checking.

        if (!hasFlagCaps || !hasFlagHeight || !hasFlagWidth || !hasFlagPixelFormat) {
            if (printDebug) System.err.println("DDS: Required flags missing!");
        }

        if (!hasCapsTexture) {
            if (printDebug) System.err.println("DDS: Required caps missing!");
        }

        // Print out the debug information received from successful load
        if (printDebug) {
            String sysout = "\nDDSHeader properties:\n"
                    + "\tdwSize \t\t\t\t\t= " + dwSize
                    + "\n\tFlags:\t\t\t\t\t";

            if (hasFlagCaps) sysout += "DDSD_CAPS | ";
            if (hasFlagHeight) sysout += "DDSD_HEIGHT | ";
            if (hasFlagWidth) sysout += "DDSD_WIDTH | ";
            if (hasFlagPitch) sysout += "DDSD_PITCH | ";
            if (hasFlagPixelFormat) sysout += "DDSD_PIXELFORMAT | ";
            if (hasFlagMipMapCount) sysout += "DDSD_MIPMAPCOUNT | ";
            if (hasFlagLinearSize) sysout += "DDSD_LINEARSIZE | ";
            if (hasFlagDepth) sysout += "DDSD_DEPTH | ";

            sysout += "\n\tdwHeight \t\t\t\t= " + dwHeight + "\n"
                    + "\tdwWidth \t\t\t\t= " + dwWidth + "\n";

            if (hasFlagPitch) sysout += "\tdwPitchOrLinearSize \t= " + dwPitchOrLinearSize + "\n";
            if (hasFlagLinearSize) sysout += "\tdwPitchOrLinearSize \t= " + dwPitchOrLinearSize + "\n";
            if (hasFlagDepth) sysout += "\tdwDepth \t\t\t\t= " + dwDepth + "\n";
            if (hasFlagMipMapCount) sysout += "\tdwMipMapCount \t\t\t= " + dwMipMapCount + "\n";
            if (hasFlagCaps) {
                sysout += "\tCaps:\t\t\t\t\t";
                if (hasCapsComplex) sysout += "DDSCAPS_COMPLEX | ";
                if (hasCapsMipMap) sysout += "DDSCAPS_MIPMAP | ";
                if (hasCapsTexture) sysout += "DDSCAPS_TEXTURE | ";

                if (hasCapsComplex) {
                    sysout += "\n\tCaps2:\t\t\t\t\t";
                    if (hasCaps2CubeMap) sysout += "DDSCAPS2_CUBEMAP | ";
                    if (hasCaps2CubeMapPX) sysout += "DDSCAPS2_CUBEMAP_POSITIVEX | ";
                    if (hasCaps2CubeMapNX) sysout += "DDSCAPS_CUBEMAP_NEGATIVEX | ";
                    if (hasCaps2CubeMapPY) sysout += "DDSCAPS2_CUBEMAP_POSITIVEY | ";
                    if (hasCaps2CubeMapNY) sysout += "DDSCAPS_CUBEMAP_NEGATIVEY | ";
                    if (hasCaps2CubeMapPZ) sysout += "DDSCAPS2_CUBEMAP_POSITIVEZ | ";
                    if (hasCaps2CubeMapNZ) sysout += "DDSCAPS_CUBEMAP_NEGATIVEZ | ";
                    if (hasCaps2Volume) sysout += "DDSCAPS2_VOLUME";
                }
            }
            System.out.println(sysout);
        }
    }

}
