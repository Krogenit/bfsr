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
package org.jbox2d.collision.shapes;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.RayCastInput;
import org.jbox2d.collision.RayCastOutput;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Rotation;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vector2;

/**
 * A circle shape.
 */
public class CircleShape extends Shape {
    public final Vector2 m_p;

    public CircleShape() {
        super(ShapeType.CIRCLE);
        m_p = new Vector2();
        radius = 0;
    }

    public final Shape clone() {
        CircleShape shape = new CircleShape();
        shape.m_p.x = m_p.x;
        shape.m_p.y = m_p.y;
        shape.radius = radius;
        return shape;
    }

    public final int getChildCount() {
        return 1;
    }

    /**
     * Get the supporting vertex index in the given direction.
     */
    public final int getSupport(final Vector2 d) {
        return 0;
    }

    /**
     * Get the supporting vertex in the given direction.
     */
    public final Vector2 getSupportVertex(final Vector2 d) {
        return m_p;
    }

    /**
     * Get the vertex count.
     */
    public final int getVertexCount() {
        return 1;
    }

    /**
     * Get a vertex by index.
     */
    public final Vector2 getVertex(final int index) {
        assert (index == 0);
        return m_p;
    }

    @Override
    public final boolean testPoint(final Transform transform, final Vector2 p) {
        // Rot.mulToOutUnsafe(transform.q, m_p, center);
        // center.addLocal(transform.p);
        //
        // final Vec2 d = center.subLocal(p).negateLocal();
        // return Vec2.dot(d, d) <= m_radius * m_radius;
        Rotation q = transform.rotation;
        Vector2 tp = transform.position;
        float centerx = -(q.cos * m_p.x - q.sin * m_p.y + tp.x - p.x);
        float centery = -(q.sin * m_p.x + q.cos * m_p.y + tp.y - p.y);

        return centerx * centerx + centery * centery <= radius * radius;
    }

    @Override
    public float computeDistanceToOut(Transform xf, Vector2 p, int childIndex, Vector2 normalOut) {
        Rotation xfq = xf.rotation;
        float centerx = xfq.cos * m_p.x - xfq.sin * m_p.y + xf.position.x;
        float centery = xfq.sin * m_p.x + xfq.cos * m_p.y + xf.position.y;
        float dx = p.x - centerx;
        float dy = p.y - centery;
        float d1 = MathUtils.sqrt(dx * dx + dy * dy);
        normalOut.x = dx * 1 / d1;
        normalOut.y = dy * 1 / d1;
        return d1 - radius;
    }

    // Collision Detection in Interactive 3D Environments by Gino van den Bergen
    // From Section 3.1.2
    // x = s + a * r
    // norm(x) = radius
    @Override
    public final boolean raycast(RayCastOutput output, RayCastInput input, Transform transform,
                                 int childIndex) {

        final Vector2 inputp1 = input.p1;
        final Vector2 inputp2 = input.p2;
        final Rotation tq = transform.rotation;
        final Vector2 tp = transform.position;

        // Rot.mulToOutUnsafe(transform.q, m_p, position);
        // position.addLocal(transform.p);
        final float positionx = tq.cos * m_p.x - tq.sin * m_p.y + tp.x;
        final float positiony = tq.sin * m_p.x + tq.cos * m_p.y + tp.y;

        final float sx = inputp1.x - positionx;
        final float sy = inputp1.y - positiony;
        // final float b = Vec2.dot(s, s) - m_radius * m_radius;
        final float b = sx * sx + sy * sy - radius * radius;

        // Solve quadratic equation.
        final float rx = inputp2.x - inputp1.x;
        final float ry = inputp2.y - inputp1.y;
        // final float c = Vec2.dot(s, r);
        // final float rr = Vec2.dot(r, r);
        final float c = sx * rx + sy * ry;
        final float rr = rx * rx + ry * ry;
        final float sigma = c * c - rr * b;

        // Check for negative discriminant and short segment.
        if (sigma < 0.0f || rr < Settings.EPSILON) {
            return false;
        }

        // Find the point of intersection of the line with the circle.
        float a = -(c + MathUtils.sqrt(sigma));

        // Is the intersection point on the segment?
        if (0.0f <= a && a <= input.maxFraction * rr) {
            a /= rr;
            output.fraction = a;
            output.normal.x = rx * a + sx;
            output.normal.y = ry * a + sy;
            output.normal.normalize();
            return true;
        }

        return false;
    }

    @Override
    public final void computeAABB(final AABB aabb, float x, float y, float sin, float cos, int childIndex) {
        final float px = cos * m_p.x - sin * m_p.y + x;
        final float py = sin * m_p.x + cos * m_p.y + y;

        aabb.lowerBound.x = px - radius;
        aabb.lowerBound.y = py - radius;
        aabb.upperBound.x = px + radius;
        aabb.upperBound.y = py + radius;
    }

    @Override
    public final void computeMass(final MassData massData, final float density) {
        massData.mass = density * Settings.PI * radius * radius;
        massData.center.x = m_p.x;
        massData.center.y = m_p.y;

        // inertia about the local origin
        // massData.I = massData.mass * (0.5f * m_radius * m_radius + Vec2.dot(m_p, m_p));
        massData.I = massData.mass * (0.5f * radius * radius + (m_p.x * m_p.x + m_p.y * m_p.y));
    }
}
