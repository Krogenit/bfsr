/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright notice,
 * 	  this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright notice,
 * 	  this list of conditions and the following disclaimer in the documentation
 * 	  and/or other materials provided with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
/*
 * JBox2D - A Java Port of Erin Catto's Box2D
 *
 * JBox2D homepage: http://jbox2d.sourceforge.net/
 * Box2D homepage: http://www.box2d.org
 *
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the authors be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not
 * claim that you wrote the original software. If you use this software
 * in a product, an acknowledgment in the product documentation would be
 * appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */
package org.jbox2d.common;

/**
 * A few math methods that don't fit very well anywhere else.
 */
public class MathUtils extends PlatformMathUtils {
    public static final float PI = (float) Math.PI;
    public static final float TWOPI = (float) (Math.PI * 2);
    public static final float HALF_PI = PI / 2;

    private static final float[] sinLUT = new float[Settings.SINCOS_LUT_LENGTH];

    static {
        for (int i = 0; i < Settings.SINCOS_LUT_LENGTH; i++) {
            sinLUT[i] = (float) Math.sin(i * Settings.SINCOS_LUT_PRECISION);
        }
    }

    public static final float sin(float x) {
        if (Settings.SINCOS_LUT_ENABLED) {
            return sinLUT(x);
        } else {
            return (float) StrictMath.sin(x);
        }
    }

    public static final float sinLUT(float x) {
        x %= TWOPI;

        if (x < 0) {
            x += TWOPI;
        }

        if (Settings.SINCOS_LUT_LERP) {

            x /= Settings.SINCOS_LUT_PRECISION;

            final int index = (int) x;

            if (index != 0) {
                x %= index;
            }

            // the next index is 0
            if (index == Settings.SINCOS_LUT_LENGTH - 1) {
                return ((1 - x) * sinLUT[index] + x * sinLUT[0]);
            } else {
                return ((1 - x) * sinLUT[index] + x * sinLUT[index + 1]);
            }
        } else {
            return sinLUT[round(x / Settings.SINCOS_LUT_PRECISION) % Settings.SINCOS_LUT_LENGTH];
        }
    }

    public static final float cos(float x) {
        if (Settings.SINCOS_LUT_ENABLED) {
            return sinLUT(HALF_PI - x);
        } else {
            return (float) StrictMath.cos(x);
        }
    }

    public static final float abs(final float x) {
        if (Settings.FAST_ABS) {
            return x > 0 ? x : -x;
        } else {
            return StrictMath.abs(x);
        }
    }

    public static final int abs(int x) {
        int y = x >> 31;
        return (x ^ y) - y;
    }

    public static final int floor(final float x) {
        if (Settings.FAST_FLOOR) {
            return fastFloor(x);
        } else {
            return (int) StrictMath.floor(x);
        }
    }

    public static final int fastFloor(final float x) {
        int y = (int) x;
        if (x < y) {
            return y - 1;
        }
        return y;
    }

    public static final int round(final float x) {
        if (Settings.FAST_ROUND) {
            return floor(x + .5f);
        } else {
            return StrictMath.round(x);
        }
    }

    public final static float max(final float a, final float b) {
        return a > b ? a : b;
    }

    public final static int max(final int a, final int b) {
        return a > b ? a : b;
    }

    public final static float min(final float a, final float b) {
        return a < b ? a : b;
    }

    public final static int min(final int a, final int b) {
        return a < b ? a : b;
    }

    public final static float map(final float val, final float fromMin, final float fromMax,
                                  final float toMin, final float toMax) {
        final float mult = (val - fromMin) / (fromMax - fromMin);
        return toMin + mult * (toMax - toMin);
    }

    /**
     * Returns the closest value to 'a' that is in between 'low' and 'high'
     */
    public final static float clamp(final float a, final float low, final float high) {
        return max(low, min(a, high));
    }

    public final static Vector2 clamp(final Vector2 a, final Vector2 low, final Vector2 high) {
        final Vector2 min = new Vector2();
        min.x = a.x < high.x ? a.x : high.x;
        min.y = a.y < high.y ? a.y : high.y;
        min.x = low.x > min.x ? low.x : min.x;
        min.y = low.y > min.y ? low.y : min.y;
        return min;
    }

    public static final float atan2(final float y, final float x) {
        if (Settings.FAST_ATAN2) {
            return fastAtan2(y, x);
        } else {
            return (float) StrictMath.atan2(y, x);
        }
    }

    public static final float fastAtan2(float y, float x) {
        if (x == 0.0f) {
            if (y > 0.0f) return HALF_PI;
            if (y == 0.0f) return 0.0f;
            return -HALF_PI;
        }
        float atan;
        final float z = y / x;
        if (abs(z) < 1.0f) {
            atan = z / (1.0f + 0.28f * z * z);
            if (x < 0.0f) {
                if (y < 0.0f) return atan - PI;
                return atan + PI;
            }
        } else {
            atan = HALF_PI - z / (z * z + 0.28f);
            if (y < 0.0f) return atan - PI;
        }
        return atan;
    }

    public static final float sqrt(float x) {
        return (float) StrictMath.sqrt(x);
    }

    public final static float distanceSquared(Vector2 v1, Vector2 v2) {
        float dx = (v1.x - v2.x);
        float dy = (v1.y - v2.y);
        return dx * dx + dy * dy;
    }

    public final static float distance(Vector2 v1, Vector2 v2) {
        return sqrt(distanceSquared(v1, v2));
    }
}