/*
 * Copyright (c) 2010-2020 William Bittle  http://www.dyn4j.org/
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions
 *     and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *     and the following disclaimer in the documentation and/or other materials provided with the
 *     distribution.
 *   * Neither the name of the copyright holder nor the names of its contributors may be used to endorse or
 *     promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dyn4j.geometry.decompose;

/**
 * Represents a vertex of a monotone polygon.
 *
 * @param <E> the vertex data type
 * @author William Bittle
 * @version 3.2.0
 * @since 2.2.0
 */
final class MonotoneVertex<E> {
    /**
     * The vertex data
     */
    final E data;

    /**
     * The next vertex in CCW winding
     */
    MonotoneVertex<E> next;

    /**
     * The previous vertex in CCW winding
     */
    MonotoneVertex<E> previous;

    /**
     * The monotone chain type indicator
     */
    MonotoneChainType chainType;

    /**
     * Minimal constructor.
     *
     * @param data the vertex data
     */
    public MonotoneVertex(E data) {
        this.data = data;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.data.toString();
    }

    /**
     * Returns true if the given vertex is adjacent to this vertex.
     *
     * @param vertex the vertex to test
     * @return boolean
     */
    public boolean isAdjacent(MonotoneVertex<E> vertex) {
        return vertex == this.previous || vertex == this.next;
    }
}