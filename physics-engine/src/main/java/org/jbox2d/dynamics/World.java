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

import lombok.Getter;
import lombok.Setter;
import org.jbox2d.callbacks.ContactFilter;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.callbacks.DestructionListener;
import org.jbox2d.callbacks.ParticleQueryCallback;
import org.jbox2d.callbacks.ParticleRaycastCallback;
import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.callbacks.TreeCallback;
import org.jbox2d.callbacks.TreeRayCastCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.RayCastInput;
import org.jbox2d.collision.RayCastOutput;
import org.jbox2d.collision.TimeOfImpact;
import org.jbox2d.collision.broadphase.BroadPhase;
import org.jbox2d.collision.broadphase.BroadPhaseStrategy;
import org.jbox2d.collision.broadphase.DefaultBroadPhaseBuffer;
import org.jbox2d.collision.broadphase.DynamicTree;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Sweep;
import org.jbox2d.common.Timer;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.contacts.ContactEdge;
import org.jbox2d.dynamics.contacts.ContactRegister;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointDef;
import org.jbox2d.dynamics.joints.JointEdge;
import org.jbox2d.pooling.IDynamicStack;
import org.jbox2d.pooling.IWorldPool;
import org.jbox2d.pooling.normal.DefaultWorldPool;

import java.util.ArrayList;
import java.util.List;

/**
 * The world class manages all physics entities, dynamic simulation, and asynchronous queries. The
 * world also contains efficient memory management facilities.
 *
 * @author Daniel Murphy
 */
public class World {
    private static final int WORLD_POOL_SIZE = 100;
    private static final int WORLD_POOL_CONTAINER_SIZE = 10;

    static final int NEW_FIXTURE = 0x0001;
    private static final int LOCKED = 0x0002;
    private static final int CLEAR_FORCES = 0x0004;

    protected int flags;

    /**
     * -- GETTER --
     * Get the contact manager for testing purposes
     */
    @Getter
    protected ContactManager contactManager;

    private final List<Body> bodyList = new ArrayList<>(128);
    private final List<Joint> jointList = new ArrayList<>();

    @Getter
    private boolean allowSleep;

    /**
     * -- SETTER --
     * Register a destruction listener. The listener is owned by you and must remain in scope.
     */
    @Getter
    @Setter
    private DestructionListener destructionListener;

    @Getter
    private final IWorldPool pool;

    /**
     * This is used to compute the time step ratio to support a variable time step.
     */
    private float inv_dt0;

    /**
     * -- SETTER --
     * Enable/disable continuous physics. For testing.
     */
    @Getter
    @Setter
    private boolean continuousPhysics;
    @Setter
    @Getter
    private boolean subStepping;

    private boolean stepComplete;

    @Getter
    private final Profile profile;

    private final ContactRegister[][] contactStacks =
            new ContactRegister[ShapeType.values().length][ShapeType.values().length];

    /**
     * Construct a world object.
     */
    public World() {
        this(new DefaultWorldPool(WORLD_POOL_SIZE, WORLD_POOL_CONTAINER_SIZE));
    }

    /**
     * Construct a world object.
     */
    public World(IWorldPool pool) {
        this(pool, new DynamicTree());
    }

    public World(IWorldPool pool, BroadPhaseStrategy strategy) {
        this(pool, new DefaultBroadPhaseBuffer(strategy));
    }

    public World(IWorldPool pool, BroadPhase broadPhase) {
        this.pool = pool;
        destructionListener = null;

        continuousPhysics = true;
        subStepping = false;
        stepComplete = true;

        allowSleep = true;

        flags = CLEAR_FORCES;

        inv_dt0 = 0f;

        contactManager = new ContactManager(this, broadPhase);
        profile = new Profile();

        initializeRegisters();
    }

    public void setAllowSleep(boolean flag) {
        if (flag == allowSleep) {
            return;
        }

        allowSleep = flag;
        if (!allowSleep) {
            for (int i = 0; i < bodyList.size(); i++) {
                bodyList.get(i).setAwake(true);
            }
        }
    }

    private void addType(IDynamicStack<Contact> creator, ShapeType type1, ShapeType type2) {
        ContactRegister register = new ContactRegister();
        register.creator = creator;
        register.primary = true;
        contactStacks[type1.ordinal()][type2.ordinal()] = register;

        if (type1 != type2) {
            ContactRegister register2 = new ContactRegister();
            register2.creator = creator;
            register2.primary = false;
            contactStacks[type2.ordinal()][type1.ordinal()] = register2;
        }
    }

    private void initializeRegisters() {
        addType(pool.getCircleContactStack(), ShapeType.CIRCLE, ShapeType.CIRCLE);
        addType(pool.getPolyCircleContactStack(), ShapeType.POLYGON, ShapeType.CIRCLE);
        addType(pool.getPolyContactStack(), ShapeType.POLYGON, ShapeType.POLYGON);
        addType(pool.getEdgeCircleContactStack(), ShapeType.EDGE, ShapeType.CIRCLE);
        addType(pool.getEdgePolyContactStack(), ShapeType.EDGE, ShapeType.POLYGON);
        addType(pool.getChainCircleContactStack(), ShapeType.CHAIN, ShapeType.CIRCLE);
        addType(pool.getChainPolyContactStack(), ShapeType.CHAIN, ShapeType.POLYGON);
    }

    public Contact popContact(Fixture fixtureA, int indexA, Fixture fixtureB, int indexB) {
        final ShapeType type1 = fixtureA.getType();
        final ShapeType type2 = fixtureB.getType();

        final ContactRegister reg = contactStacks[type1.ordinal()][type2.ordinal()];
        if (reg != null) {
            if (reg.primary) {
                Contact c = reg.creator.pop();
                c.init(fixtureA, indexA, fixtureB, indexB);
                return c;
            } else {
                Contact c = reg.creator.pop();
                c.init(fixtureB, indexB, fixtureA, indexA);
                return c;
            }
        } else {
            return null;
        }
    }

    public void pushContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        if (contact.manifold.pointCount > 0 && !fixtureA.isSensor() && !fixtureB.isSensor()) {
            fixtureA.getBody().setAwake(true);
            fixtureB.getBody().setAwake(true);
        }

        ShapeType type1 = fixtureA.getType();
        ShapeType type2 = fixtureB.getType();

        IDynamicStack<Contact> creator = contactStacks[type1.ordinal()][type2.ordinal()].creator;
        creator.push(contact);
    }

    /**
     * Register a contact filter to provide specific control over collision. Otherwise the default
     * filter is used (_defaultFilter). The listener is owned by you and must remain in scope.
     */
    public void setContactFilter(ContactFilter filter) {
        contactManager.contactFilter = filter;
    }

    /**
     * Register a contact event listener. The listener is owned by you and must remain in scope.
     */
    public void setContactListener(ContactListener listener) {
        contactManager.contactListener = listener;
    }

    public void addBody(Body body) {
        bodyList.add(body);
    }

    /**
     * destroy a rigid body given a definition. No reference to the definition is retained. This
     * function is locked during callbacks.
     *
     * @warning This automatically deletes all associated shapes and joints.
     * @warning This function is locked during callbacks.
     */
    public void removeBody(Body body) {
        if (isLocked()) {
            throw new RuntimeException("Can't remove body when world is locked");
        }

        bodyList.remove(body);

        // Delete the attached joints.
        JointEdge je = body.jointList;
        while (je != null) {
            JointEdge je0 = je;
            je = je.next;
            if (destructionListener != null) {
                destructionListener.sayGoodbye(je0.joint);
            }

            destroyJoint(je0.joint);

            body.jointList = je;
        }
        body.jointList = null;

        // Delete the attached contacts.
        while (body.contacts.size() > 0) {
            contactManager.destroy(body.contacts.get(0).contact);
        }

        for (int i = 0; i < body.fixtures.size(); i++) {
            Fixture fixture = body.fixtures.get(i);

            if (destructionListener != null) {
                destructionListener.sayGoodbye(fixture);
            }

            fixture.destroyProxies(contactManager.broadPhase);
        }

        body.fixtures.clear();
    }

    public void removeAllBodies() {
        if (bodyList.isEmpty()) {
            return;
        }

        if (isLocked()) {
            throw new RuntimeException("Can't remove all bodies when world is locked");
        }

        for (int i = 0; i < bodyList.size(); i++) {
            Body body = bodyList.get(i);
            // Delete the attached joints.
            JointEdge je = body.jointList;
            while (je != null) {
                JointEdge je0 = je;
                je = je.next;
                if (destructionListener != null) {
                    destructionListener.sayGoodbye(je0.joint);
                }

                destroyJoint(je0.joint);

                body.jointList = je;
            }

            // Delete the attached contacts.
            while (body.contacts.size() > 0) {
                contactManager.destroy(body.contacts.get(0).contact);
            }

            for (int j = 0; j < body.fixtures.size(); j++) {
                Fixture fixture = body.fixtures.get(j);

                if (destructionListener != null) {
                    destructionListener.sayGoodbye(fixture);
                }

                fixture.destroyProxies(contactManager.broadPhase);
            }

            body.fixtures.clear();
        }

        bodyList.clear();
    }

    /**
     * create a joint to constrain bodies together. No reference to the definition is retained. This
     * may cause the connected bodies to cease colliding.
     *
     * @warning This function is locked during callbacks.
     */
    public Joint createJoint(JointDef def) {
        if (isLocked()) {
            throw new RuntimeException("Can't create joint when world is locked");
        }

        Joint j = Joint.create(this, def);

        // Connect to the world list.
        jointList.add(j);

        // Connect to the bodies' doubly linked lists.
        j.m_edgeA.joint = j;
        j.m_edgeA.other = j.getBodyB();
        j.m_edgeA.prev = null;
        j.m_edgeA.next = j.getBodyA().jointList;
        if (j.getBodyA().jointList != null) {
            j.getBodyA().jointList.prev = j.m_edgeA;
        }
        j.getBodyA().jointList = j.m_edgeA;

        j.m_edgeB.joint = j;
        j.m_edgeB.other = j.getBodyA();
        j.m_edgeB.prev = null;
        j.m_edgeB.next = j.getBodyB().jointList;
        if (j.getBodyB().jointList != null) {
            j.getBodyB().jointList.prev = j.m_edgeB;
        }
        j.getBodyB().jointList = j.m_edgeB;

        Body bodyA = def.bodyA;
        Body bodyB = def.bodyB;

        // If the joint prevents collisions, then flag any contacts for filtering.
        if (!def.collideConnected) {
            for (int i = 0; i < bodyB.contacts.size(); i++) {
                ContactEdge edge = bodyB.contacts.get(i);
                if (edge.other == bodyA) {
                    // Flag the contact for filtering at the next time step (where either
                    // body is awake).
                    edge.contact.flagForFiltering();
                }
            }
        }

        // Note: creating a joint doesn't wake the bodies.

        return j;
    }

    /**
     * destroy a joint. This may cause the connected bodies to begin colliding.
     *
     * @warning This function is locked during callbacks.
     */
    public void destroyJoint(Joint j) {
        if (isLocked()) {
            throw new RuntimeException("Can't remove joint when world is locked");
        }

        if (!jointList.remove(j)) {
            throw new RuntimeException("Can't find joint in world");
        }

        boolean collideConnected = j.getCollideConnected();

        // Disconnect from island graph.
        Body bodyA = j.getBodyA();
        Body bodyB = j.getBodyB();

        // Wake up connected bodies.
        bodyA.setAwake(true);
        bodyB.setAwake(true);

        // Remove from body 1.
        if (j.m_edgeA.prev != null) {
            j.m_edgeA.prev.next = j.m_edgeA.next;
        }

        if (j.m_edgeA.next != null) {
            j.m_edgeA.next.prev = j.m_edgeA.prev;
        }

        if (j.m_edgeA == bodyA.jointList) {
            bodyA.jointList = j.m_edgeA.next;
        }

        j.m_edgeA.prev = null;
        j.m_edgeA.next = null;

        // Remove from body 2
        if (j.m_edgeB.prev != null) {
            j.m_edgeB.prev.next = j.m_edgeB.next;
        }

        if (j.m_edgeB.next != null) {
            j.m_edgeB.next.prev = j.m_edgeB.prev;
        }

        if (j.m_edgeB == bodyB.jointList) {
            bodyB.jointList = j.m_edgeB.next;
        }

        j.m_edgeB.prev = null;
        j.m_edgeB.next = null;

        Joint.destroy(j);

        // If the joint prevents collisions, then flag any contacts for filtering.
        if (!collideConnected) {
            for (int i = 0; i < bodyB.contacts.size(); i++) {
                ContactEdge edge = bodyB.contacts.get(i);
                if (edge.other == bodyA) {
                    // Flag the contact for filtering at the next time step (where either
                    // body is awake).
                    edge.contact.flagForFiltering();
                }
            }
        }
    }

    // djm pooling
    private final TimeStep step = new TimeStep();
    private final Timer stepTimer = new Timer();
    private final Timer tempTimer = new Timer();

    /**
     * Take a time step. This performs collision detection, integration, and constraint solution.
     *
     * @param dt                 the amount of time to simulate, this should not vary.
     * @param velocityIterations for the velocity constraint solver.
     * @param positionIterations for the position constraint solver.
     */
    public void step(float dt, int velocityIterations, int positionIterations) {
        stepTimer.reset();
        tempTimer.reset();
        // log.debug("Starting step");
        // If new fixtures were added, we need to find the new contacts.
        if ((flags & NEW_FIXTURE) == NEW_FIXTURE) {
            // log.debug("There's a new fixture, lets look for new contacts");
            contactManager.findNewContacts();
            flags &= ~NEW_FIXTURE;
        }

        flags |= LOCKED;

        step.dt = dt;
        step.velocityIterations = velocityIterations;
        step.positionIterations = positionIterations;
        if (dt > 0.0f) {
            step.inv_dt = 1.0f / dt;
        } else {
            step.inv_dt = 0.0f;
        }

        step.dtRatio = inv_dt0 * dt;

        step.warmStarting = true;
        profile.stepInit.record(tempTimer.getMilliseconds());

        // Update contacts. This is where some contacts are destroyed.
        tempTimer.reset();
        contactManager.collide();
        profile.collide.record(tempTimer.getMilliseconds());

        // Integrate velocities, solve velocity constraints, and integrate positions.
        if (stepComplete && step.dt > 0.0f) {
            tempTimer.reset();
            profile.solveParticleSystem.record(tempTimer.getMilliseconds());
            tempTimer.reset();
            solve(step);
            profile.solve.record(tempTimer.getMilliseconds());
        }

        // Handle TOI events.
        if (continuousPhysics && step.dt > 0.0f) {
            tempTimer.reset();
            solveTOI(step);
            profile.solveTOI.record(tempTimer.getMilliseconds());
        }

        if (step.dt > 0.0f) {
            inv_dt0 = step.inv_dt;
        }

        if ((flags & CLEAR_FORCES) == CLEAR_FORCES) {
            clearForces();
        }

        flags &= ~LOCKED;
        // log.debug("ending step");

        profile.step.record(stepTimer.getMilliseconds());
    }

    /**
     * Call this after you are done with time steps to clear the forces. You normally call this after
     * each call to Step, unless you are performing sub-steps. By default, forces will be
     * automatically cleared, so you don't need to call this function.
     */
    public void clearForces() {
        for (int i = 0; i < bodyList.size(); i++) {
            Body body = bodyList.get(i);
            body.force.setZero();
            body.torque = 0.0f;
        }
    }

    private final WorldQueryWrapper wqwrapper = new WorldQueryWrapper();

    /**
     * Query the world for all fixtures that potentially overlap the provided AABB.
     *
     * @param callback a user implemented callback class.
     * @param aabb     the query box.
     */
    public void queryAABB(QueryCallback callback, AABB aabb) {
        wqwrapper.broadPhase = contactManager.broadPhase;
        wqwrapper.callback = callback;
        contactManager.broadPhase.query(wqwrapper, aabb);
    }

    /**
     * Query the world for all fixtures and particles that potentially overlap the provided AABB.
     *
     * @param callback         a user implemented callback class.
     * @param particleCallback callback for particles.
     * @param aabb             the query box.
     */
    public void queryAABB(QueryCallback callback, ParticleQueryCallback particleCallback, AABB aabb) {
        wqwrapper.broadPhase = contactManager.broadPhase;
        wqwrapper.callback = callback;
        contactManager.broadPhase.query(wqwrapper, aabb);
    }

    private final WorldRayCastWrapper wrcwrapper = new WorldRayCastWrapper();
    private final RayCastInput input = new RayCastInput();

    /**
     * Ray-cast the world for all fixtures in the path of the ray. Your callback controls whether you
     * get the closest point, any point, or n-points. The ray-cast ignores shapes that contain the
     * starting point.
     *
     * @param callback a user implemented callback class.
     * @param point1   the ray starting point
     * @param point2   the ray ending point
     */
    public void raycast(RayCastCallback callback, Vector2 point1, Vector2 point2) {
        wrcwrapper.broadPhase = contactManager.broadPhase;
        wrcwrapper.callback = callback;
        input.maxFraction = 1.0f;
        input.p1.set(point1);
        input.p2.set(point2);
        contactManager.broadPhase.raycast(wrcwrapper, input);
    }

    /**
     * Ray-cast the world for all fixtures and particles in the path of the ray. Your callback
     * controls whether you get the closest point, any point, or n-points. The ray-cast ignores shapes
     * that contain the starting point.
     *
     * @param callback         a user implemented callback class.
     * @param particleCallback the particle callback class.
     * @param point1           the ray starting point
     * @param point2           the ray ending point
     */
    public void raycast(RayCastCallback callback, ParticleRaycastCallback particleCallback,
                        Vector2 point1, Vector2 point2) {
        wrcwrapper.broadPhase = contactManager.broadPhase;
        wrcwrapper.callback = callback;
        input.maxFraction = 1.0f;
        input.p1.set(point1);
        input.p2.set(point2);
        contactManager.broadPhase.raycast(wrcwrapper, input);
    }

    /**
     * Get the world contact list. With the returned contact, use Contact.getNext to get the next
     * contact in the world list. A null contact indicates the end of the list.
     *
     * @return the head of the world contact list.
     * @warning contacts are created and destroyed in the middle of a time step. Use ContactListener
     * to avoid missing contacts.
     */
    public List<Contact> getContactList() {
        return contactManager.contacts;
    }

    public boolean isSleepingAllowed() {
        return allowSleep;
    }

    public void setSleepingAllowed(boolean sleepingAllowed) {
        allowSleep = sleepingAllowed;
    }

    /**
     * Get the number of broad-phase proxies.
     */
    public int getProxyCount() {
        return contactManager.broadPhase.getProxyCount();
    }

    /**
     * Get the number of bodies.
     */
    public int getBodyCount() {
        return bodyList.size();
    }

    /**
     * Get the number of joints.
     */
    public int getJointCount() {
        return jointList.size();
    }

    /**
     * Get the number of contacts (each may have 0 or more contact points).
     */
    public int getContactCount() {
        return contactManager.contacts.size();
    }

    /**
     * Gets the height of the dynamic tree
     */
    public int getTreeHeight() {
        return contactManager.broadPhase.getTreeHeight();
    }

    /**
     * Gets the balance of the dynamic tree
     */
    public int getTreeBalance() {
        return contactManager.broadPhase.getTreeBalance();
    }

    /**
     * Gets the quality of the dynamic tree
     */
    public float getTreeQuality() {
        return contactManager.broadPhase.getTreeQuality();
    }

    /**
     * Is the world locked (in the middle of a time step).
     */
    public boolean isLocked() {
        return (flags & LOCKED) == LOCKED;
    }

    /**
     * Set flag to control automatic clearing of forces after each time step.
     */
    public void setAutoClearForces(boolean flag) {
        if (flag) {
            flags |= CLEAR_FORCES;
        } else {
            flags &= ~CLEAR_FORCES;
        }
    }

    /**
     * Get the flag that controls automatic clearing of forces after each time step.
     */
    public boolean getAutoClearForces() {
        return (flags & CLEAR_FORCES) == CLEAR_FORCES;
    }

    private final Island island = new Island();
    private Body[] stack = new Body[10]; // TODO djm find a good initial stack number;
    private final Timer broadphaseTimer = new Timer();

    private void solve(TimeStep step) {
        profile.solveInit.startAccum();
        profile.solveVelocity.startAccum();
        profile.solvePosition.startAccum();

        // update previous transforms
        for (int i = 0; i < bodyList.size(); i++) {
            Body body = bodyList.get(i);
            body.xf0.set(body.transform);
        }

        // Size the island for the worst case.
        island.init(bodyList.size(), contactManager.contacts.size(), jointList.size(),
                contactManager.contactListener);

        // Clear all the island flags.
        for (int i = 0; i < bodyList.size(); i++) {
            bodyList.get(i).flags &= ~Body.E_ISLAND_FLAG;
        }
        for (int i = 0; i < contactManager.contacts.size(); i++) {
            Contact c = contactManager.contacts.get(i);
            c.flags &= ~Contact.ISLAND_FLAG;
        }
        for (int i = 0; i < jointList.size(); i++) {
            jointList.get(i).m_islandFlag = false;
        }

        // Build and simulate all awake islands.
        int stackSize = bodyList.size();
        if (stack.length < stackSize) {
            stack = new Body[stackSize];
        }
        for (int i = 0; i < bodyList.size(); i++) {
            Body seed = bodyList.get(i);
            if ((seed.flags & Body.E_ISLAND_FLAG) == Body.E_ISLAND_FLAG) {
                continue;
            }

            if (!seed.isAwake() || !seed.isActive()) {
                continue;
            }

            // The seed can be dynamic or kinematic.
            if (seed.getType() == BodyType.STATIC) {
                continue;
            }

            // Reset island and stack.
            island.clear();
            int stackCount = 0;
            stack[stackCount++] = seed;
            seed.flags |= Body.E_ISLAND_FLAG;

            // Perform a depth first search (DFS) on the constraint graph.
            while (stackCount > 0) {
                // Grab the next body off the stack and add it to the island.
                Body b = stack[--stackCount];
                assert (b.isActive());
                island.add(b);

                // Make sure the body is awake.
                b.setAwake(true);

                // To keep islands as small as possible, we don't
                // propagate islands across static bodies.
                if (b.getType() == BodyType.STATIC) {
                    continue;
                }

                // Search all contacts connected to this body.
                for (int i1 = 0; i1 < b.contacts.size(); i1++) {
                    ContactEdge ce = b.contacts.get(i1);
                    Contact contact = ce.contact;

                    // Has this contact already been added to an island?
                    if ((contact.flags & Contact.ISLAND_FLAG) == Contact.ISLAND_FLAG) {
                        continue;
                    }

                    // Is this contact solid and touching?
                    if (!contact.isEnabled() || !contact.isTouching()) {
                        continue;
                    }

                    // Skip sensors.
                    boolean sensorA = contact.fixtureA.sensor;
                    boolean sensorB = contact.fixtureB.sensor;
                    if (sensorA || sensorB) {
                        continue;
                    }

                    island.add(contact);
                    contact.flags |= Contact.ISLAND_FLAG;

                    Body other = ce.other;

                    // Was the other body already added to this island?
                    if ((other.flags & Body.E_ISLAND_FLAG) == Body.E_ISLAND_FLAG) {
                        continue;
                    }

                    assert (stackCount < stackSize);
                    stack[stackCount++] = other;
                    other.flags |= Body.E_ISLAND_FLAG;
                }

                // Search all joints connect to this body.
                for (JointEdge je = b.jointList; je != null; je = je.next) {
                    if (je.joint.m_islandFlag) {
                        continue;
                    }

                    Body other = je.other;

                    // Don't simulate joints connected to inactive bodies.
                    if (!other.isActive()) {
                        continue;
                    }

                    island.add(je.joint);
                    je.joint.m_islandFlag = true;

                    if ((other.flags & Body.E_ISLAND_FLAG) == Body.E_ISLAND_FLAG) {
                        continue;
                    }

                    assert (stackCount < stackSize);
                    stack[stackCount++] = other;
                    other.flags |= Body.E_ISLAND_FLAG;
                }
            }
            island.solve(profile, step, allowSleep);

            // Post solve cleanup.
            for (int j = 0; j < island.m_bodyCount; ++j) {
                // Allow static bodies to participate in other islands.
                Body b = island.m_bodies[j];
                if (b.getType() == BodyType.STATIC) {
                    b.flags &= ~Body.E_ISLAND_FLAG;
                }
            }
        }
        profile.solveInit.endAccum();
        profile.solveVelocity.endAccum();
        profile.solvePosition.endAccum();

        broadphaseTimer.reset();
        // Synchronize fixtures, check for out of range bodies.
        for (int i = 0; i < bodyList.size(); i++) {
            Body b = bodyList.get(i);
            // If a body was not in an island then it did not move.
            if ((b.flags & Body.E_ISLAND_FLAG) == 0) {
                continue;
            }

            if (b.getType() == BodyType.STATIC) {
                continue;
            }

            // Update fixtures (for broad-phase).
            b.synchronizeFixtures();
        }

        // Look for new contacts.
        contactManager.findNewContacts();
        profile.broadphase.record(broadphaseTimer.getMilliseconds());
    }

    private final Island toiIsland = new Island();
    private final TimeOfImpact.TOIInput toiInput = new TimeOfImpact.TOIInput();
    private final TimeOfImpact.TOIOutput toiOutput = new TimeOfImpact.TOIOutput();
    private final TimeStep subStep = new TimeStep();
    private final Body[] tempBodies = new Body[2];
    private final Sweep backup1 = new Sweep();
    private final Sweep backup2 = new Sweep();

    private void solveTOI(final TimeStep step) {

        final Island island = toiIsland;
        island.init(2 * Settings.maxTOIContacts, Settings.maxTOIContacts, 0,
                contactManager.contactListener);
        if (stepComplete) {
            for (int i = 0; i < bodyList.size(); i++) {
                Body b = bodyList.get(i);
                b.flags &= ~Body.E_ISLAND_FLAG;
                b.sweep.alpha0 = 0.0f;
            }

            for (int i = 0; i < contactManager.contacts.size(); i++) {
                Contact c = contactManager.contacts.get(i);
                // Invalidate TOI
                c.flags &= ~(Contact.TOI_FLAG | Contact.ISLAND_FLAG);
                c.toiCount = 0;
                c.toi = 1.0f;
            }
        }

        // Find TOI events and solve them.
        for (; ; ) {
            // Find the first TOI.
            Contact minContact = null;
            float minAlpha = 1.0f;

            for (int i = 0; i < contactManager.contacts.size(); i++) {
                Contact c = contactManager.contacts.get(i);
                // Is this contact disabled?
                if (!c.isEnabled()) {
                    continue;
                }

                // Prevent excessive sub-stepping.
                if (c.toiCount > Settings.maxSubSteps) {
                    continue;
                }

                float alpha;
                if ((c.flags & Contact.TOI_FLAG) != 0) {
                    // This contact has a valid cached TOI.
                    alpha = c.toi;
                } else {
                    Fixture fA = c.getFixtureA();
                    Fixture fB = c.getFixtureB();

                    // Is there a sensor?
                    if (fA.isSensor() || fB.isSensor()) {
                        continue;
                    }

                    Body bA = fA.getBody();
                    Body bB = fB.getBody();

                    BodyType typeA = bA.type;
                    BodyType typeB = bB.type;
                    assert (typeA == BodyType.DYNAMIC || typeB == BodyType.DYNAMIC);

                    boolean activeA = bA.isAwake() && typeA != BodyType.STATIC;
                    boolean activeB = bB.isAwake() && typeB != BodyType.STATIC;

                    // Is at least one body active (awake and dynamic or kinematic)?
                    if (!activeA && !activeB) {
                        continue;
                    }

                    boolean collideA = bA.isBullet() || typeA != BodyType.DYNAMIC;
                    boolean collideB = bB.isBullet() || typeB != BodyType.DYNAMIC;

                    // Are these two non-bullet dynamic bodies?
                    if (!collideA && !collideB) {
                        continue;
                    }

                    // Compute the TOI for this contact.
                    // Put the sweeps onto the same time interval.
                    float alpha0 = bA.sweep.alpha0;

                    if (bA.sweep.alpha0 < bB.sweep.alpha0) {
                        alpha0 = bB.sweep.alpha0;
                        bA.sweep.advance(alpha0);
                    } else if (bB.sweep.alpha0 < bA.sweep.alpha0) {
                        alpha0 = bA.sweep.alpha0;
                        bB.sweep.advance(alpha0);
                    }

                    assert (alpha0 < 1.0f);

                    int indexA = c.getChildIndexA();
                    int indexB = c.getChildIndexB();

                    // Compute the time of impact in interval [0, minTOI]
                    final TimeOfImpact.TOIInput input = toiInput;
                    input.proxyA.set(fA.getShape(), indexA);
                    input.proxyB.set(fB.getShape(), indexB);
                    input.sweepA.set(bA.sweep);
                    input.sweepB.set(bB.sweep);
                    input.tMax = 1.0f;

                    pool.getTimeOfImpact().timeOfImpact(toiOutput, input);

                    // Beta is the fraction of the remaining portion of the .
                    float beta = toiOutput.t;
                    if (toiOutput.state == TimeOfImpact.TOIOutputState.TOUCHING) {
                        alpha = MathUtils.min(alpha0 + (1.0f - alpha0) * beta, 1.0f);
                    } else {
                        alpha = 1.0f;
                    }

                    c.toi = alpha;
                    c.flags |= Contact.TOI_FLAG;
                }

                if (alpha < minAlpha) {
                    // This is the minimum TOI found so far.
                    minContact = c;
                    minAlpha = alpha;
                }
            }

            if (minContact == null || 1.0f - 10.0f * Settings.EPSILON < minAlpha) {
                // No more TOI events. Done!
                stepComplete = true;
                break;
            }

            // Advance the bodies to the TOI.
            Fixture fA = minContact.getFixtureA();
            Fixture fB = minContact.getFixtureB();
            Body bA = fA.getBody();
            Body bB = fB.getBody();

            backup1.set(bA.sweep);
            backup2.set(bB.sweep);

            bA.advance(minAlpha);
            bB.advance(minAlpha);

            // The TOI contact likely has some new contact points.
            minContact.update(contactManager.contactListener);
            minContact.flags &= ~Contact.TOI_FLAG;
            ++minContact.toiCount;

            // Is the contact solid?
            if (!minContact.isEnabled() || !minContact.isTouching()) {
                // Restore the sweeps.
                minContact.setEnabled(false);
                bA.sweep.set(backup1);
                bB.sweep.set(backup2);
                bA.synchronizeTransform();
                bB.synchronizeTransform();
                continue;
            }

            bA.setAwake(true);
            bB.setAwake(true);

            // Build the island
            island.clear();
            island.add(bA);
            island.add(bB);
            island.add(minContact);

            bA.flags |= Body.E_ISLAND_FLAG;
            bB.flags |= Body.E_ISLAND_FLAG;
            minContact.flags |= Contact.ISLAND_FLAG;

            // Get contacts on bodyA and bodyB.
            tempBodies[0] = bA;
            tempBodies[1] = bB;
            for (int i = 0; i < 2; ++i) {
                Body body = tempBodies[i];
                if (body.type == BodyType.DYNAMIC) {
                    for (int i1 = 0; i1 < body.contacts.size(); i1++) {
                        ContactEdge ce = body.contacts.get(i1);
                        if (island.m_bodyCount == island.m_bodyCapacity) {
                            break;
                        }

                        if (island.m_contactCount == island.m_contactCapacity) {
                            break;
                        }

                        Contact contact = ce.contact;

                        // Has this contact already been added to the island?
                        if ((contact.flags & Contact.ISLAND_FLAG) != 0) {
                            continue;
                        }

                        // Only add static, kinematic, or bullet bodies.
                        Body other = ce.other;
                        if (other.type == BodyType.DYNAMIC && !body.isBullet()
                                && !other.isBullet()) {
                            continue;
                        }

                        // Skip sensors.
                        boolean sensorA = contact.fixtureA.sensor;
                        boolean sensorB = contact.fixtureB.sensor;
                        if (sensorA || sensorB) {
                            continue;
                        }

                        // Tentatively advance the body to the TOI.
                        backup1.set(other.sweep);
                        if ((other.flags & Body.E_ISLAND_FLAG) == 0) {
                            other.advance(minAlpha);
                        }

                        // Update the contact points
                        contact.update(contactManager.contactListener);

                        // Was the contact disabled by the user?
                        if (!contact.isEnabled()) {
                            other.sweep.set(backup1);
                            other.synchronizeTransform();
                            continue;
                        }

                        // Are there contact points?
                        if (!contact.isTouching()) {
                            other.sweep.set(backup1);
                            other.synchronizeTransform();
                            continue;
                        }

                        // Add the contact to the island
                        contact.flags |= Contact.ISLAND_FLAG;
                        island.add(contact);

                        // Has the other body already been added to the island?
                        if ((other.flags & Body.E_ISLAND_FLAG) != 0) {
                            continue;
                        }

                        // Add the other body to the island.
                        other.flags |= Body.E_ISLAND_FLAG;

                        if (other.type != BodyType.STATIC) {
                            other.setAwake(true);
                        }

                        island.add(other);
                    }
                }
            }

            subStep.dt = (1.0f - minAlpha) * step.dt;
            subStep.inv_dt = 1.0f / subStep.dt;
            subStep.dtRatio = 1.0f;
            subStep.positionIterations = 20;
            subStep.velocityIterations = step.velocityIterations;
            subStep.warmStarting = false;
            island.solveTOI(subStep, bA.islandIndex, bB.islandIndex);

            // Reset island flags and synchronize broad-phase proxies.
            for (int i = 0; i < island.m_bodyCount; ++i) {
                Body body = island.m_bodies[i];
                body.flags &= ~Body.E_ISLAND_FLAG;

                if (body.type != BodyType.DYNAMIC) {
                    continue;
                }

                body.synchronizeFixtures();

                // Invalidate all contact TOIs on this displaced body.
                for (int i1 = 0; i1 < body.contacts.size(); i1++) {
                    ContactEdge ce = body.contacts.get(i1);
                    ce.contact.flags &= ~(Contact.TOI_FLAG | Contact.ISLAND_FLAG);
                }
            }

            // Commit fixture proxy movements to the broad-phase so that new contacts are created.
            // Also, some contacts can be destroyed.
            contactManager.findNewContacts();

            if (subStepping) {
                stepComplete = false;
                break;
            }
        }
    }
}

class WorldQueryWrapper implements TreeCallback {
    @Override
    public boolean treeCallback(int proxyId) {
        FixtureProxy proxy = (FixtureProxy) broadPhase.getUserData(proxyId);
        return callback.reportFixture(proxy.fixture);
    }

    BroadPhase broadPhase;
    QueryCallback callback;
}

class WorldRayCastWrapper implements TreeRayCastCallback {

    // djm pooling
    private final RayCastOutput output = new RayCastOutput();
    private final Vector2 temp = new Vector2();
    private final Vector2 point = new Vector2();

    @Override
    public float raycastCallback(RayCastInput input, int nodeId) {
        Object userData = broadPhase.getUserData(nodeId);
        FixtureProxy proxy = (FixtureProxy) userData;
        Fixture fixture = proxy.fixture;
        int index = proxy.childIndex;
        boolean hit = fixture.raycast(output, input, index);

        if (hit) {
            float fraction = output.fraction;
            // Vec2 point = (1.0f - fraction) * input.p1 + fraction * input.p2;
            temp.set(input.p2).mulLocal(fraction);
            point.set(input.p1).mulLocal(1 - fraction).addLocal(temp);
            return callback.reportFixture(fixture, point, output.normal, fraction);
        }

        return input.maxFraction;
    }

    BroadPhase broadPhase;
    RayCastCallback callback;
}
