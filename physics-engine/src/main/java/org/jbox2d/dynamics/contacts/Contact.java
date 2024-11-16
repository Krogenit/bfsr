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
package org.jbox2d.dynamics.contacts;

import lombok.Getter;
import lombok.Setter;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.ContactID;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.ManifoldPoint;
import org.jbox2d.collision.WorldManifold;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Transform;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.pooling.IWorldPool;

/**
 * The class manages contact between two shapes. A contact exists for each overlapping AABB in the
 * broad-phase (except if filtered). Therefore a contact object may exist that has no contact
 * points.
 *
 * @author daniel
 */
public abstract class Contact {
    // Flags stored in m_flags
    // Used when crawling contact graph when forming islands.
    public static final int ISLAND_FLAG = 0x0001;
    // Set when the shapes are touching.
    public static final int TOUCHING_FLAG = 0x0002;
    // This contact can be disabled (by user)
    public static final int ENABLED_FLAG = 0x0004;
    // This contact needs filtering because a fixture filter was changed.
    public static final int FILTER_FLAG = 0x0008;
    // This bullet contact had a TOI event
    public static final int BULLET_HIT_FLAG = 0x0010;

    public static final int TOI_FLAG = 0x0020;

    public int flags;

    // Nodes for connecting bodies.
    public ContactEdge nodeA;
    public ContactEdge nodeB;

    @Getter
    public Fixture fixtureA;
    @Getter
    public Fixture fixtureB;

    public int indexA;
    public int indexB;

    @Getter
    public final Manifold manifold;

    public float toiCount;
    public float toi;

    @Getter
    @Setter
    public float friction;
    @Getter
    @Setter
    public float restitution;

    @Getter
    @Setter
    public float tangentSpeed;

    protected final IWorldPool pool;

    protected Contact(IWorldPool argPool) {
        fixtureA = null;
        fixtureB = null;
        nodeA = new ContactEdge();
        nodeB = new ContactEdge();
        manifold = new Manifold();
        pool = argPool;
    }

    /**
     * initialization for pooling
     */
    public void init(Fixture fixtureA, int indexA, Fixture fixtureB, int indexB) {
        flags = ENABLED_FLAG;

        this.fixtureA = fixtureA;
        this.fixtureB = fixtureB;

        this.indexA = indexA;
        this.indexB = indexB;

        manifold.pointCount = 0;

        nodeA.contact = null;

        nodeA.other = null;

        nodeB.contact = null;
        nodeB.other = null;

        toiCount = 0;
        friction = mixFriction(fixtureA.friction, fixtureB.friction);
        restitution = mixRestitution(fixtureA.restitution, fixtureB.restitution);

        tangentSpeed = 0;
    }

    /**
     * Get the world manifold.
     */
    public void getWorldManifold(WorldManifold worldManifold) {
        final Body bodyA = fixtureA.getBody();
        final Body bodyB = fixtureB.getBody();
        final Shape shapeA = fixtureA.getShape();
        final Shape shapeB = fixtureB.getShape();

        worldManifold.initialize(manifold, bodyA.getTransform(), shapeA.radius,
                bodyB.getTransform(), shapeB.radius);
    }

    /**
     * Is this contact touching
     */
    public boolean isTouching() {
        return (flags & TOUCHING_FLAG) == TOUCHING_FLAG;
    }

    /**
     * Enable/disable this contact. This can be used inside the pre-solve contact listener. The
     * contact is only disabled for the current time step (or sub-step in continuous collisions).
     */
    public void setEnabled(boolean flag) {
        if (flag) {
            flags |= ENABLED_FLAG;
        } else {
            flags &= ~ENABLED_FLAG;
        }
    }

    /**
     * Has this contact been disabled?
     */
    public boolean isEnabled() {
        return (flags & ENABLED_FLAG) == ENABLED_FLAG;
    }

    public int getChildIndexA() {
        return indexA;
    }

    public int getChildIndexB() {
        return indexB;
    }

    public void resetFriction() {
        friction = mixFriction(fixtureA.friction, fixtureB.friction);
    }

    public void resetRestitution() {
        restitution = mixRestitution(fixtureA.restitution, fixtureB.restitution);
    }

    public abstract void evaluate(Manifold manifold, Transform xfA, Transform xfB);

    /**
     * Flag this contact for filtering. Filtering will occur the next time step.
     */
    public void flagForFiltering() {
        flags |= FILTER_FLAG;
    }

    // djm pooling
    private final Manifold oldManifold = new Manifold();

    public void update(ContactListener listener) {

        oldManifold.set(manifold);

        // Re-enable this contact.
        flags |= ENABLED_FLAG;

        boolean touching;
        boolean wasTouching = (flags & TOUCHING_FLAG) == TOUCHING_FLAG;

        boolean sensorA = fixtureA.isSensor();
        boolean sensorB = fixtureB.isSensor();
        boolean sensor = sensorA || sensorB;

        Body bodyA = fixtureA.getBody();
        Body bodyB = fixtureB.getBody();
        Transform xfA = bodyA.getTransform();
        Transform xfB = bodyB.getTransform();
        // log.debug("TransformA: "+xfA);
        // log.debug("TransformB: "+xfB);

        if (sensor) {
            Shape shapeA = fixtureA.getShape();
            Shape shapeB = fixtureB.getShape();
            touching = pool.getCollision().testOverlap(shapeA, indexA, shapeB, indexB, xfA, xfB);

            // Sensors don't generate manifolds.
            manifold.pointCount = 0;
        } else {
            evaluate(manifold, xfA, xfB);
            touching = manifold.pointCount > 0;

            // Match old contact ids to new contact ids and copy the
            // stored impulses to warm start the solver.
            for (int i = 0; i < manifold.pointCount; ++i) {
                ManifoldPoint mp2 = manifold.points[i];
                mp2.normalImpulse = 0.0f;
                mp2.tangentImpulse = 0.0f;
                ContactID id2 = mp2.id;

                for (int j = 0; j < oldManifold.pointCount; ++j) {
                    ManifoldPoint mp1 = oldManifold.points[j];

                    if (mp1.id.isEqual(id2)) {
                        mp2.normalImpulse = mp1.normalImpulse;
                        mp2.tangentImpulse = mp1.tangentImpulse;
                        break;
                    }
                }
            }

            if (touching != wasTouching) {
                bodyA.setAwake(true);
                bodyB.setAwake(true);
            }
        }

        if (touching) {
            flags |= TOUCHING_FLAG;
        } else {
            flags &= ~TOUCHING_FLAG;
        }

        if (listener == null) {
            return;
        }

        if (!wasTouching && touching) {
            listener.beginContact(this);
        }

        if (wasTouching && !touching) {
            listener.endContact(this);
        }

        if (!sensor && touching) {
            listener.preSolve(this, oldManifold);
        }
    }

    /**
     * Friction mixing law. The idea is to allow either fixture to drive the restitution to zero. For
     * example, anything slides on ice.
     */
    public static final float mixFriction(float friction1, float friction2) {
        return (friction1 + friction2) / 2.0f;
    }

    /**
     * Restitution mixing law. The idea is allow for anything to bounce off an inelastic surface. For
     * example, a superball bounces on anything.
     */
    public static final float mixRestitution(float restitution1, float restitution2) {
        return restitution1 > restitution2 ? restitution1 : restitution2;
    }
}
