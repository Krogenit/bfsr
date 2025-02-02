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
package org.jbox2d.collision.shapes;

import lombok.Getter;
import lombok.Setter;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.RayCastInput;
import org.jbox2d.collision.RayCastOutput;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vector2;

/**
 * A shape is used for collision detection. You can create a shape however you like. Shapes used for
 * simulation in World are created automatically when a Fixture is created. Shapes may encapsulate a
 * one or more child shapes.
 */
@Getter
public abstract class Shape {
    /**
     * -- GETTER --
     * Get the type of this shape. You can use this to down cast to the concrete shape.
     */
    public final ShapeType type;
    /**
     * -- GETTER --
     * The radius of the underlying shape. This can refer to different things depending on the shape
     * implementation
     * -- SETTER --
     * Sets the radius of the underlying shape. This can refer to different things depending on the
     * implementation
     */
    @Setter
    public float radius;

    public Shape(ShapeType type) {
        this.type = type;
    }

    /**
     * Get the number of child primitives
     */
    public abstract int getChildCount();

    /**
     * Test a point for containment in this shape. This only works for convex shapes.
     *
     * @param xf the shape world transform.
     * @param p  a point in world coordinates.
     */
    public abstract boolean testPoint(final Transform xf, final Vector2 p);

    /**
     * Cast a ray against a child shape.
     *
     * @param output     the ray-cast results.
     * @param input      the ray-cast input parameters.
     * @param transform  the transform to be applied to the shape.
     * @param childIndex the child shape index
     * @return if hit
     */
    public abstract boolean raycast(RayCastOutput output, RayCastInput input, Transform transform,
                                    int childIndex);

    /**
     * Given a transform, compute the associated axis aligned bounding box for a child shape.
     *
     * @param aabb      returns the axis aligned box.
     * @param transform the world transform of the shape.
     */
    public abstract void computeAABB(final AABB aabb, final Transform transform, int childIndex);

    /**
     * Compute the mass properties of this shape using its dimensions and density. The inertia tensor
     * is computed about the local origin.
     *
     * @param massData returns the mass data for this shape.
     * @param density  the density in kilograms per meter squared.
     */
    public abstract void computeMass(final MassData massData, final float density);

    /**
     * Compute the distance from the current shape to the specified point. This only works for convex
     * shapes.
     *
     * @param xf        the shape world transform.
     * @param p         a point in world coordinates.
     * @param normalOut returns the direction in which the distance increases.
     * @return distance returns the distance from the current shape.
     */
    public abstract float computeDistanceToOut(Transform xf, Vector2 p, int childIndex, Vector2 normalOut);

    public abstract Shape clone();
}
