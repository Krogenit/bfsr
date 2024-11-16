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

import lombok.Getter;

/**
 * Represents a rotation
 *
 * @author Daniel
 */
@Getter
public class Rotation {
    public float sin, cos;

    public Rotation() {
        setIdentity();
    }

    public Rotation(float angle) {
        set(angle);
    }

    @Override
    public String toString() {
        return "Rotation(sin: " + sin + ", cos: " + cos + ")";
    }

    public Rotation set(float angle) {
        sin = MathUtils.sin(angle);
        cos = MathUtils.cos(angle);
        return this;
    }

    public Rotation set(float sin, float cos) {
        this.sin = sin;
        this.cos = cos;
        return this;
    }

    public Rotation set(Rotation other) {
        sin = other.sin;
        cos = other.cos;
        return this;
    }

    public Rotation setIdentity() {
        sin = 0;
        cos = 1;
        return this;
    }

    public float getAngle() {
        return MathUtils.atan2(sin, cos);
    }

    public void getXAxis(Vector2 xAxis) {
        xAxis.set(cos, sin);
    }

    public void getYAxis(Vector2 yAxis) {
        yAxis.set(-sin, cos);
    }

    // @Override // annotation omitted for GWT-compatibility
    public Rotation clone() {
        Rotation copy = new Rotation();
        copy.sin = sin;
        copy.cos = cos;
        return copy;
    }

    public static final void mul(Rotation q, Rotation r, Rotation out) {
        float tempc = q.cos * r.cos - q.sin * r.sin;
        out.sin = q.sin * r.cos + q.cos * r.sin;
        out.cos = tempc;
    }

    public static final void mulUnsafe(Rotation q, Rotation r, Rotation out) {
        assert (r != out);
        assert (q != out);
        // [qc -qs] * [rc -rs] = [qc*rc-qs*rs -qc*rs-qs*rc]
        // [qs qc] [rs rc] [qs*rc+qc*rs -qs*rs+qc*rc]
        // s = qs * rc + qc * rs
        // c = qc * rc - qs * rs
        out.sin = q.sin * r.cos + q.cos * r.sin;
        out.cos = q.cos * r.cos - q.sin * r.sin;
    }

    public static final void mulTrans(Rotation q, Rotation r, Rotation out) {
        final float tempc = q.cos * r.cos + q.sin * r.sin;
        out.sin = q.cos * r.sin - q.sin * r.cos;
        out.cos = tempc;
    }

    public static final void mulTransUnsafe(Rotation q, Rotation r, Rotation out) {
        // [ qc qs] * [rc -rs] = [qc*rc+qs*rs -qc*rs+qs*rc]
        // [-qs qc] [rs rc] [-qs*rc+qc*rs qs*rs+qc*rc]
        // s = qc * rs - qs * rc
        // c = qc * rc + qs * rs
        out.sin = q.cos * r.sin - q.sin * r.cos;
        out.cos = q.cos * r.cos + q.sin * r.sin;
    }

    public static final void mulToOut(Rotation q, Vector2 v, Vector2 out) {
        float tempy = q.sin * v.x + q.cos * v.y;
        out.x = q.cos * v.x - q.sin * v.y;
        out.y = tempy;
    }

    public static final void mulToOutUnsafe(Rotation q, Vector2 v, Vector2 out) {
        out.x = q.cos * v.x - q.sin * v.y;
        out.y = q.sin * v.x + q.cos * v.y;
    }

    public static final void mulTrans(Rotation q, Vector2 v, Vector2 out) {
        final float tempy = -q.sin * v.x + q.cos * v.y;
        out.x = q.cos * v.x + q.sin * v.y;
        out.y = tempy;
    }

    public static final void mulTransUnsafe(Rotation q, Vector2 v, Vector2 out) {
        out.x = q.cos * v.x + q.sin * v.y;
        out.y = -q.sin * v.x + q.cos * v.y;
    }
}
