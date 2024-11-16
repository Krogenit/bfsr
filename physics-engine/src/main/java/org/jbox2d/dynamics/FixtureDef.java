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
import org.jbox2d.collision.shapes.Shape;

/**
 * A fixture definition is used to create a fixture. This class defines an abstract fixture
 * definition. You can reuse fixture definitions safely.
 *
 * @author daniel
 */
public class FixtureDef {
    /**
     * The shape, this must be set. The shape will be cloned, so you can create the shape on the
     * stack.
     * -- GETTER --
     * The shape, this must be set. The shape will be cloned, so you can create the shape on the
     * stack.
     * -- SETTER --
     * The shape, this must be set. The shape will be cloned, so you can create the shape on the
     * stack.
     */
    @Setter
    @Getter
    public Shape shape;

    /**
     * Use this to store application specific fixture data.
     * -- GETTER --
     * Use this to store application specific fixture data.
     * -- SETTER --
     * Use this to store application specific fixture data.
     */
    @Setter
    @Getter
    public Object userData;

    /**
     * The friction coefficient, usually in the range [0,1].
     * -- GETTER --
     * The friction coefficient, usually in the range [0,1].
     * -- SETTER --
     * The friction coefficient, usually in the range [0,1].
     */
    @Setter
    @Getter
    public float friction;

    /**
     * The restitution (elasticity) usually in the range [0,1].
     * -- GETTER --
     * The restitution (elasticity) usually in the range [0,1].
     * -- SETTER --
     * The restitution (elasticity) usually in the range [0,1].
     */
    @Setter
    @Getter
    public float restitution;

    /**
     * The density, usually in kg/m^2
     * -- GETTER --
     * The density, usually in kg/m^2
     * -- SETTER --
     * The density, usually in kg/m^2
     */
    @Setter
    @Getter
    public float density;

    /**
     * A sensor shape collects contact information but never generates a collision response.
     */
    public boolean isSensor;

    /**
     * Contact filtering data;
     * -- GETTER --
     * Contact filtering data;
     * -- SETTER --
     * Contact filtering data;
     */
    @Setter
    @Getter
    public Filter filter;

    public FixtureDef() {
        shape = null;
        userData = null;
        friction = 0.2f;
        restitution = 0f;
        density = 0f;
        filter = new Filter();
        isSensor = false;
    }

    /**
     * A sensor shape collects contact information but never generates a collision response.
     */
    public boolean isSensor() {
        return isSensor;
    }

    /**
     * A sensor shape collects contact information but never generates a collision response.
     */
    public void setSensor(boolean isSensor) {
        this.isSensor = isSensor;
    }
}
