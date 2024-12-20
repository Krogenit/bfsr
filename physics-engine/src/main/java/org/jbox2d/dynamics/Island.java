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
package org.jbox2d.dynamics;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Sweep;
import org.jbox2d.common.Timer;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.contacts.ContactSolver;
import org.jbox2d.dynamics.contacts.ContactVelocityConstraint;
import org.jbox2d.dynamics.contacts.Position;
import org.jbox2d.dynamics.contacts.Velocity;
import org.jbox2d.dynamics.joints.Joint;

/*
 Position Correction Notes
 =========================
 I tried the several algorithms for position correction of the 2D revolute joint.
 I looked at these systems:
 - simple pendulum (1m diameter sphere on massless 5m stick) with initial angular velocity of 100 rad/s.
 - suspension bridge with 30 1m long planks of length 1m.
 - multi-link chain with 30 1m long links.

 Here are the algorithms:

 Baumgarte - A fraction of the position error is added to the velocity error. There is no
 separate position solver.

 Pseudo Velocities - After the velocity solver and position integration,
 the position error, Jacobian, and effective mass are recomputed. Then
 the velocity constraints are solved with pseudo velocities and a fraction
 of the position error is added to the pseudo velocity error. The pseudo
 velocities are initialized to zero and there is no warm-starting. After
 the position solver, the pseudo velocities are added to the positions.
 This is also called the First Order World method or the Position LCP method.

 Modified Nonlinear Gauss-Seidel (NGS) - Like Pseudo Velocities except the
 position error is re-computed for each raint and the positions are updated
 after the raint is solved. The radius vectors (aka Jacobians) are
 re-computed too (otherwise the algorithm has horrible instability). The pseudo
 velocity states are not needed because they are effectively zero at the beginning
 of each iteration. Since we have the current position error, we allow the
 iterations to terminate early if the error becomes smaller than Settings.linearSlop.

 Full NGS or just NGS - Like Modified NGS except the effective mass are re-computed
 each time a raint is solved.

 Here are the results:
 Baumgarte - this is the cheapest algorithm but it has some stability problems,
 especially with the bridge. The chain links separate easily close to the root
 and they jitter as they struggle to pull together. This is one of the most common
 methods in the field. The big drawback is that the position correction artificially
 affects the momentum, thus leading to instabilities and false bounce. I used a
 bias factor of 0.2. A larger bias factor makes the bridge less stable, a smaller
 factor makes joints and contacts more spongy.

 Pseudo Velocities - the is more stable than the Baumgarte method. The bridge is
 stable. However, joints still separate with large angular velocities. Drag the
 simple pendulum in a circle quickly and the joint will separate. The chain separates
 easily and does not recover. I used a bias factor of 0.2. A larger value lead to
 the bridge collapsing when a heavy cube drops on it.

 Modified NGS - this algorithm is better in some ways than Baumgarte and Pseudo
 Velocities, but in other ways it is worse. The bridge and chain are much more
 stable, but the simple pendulum goes unstable at high angular velocities.

 Full NGS - stable in all tests. The joints display good stiffness. The bridge
 still sags, but this is better than infinite forces.

 Recommendations
 Pseudo Velocities are not really worthwhile because the bridge and chain cannot
 recover from joint separation. In other cases the benefit over Baumgarte is small.

 Modified NGS is not a robust method for the revolute joint due to the violent
 instability seen in the simple pendulum. Perhaps it is viable with other raint
 types, especially scalar constraints where the effective mass is a scalar.

 This leaves Baumgarte and Full NGS. Baumgarte has small, but manageable instabilities
 and is very fast. I don't think we can escape Baumgarte, especially in highly
 demanding cases where high raint fidelity is not needed.

 Full NGS is robust and easy on the eyes. I recommend this as an option for
 higher fidelity simulation and certainly for suspension bridges and long chains.
 Full NGS might be a good choice for ragdolls, especially motorized ragdolls where
 joint separation can be problematic. The number of NGS iterations can be reduced
 for better performance without harming robustness much.

 Each joint in a can be handled differently in the position solver. So I recommend
 a system where the user can select the algorithm on a per joint basis. I would
 probably default to the slower Full NGS and let the user select the faster
 Baumgarte method in performance critical scenarios.
 */

/*
 Cache Performance

 The Box2D solvers are dominated by cache misses. Data structures are designed
 to increase the number of cache hits. Much of misses are due to random access
 to body data. The raint structures are iterated over linearly, which leads
 to few cache misses.

 The bodies are not accessed during iteration. Instead read only data, such as
 the mass values are stored with the constraints. The mutable data are the raint
 impulses and the bodies velocities/positions. The impulses are held inside the
 raint structures. The body velocities/positions are held in compact, temporary
 arrays to increase the number of cache hits. Linear and angular velocity are
 stored in a single array since multiple arrays lead to multiple misses.
 */

/*
 2D Rotation

 R = [cos(theta) -sin(theta)]
 [sin(theta) cos(theta) ]

 thetaDot = omega

 Let q1 = cos(theta), q2 = sin(theta).
 R = [q1 -q2]
 [q2  q1]

 q1Dot = -thetaDot * q2
 q2Dot = thetaDot * q1

 q1_new = q1_old - dt * w * q2
 q2_new = q2_old + dt * w * q1
 then normalize.

 This might be faster than computing sin+cos.
 However, we can compute sin+cos of the same angle fast.
 */

/**
 * This is an internal class.
 *
 * @author Daniel Murphy
 */
public class Island {
    public ContactListener m_listener;

    public Body[] m_bodies;
    public Contact[] m_contacts;
    public Joint[] m_joints;

    public Position[] m_positions;
    public Velocity[] m_velocities;

    public int m_bodyCount;
    public int m_jointCount;
    public int m_contactCount;

    public int m_bodyCapacity;
    public int m_contactCapacity;
    public int m_jointCapacity;

    public void init(int bodyCapacity, int contactCapacity, int jointCapacity,
                     ContactListener listener) {
        // System.out.println("Initializing Island");
        m_bodyCapacity = bodyCapacity;
        m_contactCapacity = contactCapacity;
        m_jointCapacity = jointCapacity;
        m_bodyCount = 0;
        m_contactCount = 0;
        m_jointCount = 0;

        m_listener = listener;

        if (m_bodies == null || m_bodyCapacity > m_bodies.length) {
            m_bodies = new Body[m_bodyCapacity];
        }
        if (m_joints == null || m_jointCapacity > m_joints.length) {
            m_joints = new Joint[m_jointCapacity];
        }
        if (m_contacts == null || m_contactCapacity > m_contacts.length) {
            m_contacts = new Contact[m_contactCapacity];
        }

        // dynamic array
        if (m_velocities == null || m_bodyCapacity > m_velocities.length) {
            final Velocity[] old = m_velocities == null ? new Velocity[0] : m_velocities;
            m_velocities = new Velocity[m_bodyCapacity];
            System.arraycopy(old, 0, m_velocities, 0, old.length);
            for (int i = old.length; i < m_velocities.length; i++) {
                m_velocities[i] = new Velocity();
            }
        }

        // dynamic array
        if (m_positions == null || m_bodyCapacity > m_positions.length) {
            final Position[] old = m_positions == null ? new Position[0] : m_positions;
            m_positions = new Position[m_bodyCapacity];
            System.arraycopy(old, 0, m_positions, 0, old.length);
            for (int i = old.length; i < m_positions.length; i++) {
                m_positions[i] = new Position();
            }
        }
    }

    public void clear() {
        m_bodyCount = 0;
        m_contactCount = 0;
        m_jointCount = 0;
    }

    private final ContactSolver contactSolver = new ContactSolver();
    private final Timer timer = new Timer();
    private final SolverData solverData = new SolverData();
    private final ContactSolver.ContactSolverDef solverDef = new ContactSolver.ContactSolverDef();

    public void solve(Profile profile, TimeStep step, Vector2 gravity, boolean allowSleep) {

        // System.out.println("Solving Island");
        float h = step.dt;

        // Integrate velocities and apply damping. Initialize the body state.
        for (int i = 0; i < m_bodyCount; ++i) {
            final Body b = m_bodies[i];
            final Sweep bm_sweep = b.sweep;
            final Vector2 c = bm_sweep.center;
            float a = bm_sweep.angle;
            final Vector2 v = b.linearVelocity;
            float w = b.angularVelocity;

            // Store positions for continuous collision.
            bm_sweep.center0.set(bm_sweep.center);
            bm_sweep.angle0 = bm_sweep.angle;

            if (b.type == BodyType.DYNAMIC) {
                // Integrate velocities.
                // v += h * (b.m_gravityScale * gravity + b.m_invMass * b.m_force);
                v.x += h * (b.gravityScale * gravity.x + b.invMass * b.force.x);
                v.y += h * (b.gravityScale * gravity.y + b.invMass * b.force.y);
                w += h * b.invI * b.torque;

                // Apply damping.
                // ODE: dv/dt + c * v = 0
                // Solution: v(t) = v0 * exp(-c * t)
                // Time step: v(t + dt) = v0 * exp(-c * (t + dt)) = v0 * exp(-c * t) * exp(-c * dt) = v *
                // exp(-c * dt)
                // v2 = exp(-c * dt) * v1
                // Pade approximation:
                // v2 = v1 * 1 / (1 + c * dt)
                v.x *= 1.0f / (1.0f + h * b.linearDamping);
                v.y *= 1.0f / (1.0f + h * b.linearDamping);
                w *= 1.0f / (1.0f + h * b.angularDamping);
            }

            m_positions[i].c.x = c.x;
            m_positions[i].c.y = c.y;
            m_positions[i].a = a;
            m_velocities[i].v.x = v.x;
            m_velocities[i].v.y = v.y;
            m_velocities[i].w = w;
        }

        timer.reset();

        // Solver data
        solverData.step = step;
        solverData.positions = m_positions;
        solverData.velocities = m_velocities;

        // Initialize velocity constraints.
        solverDef.step = step;
        solverDef.contacts = m_contacts;
        solverDef.count = m_contactCount;
        solverDef.positions = m_positions;
        solverDef.velocities = m_velocities;

        contactSolver.init(solverDef);
        contactSolver.initializeVelocityConstraints();

        if (step.warmStarting) {
            contactSolver.warmStart();
        }

        for (int i = 0; i < m_jointCount; ++i) {
            m_joints[i].initVelocityConstraints(solverData);
        }

        profile.solveInit.accum(timer.getMilliseconds());

        // Solve velocity constraints
        timer.reset();
        for (int i = 0; i < step.velocityIterations; ++i) {
            for (int j = 0; j < m_jointCount; ++j) {
                m_joints[j].solveVelocityConstraints(solverData);
            }

            contactSolver.solveVelocityConstraints();
        }

        // Store impulses for warm starting
        contactSolver.storeImpulses();
        profile.solveVelocity.accum(timer.getMilliseconds());

        // Integrate positions
        for (int i = 0; i < m_bodyCount; ++i) {
            final Vector2 c = m_positions[i].c;
            float a = m_positions[i].a;
            final Vector2 v = m_velocities[i].v;
            float w = m_velocities[i].w;

            // Check for large velocities
            float translationx = v.x * h;
            float translationy = v.y * h;

            if (translationx * translationx + translationy * translationy > Settings.maxTranslationSquared) {
                float ratio = Settings.maxTranslation
                        / MathUtils.sqrt(translationx * translationx + translationy * translationy);
                v.x *= ratio;
                v.y *= ratio;
            }

            float rotation = h * w;
            if (rotation * rotation > Settings.maxRotationSquared) {
                float ratio = Settings.maxRotation / MathUtils.abs(rotation);
                w *= ratio;
            }

            // Integrate
            c.x += h * v.x;
            c.y += h * v.y;
            a += h * w;

            m_positions[i].a = a;
            m_velocities[i].w = w;
        }

        // Solve position constraints
        timer.reset();
        boolean positionSolved = false;
        for (int i = 0; i < step.positionIterations; ++i) {
            boolean contactsOkay = contactSolver.solvePositionConstraints();

            boolean jointsOkay = true;
            for (int j = 0; j < m_jointCount; ++j) {
                boolean jointOkay = m_joints[j].solvePositionConstraints(solverData);
                jointsOkay = jointsOkay && jointOkay;
            }

            if (contactsOkay && jointsOkay) {
                // Exit early if the position errors are small.
                positionSolved = true;
                break;
            }
        }

        // Copy state buffers back to the bodies
        for (int i = 0; i < m_bodyCount; ++i) {
            Body body = m_bodies[i];
            body.sweep.center.x = m_positions[i].c.x;
            body.sweep.center.y = m_positions[i].c.y;
            body.sweep.angle = m_positions[i].a;
            body.linearVelocity.x = m_velocities[i].v.x;
            body.linearVelocity.y = m_velocities[i].v.y;
            body.angularVelocity = m_velocities[i].w;
            body.synchronizeTransform();
        }

        profile.solvePosition.accum(timer.getMilliseconds());

        report(contactSolver.velocityConstraints);

        if (allowSleep) {
            float minSleepTime = Float.MAX_VALUE;

            final float linTolSqr = Settings.linearSleepTolerance * Settings.linearSleepTolerance;
            final float angTolSqr = Settings.angularSleepTolerance * Settings.angularSleepTolerance;

            for (int i = 0; i < m_bodyCount; ++i) {
                Body b = m_bodies[i];
                if (b.getType() == BodyType.STATIC) {
                    continue;
                }

                if ((b.flags & Body.E_AUTO_SLEEP_FLAG) == 0
                        || b.angularVelocity * b.angularVelocity > angTolSqr
                        || Vector2.dot(b.linearVelocity, b.linearVelocity) > linTolSqr) {
                    b.sleepTime = 0.0f;
                    minSleepTime = 0.0f;
                } else {
                    b.sleepTime += h;
                    minSleepTime = MathUtils.min(minSleepTime, b.sleepTime);
                }
            }

            if (minSleepTime >= Settings.timeToSleep && positionSolved) {
                for (int i = 0; i < m_bodyCount; ++i) {
                    Body b = m_bodies[i];
                    b.setAwake(false);
                }
            }
        }
    }

    private final ContactSolver toiContactSolver = new ContactSolver();
    private final ContactSolver.ContactSolverDef toiSolverDef = new ContactSolver.ContactSolverDef();

    public void solveTOI(TimeStep subStep, int toiIndexA, int toiIndexB) {
        assert (toiIndexA < m_bodyCount);
        assert (toiIndexB < m_bodyCount);

        // Initialize the body state.
        for (int i = 0; i < m_bodyCount; ++i) {
            m_positions[i].c.x = m_bodies[i].sweep.center.x;
            m_positions[i].c.y = m_bodies[i].sweep.center.y;
            m_positions[i].a = m_bodies[i].sweep.angle;
            m_velocities[i].v.x = m_bodies[i].linearVelocity.x;
            m_velocities[i].v.y = m_bodies[i].linearVelocity.y;
            m_velocities[i].w = m_bodies[i].angularVelocity;
        }

        toiSolverDef.contacts = m_contacts;
        toiSolverDef.count = m_contactCount;
        toiSolverDef.step = subStep;
        toiSolverDef.positions = m_positions;
        toiSolverDef.velocities = m_velocities;
        toiContactSolver.init(toiSolverDef);

        // Solve position constraints.
        for (int i = 0; i < subStep.positionIterations; ++i) {
            boolean contactsOkay = toiContactSolver.solveTOIPositionConstraints(toiIndexA, toiIndexB);
            if (contactsOkay) {
                break;
            }
        }
        // #if 0
        // // Is the new position really safe?
        // for (int i = 0; i < m_contactCount; ++i)
        // {
        // Contact* c = m_contacts[i];
        // Fixture* fA = c.GetFixtureA();
        // Fixture* fB = c.GetFixtureB();
        //
        // Body bA = fA.GetBody();
        // Body bB = fB.GetBody();
        //
        // int indexA = c.GetChildIndexA();
        // int indexB = c.GetChildIndexB();
        //
        // DistanceInput input;
        // input.proxyA.Set(fA.GetShape(), indexA);
        // input.proxyB.Set(fB.GetShape(), indexB);
        // input.transformA = bA.GetTransform();
        // input.transformB = bB.GetTransform();
        // input.useRadii = false;
        //
        // DistanceOutput output;
        // SimplexCache cache;
        // cache.count = 0;
        // Distance(&output, &cache, &input);
        //
        // if (output.distance == 0 || cache.count == 3)
        // {
        // cache.count += 0;
        // }
        // }
        // #endif

        // Leap of faith to new safe state.
        m_bodies[toiIndexA].sweep.center0.x = m_positions[toiIndexA].c.x;
        m_bodies[toiIndexA].sweep.center0.y = m_positions[toiIndexA].c.y;
        m_bodies[toiIndexA].sweep.angle0 = m_positions[toiIndexA].a;
        m_bodies[toiIndexB].sweep.center0.set(m_positions[toiIndexB].c);
        m_bodies[toiIndexB].sweep.angle0 = m_positions[toiIndexB].a;

        // No warm starting is needed for TOI events because warm
        // starting impulses were applied in the discrete solver.
        toiContactSolver.initializeVelocityConstraints();

        // Solve velocity constraints.
        for (int i = 0; i < subStep.velocityIterations; ++i) {
            toiContactSolver.solveVelocityConstraints();
        }

        // Don't store the TOI contact forces for warm starting
        // because they can be quite large.

        float h = subStep.dt;

        // Integrate positions
        for (int i = 0; i < m_bodyCount; ++i) {
            Vector2 c = m_positions[i].c;
            float a = m_positions[i].a;
            Vector2 v = m_velocities[i].v;
            float w = m_velocities[i].w;

            // Check for large velocities
            float translationx = v.x * h;
            float translationy = v.y * h;
            if (translationx * translationx + translationy * translationy > Settings.maxTranslationSquared) {
                float ratio =
                        Settings.maxTranslation
                                / MathUtils.sqrt(translationx * translationx + translationy * translationy);
                v.mulLocal(ratio);
            }

            float rotation = h * w;
            if (rotation * rotation > Settings.maxRotationSquared) {
                float ratio = Settings.maxRotation / MathUtils.abs(rotation);
                w *= ratio;
            }

            // Integrate
            c.x += v.x * h;
            c.y += v.y * h;
            a += h * w;

            m_positions[i].c.x = c.x;
            m_positions[i].c.y = c.y;
            m_positions[i].a = a;
            m_velocities[i].v.x = v.x;
            m_velocities[i].v.y = v.y;
            m_velocities[i].w = w;

            // Sync bodies
            Body body = m_bodies[i];
            body.sweep.center.x = c.x;
            body.sweep.center.y = c.y;
            body.sweep.angle = a;
            body.linearVelocity.x = v.x;
            body.linearVelocity.y = v.y;
            body.angularVelocity = w;
            body.synchronizeTransform();
        }

        report(toiContactSolver.velocityConstraints);
    }

    public void add(Body body) {
        assert (m_bodyCount < m_bodyCapacity);
        body.islandIndex = m_bodyCount;
        m_bodies[m_bodyCount] = body;
        ++m_bodyCount;
    }

    public void add(Contact contact) {
        assert (m_contactCount < m_contactCapacity);
        m_contacts[m_contactCount++] = contact;
    }

    public void add(Joint joint) {
        assert (m_jointCount < m_jointCapacity);
        m_joints[m_jointCount++] = joint;
    }

    private final ContactImpulse impulse = new ContactImpulse();

    public void report(ContactVelocityConstraint[] constraints) {
        if (m_listener == null) {
            return;
        }

        for (int i = 0; i < m_contactCount; ++i) {
            Contact c = m_contacts[i];

            ContactVelocityConstraint vc = constraints[i];
            impulse.count = vc.pointCount;
            for (int j = 0; j < vc.pointCount; ++j) {
                impulse.normalImpulses[j] = vc.points[j].normalImpulse;
                impulse.tangentImpulses[j] = vc.points[j].tangentImpulse;
            }

            m_listener.postSolve(c, impulse);
        }
    }
}
