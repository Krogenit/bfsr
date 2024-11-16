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
package org.jbox2d.dynamics.contacts;

import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.ManifoldPoint;
import org.jbox2d.collision.WorldManifold;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Mat22;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Rotation;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.TimeStep;

/**
 * @author Daniel
 */
public class ContactSolver {
    public static final boolean DEBUG_SOLVER = false;
    public static final float k_errorTol = 1e-3f;
    /**
     * For each solver, this is the initial number of constraints in the array, which expands as
     * needed.
     */
    public static final int INITIAL_NUM_CONSTRAINTS = 256;

    /**
     * Ensure a reasonable condition number. for the block solver
     */
    public static final float k_maxConditionNumber = 100.0f;

    public TimeStep step;
    public Position[] positions;
    public Velocity[] velocities;
    public ContactPositionConstraint[] positionConstraints;
    public ContactVelocityConstraint[] velocityConstraints;
    public Contact[] contacts;
    public int count;

    public ContactSolver() {
        positionConstraints = new ContactPositionConstraint[INITIAL_NUM_CONSTRAINTS];
        velocityConstraints = new ContactVelocityConstraint[INITIAL_NUM_CONSTRAINTS];
        for (int i = 0; i < INITIAL_NUM_CONSTRAINTS; i++) {
            positionConstraints[i] = new ContactPositionConstraint();
            velocityConstraints[i] = new ContactVelocityConstraint();
        }
    }

    public final void init(ContactSolverDef def) {
        step = def.step;
        count = def.count;

        if (positionConstraints.length < count) {
            ContactPositionConstraint[] old = positionConstraints;
            positionConstraints = new ContactPositionConstraint[MathUtils.max(old.length << 1, count)];
            System.arraycopy(old, 0, positionConstraints, 0, old.length);
            for (int i = old.length; i < positionConstraints.length; i++) {
                positionConstraints[i] = new ContactPositionConstraint();
            }
        }

        if (velocityConstraints.length < count) {
            ContactVelocityConstraint[] old = velocityConstraints;
            velocityConstraints = new ContactVelocityConstraint[MathUtils.max(old.length << 1, count)];
            System.arraycopy(old, 0, velocityConstraints, 0, old.length);
            for (int i = old.length; i < velocityConstraints.length; i++) {
                velocityConstraints[i] = new ContactVelocityConstraint();
            }
        }

        positions = def.positions;
        velocities = def.velocities;
        contacts = def.contacts;

        for (int i = 0; i < count; ++i) {
            final Contact contact = contacts[i];

            final Fixture fixtureA = contact.fixtureA;
            final Fixture fixtureB = contact.fixtureB;
            final Shape shapeA = fixtureA.getShape();
            final Shape shapeB = fixtureB.getShape();
            final float radiusA = shapeA.radius;
            final float radiusB = shapeB.radius;
            final Body bodyA = fixtureA.getBody();
            final Body bodyB = fixtureB.getBody();
            final Manifold manifold = contact.getManifold();

            int pointCount = manifold.pointCount;
            assert (pointCount > 0);

            ContactVelocityConstraint vc = velocityConstraints[i];
            vc.friction = contact.friction;
            vc.restitution = contact.restitution;
            vc.tangentSpeed = contact.tangentSpeed;
            vc.indexA = bodyA.islandIndex;
            vc.indexB = bodyB.islandIndex;
            vc.invMassA = bodyA.invMass;
            vc.invMassB = bodyB.invMass;
            vc.invIA = bodyA.invI;
            vc.invIB = bodyB.invI;
            vc.contactIndex = i;
            vc.pointCount = pointCount;
            vc.K.setZero();
            vc.normalMass.setZero();

            ContactPositionConstraint pc = positionConstraints[i];
            pc.indexA = bodyA.islandIndex;
            pc.indexB = bodyB.islandIndex;
            pc.invMassA = bodyA.invMass;
            pc.invMassB = bodyB.invMass;
            pc.localCenterA.set(bodyA.sweep.localCenter);
            pc.localCenterB.set(bodyB.sweep.localCenter);
            pc.invIA = bodyA.invI;
            pc.invIB = bodyB.invI;
            pc.localNormal.set(manifold.localNormal);
            pc.localPoint.set(manifold.localPoint);
            pc.pointCount = pointCount;
            pc.radiusA = radiusA;
            pc.radiusB = radiusB;
            pc.type = manifold.type;

            for (int j = 0; j < pointCount; j++) {
                ManifoldPoint cp = manifold.points[j];
                ContactVelocityConstraint.VelocityConstraintPoint vcp = vc.points[j];

                if (step.warmStarting) {
                    vcp.normalImpulse = step.dtRatio * cp.normalImpulse;
                    vcp.tangentImpulse = step.dtRatio * cp.tangentImpulse;
                } else {
                    vcp.normalImpulse = 0;
                    vcp.tangentImpulse = 0;
                }

                vcp.rA.setZero();
                vcp.rB.setZero();
                vcp.normalMass = 0;
                vcp.tangentMass = 0;
                vcp.velocityBias = 0;
                pc.localPoints[j].x = cp.localPoint.x;
                pc.localPoints[j].y = cp.localPoint.y;
            }
        }
    }

    public void warmStart() {
        // Warm start.
        for (int i = 0; i < count; ++i) {
            final ContactVelocityConstraint vc = velocityConstraints[i];

            int indexA = vc.indexA;
            int indexB = vc.indexB;
            float mA = vc.invMassA;
            float iA = vc.invIA;
            float mB = vc.invMassB;
            float iB = vc.invIB;
            int pointCount = vc.pointCount;

            Vector2 vA = velocities[indexA].v;
            float wA = velocities[indexA].w;
            Vector2 vB = velocities[indexB].v;
            float wB = velocities[indexB].w;

            Vector2 normal = vc.normal;
            float tangentx = normal.y;
            float tangenty = -1.0f * normal.x;

            for (int j = 0; j < pointCount; ++j) {
                ContactVelocityConstraint.VelocityConstraintPoint vcp = vc.points[j];
                float Px = tangentx * vcp.tangentImpulse + normal.x * vcp.normalImpulse;
                float Py = tangenty * vcp.tangentImpulse + normal.y * vcp.normalImpulse;

                wA -= iA * (vcp.rA.x * Py - vcp.rA.y * Px);
                vA.x -= Px * mA;
                vA.y -= Py * mA;
                wB += iB * (vcp.rB.x * Py - vcp.rB.y * Px);
                vB.x += Px * mB;
                vB.y += Py * mB;
            }
            velocities[indexA].w = wA;
            velocities[indexB].w = wB;
        }
    }

    // djm pooling, and from above
    private final Transform xfA = new Transform();
    private final Transform xfB = new Transform();
    private final WorldManifold worldManifold = new WorldManifold();

    public final void initializeVelocityConstraints() {
        // Warm start.
        for (int i = 0; i < count; ++i) {
            ContactVelocityConstraint vc = velocityConstraints[i];
            ContactPositionConstraint pc = positionConstraints[i];

            float radiusA = pc.radiusA;
            float radiusB = pc.radiusB;
            Manifold manifold = contacts[vc.contactIndex].getManifold();

            int indexA = vc.indexA;
            int indexB = vc.indexB;

            float mA = vc.invMassA;
            float mB = vc.invMassB;
            float iA = vc.invIA;
            float iB = vc.invIB;
            Vector2 localCenterA = pc.localCenterA;
            Vector2 localCenterB = pc.localCenterB;

            Vector2 cA = positions[indexA].c;
            float aA = positions[indexA].a;
            Vector2 vA = velocities[indexA].v;
            float wA = velocities[indexA].w;

            Vector2 cB = positions[indexB].c;
            float aB = positions[indexB].a;
            Vector2 vB = velocities[indexB].v;
            float wB = velocities[indexB].w;

            assert (manifold.pointCount > 0);

            final Rotation xfAq = xfA.rotation;
            final Rotation xfBq = xfB.rotation;
            xfAq.set(aA);
            xfBq.set(aB);
            xfA.position.x = cA.x - (xfAq.cos * localCenterA.x - xfAq.sin * localCenterA.y);
            xfA.position.y = cA.y - (xfAq.sin * localCenterA.x + xfAq.cos * localCenterA.y);
            xfB.position.x = cB.x - (xfBq.cos * localCenterB.x - xfBq.sin * localCenterB.y);
            xfB.position.y = cB.y - (xfBq.sin * localCenterB.x + xfBq.cos * localCenterB.y);

            worldManifold.initialize(manifold, xfA, radiusA, xfB, radiusB);

            final Vector2 vcnormal = vc.normal;
            vcnormal.x = worldManifold.normal.x;
            vcnormal.y = worldManifold.normal.y;

            int pointCount = vc.pointCount;
            for (int j = 0; j < pointCount; ++j) {
                ContactVelocityConstraint.VelocityConstraintPoint vcp = vc.points[j];
                Vector2 wmPj = worldManifold.points[j];
                final Vector2 vcprA = vcp.rA;
                final Vector2 vcprB = vcp.rB;
                vcprA.x = wmPj.x - cA.x;
                vcprA.y = wmPj.y - cA.y;
                vcprB.x = wmPj.x - cB.x;
                vcprB.y = wmPj.y - cB.y;

                float rnA = vcprA.x * vcnormal.y - vcprA.y * vcnormal.x;
                float rnB = vcprB.x * vcnormal.y - vcprB.y * vcnormal.x;

                float kNormal = mA + mB + iA * rnA * rnA + iB * rnB * rnB;

                vcp.normalMass = kNormal > 0.0f ? 1.0f / kNormal : 0.0f;

                float tangentx = vcnormal.y;
                float tangenty = -1.0f * vcnormal.x;

                float rtA = vcprA.x * tangenty - vcprA.y * tangentx;
                float rtB = vcprB.x * tangenty - vcprB.y * tangentx;

                float kTangent = mA + mB + iA * rtA * rtA + iB * rtB * rtB;

                vcp.tangentMass = kTangent > 0.0f ? 1.0f / kTangent : 0.0f;

                // Setup a velocity bias for restitution.
                vcp.velocityBias = 0.0f;
                float tempx = vB.x + -wB * vcprB.y - vA.x - (-wA * vcprA.y);
                float tempy = vB.y + wB * vcprB.x - vA.y - (wA * vcprA.x);
                float vRel = vcnormal.x * tempx + vcnormal.y * tempy;
                if (vRel < -Settings.velocityThreshold) {
                    vcp.velocityBias = -vc.restitution * vRel;
                }
            }

            // If we have two points, then prepare the block solver.
            if (vc.pointCount == 2) {
                ContactVelocityConstraint.VelocityConstraintPoint vcp1 = vc.points[0];
                ContactVelocityConstraint.VelocityConstraintPoint vcp2 = vc.points[1];
                float rn1A = vcp1.rA.x * vcnormal.y - vcp1.rA.y * vcnormal.x;
                float rn1B = vcp1.rB.x * vcnormal.y - vcp1.rB.y * vcnormal.x;
                float rn2A = vcp2.rA.x * vcnormal.y - vcp2.rA.y * vcnormal.x;
                float rn2B = vcp2.rB.x * vcnormal.y - vcp2.rB.y * vcnormal.x;

                float k11 = mA + mB + iA * rn1A * rn1A + iB * rn1B * rn1B;
                float k22 = mA + mB + iA * rn2A * rn2A + iB * rn2B * rn2B;
                float k12 = mA + mB + iA * rn1A * rn2A + iB * rn1B * rn2B;
                if (k11 * k11 < k_maxConditionNumber * (k11 * k22 - k12 * k12)) {
                    // K is safe to invert.
                    vc.K.ex.x = k11;
                    vc.K.ex.y = k12;
                    vc.K.ey.x = k12;
                    vc.K.ey.y = k22;
                    vc.K.invertToOut(vc.normalMass);
                } else {
                    // The constraints are redundant, just use one.
                    // TODO_ERIN use deepest?
                    vc.pointCount = 1;
                }
            }
        }
    }

    public final void solveVelocityConstraints() {
        for (int i = 0; i < count; ++i) {
            final ContactVelocityConstraint vc = velocityConstraints[i];

            int indexA = vc.indexA;
            int indexB = vc.indexB;

            float mA = vc.invMassA;
            float mB = vc.invMassB;
            float iA = vc.invIA;
            float iB = vc.invIB;
            int pointCount = vc.pointCount;

            Vector2 vA = velocities[indexA].v;
            float wA = velocities[indexA].w;
            Vector2 vB = velocities[indexB].v;
            float wB = velocities[indexB].w;

            Vector2 normal = vc.normal;
            final float normalx = normal.x;
            final float normaly = normal.y;
            float tangentx = vc.normal.y;
            float tangenty = -1.0f * vc.normal.x;
            final float friction = vc.friction;

            assert (pointCount == 1 || pointCount == 2);

            // Solve tangent constraints
            for (int j = 0; j < pointCount; ++j) {
                final ContactVelocityConstraint.VelocityConstraintPoint vcp = vc.points[j];
                final Vector2 a = vcp.rA;
                float dvx = -wB * vcp.rB.y + vB.x - vA.x + wA * a.y;
                float dvy = wB * vcp.rB.x + vB.y - vA.y - wA * a.x;

                // Compute tangent force
                final float vt = dvx * tangentx + dvy * tangenty - vc.tangentSpeed;
                float lambda = vcp.tangentMass * (-vt);

                // Clamp the accumulated force
                final float maxFriction = friction * vcp.normalImpulse;
                final float newImpulse =
                        MathUtils.clamp(vcp.tangentImpulse + lambda, -maxFriction, maxFriction);
                lambda = newImpulse - vcp.tangentImpulse;
                vcp.tangentImpulse = newImpulse;

                // Apply contact impulse
                final float Px = tangentx * lambda;
                final float Py = tangenty * lambda;

                vA.x -= Px * mA;
                vA.y -= Py * mA;
                wA -= iA * (vcp.rA.x * Py - vcp.rA.y * Px);

                vB.x += Px * mB;
                vB.y += Py * mB;
                wB += iB * (vcp.rB.x * Py - vcp.rB.y * Px);
            }

            // Solve normal constraints
            if (vc.pointCount == 1) {
                final ContactVelocityConstraint.VelocityConstraintPoint vcp = vc.points[0];

                // Relative velocity at contact
                float dvx = -wB * vcp.rB.y + vB.x - vA.x + wA * vcp.rA.y;
                float dvy = wB * vcp.rB.x + vB.y - vA.y - wA * vcp.rA.x;

                // Compute normal impulse
                final float vn = dvx * normalx + dvy * normaly;
                float lambda = -vcp.normalMass * (vn - vcp.velocityBias);

                // Clamp the accumulated impulse
                float a = vcp.normalImpulse + lambda;
                final float newImpulse = (a > 0.0f ? a : 0.0f);
                lambda = newImpulse - vcp.normalImpulse;
                vcp.normalImpulse = newImpulse;

                // Apply contact impulse
                float Px = normalx * lambda;
                float Py = normaly * lambda;

                vA.x -= Px * mA;
                vA.y -= Py * mA;
                wA -= iA * (vcp.rA.x * Py - vcp.rA.y * Px);

                vB.x += Px * mB;
                vB.y += Py * mB;
                wB += iB * (vcp.rB.x * Py - vcp.rB.y * Px);
            } else {
                final ContactVelocityConstraint.VelocityConstraintPoint cp1 = vc.points[0];
                final ContactVelocityConstraint.VelocityConstraintPoint cp2 = vc.points[1];
                final Vector2 cp1rA = cp1.rA;
                final Vector2 cp1rB = cp1.rB;
                final Vector2 cp2rA = cp2.rA;
                final Vector2 cp2rB = cp2.rB;
                float ax = cp1.normalImpulse;
                float ay = cp2.normalImpulse;

                assert (ax >= 0.0f && ay >= 0.0f);
                // Relative velocity at contact
                float dv1x = -wB * cp1rB.y + vB.x - vA.x + wA * cp1rA.y;
                float dv1y = wB * cp1rB.x + vB.y - vA.y - wA * cp1rA.x;

                float dv2x = -wB * cp2rB.y + vB.x - vA.x + wA * cp2rA.y;
                float dv2y = wB * cp2rB.x + vB.y - vA.y - wA * cp2rA.x;

                // Compute normal velocity
                float vn1 = dv1x * normalx + dv1y * normaly;
                float vn2 = dv2x * normalx + dv2y * normaly;

                float bx = vn1 - cp1.velocityBias;
                float by = vn2 - cp2.velocityBias;

                // Compute b'
                Mat22 R = vc.K;
                bx -= R.ex.x * ax + R.ey.x * ay;
                by -= R.ex.y * ax + R.ey.y * ay;

                for (; ; ) {
                    Mat22 R1 = vc.normalMass;
                    float xx = R1.ex.x * bx + R1.ey.x * by;
                    float xy = R1.ex.y * bx + R1.ey.y * by;
                    xx *= -1;
                    xy *= -1;

                    if (xx >= 0.0f && xy >= 0.0f) {
                        // Get the incremental impulse
                        float dx = xx - ax;
                        float dy = xy - ay;

                        // Apply incremental impulse
                        float P1x = dx * normalx;
                        float P1y = dx * normaly;
                        float P2x = dy * normalx;
                        float P2y = dy * normaly;

                        vA.x -= mA * (P1x + P2x);
                        vA.y -= mA * (P1y + P2y);
                        vB.x += mB * (P1x + P2x);
                        vB.y += mB * (P1y + P2y);

                        wA -= iA * (cp1rA.x * P1y - cp1rA.y * P1x + (cp2rA.x * P2y - cp2rA.y * P2x));
                        wB += iB * (cp1rB.x * P1y - cp1rB.y * P1x + (cp2rB.x * P2y - cp2rB.y * P2x));

                        // Accumulate
                        cp1.normalImpulse = xx;
                        cp2.normalImpulse = xy;

                        if (DEBUG_SOLVER) {
                            // Postconditions
                            Vector2 dv1 = vB.add(Vector2.cross(wB, cp1rB).subLocal(vA).subLocal(Vector2.cross(wA, cp1rA)));
                            Vector2 dv2 = vB.add(Vector2.cross(wB, cp2rB).subLocal(vA).subLocal(Vector2.cross(wA, cp2rA)));
                            // Compute normal velocity
                            vn1 = Vector2.dot(dv1, normal);
                            vn2 = Vector2.dot(dv2, normal);

                            assert (MathUtils.abs(vn1 - cp1.velocityBias) < k_errorTol);
                            assert (MathUtils.abs(vn2 - cp2.velocityBias) < k_errorTol);
                        }
                        break;
                    }

                    xx = -cp1.normalMass * bx;
                    xy = 0.0f;
                    vn2 = vc.K.ex.y * xx + by;

                    if (xx >= 0.0f && vn2 >= 0.0f) {
                        // Get the incremental impulse
                        float dx = xx - ax;
                        float dy = xy - ay;

                        // Apply incremental impulse
                        float P1x = normalx * dx;
                        float P1y = normaly * dx;
                        float P2x = normalx * dy;
                        float P2y = normaly * dy;

                        vA.x -= mA * (P1x + P2x);
                        vA.y -= mA * (P1y + P2y);
                        vB.x += mB * (P1x + P2x);
                        vB.y += mB * (P1y + P2y);

                        wA -= iA * (cp1rA.x * P1y - cp1rA.y * P1x + (cp2rA.x * P2y - cp2rA.y * P2x));
                        wB += iB * (cp1rB.x * P1y - cp1rB.y * P1x + (cp2rB.x * P2y - cp2rB.y * P2x));

                        // Accumulate
                        cp1.normalImpulse = xx;
                        cp2.normalImpulse = xy;

                        if (DEBUG_SOLVER) {
                            // Postconditions
                            Vector2 dv1 = vB.add(Vector2.cross(wB, cp1rB).subLocal(vA).subLocal(Vector2.cross(wA, cp1rA)));
                            // Compute normal velocity
                            vn1 = Vector2.dot(dv1, normal);

                            assert (MathUtils.abs(vn1 - cp1.velocityBias) < k_errorTol);
                        }
                        break;
                    }

                    xx = 0.0f;
                    xy = -cp2.normalMass * by;
                    vn1 = vc.K.ey.x * xy + bx;

                    if (xy >= 0.0f && vn1 >= 0.0f) {
                        // Resubstitute for the incremental impulse
                        float dx = xx - ax;
                        float dy = xy - ay;

                        float P1x = normalx * dx;
                        float P1y = normaly * dx;
                        float P2x = normalx * dy;
                        float P2y = normaly * dy;

                        vA.x -= mA * (P1x + P2x);
                        vA.y -= mA * (P1y + P2y);
                        vB.x += mB * (P1x + P2x);
                        vB.y += mB * (P1y + P2y);

                        wA -= iA * (cp1rA.x * P1y - cp1rA.y * P1x + (cp2rA.x * P2y - cp2rA.y * P2x));
                        wB += iB * (cp1rB.x * P1y - cp1rB.y * P1x + (cp2rB.x * P2y - cp2rB.y * P2x));

                        // Accumulate
                        cp1.normalImpulse = xx;
                        cp2.normalImpulse = xy;

                        if (DEBUG_SOLVER) {
                            // Postconditions
                            Vector2 dv2 = vB.add(Vector2.cross(wB, cp2rB).subLocal(vA).subLocal(Vector2.cross(wA, cp2rA)));
                            // Compute normal velocity
                            vn2 = Vector2.dot(dv2, normal);

                            assert (MathUtils.abs(vn2 - cp2.velocityBias) < k_errorTol);
                        }
                        break;
                    }

                    xx = 0.0f;
                    xy = 0.0f;
                    vn1 = bx;
                    vn2 = by;

                    if (vn1 >= 0.0f && vn2 >= 0.0f) {
                        // Resubstitute for the incremental impulse
                        float dx = xx - ax;
                        float dy = xy - ay;

                        float P1x = normalx * dx;
                        float P1y = normaly * dx;
                        float P2x = normalx * dy;
                        float P2y = normaly * dy;

                        vA.x -= mA * (P1x + P2x);
                        vA.y -= mA * (P1y + P2y);
                        vB.x += mB * (P1x + P2x);
                        vB.y += mB * (P1y + P2y);

                        wA -= iA * (cp1rA.x * P1y - cp1rA.y * P1x + (cp2rA.x * P2y - cp2rA.y * P2x));
                        wB += iB * (cp1rB.x * P1y - cp1rB.y * P1x + (cp2rB.x * P2y - cp2rB.y * P2x));

                        // Accumulate
                        cp1.normalImpulse = xx;
                        cp2.normalImpulse = xy;

                        break;
                    }

                    // No solution, give up. This is hit sometimes, but it doesn't seem to matter.
                    break;
                }
            }

            velocities[indexA].w = wA;
            velocities[indexB].w = wB;
        }
    }

    public void storeImpulses() {
        for (int i = 0; i < count; i++) {
            final ContactVelocityConstraint vc = velocityConstraints[i];
            final Manifold manifold = contacts[vc.contactIndex].getManifold();

            for (int j = 0; j < vc.pointCount; j++) {
                manifold.points[j].normalImpulse = vc.points[j].normalImpulse;
                manifold.points[j].tangentImpulse = vc.points[j].tangentImpulse;
            }
        }
    }

    private final PositionSolverManifold psolver = new PositionSolverManifold();

    /**
     * Sequential solver.
     */
    public final boolean solvePositionConstraints() {
        float minSeparation = 0.0f;

        for (int i = 0; i < count; ++i) {
            ContactPositionConstraint pc = positionConstraints[i];

            int indexA = pc.indexA;
            int indexB = pc.indexB;

            float mA = pc.invMassA;
            float iA = pc.invIA;
            Vector2 localCenterA = pc.localCenterA;
            final float localCenterAx = localCenterA.x;
            final float localCenterAy = localCenterA.y;
            float mB = pc.invMassB;
            float iB = pc.invIB;
            Vector2 localCenterB = pc.localCenterB;
            final float localCenterBx = localCenterB.x;
            final float localCenterBy = localCenterB.y;
            int pointCount = pc.pointCount;

            Vector2 cA = positions[indexA].c;
            float aA = positions[indexA].a;
            Vector2 cB = positions[indexB].c;
            float aB = positions[indexB].a;

            // Solve normal constraints
            for (int j = 0; j < pointCount; ++j) {
                final Rotation xfAq = xfA.rotation;
                final Rotation xfBq = xfB.rotation;
                xfAq.set(aA);
                xfBq.set(aB);
                xfA.position.x = cA.x - xfAq.cos * localCenterAx + xfAq.sin * localCenterAy;
                xfA.position.y = cA.y - xfAq.sin * localCenterAx - xfAq.cos * localCenterAy;
                xfB.position.x = cB.x - xfBq.cos * localCenterBx + xfBq.sin * localCenterBy;
                xfB.position.y = cB.y - xfBq.sin * localCenterBx - xfBq.cos * localCenterBy;

                final PositionSolverManifold psm = psolver;
                psm.initialize(pc, xfA, xfB, j);
                final Vector2 normal = psm.normal;
                final Vector2 point = psm.point;
                final float separation = psm.separation;

                float rAx = point.x - cA.x;
                float rAy = point.y - cA.y;
                float rBx = point.x - cB.x;
                float rBy = point.y - cB.y;

                // Track max constraint error.
                minSeparation = MathUtils.min(minSeparation, separation);

                // Prevent large corrections and allow slop.
                final float C =
                        MathUtils.clamp(Settings.baumgarte * (separation + Settings.linearSlop),
                                -Settings.maxLinearCorrection, 0.0f);

                // Compute the effective mass.
                final float rnA = rAx * normal.y - rAy * normal.x;
                final float rnB = rBx * normal.y - rBy * normal.x;
                final float K = mA + mB + iA * rnA * rnA + iB * rnB * rnB;

                // Compute normal impulse
                final float impulse = K > 0.0f ? -C / K : 0.0f;

                float Px = normal.x * impulse;
                float Py = normal.y * impulse;

                cA.x -= Px * mA;
                cA.y -= Py * mA;
                aA -= iA * (rAx * Py - rAy * Px);

                cB.x += Px * mB;
                cB.y += Py * mB;
                aB += iB * (rBx * Py - rBy * Px);
            }

            positions[indexA].a = aA;
            positions[indexB].a = aB;
        }

        // We can't expect minSpeparation >= -linearSlop because we don't
        // push the separation above -linearSlop.
        return minSeparation >= -3.0f * Settings.linearSlop;
    }

    // Sequential position solver for position constraints.
    public boolean solveTOIPositionConstraints(int toiIndexA, int toiIndexB) {
        float minSeparation = 0.0f;

        for (int i = 0; i < count; ++i) {
            ContactPositionConstraint pc = positionConstraints[i];

            int indexA = pc.indexA;
            int indexB = pc.indexB;
            Vector2 localCenterA = pc.localCenterA;
            Vector2 localCenterB = pc.localCenterB;
            final float localCenterAx = localCenterA.x;
            final float localCenterAy = localCenterA.y;
            final float localCenterBx = localCenterB.x;
            final float localCenterBy = localCenterB.y;
            int pointCount = pc.pointCount;

            float mA = 0.0f;
            float iA = 0.0f;
            if (indexA == toiIndexA || indexA == toiIndexB) {
                mA = pc.invMassA;
                iA = pc.invIA;
            }

            float mB = 0f;
            float iB = 0f;
            if (indexB == toiIndexA || indexB == toiIndexB) {
                mB = pc.invMassB;
                iB = pc.invIB;
            }

            Vector2 cA = positions[indexA].c;
            float aA = positions[indexA].a;

            Vector2 cB = positions[indexB].c;
            float aB = positions[indexB].a;

            // Solve normal constraints
            for (int j = 0; j < pointCount; ++j) {
                final Rotation xfAq = xfA.rotation;
                final Rotation xfBq = xfB.rotation;
                xfAq.set(aA);
                xfBq.set(aB);
                xfA.position.x = cA.x - xfAq.cos * localCenterAx + xfAq.sin * localCenterAy;
                xfA.position.y = cA.y - xfAq.sin * localCenterAx - xfAq.cos * localCenterAy;
                xfB.position.x = cB.x - xfBq.cos * localCenterBx + xfBq.sin * localCenterBy;
                xfB.position.y = cB.y - xfBq.sin * localCenterBx - xfBq.cos * localCenterBy;

                final PositionSolverManifold psm = psolver;
                psm.initialize(pc, xfA, xfB, j);
                Vector2 normal = psm.normal;

                Vector2 point = psm.point;
                float separation = psm.separation;

                float rAx = point.x - cA.x;
                float rAy = point.y - cA.y;
                float rBx = point.x - cB.x;
                float rBy = point.y - cB.y;

                // Track max constraint error.
                minSeparation = MathUtils.min(minSeparation, separation);

                // Prevent large corrections and allow slop.
                float C =
                        MathUtils.clamp(Settings.toiBaugarte * (separation + Settings.linearSlop),
                                -Settings.maxLinearCorrection, 0.0f);

                // Compute the effective mass.
                float rnA = rAx * normal.y - rAy * normal.x;
                float rnB = rBx * normal.y - rBy * normal.x;
                float K = mA + mB + iA * rnA * rnA + iB * rnB * rnB;

                // Compute normal impulse
                float impulse = K > 0.0f ? -C / K : 0.0f;

                float Px = normal.x * impulse;
                float Py = normal.y * impulse;

                cA.x -= Px * mA;
                cA.y -= Py * mA;
                aA -= iA * (rAx * Py - rAy * Px);

                cB.x += Px * mB;
                cB.y += Py * mB;
                aB += iB * (rBx * Py - rBy * Px);
            }

            positions[indexA].a = aA;
            positions[indexB].a = aB;
        }

        // We can't expect minSpeparation >= -_linearSlop because we don't
        // push the separation above -_linearSlop.
        return minSeparation >= -1.5f * Settings.linearSlop;
    }

    public static class ContactSolverDef {
        public TimeStep step;
        public Contact[] contacts;
        public int count;
        public Position[] positions;
        public Velocity[] velocities;
    }
}

class PositionSolverManifold {

    public final Vector2 normal = new Vector2();
    public final Vector2 point = new Vector2();
    public float separation;

    public void initialize(ContactPositionConstraint pc, Transform xfA, Transform xfB, int index) {
        assert (pc.pointCount > 0);

        final Rotation xfAq = xfA.rotation;
        final Rotation xfBq = xfB.rotation;
        final Vector2 pcLocalPointsI = pc.localPoints[index];
        switch (pc.type) {
            case CIRCLES: {
                final Vector2 plocalPoint = pc.localPoint;
                final Vector2 pLocalPoints0 = pc.localPoints[0];
                final float pointAx = (xfAq.cos * plocalPoint.x - xfAq.sin * plocalPoint.y) + xfA.position.x;
                final float pointAy = (xfAq.sin * plocalPoint.x + xfAq.cos * plocalPoint.y) + xfA.position.y;
                final float pointBx = (xfBq.cos * pLocalPoints0.x - xfBq.sin * pLocalPoints0.y) + xfB.position.x;
                final float pointBy = (xfBq.sin * pLocalPoints0.x + xfBq.cos * pLocalPoints0.y) + xfB.position.y;
                normal.x = pointBx - pointAx;
                normal.y = pointBy - pointAy;
                normal.normalize();

                point.x = (pointAx + pointBx) * .5f;
                point.y = (pointAy + pointBy) * .5f;
                final float tempx = pointBx - pointAx;
                final float tempy = pointBy - pointAy;
                separation = tempx * normal.x + tempy * normal.y - pc.radiusA - pc.radiusB;
                break;
            }

            case FACE_A: {
                final Vector2 pcLocalNormal = pc.localNormal;
                final Vector2 pcLocalPoint = pc.localPoint;
                normal.x = xfAq.cos * pcLocalNormal.x - xfAq.sin * pcLocalNormal.y;
                normal.y = xfAq.sin * pcLocalNormal.x + xfAq.cos * pcLocalNormal.y;
                final float planePointx = (xfAq.cos * pcLocalPoint.x - xfAq.sin * pcLocalPoint.y) + xfA.position.x;
                final float planePointy = (xfAq.sin * pcLocalPoint.x + xfAq.cos * pcLocalPoint.y) + xfA.position.y;

                final float clipPointx = (xfBq.cos * pcLocalPointsI.x - xfBq.sin * pcLocalPointsI.y) + xfB.position.x;
                final float clipPointy = (xfBq.sin * pcLocalPointsI.x + xfBq.cos * pcLocalPointsI.y) + xfB.position.y;
                final float tempx = clipPointx - planePointx;
                final float tempy = clipPointy - planePointy;
                separation = tempx * normal.x + tempy * normal.y - pc.radiusA - pc.radiusB;
                point.x = clipPointx;
                point.y = clipPointy;
                break;
            }

            case FACE_B: {
                final Vector2 pcLocalNormal = pc.localNormal;
                final Vector2 pcLocalPoint = pc.localPoint;
                normal.x = xfBq.cos * pcLocalNormal.x - xfBq.sin * pcLocalNormal.y;
                normal.y = xfBq.sin * pcLocalNormal.x + xfBq.cos * pcLocalNormal.y;
                final float planePointx = (xfBq.cos * pcLocalPoint.x - xfBq.sin * pcLocalPoint.y) + xfB.position.x;
                final float planePointy = (xfBq.sin * pcLocalPoint.x + xfBq.cos * pcLocalPoint.y) + xfB.position.y;

                final float clipPointx = (xfAq.cos * pcLocalPointsI.x - xfAq.sin * pcLocalPointsI.y) + xfA.position.x;
                final float clipPointy = (xfAq.sin * pcLocalPointsI.x + xfAq.cos * pcLocalPointsI.y) + xfA.position.y;
                final float tempx = clipPointx - planePointx;
                final float tempy = clipPointy - planePointy;
                separation = tempx * normal.x + tempy * normal.y - pc.radiusA - pc.radiusB;
                point.x = clipPointx;
                point.y = clipPointy;
                normal.x *= -1;
                normal.y *= -1;
            }
            break;
        }
    }
}
