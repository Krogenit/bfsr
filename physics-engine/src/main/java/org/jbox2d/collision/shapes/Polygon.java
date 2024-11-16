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
package org.jbox2d.collision.shapes;

import lombok.Getter;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.RayCastInput;
import org.jbox2d.collision.RayCastOutput;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Rotation;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vector2;

/**
 * A convex polygon shape. Polygons have a maximum number of vertices equal to _maxPolygonVertices.
 * In most cases you should not need many vertices for a convex polygon.
 */
public class Polygon extends Shape {
    /**
     * Dump lots of debug information.
     */
    private final static boolean debug = false;

    /**
     * Local position of the shape centroid in parent body frame.
     */
    public final Vector2 centroid = new Vector2();

    /**
     * The vertices of the shape. Note: use getVertexCount(), not m_vertices.length, to get number of
     * active vertices.
     * -- GETTER --
     * Get the vertices in local coordinates.
     */
    @Getter
    public final Vector2[] vertices;

    /**
     * The normals of the shape. Note: use getVertexCount(), not m_normals.length, to get number of
     * active normals.
     * -- GETTER --
     * Get the edge normal vectors. There is one for each vertex.
     */
    @Getter
    public final Vector2[] normals;

    /**
     * Number of active vertices in the shape.
     */
    public int count;

    // pooling
    private final Vector2 pool1 = new Vector2();
    private final Vector2 pool2 = new Vector2();
    private final Vector2 pool3 = new Vector2();
    private final Vector2 pool4 = new Vector2();

    public Polygon(Vector2[] vertices) {
        super(ShapeType.POLYGON);
        this.count = vertices.length;

        this.vertices = new Vector2[vertices.length];
        this.normals = new Vector2[vertices.length];
        for (int i = 0; i < normals.length; i++) {
            normals[i] = new Vector2();
        }

        setRadius(Settings.polygonRadius);

        assert (3 <= vertices.length);

        // Copy vertices.
        for (int i = 0; i < count; ++i) {
            this.vertices[i] = new Vector2(vertices[i]);
        }

        final Vector2 edge = pool1;

        // Compute normals. Ensure the edges have non-zero length.
        for (int i = 0; i < count; ++i) {
            final int i2 = i + 1 < count ? i + 1 : 0;
            edge.set(this.vertices[i2]).subLocal(this.vertices[i]);

            assert (edge.lengthSquared() > Settings.EPSILON * Settings.EPSILON);
            Vector2.crossToOutUnsafe(edge, 1f, normals[i]);
            normals[i].normalize();
        }

        // Compute the polygon centroid.
        computeCentroidToOut(this.vertices, count, centroid);
    }

    public Polygon(Vector2[] vertices, int count, Vector2 centroid, Vector2[] normals, float radius) {
        super(ShapeType.POLYGON);
        this.count = count;
        this.centroid.set(centroid);
        this.vertices = new Vector2[count];
        this.normals = new Vector2[count];

        for (int i = 0; i < count; i++) {
            this.normals[i] = new Vector2(normals[i]);
            this.vertices[i] = new Vector2(vertices[i]);
        }

        setRadius(radius);
    }

    public void translate(float x, float y) {
        for (int i = 0; i < vertices.length; i++) {
            vertices[i].addLocal(x, y);
        }
    }

    @Override
    public final Polygon clone() {
        return new Polygon(vertices, count, centroid, normals, getRadius());
    }

    public int getChildCount() {
        return 1;
    }

    @Override
    public final boolean testPoint(final Transform xf, final Vector2 p) {
        float tempx, tempy;
        final Rotation xfq = xf.rotation;

        tempx = p.x - xf.position.x;
        tempy = p.y - xf.position.y;
        final float pLocalx = xfq.cos * tempx + xfq.sin * tempy;
        final float pLocaly = -xfq.sin * tempx + xfq.cos * tempy;

        if (debug) {
            System.out.println("--testPoint debug--");
            System.out.println("Vertices: ");
            for (int i = 0; i < count; ++i) {
                System.out.println(vertices[i]);
            }
            System.out.println("pLocal: " + pLocalx + ", " + pLocaly);
        }

        for (int i = 0; i < count; ++i) {
            Vector2 vertex = vertices[i];
            Vector2 normal = normals[i];
            tempx = pLocalx - vertex.x;
            tempy = pLocaly - vertex.y;
            final float dot = normal.x * tempx + normal.y * tempy;
            if (dot > 0.0f) {
                return false;
            }
        }

        return true;
    }

    @Override
    public final void computeAABB(final AABB aabb, final Transform transform, int childIndex) {
        final Vector2 lower = aabb.lowerBound;
        final Vector2 upper = aabb.upperBound;
        final Vector2 v1 = vertices[0];
        final float xfqc = transform.rotation.cos;
        final float xfqs = transform.rotation.sin;
        final float xfpx = transform.position.x;
        final float xfpy = transform.position.y;
        lower.x = (xfqc * v1.x - xfqs * v1.y) + xfpx;
        lower.y = (xfqs * v1.x + xfqc * v1.y) + xfpy;
        upper.x = lower.x;
        upper.y = lower.y;

        for (int i = 1; i < count; ++i) {
            Vector2 v2 = vertices[i];
            // Vec2 v = Mul(xf, m_vertices[i]);
            float vx = (xfqc * v2.x - xfqs * v2.y) + xfpx;
            float vy = (xfqs * v2.x + xfqc * v2.y) + xfpy;
            lower.x = lower.x < vx ? lower.x : vx;
            lower.y = lower.y < vy ? lower.y : vy;
            upper.x = upper.x > vx ? upper.x : vx;
            upper.y = upper.y > vy ? upper.y : vy;
        }

        lower.x -= radius;
        lower.y -= radius;
        upper.x += radius;
        upper.y += radius;
    }

    /**
     * Get the vertex count.
     */
    public final int getVertexCount() {
        return count;
    }

    /**
     * Get a vertex by index.
     */
    public final Vector2 getVertex(final int index) {
        assert (0 <= index && index < count);
        return vertices[index];
    }

    @Override
    public float computeDistanceToOut(Transform xf, Vector2 p, int childIndex, Vector2 normalOut) {
        float xfqc = xf.rotation.cos;
        float xfqs = xf.rotation.sin;
        float tx = p.x - xf.position.x;
        float ty = p.y - xf.position.y;
        float pLocalx = xfqc * tx + xfqs * ty;
        float pLocaly = -xfqs * tx + xfqc * ty;

        float maxDistance = -Float.MAX_VALUE;
        float normalForMaxDistanceX = pLocalx;
        float normalForMaxDistanceY = pLocaly;

        for (int i = 0; i < count; ++i) {
            Vector2 vertex = vertices[i];
            Vector2 normal = normals[i];
            tx = pLocalx - vertex.x;
            ty = pLocaly - vertex.y;
            float dot = normal.x * tx + normal.y * ty;
            if (dot > maxDistance) {
                maxDistance = dot;
                normalForMaxDistanceX = normal.x;
                normalForMaxDistanceY = normal.y;
            }
        }

        float distance;
        if (maxDistance > 0) {
            float minDistanceX = normalForMaxDistanceX;
            float minDistanceY = normalForMaxDistanceY;
            float minDistance2 = maxDistance * maxDistance;
            for (int i = 0; i < count; ++i) {
                Vector2 vertex = vertices[i];
                float distanceVecX = pLocalx - vertex.x;
                float distanceVecY = pLocaly - vertex.y;
                float distance2 = (distanceVecX * distanceVecX + distanceVecY * distanceVecY);
                if (minDistance2 > distance2) {
                    minDistanceX = distanceVecX;
                    minDistanceY = distanceVecY;
                    minDistance2 = distance2;
                }
            }
            distance = MathUtils.sqrt(minDistance2);
            normalOut.x = xfqc * minDistanceX - xfqs * minDistanceY;
            normalOut.y = xfqs * minDistanceX + xfqc * minDistanceY;
            normalOut.normalize();
        } else {
            distance = maxDistance;
            normalOut.x = xfqc * normalForMaxDistanceX - xfqs * normalForMaxDistanceY;
            normalOut.y = xfqs * normalForMaxDistanceX + xfqc * normalForMaxDistanceY;
        }

        return distance;
    }

    @Override
    public final boolean raycast(RayCastOutput output, RayCastInput input, Transform xf,
                                 int childIndex) {
        final float xfqc = xf.rotation.cos;
        final float xfqs = xf.rotation.sin;
        final Vector2 xfp = xf.position;
        float tempx, tempy;
        // b2Vec2 p1 = b2MulT(xf.q, input.p1 - xf.p);
        // b2Vec2 p2 = b2MulT(xf.q, input.p2 - xf.p);
        tempx = input.p1.x - xfp.x;
        tempy = input.p1.y - xfp.y;
        final float p1x = xfqc * tempx + xfqs * tempy;
        final float p1y = -xfqs * tempx + xfqc * tempy;

        tempx = input.p2.x - xfp.x;
        tempy = input.p2.y - xfp.y;
        final float p2x = xfqc * tempx + xfqs * tempy;
        final float p2y = -xfqs * tempx + xfqc * tempy;

        final float dx = p2x - p1x;
        final float dy = p2y - p1y;

        float lower = 0, upper = input.maxFraction;

        int index = -1;

        for (int i = 0; i < count; ++i) {
            Vector2 normal = normals[i];
            Vector2 vertex = vertices[i];
            // p = p1 + a * d
            // dot(normal, p - v) = 0
            // dot(normal, p1 - v) + a * dot(normal, d) = 0
            float tempxn = vertex.x - p1x;
            float tempyn = vertex.y - p1y;
            final float numerator = normal.x * tempxn + normal.y * tempyn;
            final float denominator = normal.x * dx + normal.y * dy;

            if (denominator == 0.0f) {
                if (numerator < 0.0f) {
                    return false;
                }
            } else {
                // Note: we want this predicate without division:
                // lower < numerator / denominator, where denominator < 0
                // Since denominator < 0, we have to flip the inequality:
                // lower < numerator / denominator <==> denominator * lower >
                // numerator.
                if (denominator < 0.0f && numerator < lower * denominator) {
                    // Increase lower.
                    // The segment enters this half-space.
                    lower = numerator / denominator;
                    index = i;
                } else if (denominator > 0.0f && numerator < upper * denominator) {
                    // Decrease upper.
                    // The segment exits this half-space.
                    upper = numerator / denominator;
                }
            }

            if (upper < lower) {
                return false;
            }
        }

        assert (0.0f <= lower && lower <= input.maxFraction);

        if (index >= 0) {
            output.fraction = lower;
            // normal = Mul(xf.R, m_normals[index]);
            Vector2 normal = normals[index];
            Vector2 out = output.normal;
            out.x = xfqc * normal.x - xfqs * normal.y;
            out.y = xfqs * normal.x + xfqc * normal.y;
            return true;
        }
        return false;
    }

    public final void computeCentroidToOut(final Vector2[] vs, final int count, final Vector2 out) {
        assert (count >= 3);

        out.set(0.0f, 0.0f);
        float area = 0.0f;

        // pRef is the reference point for forming triangles.
        // It's location doesn't change the result (except for rounding error).
        final Vector2 pRef = pool1;
        pRef.setZero();

        final Vector2 e1 = pool2;
        final Vector2 e2 = pool3;

        final float inv3 = 1.0f / 3.0f;

        for (int i = 0; i < count; ++i) {
            // Triangle vertices.
            final Vector2 p2 = vs[i];
            final Vector2 p3 = i + 1 < count ? vs[i + 1] : vs[0];

            e1.set(p2).subLocal(pRef);
            e2.set(p3).subLocal(pRef);

            final float D = Vector2.cross(e1, e2);

            final float triangleArea = 0.5f * D;
            area += triangleArea;

            // Area weighted centroid
            e1.set(pRef).addLocal(p2).addLocal(p3).mulLocal(triangleArea * inv3);
            out.addLocal(e1);
        }

        // Centroid
        assert (area > Settings.EPSILON);
        out.mulLocal(1.0f / area);
    }

    @Override
    public void computeMass(final MassData massData, float density) {
        // Polygon mass, centroid, and inertia.
        // Let rho be the polygon density in mass per unit area.
        // Then:
        // mass = rho * int(dA)
        // centroid.x = (1/mass) * rho * int(x * dA)
        // centroid.y = (1/mass) * rho * int(y * dA)
        // I = rho * int((x*x + y*y) * dA)
        //
        // We can compute these integrals by summing all the integrals
        // for each triangle of the polygon. To evaluate the integral
        // for a single triangle, we make a change of variables to
        // the (u,v) coordinates of the triangle:
        // x = x0 + e1x * u + e2x * v
        // y = y0 + e1y * u + e2y * v
        // where 0 <= u && 0 <= v && u + v <= 1.
        //
        // We integrate u from [0,1-v] and then v from [0,1].
        // We also need to use the Jacobian of the transformation:
        // D = cross(e1, e2)
        //
        // Simplification: triangle centroid = (1/3) * (p1 + p2 + p3)
        //
        // The rest of the derivation is handled by computer algebra.

        assert (count >= 3);

        final Vector2 center = pool1;
        center.setZero();
        float area = 0.0f;
        float I = 0.0f;

        // pRef is the reference point for forming triangles.
        // It's location doesn't change the result (except for rounding error).
        final Vector2 s = pool2;
        s.setZero();
        // This code would put the reference point inside the polygon.
        for (int i = 0; i < count; ++i) {
            s.addLocal(vertices[i]);
        }
        s.mulLocal(1.0f / count);

        final float k_inv3 = 1.0f / 3.0f;

        final Vector2 e1 = pool3;
        final Vector2 e2 = pool4;

        for (int i = 0; i < count; ++i) {
            // Triangle vertices.
            e1.set(vertices[i]).subLocal(s);
            e2.set(s).negateLocal().addLocal(i + 1 < count ? vertices[i + 1] : vertices[0]);

            final float D = Vector2.cross(e1, e2);

            final float triangleArea = 0.5f * D;
            area += triangleArea;

            // Area weighted centroid
            center.x += triangleArea * k_inv3 * (e1.x + e2.x);
            center.y += triangleArea * k_inv3 * (e1.y + e2.y);

            final float ex1 = e1.x, ey1 = e1.y;
            final float ex2 = e2.x, ey2 = e2.y;

            float intx2 = ex1 * ex1 + ex2 * ex1 + ex2 * ex2;
            float inty2 = ey1 * ey1 + ey2 * ey1 + ey2 * ey2;

            I += (0.25f * k_inv3 * D) * (intx2 + inty2);
        }

        // Total mass
        massData.mass = density * area;

        assert (area > Settings.EPSILON);
        center.mulLocal(1.0f / area);
        massData.center.set(center).addLocal(s);

        // Inertia tensor relative to the local origin (point s)
        massData.I = I * density;

        // Shift to center of mass then to original body origin.
        massData.I += massData.mass * (Vector2.dot(massData.center, massData.center));
    }

    /**
     * Get the centroid and apply the supplied transform.
     */
    public Vector2 centroid(final Transform xf) {
        return Transform.mul(xf, centroid);
    }

    /**
     * Get the centroid and apply the supplied transform.
     */
    public Vector2 centroidToOut(final Transform xf, final Vector2 out) {
        Transform.mulToOutUnsafe(xf, centroid, out);
        return out;
    }
}
