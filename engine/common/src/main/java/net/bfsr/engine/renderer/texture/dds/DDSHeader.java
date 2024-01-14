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

package net.bfsr.engine.renderer.texture.dds;

import java.io.IOException;
import java.nio.ByteBuffer;

class DDSHeader {
    /* Flags */
    private static final int DDSD_MIPMAPCOUNT = 0x020000;
    private static final int DDSCAPS2_CUBEMAP = 0x200;

    /**
     * Size of header in bytes
     */
    private int dwSize;

    /**
     * hasFlags to indicate which members contain valid data
     */
    private int dwFlags;

    /**
     * Height in pixels of surface
     */
    int dwHeight;

    /**
     * Width in pixels of surface
     */
    int dwWidth;

    /**
     * The pitch or number of bytes per scan line in an uncompressed texture
     */
    int dwPitchOrLinearSize;

    /**
     * Depth of a volume texture in pixels, otherwise unused
     */
    private int dwDepth;

    /**
     * Number of mipmap levels, otherwise unused
     */
    int dwMipMapCount;

    /**
     * Unused
     */
    private final int[] dwReserved = new int[11];

    /**
     * The pixel format
     */
    DDSPixelFormat ddspf;

    /**
     * Specifies the complexity of the surfaces stored
     */
    private int dwCaps;

    /**
     * Additional details about the surfaces stored
     */
    private int dwCaps2;

    boolean hasFlagMipMapCount;

    boolean hasCaps2CubeMap;

    DDSHeader(ByteBuffer header) throws IOException {
        if (header.capacity() != 124) {
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

        ddspf = new DDSPixelFormat(header);

        dwCaps = header.getInt();
        dwCaps2 = header.getInt();

        /* Flags */
        hasFlagMipMapCount = (dwFlags & DDSD_MIPMAPCOUNT) == DDSD_MIPMAPCOUNT;

        /* Caps2 */
        hasCaps2CubeMap = (dwCaps2 & DDSCAPS2_CUBEMAP) == DDSCAPS2_CUBEMAP;
    }
}