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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

class DDSPixelFormat {
    /* Flags */
    private static final int DDPF_FOURCC = 0x00004;

    /**
     * Structure size in bytes
     */
    private final int dwSize;

    /**
     * Values which indicate what type of data is in the surface
     */
    private final int dwFlags;

    /**
     * Four-character code for specifying compressed or custom format
     */
    private final int dwFourCC;

    /**
     * Number of bits in an RGB (possibly including alpha) format
     */
    private final int dwRGBBitCount;

    /**
     * Red (or lumiannce or Y) mask for reading color data
     */
    private final int dwRBitMask;

    /**
     * Green (or U) mask for reading color data
     */
    private final int dwGBitMask;

    /**
     * Blue (or V) mask for reading color data
     */
    private final int dwBBitMask;

    /**
     * Alpha mask for reading alpha data
     */
    private final int dwABitMask;

    /**
     * Four-character code's String representation
     */
    final String sFourCC;

    DDSPixelFormat(ByteBuffer header) {
        dwSize = header.getInt();
        dwFlags = header.getInt();
        dwFourCC = header.getInt();
        dwRGBBitCount = header.getInt();
        dwRBitMask = header.getInt();
        dwGBitMask = header.getInt();
        dwBBitMask = header.getInt();
        dwABitMask = header.getInt();

        sFourCC = createFourCCString(dwFourCC);
    }

    /**
     * Constructs the four-character code's String representation from the integer value.
     */
    private String createFourCCString(int fourCC) {
        byte[] fourCCString = new byte[DDPF_FOURCC];
        for (int i = 0; i < fourCCString.length; i++) fourCCString[i] = (byte) (fourCC >> (i << 3));
        return new String(fourCCString, StandardCharsets.UTF_8);
    }
}