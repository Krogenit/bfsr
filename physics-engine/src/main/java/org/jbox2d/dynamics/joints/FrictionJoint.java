/**
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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
 * <p>
 * Created at 7:27:32 AM Jan 20, 2011
 * <p>
 * Created at 7:27:32 AM Jan 20, 2011
 * <p>
 * Created at 7:27:32 AM Jan 20, 2011
 */
/**
 * Created at 7:27:32 AM Jan 20, 2011
 */
package org.jbox2d.dynamics.joints;

import lombok.Getter;
import org.jbox2d.common.Mat22;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Rotation;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.SolverData;
import org.jbox2d.pooling.IWorldPool;

/**
 * @author Daniel Murphy
 */
public class FrictionJoint extends Joint {

    @Getter
    private final Vector2 localAnchorA;
    @Getter
    private final Vector2 localAnchorB;

    // Solver shared
    private final Vector2 linearImpulse;
    private float angularImpulse;
    @Getter
    private float maxForce;
    @Getter
    private float maxTorque;

    // Solver temp
    private int indexA;
    private int indexB;
    private final Vector2 rA = new Vector2();
    private final Vector2 rB = new Vector2();
    private final Vector2 localCenterA = new Vector2();
    private final Vector2 localCenterB = new Vector2();
    private float invMassA;
    private float invMassB;
    private float invIA;
    private float invIB;
    private final Mat22 linearMass = new Mat22();
    private float angularMass;

    protected FrictionJoint(IWorldPool argWorldPool, FrictionJointDef def) {
        super(argWorldPool, def);
        localAnchorA = new Vector2(def.localAnchorA);
        localAnchorB = new Vector2(def.localAnchorB);

        linearImpulse = new Vector2();
        angularImpulse = 0.0f;

        maxForce = def.maxForce;
        maxTorque = def.maxTorque;
    }

    @Override
    public void getAnchorA(Vector2 argOut) {
        m_bodyA.getWorldPointToOut(localAnchorA, argOut);
    }

    @Override
    public void getAnchorB(Vector2 argOut) {
        m_bodyB.getWorldPointToOut(localAnchorB, argOut);
    }

    @Override
    public void getReactionForce(float inv_dt, Vector2 argOut) {
        argOut.set(linearImpulse).mulLocal(inv_dt);
    }

    @Override
    public float getReactionTorque(float inv_dt) {
        return inv_dt * angularImpulse;
    }

    public void setMaxForce(float force) {
        assert (force >= 0.0f);
        maxForce = force;
    }

    public void setMaxTorque(float torque) {
        assert (torque >= 0.0f);
        maxTorque = torque;
    }

    @Override
    public void initVelocityConstraints(final SolverData data) {
        indexA = m_bodyA.islandIndex;
        indexB = m_bodyB.islandIndex;
        localCenterA.set(m_bodyA.sweep.localCenter);
        localCenterB.set(m_bodyB.sweep.localCenter);
        invMassA = m_bodyA.invMass;
        invMassB = m_bodyB.invMass;
        invIA = m_bodyA.invI;
        invIB = m_bodyB.invI;

        float aA = data.positions[indexA].a;
        Vector2 vA = data.velocities[indexA].v;
        float wA = data.velocities[indexA].w;

        float aB = data.positions[indexB].a;
        Vector2 vB = data.velocities[indexB].v;
        float wB = data.velocities[indexB].w;

        final Vector2 temp = pool.popVec2();
        final Rotation qA = pool.popRot();
        final Rotation qB = pool.popRot();

        qA.set(aA);
        qB.set(aB);

        // Compute the effective mass matrix.
        Rotation.mulToOutUnsafe(qA, temp.set(localAnchorA).subLocal(localCenterA), rA);
        Rotation.mulToOutUnsafe(qB, temp.set(localAnchorB).subLocal(localCenterB), rB);

        float mA = invMassA, mB = invMassB;
        float iA = invIA, iB = invIB;

        final Mat22 K = pool.popMat22();
        K.ex.x = mA + mB + iA * rA.y * rA.y + iB * rB.y * rB.y;
        K.ex.y = -iA * rA.x * rA.y - iB * rB.x * rB.y;
        K.ey.x = K.ex.y;
        K.ey.y = mA + mB + iA * rA.x * rA.x + iB * rB.x * rB.x;

        K.invertToOut(linearMass);

        angularMass = iA + iB;
        if (angularMass > 0.0f) {
            angularMass = 1.0f / angularMass;
        }

        if (data.step.warmStarting) {
            // Scale impulses to support a variable time step.
            linearImpulse.mulLocal(data.step.dtRatio);
            angularImpulse *= data.step.dtRatio;

            final Vector2 P = pool.popVec2();
            P.set(linearImpulse);

            temp.set(P).mulLocal(mA);
            vA.subLocal(temp);
            wA -= iA * (Vector2.cross(rA, P) + angularImpulse);

            temp.set(P).mulLocal(mB);
            vB.addLocal(temp);
            wB += iB * (Vector2.cross(rB, P) + angularImpulse);

            pool.pushVec2(1);
        } else {
            linearImpulse.setZero();
            angularImpulse = 0.0f;
        }

        if (data.velocities[indexA].w != wA) {
            assert (data.velocities[indexA].w != wA);
        }
        data.velocities[indexA].w = wA;
        data.velocities[indexB].w = wB;

        pool.pushRot(2);
        pool.pushVec2(1);
        pool.pushMat22(1);
    }

    @Override
    public void solveVelocityConstraints(final SolverData data) {
        Vector2 vA = data.velocities[indexA].v;
        float wA = data.velocities[indexA].w;
        Vector2 vB = data.velocities[indexB].v;
        float wB = data.velocities[indexB].w;

        float mA = invMassA, mB = invMassB;
        float iA = invIA, iB = invIB;

        float h = data.step.dt;

        // Solve angular friction
        {
            float Cdot = wB - wA;
            float impulse = -angularMass * Cdot;

            float oldImpulse = angularImpulse;
            float maxImpulse = h * maxTorque;
            angularImpulse = MathUtils.clamp(angularImpulse + impulse, -maxImpulse, maxImpulse);
            impulse = angularImpulse - oldImpulse;

            wA -= iA * impulse;
            wB += iB * impulse;
        }

        // Solve linear friction
        {
            final Vector2 Cdot = pool.popVec2();
            final Vector2 temp = pool.popVec2();

            Vector2.crossToOutUnsafe(wA, rA, temp);
            Vector2.crossToOutUnsafe(wB, rB, Cdot);
            Cdot.addLocal(vB).subLocal(vA).subLocal(temp);

            final Vector2 impulse = pool.popVec2();
            Mat22.mulToOutUnsafe(linearMass, Cdot, impulse);
            impulse.negateLocal();

            final Vector2 oldImpulse = pool.popVec2();
            oldImpulse.set(linearImpulse);
            linearImpulse.addLocal(impulse);

            float maxImpulse = h * maxForce;

            if (linearImpulse.lengthSquared() > maxImpulse * maxImpulse) {
                linearImpulse.normalize();
                linearImpulse.mulLocal(maxImpulse);
            }

            impulse.set(linearImpulse).subLocal(oldImpulse);

            temp.set(impulse).mulLocal(mA);
            vA.subLocal(temp);
            wA -= iA * Vector2.cross(rA, impulse);

            temp.set(impulse).mulLocal(mB);
            vB.addLocal(temp);
            wB += iB * Vector2.cross(rB, impulse);
        }

        if (data.velocities[indexA].w != wA) {
            assert (data.velocities[indexA].w != wA);
        }

        data.velocities[indexA].w = wA;
        data.velocities[indexB].w = wB;

        pool.pushVec2(4);
    }

    @Override
    public boolean solvePositionConstraints(final SolverData data) {
        return true;
    }
}
