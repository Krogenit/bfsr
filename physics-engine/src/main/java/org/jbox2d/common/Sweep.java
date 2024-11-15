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
package org.jbox2d.common;

/**
 * This describes the motion of a body/shape for TOI computation. Shapes are defined with respect to
 * the body origin, which may not coincide with the center of mass. However, to support dynamics we
 * must interpolate the center of mass position.
 */
public class Sweep {
    /**
     * Local center of mass position
     */
    public final Vector2 localCenter;
    /**
     * Center world positions
     */
    public final Vector2 center0, center;
    /**
     * World angles
     */
    public float angle0, angle;

    /**
     * Fraction of the current time step in the range [0,1] c0 and a0 are the positions at alpha0.
     */
    public float alpha0;

    public String toString() {
        String s = "Sweep:\nlocalCenter: " + localCenter + "\n";
        s += "c0: " + center0 + ", c: " + center + "\n";
        s += "a0: " + angle0 + ", a: " + angle + "\n";
        s += "alpha0: " + alpha0;
        return s;
    }

    public Sweep() {
        localCenter = new Vector2();
        center0 = new Vector2();
        center = new Vector2();
    }

    public final void normalize() {
        float d = MathUtils.TWOPI * MathUtils.floor(angle0 / MathUtils.TWOPI);
        angle0 -= d;
        angle -= d;
    }

    public final Sweep set(Sweep other) {
        localCenter.set(other.localCenter);
        center0.set(other.center0);
        center.set(other.center);
        angle0 = other.angle0;
        angle = other.angle;
        alpha0 = other.alpha0;
        return this;
    }

    /**
     * Get the interpolated transform at a specific time.
     *
     * @param xf   the result is placed here - must not be null
     * @param beta the normalized time in [0,1].
     */
    public final void getTransform(final Transform xf, final float beta) {
        assert (xf != null);
        // xf->p = (1.0f - beta) * c0 + beta * c;
        // float32 angle = (1.0f - beta) * a0 + beta * a;
        // xf->q.Set(angle);
        xf.position.x = (1.0f - beta) * center0.x + beta * center.x;
        xf.position.y = (1.0f - beta) * center0.y + beta * center.y;
        float angle = (1.0f - beta) * angle0 + beta * this.angle;
        xf.rotation.set(angle);

        // Shift to origin
        // xf->p -= b2Mul(xf->q, localCenter);
        final Rotation q = xf.rotation;
        xf.position.x -= q.cos * localCenter.x - q.sin * localCenter.y;
        xf.position.y -= q.sin * localCenter.x + q.cos * localCenter.y;
    }

    /**
     * Advance the sweep forward, yielding a new initial state.
     *
     * @param alpha the new initial time.
     */
    public final void advance(final float alpha) {
        assert (alpha0 < 1.0f);
        // float32 beta = (alpha - alpha0) / (1.0f - alpha0);
        // c0 += beta * (c - c0);
        // a0 += beta * (a - a0);
        // alpha0 = alpha;
        float beta = (alpha - alpha0) / (1.0f - alpha0);
        center0.x += beta * (center.x - center0.x);
        center0.y += beta * (center.y - center0.y);
        angle0 += beta * (angle - angle0);
        alpha0 = alpha;
    }
}
