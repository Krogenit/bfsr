/*
 * Copyright (c) 2010-2022 William Bittle  http://www.dyn4j.org/
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions
 *     and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *     and the following disclaimer in the documentation and/or other materials provided with the
 *     distribution.
 *   * Neither the name of the copyright holder nor the names of its contributors may be used to endorse or
 *     promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dyn4j.geometry;

import org.dyn4j.Epsilon;
import org.jbox2d.common.Vector2;

/**
 * Implementation of a Line Segment {@link org.jbox2d.collision.shapes.Shape}.
 * <p>
 * This class represents a line segment that is infinitely thin.
 *
 * @author William Bittle
 * @version 4.2.1
 * @since 1.0.0
 */
public class Segment {

    /**
     * Determines where the point is relative to the given line.
     * <p style="white-space: pre;"> Set L = linePoint2 - linePoint1
     * Set P = point - linePoint1
     * location = L.cross(P)</p>
     * Returns 0 if the point lies on the line created from the line segment.<br>
     * Assuming a right handed coordinate system:<br>
     * Returns &lt; 0 if the point lies on the right side of the line<br>
     * Returns &gt; 0 if the point lies on the left side of the line
     * <p>
     * Assumes all points are in world space.
     *
     * @param point      the point
     * @param linePoint1 the first point of the line
     * @param linePoint2 the second point of the line
     * @return double
     * @throws NullPointerException if point, linePoint1, or linePoint2 is null
     */
    public static final double getLocation(Vector2 point, Vector2 linePoint1, Vector2 linePoint2) {
        return (linePoint2.x - linePoint1.x) * (point.y - linePoint1.y) -
                (point.x - linePoint1.x) * (linePoint2.y - linePoint1.y);
    }

    /**
     * Returns the intersection point of the two line segments or null if they are parallel, coincident
     * or don't intersect.
     * <p>
     * If we let:
     * <p style="white-space: pre;"> A = A<sub>p2</sub> - A<sub>p1</sub>
     * B = B<sub>p2</sub> - B<sub>p1</sub></p>
     * we can create two parametric equations:
     * <p style="white-space: pre;"> Q = A<sub>p1</sub> + t<sub>a</sub>A
     * Q = B<sub>p1</sub> + t<sub>b</sub>B</p>
     * Where Q is the intersection point:
     * <p style="white-space: pre;"> A<sub>p1</sub> + t<sub>a</sub>A = B<sub>p1</sub> + t<sub>b</sub>B</p>
     * We can solve for t<sub>b</sub> by applying the cross product with A on both sides:
     * <p style="white-space: pre;"> (A<sub>p1</sub> + t<sub>a</sub>A) x A = (B<sub>p1</sub> + t<sub>b</sub>B) x A
     * A<sub>p1</sub> x A = B<sub>p1</sub> x A + t<sub>b</sub>B x A
     * (A<sub>p1</sub> - B<sub>p1</sub>) x A = t<sub>b</sub>B x A
     * t<sub>b</sub> = ((A<sub>p1</sub> - B<sub>p1</sub>) x A) / (B x A)</p>
     * If B x A == 0 then the segments are parallel.  If the top == 0 then they don't intersect.  If both the
     * top and bottom are zero then the segments are coincident.
     * <p>
     * If t<sub>b</sub> or t<sub>a</sub> less than zero or greater than 1 then the segments do not intersect.
     * <p>
     * If the segments do not intersect, are parallel, or are coincident, null is returned.
     *
     * @param ap1 the first point of the first line segment
     * @param ap2 the second point of the first line segment
     * @param bp1 the first point of the second line segment
     * @param bp2 the second point of the second line segment
     * @return Vector2 the intersection point; null if the line segments don't intersect, are parallel, or are coincident
     * @throws NullPointerException if ap1, ap2, bp1, or bp2 is null
     * @since 3.1.1
     */
    public static final Vector2 getSegmentIntersection(Vector2 ap1, Vector2 ap2, Vector2 bp1, Vector2 bp2) {
        return getSegmentIntersection(ap1, ap2, bp1, bp2, true);
    }

    /**
     * Returns the intersection point of the two line segments or null if they are parallel, coincident
     * or don't intersect.
     * <p>
     * In the scenario where two segments intersect at an end point, the behavior is determined by the inclusive
     * parameter.  When true, this method will return the intersection point - the end point.  When false, this
     * method will return null (indicating no intersection.
     *
     * @param ap1       the first point of the first line segment
     * @param ap2       the second point of the first line segment
     * @param bp1       the first point of the second line segment
     * @param bp2       the second point of the second line segment
     * @param inclusive see method documentation for more detail
     * @return Vector2 the intersection point; null if the line segments don't intersect, are parallel, or are coincident
     * @throws NullPointerException if ap1, ap2, bp1, or bp2 is null
     * @see #getSegmentIntersection(Vector2, Vector2, Vector2, Vector2)
     * @since 4.2.1
     */
    public static final Vector2 getSegmentIntersection(Vector2 ap1, Vector2 ap2, Vector2 bp1, Vector2 bp2, boolean inclusive) {
        Vector2 A = ap1.to(ap2);
        Vector2 B = bp1.to(bp2);

        // compute the bottom
        float BxA = B.cross(A);
        if (Math.abs(BxA) <= Epsilon.E) {
            // the line segments are parallel and don't intersect
            return null;
        }

        // compute the top
        float ambxA = ap1.sub(bp1).cross(A);
        if (Math.abs(ambxA) <= Epsilon.E) {
            // the line segments are coincident
            return null;
        }

        // compute tb
        float tb = ambxA / BxA;
        if (inclusive) {
            if (tb < 0.0 || tb > 1.0) return null;
        } else {
            if (tb <= 0.0 || tb >= 1.0) return null;
        }

        // compute the intersection point
        Vector2 ip = B.mul(tb).add(bp1);

        // since both are segments we need to verify that
        // ta is also valid.
        // compute ta
        float ta = ip.sub(ap1).dot(A) / A.dot(A);
        if (inclusive) {
            if (ta < 0.0 || ta > 1.0) return null;
        } else {
            if (ta <= 0.0 || ta >= 1.0) return null;
        }

        return ip;
    }
}