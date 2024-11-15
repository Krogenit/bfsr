/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright notice,
 * 	  this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright notice,
 * 	  this list of conditions and the following disclaimer in the documentation
 * 	  and/or other materials provided with the distribution.
 *
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

import java.io.Serializable;

// updated to rev 100

/**
 * A transform contains translation and rotation. It is used to represent the position and
 * orientation of rigid frames.
 */
public class Transform implements Serializable {
    /**
     * The translation caused by the transform
     */
    public final Vector2 position;

    /**
     * A matrix representing a rotation
     */
    public final Rotation rotation;

    /**
     * The default constructor.
     */
    public Transform() {
        position = new Vector2();
        rotation = new Rotation();
    }

    /**
     * Initialize as a copy of another transform.
     */
    public Transform(final Transform xf) {
        position = xf.position.clone();
        rotation = xf.rotation.clone();
    }

    /**
     * Initialize using a position vector and a rotation matrix.
     */
    public Transform(final Vector2 position, final Rotation rotation) {
        this.position = position.clone();
        this.rotation = rotation.clone();
    }

    /**
     * Set this to equal another transform.
     */
    public final Transform set(final Transform transform) {
        position.set(transform.position);
        rotation.set(transform.rotation);
        return this;
    }

    /**
     * Set this based on the position and angle.
     */
    public final void set(Vector2 position, float angle) {
        this.position.set(position);
        rotation.set(angle);
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
    }

    public void setRotation(float sin, float cos) {
        rotation.set(sin, cos);
    }

    /**
     * Set this to the identity transform.
     */
    public final void setIdentity() {
        position.setZero();
        rotation.setIdentity();
    }

    public float getSin() {
        return rotation.sin;
    }

    public float getCos() {
        return rotation.cos;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public final static Vector2 mul(final Transform T, final Vector2 v) {
        return new Vector2((T.rotation.cos * v.x - T.rotation.sin * v.y) + T.position.x,
                (T.rotation.sin * v.x + T.rotation.cos * v.y) + T.position.y);
    }

    public final static void mulToOut(final Transform T, final Vector2 v, final Vector2 out) {
        final float tempy = (T.rotation.sin * v.x + T.rotation.cos * v.y) + T.position.y;
        out.x = (T.rotation.cos * v.x - T.rotation.sin * v.y) + T.position.x;
        out.y = tempy;
    }

    public final static void mulToOutUnsafe(final Transform T, final Vector2 v, final Vector2 out) {
        assert (v != out);
        out.x = (T.rotation.cos * v.x - T.rotation.sin * v.y) + T.position.x;
        out.y = (T.rotation.sin * v.x + T.rotation.cos * v.y) + T.position.y;
    }

    public final static Vector2 mulTrans(final Transform T, final Vector2 v) {
        final float px = v.x - T.position.x;
        final float py = v.y - T.position.y;
        return new Vector2((T.rotation.cos * px + T.rotation.sin * py), (-T.rotation.sin * px + T.rotation.cos * py));
    }

    public final static void mulTransToOut(final Transform T, final Vector2 v, final Vector2 out) {
        final float px = v.x - T.position.x;
        final float py = v.y - T.position.y;
        final float tempy = (-T.rotation.sin * px + T.rotation.cos * py);
        out.x = (T.rotation.cos * px + T.rotation.sin * py);
        out.y = tempy;
    }

    public final static void mulTransToOutUnsafe(final Transform T, final Vector2 v, final Vector2 out) {
        assert (v != out);
        final float px = v.x - T.position.x;
        final float py = v.y - T.position.y;
        out.x = (T.rotation.cos * px + T.rotation.sin * py);
        out.y = (-T.rotation.sin * px + T.rotation.cos * py);
    }

    public final static Transform mul(final Transform A, final Transform B) {
        Transform C = new Transform();
        Rotation.mulUnsafe(A.rotation, B.rotation, C.rotation);
        Rotation.mulToOutUnsafe(A.rotation, B.position, C.position);
        C.position.addLocal(A.position);
        return C;
    }

    public final static void mulToOut(final Transform A, final Transform B, final Transform out) {
        assert (out != A);
        Rotation.mul(A.rotation, B.rotation, out.rotation);
        Rotation.mulToOut(A.rotation, B.position, out.position);
        out.position.addLocal(A.position);
    }

    public final static void mulToOutUnsafe(final Transform A, final Transform B, final Transform out) {
        assert (out != B);
        assert (out != A);
        Rotation.mulUnsafe(A.rotation, B.rotation, out.rotation);
        Rotation.mulToOutUnsafe(A.rotation, B.position, out.position);
        out.position.addLocal(A.position);
    }

    private static final Vector2 pool = new Vector2();

    public final static Transform mulTrans(final Transform A, final Transform B) {
        Transform C = new Transform();
        Rotation.mulTransUnsafe(A.rotation, B.rotation, C.rotation);
        pool.set(B.position).subLocal(A.position);
        Rotation.mulTransUnsafe(A.rotation, pool, C.position);
        return C;
    }

    public final static void mulTransToOut(final Transform A, final Transform B, final Transform out) {
        assert (out != A);
        Rotation.mulTrans(A.rotation, B.rotation, out.rotation);
        pool.set(B.position).subLocal(A.position);
        Rotation.mulTrans(A.rotation, pool, out.position);
    }

    public final static void mulTransToOutUnsafe(final Transform A, final Transform B,
                                                 final Transform out) {
        assert (out != A);
        assert (out != B);
        Rotation.mulTransUnsafe(A.rotation, B.rotation, out.rotation);
        pool.set(B.position).subLocal(A.position);
        Rotation.mulTransUnsafe(A.rotation, pool, out.position);
    }

    @Override
    public final String toString() {
        String s = "XForm:\n";
        s += "Position: " + position + "\n";
        s += "R: \n" + rotation + "\n";
        return s;
    }
}
