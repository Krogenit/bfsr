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
 * Created at 3:26:14 AM Jan 11, 2011
 * <p>
 * Created at 3:26:14 AM Jan 11, 2011
 */
/**
 * Created at 3:26:14 AM Jan 11, 2011
 */
package org.jbox2d.pooling.normal;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.Collision;
import org.jbox2d.collision.Distance;
import org.jbox2d.collision.TimeOfImpact;
import org.jbox2d.common.Mat22;
import org.jbox2d.common.Mat33;
import org.jbox2d.common.Rotation;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Vector2;
import org.jbox2d.common.Vector3;
import org.jbox2d.dynamics.contacts.ChainAndCircleContact;
import org.jbox2d.dynamics.contacts.ChainAndPolygonContact;
import org.jbox2d.dynamics.contacts.CircleContact;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.contacts.EdgeAndCircleContact;
import org.jbox2d.dynamics.contacts.EdgeAndPolygonContact;
import org.jbox2d.dynamics.contacts.PolygonAndCircleContact;
import org.jbox2d.dynamics.contacts.PolygonContact;
import org.jbox2d.pooling.IDynamicStack;
import org.jbox2d.pooling.IWorldPool;

import java.util.HashMap;

/**
 * Provides object pooling for all objects used in the engine. Objects retrieved from here should
 * only be used temporarily, and then pushed back (with the exception of arrays).
 *
 * @author Daniel Murphy
 */
public class DefaultWorldPool implements IWorldPool {

    private final OrderedStack<Vector2> vecs;
    private final OrderedStack<Vector3> vec3s;
    private final OrderedStack<Mat22> mats;
    private final OrderedStack<Mat33> mat33s;
    private final OrderedStack<AABB> aabbs;
    private final OrderedStack<Rotation> rots;

    private final HashMap<Integer, float[]> afloats = new HashMap<>();
    private final HashMap<Integer, int[]> aints = new HashMap<>();
    private final HashMap<Integer, Vector2[]> avecs = new HashMap<>();

    private final IWorldPool world = this;

    private final MutableStack<Contact> pcstack =
            new MutableStack<>(Settings.CONTACT_STACK_INIT_SIZE) {
                protected Contact newInstance() {return new PolygonContact(world);}

                protected Contact[] newArray(int size) {return new PolygonContact[size];}
            };

    private final MutableStack<Contact> ccstack =
            new MutableStack<>(Settings.CONTACT_STACK_INIT_SIZE) {
                protected Contact newInstance() {return new CircleContact(world);}

                protected Contact[] newArray(int size) {return new CircleContact[size];}
            };

    private final MutableStack<Contact> cpstack =
            new MutableStack<>(Settings.CONTACT_STACK_INIT_SIZE) {
                protected Contact newInstance() {return new PolygonAndCircleContact(world);}

                protected Contact[] newArray(int size) {return new PolygonAndCircleContact[size];}
            };

    private final MutableStack<Contact> ecstack =
            new MutableStack<>(Settings.CONTACT_STACK_INIT_SIZE) {
                protected Contact newInstance() {return new EdgeAndCircleContact(world);}

                protected Contact[] newArray(int size) {return new EdgeAndCircleContact[size];}
            };

    private final MutableStack<Contact> epstack =
            new MutableStack<>(Settings.CONTACT_STACK_INIT_SIZE) {
                protected Contact newInstance() {return new EdgeAndPolygonContact(world);}

                protected Contact[] newArray(int size) {return new EdgeAndPolygonContact[size];}
            };

    private final MutableStack<Contact> chcstack =
            new MutableStack<>(Settings.CONTACT_STACK_INIT_SIZE) {
                protected Contact newInstance() {return new ChainAndCircleContact(world);}

                protected Contact[] newArray(int size) {return new ChainAndCircleContact[size];}
            };

    private final MutableStack<Contact> chpstack =
            new MutableStack<>(Settings.CONTACT_STACK_INIT_SIZE) {
                protected Contact newInstance() {return new ChainAndPolygonContact(world);}

                protected Contact[] newArray(int size) {return new ChainAndPolygonContact[size];}
            };

    private final Collision collision;
    private final TimeOfImpact toi;
    private final Distance dist;

    public DefaultWorldPool(int argSize, int argContainerSize) {
        vecs = new OrderedStack<>(argSize, argContainerSize) {
            protected Vector2 newInstance() {return new Vector2();}
        };
        vec3s = new OrderedStack<>(argSize, argContainerSize) {
            protected Vector3 newInstance() {return new Vector3();}
        };
        mats = new OrderedStack<>(argSize, argContainerSize) {
            protected Mat22 newInstance() {return new Mat22();}
        };
        aabbs = new OrderedStack<>(argSize, argContainerSize) {
            protected AABB newInstance() {return new AABB();}
        };
        rots = new OrderedStack<>(argSize, argContainerSize) {
            protected Rotation newInstance() {return new Rotation();}
        };
        mat33s = new OrderedStack<>(argSize, argContainerSize) {
            protected Mat33 newInstance() {return new Mat33();}
        };

        dist = new Distance();
        collision = new Collision(this);
        toi = new TimeOfImpact(this);
    }

    public final IDynamicStack<Contact> getPolyContactStack() {
        return pcstack;
    }

    public final IDynamicStack<Contact> getCircleContactStack() {
        return ccstack;
    }

    public final IDynamicStack<Contact> getPolyCircleContactStack() {
        return cpstack;
    }

    @Override
    public IDynamicStack<Contact> getEdgeCircleContactStack() {
        return ecstack;
    }

    @Override
    public IDynamicStack<Contact> getEdgePolyContactStack() {
        return epstack;
    }

    @Override
    public IDynamicStack<Contact> getChainCircleContactStack() {
        return chcstack;
    }

    @Override
    public IDynamicStack<Contact> getChainPolyContactStack() {
        return chpstack;
    }

    public final Vector2 popVec2() {
        return vecs.pop();
    }

    public final Vector2[] popVec2(int argNum) {
        return vecs.pop(argNum);
    }

    public final void pushVec2(int argNum) {
        vecs.push(argNum);
    }

    public final Vector3 popVec3() {
        return vec3s.pop();
    }

    public final Vector3[] popVec3(int argNum) {
        return vec3s.pop(argNum);
    }

    public final void pushVec3(int argNum) {
        vec3s.push(argNum);
    }

    public final Mat22 popMat22() {
        return mats.pop();
    }

    public final Mat22[] popMat22(int argNum) {
        return mats.pop(argNum);
    }

    public final void pushMat22(int argNum) {
        mats.push(argNum);
    }

    public final Mat33 popMat33() {
        return mat33s.pop();
    }

    public final void pushMat33(int argNum) {
        mat33s.push(argNum);
    }

    public final AABB popAABB() {
        return aabbs.pop();
    }

    public final AABB[] popAABB(int argNum) {
        return aabbs.pop(argNum);
    }

    public final void pushAABB(int argNum) {
        aabbs.push(argNum);
    }

    public final Rotation popRot() {
        return rots.pop();
    }

    public final void pushRot(int num) {
        rots.push(num);
    }

    public final Collision getCollision() {
        return collision;
    }

    public final TimeOfImpact getTimeOfImpact() {
        return toi;
    }

    public final Distance getDistance() {
        return dist;
    }

    public final float[] getFloatArray(int argLength) {
        if (!afloats.containsKey(argLength)) {
            afloats.put(argLength, new float[argLength]);
        }

        assert (afloats.get(argLength).length == argLength) : "Array not built with correct length";
        return afloats.get(argLength);
    }

    public final int[] getIntArray(int argLength) {
        if (!aints.containsKey(argLength)) {
            aints.put(argLength, new int[argLength]);
        }

        assert (aints.get(argLength).length == argLength) : "Array not built with correct length";
        return aints.get(argLength);
    }

    public final Vector2[] getVec2Array(int argLength) {
        if (!avecs.containsKey(argLength)) {
            Vector2[] ray = new Vector2[argLength];
            for (int i = 0; i < argLength; i++) {
                ray[i] = new Vector2();
            }
            avecs.put(argLength, ray);
        }

        assert (avecs.get(argLength).length == argLength) : "Array not built with correct length";
        return avecs.get(argLength);
    }
}
