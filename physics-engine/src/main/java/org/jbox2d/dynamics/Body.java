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
package org.jbox2d.dynamics;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jbox2d.collision.broadphase.BroadPhase;
import org.jbox2d.collision.shapes.MassData;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Rotation;
import org.jbox2d.common.Sweep;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.contacts.ContactEdge;
import org.jbox2d.dynamics.joints.JointEdge;

import java.util.ArrayList;
import java.util.List;

/**
 * A rigid body. These are created via World.createBody.
 *
 * @author Daniel Murphy
 */
@NoArgsConstructor
public class Body {
    public static final int E_ISLAND_FLAG = 0x0001;
    public static final int E_AWAKE_FLAG = 0x0002;
    public static final int E_AUTO_SLEEP_FLAG = 0x0004;
    public static final int E_BULLET_FLAG = 0x0008;
    public static final int E_FIXED_ROTATION_FLAG = 0x0010;
    public static final int E_ACTIVE_FLAG = 0x0020;
    public static final int E_TOI_FLAG = 0x0040;

    @Getter
    public BodyType type = BodyType.DYNAMIC;

    public int flags;

    public int islandIndex;

    /**
     * The body origin transform.
     */
    public final Transform transform = new Transform();
    /**
     * The previous transform for particle simulation
     */
    public final Transform xf0 = new Transform();

    /**
     * The swept motion for CCD
     */
    public final Sweep sweep = new Sweep();

    public final Vector2 linearVelocity = new Vector2();
    public float angularVelocity;

    public final Vector2 force = new Vector2();
    public float torque;

    public World world;

    @Getter
    public List<Fixture> fixtures = new ArrayList<>();

    public JointEdge jointList;
    @Getter
    public List<ContactEdge> contacts = new ArrayList<>();

    public float mass, invMass;

    // Rotational inertia about the center of mass.
    public float I, invI;

    public float linearDamping;
    public float angularDamping;
    /**
     * -- GETTER --
     * Get the gravity scale of the body.
     * -- SETTER --
     * Set the gravity scale of the body.
     */
    @Setter
    @Getter
    public float gravityScale;

    public float sleepTime;

    public Object userData;

    public void addFixture(Fixture fixture) {
        if (fixture.body == this) {
            throw new RuntimeException("Can't add same fixture");
        }

        if (fixture.body != null) {
            throw new RuntimeException("Can't add other's fixture");
        }

        if (world.isLocked()) {
            throw new RuntimeException("Can't add fixture when world is locked");
        }

        if ((flags & E_ACTIVE_FLAG) == E_ACTIVE_FLAG) {
            BroadPhase broadPhase = world.contactManager.broadPhase;
            fixture.createProxies(broadPhase, transform);
        }

        fixtures.add(fixture);

        fixture.body = this;

        // Adjust mass properties if needed.
        if (fixture.density > 0.0f) {
            resetMassData();
        }

        // Let the world know we have a new fixture. This will cause new contacts
        // to be created at the beginning of the next time step.
        world.flags |= World.NEW_FIXTURE;
    }

    /**
     * Destroy a fixture. This removes the fixture from the broad-phase and destroys all contacts
     * associated with this fixture. This will automatically adjust the mass of the body if the body
     * is dynamic and the fixture has positive density. All fixtures attached to a body are implicitly
     * destroyed when the body is destroyed.
     *
     * @param fixture the fixture to be removed.
     * @warning This function is locked during callbacks.
     */
    public final void removeFixture(Fixture fixture) {
        if (world.isLocked()) {
            throw new RuntimeException("Can't remove fixture when world is locked");
        }

        if (fixture.body != this) {
            throw new RuntimeException("Can't remove other's fixture");
        }

        if (!fixtures.remove(fixture)) {
            throw new RuntimeException("Can't find fixture");
        }

        // Destroy any contacts associated with the fixture.
        for (int i = 0; i < contacts.size(); i++) {
            Contact c = contacts.get(i).contact;

            Fixture fixtureA = c.getFixtureA();
            Fixture fixtureB = c.getFixtureB();

            if (fixture == fixtureA || fixture == fixtureB) {
                // This destroys the contact and removes it from
                // this body's contact list.
                world.contactManager.destroy(c);
                i--;
            }
        }

        if ((flags & E_ACTIVE_FLAG) == E_ACTIVE_FLAG) {
            BroadPhase broadPhase = world.contactManager.broadPhase;
            fixture.destroyProxies(broadPhase);
        }

        fixture.body = null;

        // Reset the mass data.
        resetMassData();
    }

    public final void removeFixtures(List<Fixture> fixtures) {
        if (world.isLocked()) {
            throw new RuntimeException("Can't remove fixture when world is locked");
        }

        for (int i = 0; i < fixtures.size(); i++) {
            Fixture fixture = fixtures.get(i);

            if (fixture.body != this) {
                throw new RuntimeException("Can't remove other's fixture");
            }

            if (!this.fixtures.remove(fixture)) {
                throw new RuntimeException("Can't find fixture");
            }

            // Destroy any contacts associated with the fixture.
            for (int i1 = 0; i1 < contacts.size(); i1++) {
                Contact c = contacts.get(i1).contact;

                Fixture fixtureA = c.getFixtureA();
                Fixture fixtureB = c.getFixtureB();

                if (fixture == fixtureA || fixture == fixtureB) {
                    // This destroys the contact and removes it from
                    // this body's contact list.
                    world.contactManager.destroy(c);
                    i1--;
                }
            }

            if ((flags & E_ACTIVE_FLAG) == E_ACTIVE_FLAG) {
                BroadPhase broadPhase = world.contactManager.broadPhase;
                fixture.destroyProxies(broadPhase);
            }

            fixture.body = null;
        }

        // Reset the mass data.
        resetMassData();
    }

    public void removeAllFixtures() {
        if (world.isLocked()) {
            throw new RuntimeException("Can't remove fixture when world is locked");
        }

        if (fixtures.isEmpty()) {
            return;
        }

        while (contacts.size() > 0) {
            world.contactManager.destroy(contacts.get(0).contact);
        }

        for (int i = 0; i < fixtures.size(); i++) {
            Fixture fixture = fixtures.get(i);

            if ((flags & E_ACTIVE_FLAG) == E_ACTIVE_FLAG) {
                BroadPhase broadPhase = world.contactManager.broadPhase;
                fixture.destroyProxies(broadPhase);
            }

            fixture.body = null;
        }

        fixtures.clear();

        resetMassData();
    }

    public void addFixtures(List<Fixture> fixtures) {
        if (world.isLocked()) {
            throw new RuntimeException("Can't remove fixture when world is locked");
        }

        for (int i = 0; i < fixtures.size(); i++) {
            Fixture fixture = fixtures.get(i);

            if (fixture.body == this) {
                throw new RuntimeException("Can't add same fixture");
            }

            if (fixture.body != null) {
                throw new RuntimeException("Can't add other's fixture");
            }

            if ((flags & E_ACTIVE_FLAG) == E_ACTIVE_FLAG) {
                BroadPhase broadPhase = world.contactManager.broadPhase;
                fixture.createProxies(broadPhase, transform);
            }

            this.fixtures.add(fixture);

            fixture.body = this;
        }

        resetMassData();

        // Let the world know we have a new fixture. This will cause new contacts
        // to be created at the beginning of the next time step.
        world.flags |= World.NEW_FIXTURE;
    }

    public void setFixtures(List<Fixture> fixtures) {
        if (world.isLocked()) {
            throw new RuntimeException("Can't remove fixture when world is locked");
        }

        while (contacts.size() > 0) {
            world.contactManager.destroy(contacts.get(0).contact);
        }

        for (int i = 0; i < this.fixtures.size(); i++) {
            Fixture fixture = this.fixtures.get(i);

            if ((flags & E_ACTIVE_FLAG) == E_ACTIVE_FLAG) {
                BroadPhase broadPhase = world.contactManager.broadPhase;
                fixture.destroyProxies(broadPhase);
            }

            fixture.body = null;
        }

        this.fixtures.clear();

        for (int i = 0; i < fixtures.size(); i++) {
            Fixture fixture = fixtures.get(i);

            if (fixture.body == this) {
                throw new RuntimeException("Can't add same fixture");
            }

            if (fixture.body != null) {
                throw new RuntimeException("Can't add other's fixture");
            }

            if ((flags & E_ACTIVE_FLAG) == E_ACTIVE_FLAG) {
                BroadPhase broadPhase = world.contactManager.broadPhase;
                fixture.createProxies(broadPhase, transform);
            }

            this.fixtures.add(fixture);

            fixture.body = this;
        }

        resetMassData();

        // Let the world know we have a new fixture. This will cause new contacts
        // to be created at the beginning of the next time step.
        world.flags |= World.NEW_FIXTURE;
    }

    /**
     * Set the position of the body's origin and rotation. This breaks any contacts and wakes the
     * other bodies. Manipulating a body's transform may cause non-physical behavior. Note: contacts
     * are updated on the next call to World.step().
     *
     * @param position the world position of the body's local origin.
     * @param angle    the world rotation in radians.
     */
    public final void setTransform(Vector2 position, float angle) {
        if (world.isLocked()) {
            throw new RuntimeException("Can't set fixture transform when world is locked");
        }

        transform.rotation.set(angle);
        transform.position.set(position);

        // m_sweep.c0 = m_sweep.c = Mul(m_xf, m_sweep.localCenter);
        Transform.mulToOutUnsafe(transform, sweep.localCenter, sweep.center);
        sweep.angle = angle;

        sweep.center0.set(sweep.center);
        sweep.angle0 = sweep.angle;

        BroadPhase broadPhase = world.contactManager.broadPhase;
        for (int i = 0; i < fixtures.size(); i++) {
            fixtures.get(i).synchronize(broadPhase, transform, transform);
        }
    }

    /**
     * Get the body transform for the body's origin.
     *
     * @return the world transform of the body's origin.
     */
    public final Transform getTransform() {
        return transform;
    }

    /**
     * Get the world body origin position. Do not modify.
     *
     * @return the world position of the body's origin.
     */
    public final Vector2 getPosition() {
        return transform.position;
    }

    /**
     * Get the angle in radians.
     *
     * @return the current world rotation angle in radians.
     */
    public final float getAngle() {
        return sweep.angle;
    }

    /**
     * Get the world position of the center of mass. Do not modify.
     */
    public final Vector2 getWorldCenter() {
        return sweep.center;
    }

    /**
     * Get the local position of the center of mass. Do not modify.
     */
    public final Vector2 getLocalCenter() {
        return sweep.localCenter;
    }

    /**
     * Set the linear velocity of the center of mass.
     *
     * @param v the new linear velocity of the center of mass.
     */
    public final void setLinearVelocity(Vector2 v) {
        if (type == BodyType.STATIC) {
            return;
        }

        if (Vector2.dot(v, v) > 0.0f) {
            setAwake(true);
        }

        linearVelocity.set(v);
    }

    public final void setLinearVelocity(float x, float y) {
        if (type == BodyType.STATIC) {
            return;
        }

        if (x * x + y * y > 0.0f) {
            setAwake(true);
        }

        linearVelocity.set(x, y);
    }

    /**
     * Get the linear velocity of the center of mass. Do not modify, instead use
     * {@link #setLinearVelocity(Vector2)}.
     *
     * @return the linear velocity of the center of mass.
     */
    public final Vector2 getLinearVelocity() {
        return linearVelocity;
    }

    /**
     * Set the angular velocity.
     *
     * @param angularVelocity the new angular velocity in radians/second.
     */
    public final void setAngularVelocity(float angularVelocity) {
        if (type == BodyType.STATIC) {
            return;
        }

        if (angularVelocity * angularVelocity > 0f) {
            setAwake(true);
        }

        this.angularVelocity = angularVelocity;
    }

    /**
     * Get the angular velocity.
     *
     * @return the angular velocity in radians/second.
     */
    public final float getAngularVelocity() {
        return angularVelocity;
    }

    /**
     * Apply a force at a world point. If the force is not applied at the center of mass, it will
     * generate a torque and affect the angular velocity. This wakes up the body.
     *
     * @param force the world force vector, usually in Newtons (N).
     * @param point the world position of the point of application.
     */
    public final void applyForce(Vector2 force, Vector2 point) {
        if (type != BodyType.DYNAMIC) {
            return;
        }

        if (!isAwake()) {
            setAwake(true);
        }

        // m_force.addLocal(force);
        // Vec2 temp = tltemp.get();
        // temp.set(point).subLocal(m_sweep.c);
        // m_torque += Vec2.cross(temp, force);

        this.force.x += force.x;
        this.force.y += force.y;

        torque += (point.x - sweep.center.x) * force.y - (point.y - sweep.center.y) * force.x;
    }

    /**
     * Apply a force to the center of mass. This wakes up the body.
     *
     * @param force the world force vector, usually in Newtons (N).
     */
    public final void applyForceToCenter(Vector2 force) {
        applyForceToCenter(force.x, force.y);
    }

    public final void applyForceToCenter(float x, float y) {
        if (type != BodyType.DYNAMIC) {
            return;
        }

        if (!isAwake()) {
            setAwake(true);
        }

        this.force.x += x;
        this.force.y += y;
    }

    /**
     * Apply a torque. This affects the angular velocity without affecting the linear velocity of the
     * center of mass. This wakes up the body.
     *
     * @param torque about the z-axis (out of the screen), usually in N-m.
     */
    public final void applyTorque(float torque) {
        if (type != BodyType.DYNAMIC) {
            return;
        }

        if (!isAwake()) {
            setAwake(true);
        }

        this.torque += torque;
    }

    /**
     * Apply an impulse at a point. This immediately modifies the velocity. It also modifies the
     * angular velocity if the point of application is not at the center of mass. This wakes up the
     * body if 'wake' is set to true. If the body is sleeping and 'wake' is false, then there is no
     * effect.
     *
     * @param impulse the world impulse vector, usually in N-seconds or kg-m/s.
     * @param point   the world position of the point of application.
     * @param wake    also wake up the body
     */
    public final void applyLinearImpulse(Vector2 impulse, Vector2 point, boolean wake) {
        if (type != BodyType.DYNAMIC) {
            return;
        }

        if (!isAwake()) {
            if (wake) {
                setAwake(true);
            } else {
                return;
            }
        }

        linearVelocity.x += impulse.x * invMass;
        linearVelocity.y += impulse.y * invMass;

        angularVelocity += invI * ((point.x - sweep.center.x) * impulse.y - (point.y - sweep.center.y) * impulse.x);
    }

    /**
     * Apply an angular impulse.
     *
     * @param impulse the angular impulse in units of kg*m*m/s
     */
    public void applyAngularImpulse(float impulse) {
        if (type != BodyType.DYNAMIC) {
            return;
        }

        if (!isAwake()) {
            setAwake(true);
        }
        angularVelocity += invI * impulse;
    }

    /**
     * Get the total mass of the body.
     *
     * @return the mass, usually in kilograms (kg).
     */
    public final float getMass() {
        return mass;
    }

    /**
     * Get the central rotational inertia of the body.
     *
     * @return the rotational inertia, usually in kg-m^2.
     */
    public final float getInertia() {
        return I + mass * (sweep.localCenter.x * sweep.localCenter.x + sweep.localCenter.y * sweep.localCenter.y);
    }

    /**
     * Get the mass data of the body. The rotational inertia is relative to the center of mass.
     */
    public final void getMassData(MassData data) {
        data.mass = mass;
        data.I = I + mass * (sweep.localCenter.x * sweep.localCenter.x + sweep.localCenter.y * sweep.localCenter.y);
        data.center.x = sweep.localCenter.x;
        data.center.y = sweep.localCenter.y;
    }

    /**
     * Set the mass properties to override the mass properties of the fixtures. Note that this changes
     * the center of mass position. Note that creating or destroying fixtures can also alter the mass.
     * This function has no effect if the body isn't dynamic.
     *
     * @param massData the mass properties.
     */
    public final void setMassData(MassData massData) {
        // TODO_ERIN adjust linear velocity and torque to account for movement of center.
        if (world.isLocked()) {
            throw new RuntimeException("Can't set fixture's mass data when world is locked");
        }

        if (type != BodyType.DYNAMIC) {
            return;
        }

        I = 0.0f;
        invI = 0.0f;

        mass = massData.mass;
        if (mass <= 0.0f) {
            mass = 1f;
        }

        invMass = 1.0f / mass;

        if (massData.I > 0.0f && (flags & E_FIXED_ROTATION_FLAG) == 0) {
            I = massData.I - mass * Vector2.dot(massData.center, massData.center);
            assert (I > 0.0f);
            invI = 1.0f / I;
        }

        final Vector2 oldCenter = world.getPool().popVec2();
        // Move center of mass.
        oldCenter.set(sweep.center);
        sweep.localCenter.set(massData.center);
        // m_sweep.c0 = m_sweep.c = Mul(m_xf, m_sweep.localCenter);
        Transform.mulToOutUnsafe(transform, sweep.localCenter, sweep.center0);
        sweep.center.set(sweep.center0);

        // Update center of mass velocity.
        // m_linearVelocity += Cross(m_angularVelocity, m_sweep.c - oldCenter);
        final Vector2 temp = world.getPool().popVec2();
        temp.set(sweep.center).subLocal(oldCenter);
        Vector2.crossToOut(angularVelocity, temp, temp);
        linearVelocity.addLocal(temp);

        world.getPool().pushVec2(2);
    }

    private final MassData pmd = new MassData();

    /**
     * This resets the mass properties to the sum of the mass properties of the fixtures. This
     * normally does not need to be called unless you called setMassData to override the mass and you
     * later want to reset the mass.
     */
    public final void resetMassData() {
        // Compute mass data from shapes. Each shape has its own density.
        mass = 0.0f;
        invMass = 0.0f;
        I = 0.0f;
        invI = 0.0f;
        sweep.localCenter.setZero();

        // Static and kinematic bodies have zero mass.
        if (type == BodyType.STATIC || type == BodyType.KINEMATIC) {
            // m_sweep.c0 = m_sweep.c = m_xf.position;
            sweep.center0.set(transform.position);
            sweep.center.set(transform.position);
            sweep.angle0 = sweep.angle;
            return;
        }

        if (type != BodyType.DYNAMIC) {
            throw new RuntimeException("Can't reset mass data of not dynamic fixture");
        }

        // Accumulate mass over all fixtures.
        final Vector2 localCenter = world.getPool().popVec2();
        localCenter.setZero();
        final Vector2 temp = world.getPool().popVec2();
        final MassData massData = pmd;
        for (int i = 0; i < fixtures.size(); i++) {
            Fixture fixture = fixtures.get(i);
            if (fixture.density == 0.0f) {
                continue;
            }

            fixture.getMassData(massData);
            mass += massData.mass;
            // center += massData.mass * massData.center;
            temp.set(massData.center).mulLocal(massData.mass);
            localCenter.addLocal(temp);
            I += massData.I;
        }

        // Compute center of mass.
        if (mass > 0.0f) {
            invMass = 1.0f / mass;
            localCenter.mulLocal(invMass);
        } else {
            // Force all dynamic bodies to have a positive mass.
            mass = 1.0f;
            invMass = 1.0f;
        }

        if (I > 0.0f && (flags & E_FIXED_ROTATION_FLAG) == 0) {
            // Center the inertia about the center of mass.
            I -= mass * Vector2.dot(localCenter, localCenter);
            if (I <= 0.0f) {
                throw new RuntimeException("Rotational inertia below zero");
            }

            invI = 1.0f / I;
        } else {
            I = 0.0f;
            invI = 0.0f;
        }

        Vector2 oldCenter = world.getPool().popVec2();
        // Move center of mass.
        oldCenter.set(sweep.center);
        sweep.localCenter.set(localCenter);
        // m_sweep.c0 = m_sweep.c = Mul(m_xf, m_sweep.localCenter);
        Transform.mulToOutUnsafe(transform, sweep.localCenter, sweep.center0);
        sweep.center.set(sweep.center0);

        // Update center of mass velocity.
        // m_linearVelocity += Cross(m_angularVelocity, m_sweep.c - oldCenter);
        temp.set(sweep.center).subLocal(oldCenter);

        Vector2.crossToOutUnsafe(angularVelocity, temp, oldCenter);
        linearVelocity.addLocal(oldCenter);

        world.getPool().pushVec2(3);
    }

    /**
     * Get the world coordinates of a point given the local coordinates.
     *
     * @param localPoint a point on the body measured relative the the body's origin.
     * @return the same point expressed in world coordinates.
     */
    public final Vector2 getWorldPoint(Vector2 localPoint) {
        Vector2 v = new Vector2();
        getWorldPointToOut(localPoint, v);
        return v;
    }

    public final void getWorldPointToOut(Vector2 localPoint, Vector2 out) {
        Transform.mulToOut(transform, localPoint, out);
    }

    /**
     * Get the world coordinates of a vector given the local coordinates.
     *
     * @param localVector a vector fixed in the body.
     * @return the same vector expressed in world coordinates.
     */
    public final Vector2 getWorldVector(Vector2 localVector) {
        Vector2 out = new Vector2();
        getWorldVectorToOut(localVector, out);
        return out;
    }

    public final void getWorldVectorToOut(Vector2 localVector, Vector2 out) {
        Rotation.mulToOut(transform.rotation, localVector, out);
    }

    public final void getWorldVectorToOutUnsafe(Vector2 localVector, Vector2 out) {
        Rotation.mulToOutUnsafe(transform.rotation, localVector, out);
    }

    /**
     * Gets a local point relative to the body's origin given a world point.
     *
     * @param worldPoint point in world coordinates.
     * @return the corresponding local point relative to the body's origin.
     */
    public final Vector2 getLocalPoint(Vector2 worldPoint) {
        Vector2 out = new Vector2();
        getLocalPointToOut(worldPoint, out);
        return out;
    }

    public final void getLocalPointToOut(Vector2 worldPoint, Vector2 out) {
        Transform.mulTransToOut(transform, worldPoint, out);
    }

    /**
     * Gets a local vector given a world vector.
     *
     * @param worldVector vector in world coordinates.
     * @return the corresponding local vector.
     */
    public final Vector2 getLocalVector(Vector2 worldVector) {
        Vector2 out = new Vector2();
        getLocalVectorToOut(worldVector, out);
        return out;
    }

    public final void getLocalVectorToOut(Vector2 worldVector, Vector2 out) {
        Rotation.mulTrans(transform.rotation, worldVector, out);
    }

    public final void getLocalVectorToOutUnsafe(Vector2 worldVector, Vector2 out) {
        Rotation.mulTransUnsafe(transform.rotation, worldVector, out);
    }

    /**
     * Get the world linear velocity of a world point attached to this body.
     *
     * @param worldPoint point in world coordinates.
     * @return the world velocity of a point.
     */
    public final Vector2 getLinearVelocityFromWorldPoint(Vector2 worldPoint) {
        Vector2 out = new Vector2();
        getLinearVelocityFromWorldPointToOut(worldPoint, out);
        return out;
    }

    public final void getLinearVelocityFromWorldPointToOut(Vector2 worldPoint, Vector2 out) {
        final float tempX = worldPoint.x - sweep.center.x;
        final float tempY = worldPoint.y - sweep.center.y;
        out.x = -angularVelocity * tempY + linearVelocity.x;
        out.y = angularVelocity * tempX + linearVelocity.y;
    }

    /**
     * Get the world velocity of a local point.
     *
     * @param localPoint point in local coordinates.
     * @return the world velocity of a point.
     */
    public final Vector2 getLinearVelocityFromLocalPoint(Vector2 localPoint) {
        Vector2 out = new Vector2();
        getLinearVelocityFromLocalPointToOut(localPoint, out);
        return out;
    }

    public final void getLinearVelocityFromLocalPointToOut(Vector2 localPoint, Vector2 out) {
        getWorldPointToOut(localPoint, out);
        getLinearVelocityFromWorldPointToOut(out, out);
    }

    /**
     * Get the linear damping of the body.
     */
    public final float getLinearDamping() {
        return linearDamping;
    }

    /**
     * Set the linear damping of the body.
     */
    public final void setLinearDamping(float linearDamping) {
        this.linearDamping = linearDamping;
    }

    /**
     * Get the angular damping of the body.
     */
    public final float getAngularDamping() {
        return angularDamping;
    }

    /**
     * Set the angular damping of the body.
     */
    public final void setAngularDamping(float angularDamping) {
        this.angularDamping = angularDamping;
    }

    public void setPosition(float x, float y) {
        transform.position.set(x, y);

        Transform.mulToOutUnsafe(transform, sweep.localCenter, sweep.center);

        sweep.center0.set(sweep.center);

        if (fixtures.size() > 0) {
            BroadPhase broadPhase = world.contactManager.broadPhase;
            for (int i = 0; i < fixtures.size(); i++) {
                fixtures.get(i).synchronize(broadPhase, transform, transform);
            }
        }
    }

    public void setRotation(float sin, float cos) {
        transform.rotation.set(sin, cos);

        Transform.mulToOutUnsafe(transform, sweep.localCenter, sweep.center);
        sweep.angle = MathUtils.atan2(sin, cos);

        sweep.center0.set(sweep.center);
        sweep.angle0 = sweep.angle;

        if (fixtures.size() > 0) {
            BroadPhase broadPhase = world.contactManager.broadPhase;
            for (int i = 0; i < fixtures.size(); i++) {
                fixtures.get(i).synchronize(broadPhase, transform, transform);
            }
        }
    }

    /**
     * Set the type of this body. This may alter the mass and velocity.
     */
    public void setType(BodyType type) {
        if (world.isLocked()) {
            throw new RuntimeException("Can't set type when world is locked");
        }

        if (this.type == type) {
            return;
        }

        this.type = type;

        resetMassData();

        if (this.type == BodyType.STATIC) {
            linearVelocity.setZero();
            angularVelocity = 0.0f;
            sweep.angle0 = sweep.angle;
            sweep.center0.set(sweep.center);
            synchronizeFixtures();
        }

        setAwake(true);

        force.setZero();
        torque = 0.0f;

        // Delete the attached contacts.
        while (contacts.size() > 0) {
            world.contactManager.destroy(contacts.get(0).contact);
        }

        // Touch the proxies so that new contacts will be created (when appropriate)
        BroadPhase broadPhase = world.contactManager.broadPhase;
        for (int i = 0; i < fixtures.size(); i++) {
            Fixture fixture = fixtures.get(i);
            int proxyCount = fixture.proxyCount;
            for (int j = 0; j < proxyCount; ++j) {
                broadPhase.touchProxy(fixture.proxies[j].proxyId);
            }
        }
    }

    /**
     * Is this body treated like a bullet for continuous collision detection?
     */
    public final boolean isBullet() {
        return (flags & E_BULLET_FLAG) == E_BULLET_FLAG;
    }

    /**
     * Should this body be treated like a bullet for continuous collision detection?
     */
    public final void setBullet(boolean flag) {
        if (flag) {
            flags |= E_BULLET_FLAG;
        } else {
            flags &= ~E_BULLET_FLAG;
        }
    }

    /**
     * You can disable sleeping on this body. If you disable sleeping, the body will be woken.
     */
    public void setSleepingAllowed(boolean flag) {
        if (flag) {
            flags |= E_AUTO_SLEEP_FLAG;
        } else {
            flags &= ~E_AUTO_SLEEP_FLAG;
            setAwake(true);
        }
    }

    /**
     * Is this body allowed to sleep
     */
    public boolean isSleepingAllowed() {
        return (flags & E_AUTO_SLEEP_FLAG) == E_AUTO_SLEEP_FLAG;
    }

    /**
     * Set the sleep state of the body. A sleeping body has very low CPU cost.
     * Note that putting it to sleep will set its velocities and forces to zero.
     *
     * @param flag set to true to wake the body, false to put it to sleep
     */
    public void setAwake(boolean flag) {
        if (flag) {
            if ((flags & E_AWAKE_FLAG) == 0) {
                flags |= E_AWAKE_FLAG;
                sleepTime = 0.0f;
            }
        } else {
            flags &= ~E_AWAKE_FLAG;
            sleepTime = 0.0f;
            linearVelocity.setZero();
            angularVelocity = 0.0f;
            force.setZero();
            torque = 0.0f;
        }
    }

    /**
     * Get the sleeping state of this body.
     *
     * @return true if the body is awake.
     */
    public boolean isAwake() {
        return (flags & E_AWAKE_FLAG) == E_AWAKE_FLAG;
    }

    /**
     * Set the active state of the body. An inactive body is not simulated and cannot be collided with
     * or woken up. If you pass a flag of true, all fixtures will be added to the broad-phase. If you
     * pass a flag of false, all fixtures will be removed from the broad-phase and all contacts will
     * be destroyed. Fixtures and joints are otherwise unaffected. You may continue to create/destroy
     * fixtures and joints on inactive bodies. Fixtures on an inactive body are implicitly inactive
     * and will not participate in collisions, ray-casts, or queries. Joints connected to an inactive
     * body are implicitly inactive. An inactive body is still owned by a World object and remains in
     * the body list.
     */
    public void setActive(boolean flag) {
        assert (!world.isLocked());

        if (flag == isActive()) {
            return;
        }

        if (flag) {
            flags |= E_ACTIVE_FLAG;

            // Create all proxies.
            BroadPhase broadPhase = world.contactManager.broadPhase;
            for (int i = 0; i < fixtures.size(); i++) {
                fixtures.get(i).createProxies(broadPhase, transform);
            }

            // Contacts are created the next time step.
        } else {
            flags &= ~E_ACTIVE_FLAG;

            // Destroy all proxies.
            BroadPhase broadPhase = world.contactManager.broadPhase;
            for (int i = 0; i < fixtures.size(); i++) {
                fixtures.get(i).destroyProxies(broadPhase);
            }

            // Destroy the attached contacts.
            while (contacts.size() > 0) {
                world.contactManager.destroy(contacts.get(0).contact);
            }
        }
    }

    /**
     * Get the active state of the body.
     */
    public boolean isActive() {
        return (flags & E_ACTIVE_FLAG) == E_ACTIVE_FLAG;
    }

    /**
     * Set this body to have fixed rotation. This causes the mass to be reset.
     */
    public void setFixedRotation(boolean flag) {
        if (flag) {
            flags |= E_FIXED_ROTATION_FLAG;
        } else {
            flags &= ~E_FIXED_ROTATION_FLAG;
        }

        resetMassData();
    }

    /**
     * Does this body have fixed rotation?
     */
    public boolean isFixedRotation() {
        return (flags & E_FIXED_ROTATION_FLAG) == E_FIXED_ROTATION_FLAG;
    }

    /**
     * Get the list of all joints attached to this body.
     */
    public final JointEdge getJointList() {
        return jointList;
    }

    /**
     * Get the user data pointer that was provided in the body definition.
     */
    public final Object getUserData() {
        return userData;
    }

    /**
     * Set the user data. Use this to store your application specific data.
     */
    public final void setUserData(Object data) {
        userData = data;
    }

    /**
     * Get the parent world of this body.
     */
    public final World getWorld() {
        return world;
    }

    // djm pooling
    private final Transform pxf = new Transform();

    protected final void synchronizeFixtures() {
        final Transform xf1 = pxf;
        // xf1.position = m_sweep.c0 - Mul(xf1.R, m_sweep.localCenter);

        // xf1.q.set(m_sweep.a0);
        // Rot.mulToOutUnsafe(xf1.q, m_sweep.localCenter, xf1.p);
        // xf1.p.mulLocal(-1).addLocal(m_sweep.c0);
        // inlined:
        xf1.rotation.sin = MathUtils.sin(sweep.angle0);
        xf1.rotation.cos = MathUtils.cos(sweep.angle0);
        xf1.position.x = sweep.center0.x - xf1.rotation.cos * sweep.localCenter.x + xf1.rotation.sin * sweep.localCenter.y;
        xf1.position.y = sweep.center0.y - xf1.rotation.sin * sweep.localCenter.x - xf1.rotation.cos * sweep.localCenter.y;
        // end inline

        for (int i = 0; i < fixtures.size(); i++) {
            fixtures.get(i).synchronize(world.contactManager.broadPhase, xf1, transform);
        }
    }

    public final void synchronizeTransform() {
        // m_xf.q.set(m_sweep.a);
        //
        // // m_xf.position = m_sweep.c - Mul(m_xf.R, m_sweep.localCenter);
        // Rot.mulToOutUnsafe(m_xf.q, m_sweep.localCenter, m_xf.p);
        // m_xf.p.mulLocal(-1).addLocal(m_sweep.c);
        //
        transform.rotation.sin = MathUtils.sin(sweep.angle);
        transform.rotation.cos = MathUtils.cos(sweep.angle);
        Rotation q = transform.rotation;
        Vector2 v = sweep.localCenter;
        transform.position.x = sweep.center.x - q.cos * v.x + q.sin * v.y;
        transform.position.y = sweep.center.y - q.sin * v.x - q.cos * v.y;
    }

    /**
     * This is used to prevent connected bodies from colliding. It may lie, depending on the
     * collideConnected flag.
     */
    public boolean shouldCollide(Body other) {
        // At least one body should be dynamic.
        if (type != BodyType.DYNAMIC && other.type != BodyType.DYNAMIC) {
            return false;
        }

        // Does a joint prevent collision?
        for (JointEdge jn = jointList; jn != null; jn = jn.next) {
            if (jn.other == other) {
                if (!jn.joint.getCollideConnected()) {
                    return false;
                }
            }
        }

        return true;
    }

    protected final void advance(float t) {
        // Advance to the new safe time. This doesn't sync the broad-phase.
        sweep.advance(t);
        sweep.center.set(sweep.center0);
        sweep.angle = sweep.angle0;
        transform.rotation.set(sweep.angle);
        // m_xf.position = m_sweep.c - Mul(m_xf.R, m_sweep.localCenter);
        Rotation.mulToOutUnsafe(transform.rotation, sweep.localCenter, transform.position);
        transform.position.mulLocal(-1).addLocal(sweep.center);
    }
}
