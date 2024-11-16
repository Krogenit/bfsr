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
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.RayCastInput;
import org.jbox2d.collision.RayCastOutput;
import org.jbox2d.collision.broadphase.BroadPhase;
import org.jbox2d.collision.shapes.MassData;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vector2;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.contacts.ContactEdge;

import java.util.List;

/**
 * A fixture is used to attach a shape to a body for collision detection. A fixture inherits its
 * transform from its parent. Fixtures hold additional non-geometric data such as friction,
 * collision filters, etc. Fixtures are created via Body::CreateFixture.
 *
 * @author daniel
 * @warning you cannot reuse fixtures.
 */
public class Fixture {

    @Getter
    public float density;
    @Getter
    public Body body;
    @Getter
    public Shape shape;
    @Setter
    @Getter
    public float friction;
    @Setter
    @Getter
    public float restitution;

    public FixtureProxy[] proxies;
    public int proxyCount;
    @Getter
    public final Filter filter = new Filter();
    @Getter
    public boolean sensor;
    @Setter
    @Getter
    public Object userData;

    public Fixture(Shape shape, Filter filter, Object userData, float density) {
        this.shape = shape;

        if (filter != null) {
            this.filter.set(filter);
        }

        this.userData = userData;
        this.density = density;

        // Reserve proxy space
        int childCount = shape.getChildCount();
        if (proxies == null) {
            proxies = new FixtureProxy[childCount];
            for (int i = 0; i < childCount; i++) {
                proxies[i] = new FixtureProxy();
                proxies[i].fixture = null;
                proxies[i].proxyId = BroadPhase.NULL_PROXY;
            }
        }
    }

    public Fixture(Shape shape) {
        this(shape, null, null, 0.0f);
    }

    /**
     * Get the type of the child shape. You can use this to down cast to the concrete shape.
     *
     * @return the shape type.
     */
    public ShapeType getType() {
        return shape.getType();
    }

    /**
     * Set if this fixture is a sensor.
     */
    public void setSensor(boolean sensor) {
        if (sensor != this.sensor) {
            this.sensor = sensor;
        }
    }

    /**
     * Set the contact filtering data. This is an expensive operation and should not be called
     * frequently. This will not update contacts until the next time step when either parent body is
     * awake. This automatically calls refilter.
     */
    public void setFilter(final Filter filter) {
        this.filter.set(filter);

        refilter();
    }

    /**
     * Call this if you want to establish collision that was previously disabled by
     * ContactFilter::ShouldCollide.
     */
    public void refilter() {
        if (body == null) {
            return;
        }

        // Flag associated contacts for filtering.
        List<ContactEdge> contacts = body.getContacts();
        for (int i = 0; i < contacts.size(); i++) {
            ContactEdge edge = contacts.get(i);
            Contact contact = edge.contact;
            Fixture fixtureA = contact.getFixtureA();
            Fixture fixtureB = contact.getFixtureB();
            if (fixtureA == this || fixtureB == this) {
                contact.flagForFiltering();
            }
        }

        World world = body.getWorld();

        if (world == null) {
            return;
        }

        // Touch each proxy so that new pairs may be created
        BroadPhase broadPhase = world.contactManager.broadPhase;
        for (int i = 0; i < proxyCount; ++i) {
            broadPhase.touchProxy(proxies[i].proxyId);
        }
    }

    public void setDensity(float density) {
        assert (density >= 0f);
        this.density = density;
    }

    /**
     * Test a point for containment in this fixture. This only works for convex shapes.
     *
     * @param p a point in world coordinates.
     */
    public boolean testPoint(final Vector2 p) {
        return shape.testPoint(body.transform, p);
    }

    /**
     * Cast a ray against this shape.
     *
     * @param output the ray-cast results.
     * @param input  the ray-cast input parameters.
     */
    public boolean raycast(RayCastOutput output, RayCastInput input, int childIndex) {
        return shape.raycast(output, input, body.transform, childIndex);
    }

    /**
     * Get the mass data for this fixture. The mass data is based on the density and the shape. The
     * rotational inertia is about the shape's origin.
     */
    public void getMassData(MassData massData) {
        shape.computeMass(massData, density);
    }

    /**
     * Get the fixture's AABB. This AABB may be enlarged and/or stale. If you need a more accurate
     * AABB, compute it using the shape and the body transform.
     */
    public AABB getAABB(int childIndex) {
        assert (childIndex >= 0 && childIndex < proxyCount);
        return proxies[childIndex].aabb;
    }

    /**
     * Compute the distance from this fixture.
     *
     * @param p a point in world coordinates.
     * @return distance
     */
    public float computeDistance(Vector2 p, int childIndex, Vector2 normalOut) {
        return shape.computeDistanceToOut(body.getTransform(), p, childIndex, normalOut);
    }

    // We need separation create/destroy functions from the constructor/destructor because
    // the destructor cannot access the allocator (no destructor arguments allowed by C++).

    public void create(FixtureDef def) {
        userData = def.userData;
        friction = def.friction;
        restitution = def.restitution;

        filter.set(def.filter);

        sensor = def.isSensor;

        shape = def.shape.clone();

        // Reserve proxy space
        int childCount = shape.getChildCount();
        if (proxies == null) {
            proxies = new FixtureProxy[childCount];
            for (int i = 0; i < childCount; i++) {
                proxies[i] = new FixtureProxy();
                proxies[i].fixture = null;
                proxies[i].proxyId = BroadPhase.NULL_PROXY;
            }
        }

        if (proxies.length < childCount) {
            FixtureProxy[] old = proxies;
            int newLen = MathUtils.max(old.length << 1, childCount);
            proxies = new FixtureProxy[newLen];
            System.arraycopy(old, 0, proxies, 0, old.length);
            for (int i = 0; i < newLen; i++) {
                if (i >= old.length) {
                    proxies[i] = new FixtureProxy();
                }
                proxies[i].fixture = null;
                proxies[i].proxyId = BroadPhase.NULL_PROXY;
            }
        }
        proxyCount = 0;

        density = def.density;
    }

    // These support body activation/deactivation.
    public void createProxies(BroadPhase broadPhase, final Transform xf) {
        assert (proxyCount == 0);

        // Create proxies in the broad-phase.
        proxyCount = shape.getChildCount();

        for (int i = 0; i < proxyCount; ++i) {
            FixtureProxy proxy = proxies[i];
            shape.computeAABB(proxy.aabb, xf, i);
            proxy.proxyId = broadPhase.createProxy(proxy.aabb, proxy);
            proxy.fixture = this;
            proxy.childIndex = i;
        }
    }

    /**
     * Internal method
     */
    public void destroyProxies(BroadPhase broadPhase) {
        // Destroy proxies in the broad-phase.
        for (int i = 0; i < proxyCount; ++i) {
            FixtureProxy proxy = proxies[i];
            broadPhase.destroyProxy(proxy.proxyId);
            proxy.proxyId = BroadPhase.NULL_PROXY;
        }

        proxyCount = 0;
    }

    private final AABB pool1 = new AABB();
    private final AABB pool2 = new AABB();
    private final Vector2 displacement = new Vector2();

    /**
     * Internal method
     */
    protected void synchronize(BroadPhase broadPhase, final Transform transform1,
                               final Transform transform2) {
        if (proxyCount == 0) {
            return;
        }

        for (int i = 0; i < proxyCount; ++i) {
            FixtureProxy proxy = proxies[i];

            // Compute an AABB that covers the swept shape (may miss some rotation effect).
            final AABB aabb1 = pool1;
            final AABB aab = pool2;
            shape.computeAABB(aabb1, transform1, proxy.childIndex);
            shape.computeAABB(aab, transform2, proxy.childIndex);

            proxy.aabb.lowerBound.x =
                    aabb1.lowerBound.x < aab.lowerBound.x ? aabb1.lowerBound.x : aab.lowerBound.x;
            proxy.aabb.lowerBound.y =
                    aabb1.lowerBound.y < aab.lowerBound.y ? aabb1.lowerBound.y : aab.lowerBound.y;
            proxy.aabb.upperBound.x =
                    aabb1.upperBound.x > aab.upperBound.x ? aabb1.upperBound.x : aab.upperBound.x;
            proxy.aabb.upperBound.y =
                    aabb1.upperBound.y > aab.upperBound.y ? aabb1.upperBound.y : aab.upperBound.y;
            displacement.x = transform2.position.x - transform1.position.x;
            displacement.y = transform2.position.y - transform1.position.y;

            broadPhase.moveProxy(proxy.proxyId, proxy.aabb, displacement);
        }
    }
}
