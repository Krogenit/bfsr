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
package org.jbox2d.collision;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Rotation;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vector2;

/**
 * This is used to compute the current state of a contact manifold.
 *
 * @author daniel
 */
public class WorldManifold {
    /**
     * World vector pointing from A to B
     */
    public final Vector2 normal;

    /**
     * World contact point (point of intersection)
     */
    public final Vector2[] points;

    /**
     * A negative value indicates overlap, in meters.
     */
    public final float[] separations;

    public WorldManifold() {
        normal = new Vector2();
        points = new Vector2[Settings.maxManifoldPoints];
        separations = new float[Settings.maxManifoldPoints];
        for (int i = 0; i < Settings.maxManifoldPoints; i++) {
            points[i] = new Vector2();
        }
    }

    private final Vector2 pool3 = new Vector2();
    private final Vector2 pool4 = new Vector2();

    public final void initialize(final Manifold manifold, final Transform xfA, float radiusA,
                                 final Transform xfB, float radiusB) {
        if (manifold.pointCount == 0) {
            return;
        }

        switch (manifold.type) {
            case CIRCLES: {
                final Vector2 pointA = pool3;
                final Vector2 pointB = pool4;

                normal.x = 1;
                normal.y = 0;
                Vector2 v = manifold.localPoint;
                // Transform.mulToOutUnsafe(xfA, manifold.localPoint, pointA);
                // Transform.mulToOutUnsafe(xfB, manifold.points[0].localPoint, pointB);
                pointA.x = (xfA.rotation.cos * v.x - xfA.rotation.sin * v.y) + xfA.position.x;
                pointA.y = (xfA.rotation.sin * v.x + xfA.rotation.cos * v.y) + xfA.position.y;
                Vector2 mp0p = manifold.points[0].localPoint;
                pointB.x = (xfB.rotation.cos * mp0p.x - xfB.rotation.sin * mp0p.y) + xfB.position.x;
                pointB.y = (xfB.rotation.sin * mp0p.x + xfB.rotation.cos * mp0p.y) + xfB.position.y;

                if (MathUtils.distanceSquared(pointA, pointB) > Settings.EPSILON * Settings.EPSILON) {
                    normal.x = pointB.x - pointA.x;
                    normal.y = pointB.y - pointA.y;
                    normal.normalize();
                }

                final float cAx = normal.x * radiusA + pointA.x;
                final float cAy = normal.y * radiusA + pointA.y;

                final float cBx = -normal.x * radiusB + pointB.x;
                final float cBy = -normal.y * radiusB + pointB.y;

                points[0].x = (cAx + cBx) * .5f;
                points[0].y = (cAy + cBy) * .5f;
                separations[0] = (cBx - cAx) * normal.x + (cBy - cAy) * normal.y;
            }
            break;
            case FACE_A: {
                final Vector2 planePoint = pool3;

                Rotation.mulToOutUnsafe(xfA.rotation, manifold.localNormal, normal);
                Transform.mulToOut(xfA, manifold.localPoint, planePoint);

                final Vector2 clipPoint = pool4;

                for (int i = 0; i < manifold.pointCount; i++) {
                    // b2Vec2 clipPoint = b2Mul(xfB, manifold->points[i].localPoint);
                    // b2Vec2 cA = clipPoint + (radiusA - b2Dot(clipPoint - planePoint,
                    // normal)) * normal;
                    // b2Vec2 cB = clipPoint - radiusB * normal;
                    // points[i] = 0.5f * (cA + cB);
                    Transform.mulToOut(xfB, manifold.points[i].localPoint, clipPoint);
                    // use cA as temporary for now
                    // cA.set(clipPoint).subLocal(planePoint);
                    // float scalar = radiusA - Vec2.dot(cA, normal);
                    // cA.set(normal).mulLocal(scalar).addLocal(clipPoint);
                    // cB.set(normal).mulLocal(radiusB).subLocal(clipPoint).negateLocal();
                    // points[i].set(cA).addLocal(cB).mulLocal(0.5f);

                    final float scalar =
                            radiusA
                                    - ((clipPoint.x - planePoint.x) * normal.x + (clipPoint.y - planePoint.y)
                                    * normal.y);

                    final float cAx = normal.x * scalar + clipPoint.x;
                    final float cAy = normal.y * scalar + clipPoint.y;

                    final float cBx = -normal.x * radiusB + clipPoint.x;
                    final float cBy = -normal.y * radiusB + clipPoint.y;

                    points[i].x = (cAx + cBx) * .5f;
                    points[i].y = (cAy + cBy) * .5f;
                    separations[i] = (cBx - cAx) * normal.x + (cBy - cAy) * normal.y;
                }
            }
            break;
            case FACE_B:
                final Vector2 planePoint = pool3;
                Rotation.mulToOutUnsafe(xfB.rotation, manifold.localNormal, normal);
                Transform.mulToOut(xfB, manifold.localPoint, planePoint);

                // final Mat22 R = xfB.q;
                // normal.x = R.ex.x * manifold.localNormal.x + R.ey.x * manifold.localNormal.y;
                // normal.y = R.ex.y * manifold.localNormal.x + R.ey.y * manifold.localNormal.y;
                // final Vec2 v = manifold.localPoint;
                // planePoint.x = xfB.p.x + xfB.q.ex.x * v.x + xfB.q.ey.x * v.y;
                // planePoint.y = xfB.p.y + xfB.q.ex.y * v.x + xfB.q.ey.y * v.y;

                final Vector2 clipPoint = pool4;

                for (int i = 0; i < manifold.pointCount; i++) {
                    // b2Vec2 clipPoint = b2Mul(xfA, manifold->points[i].localPoint);
                    // b2Vec2 cB = clipPoint + (radiusB - b2Dot(clipPoint - planePoint,
                    // normal)) * normal;
                    // b2Vec2 cA = clipPoint - radiusA * normal;
                    // points[i] = 0.5f * (cA + cB);

                    Transform.mulToOut(xfA, manifold.points[i].localPoint, clipPoint);
                    // cB.set(clipPoint).subLocal(planePoint);
                    // float scalar = radiusB - Vec2.dot(cB, normal);
                    // cB.set(normal).mulLocal(scalar).addLocal(clipPoint);
                    // cA.set(normal).mulLocal(radiusA).subLocal(clipPoint).negateLocal();
                    // points[i].set(cA).addLocal(cB).mulLocal(0.5f);

                    // points[i] = 0.5f * (cA + cB);

                    //
                    // clipPoint.x = xfA.p.x + xfA.q.ex.x * manifold.points[i].localPoint.x + xfA.q.ey.x *
                    // manifold.points[i].localPoint.y;
                    // clipPoint.y = xfA.p.y + xfA.q.ex.y * manifold.points[i].localPoint.x + xfA.q.ey.y *
                    // manifold.points[i].localPoint.y;

                    final float scalar =
                            radiusB
                                    - ((clipPoint.x - planePoint.x) * normal.x + (clipPoint.y - planePoint.y)
                                    * normal.y);

                    final float cBx = normal.x * scalar + clipPoint.x;
                    final float cBy = normal.y * scalar + clipPoint.y;

                    final float cAx = -normal.x * radiusA + clipPoint.x;
                    final float cAy = -normal.y * radiusA + clipPoint.y;

                    points[i].x = (cAx + cBx) * .5f;
                    points[i].y = (cAy + cBy) * .5f;
                    separations[i] = (cAx - cBx) * normal.x + (cAy - cBy) * normal.y;
                }
                // Ensure normal points from A to B.
                normal.x = -normal.x;
                normal.y = -normal.y;
                break;
        }
    }
}
