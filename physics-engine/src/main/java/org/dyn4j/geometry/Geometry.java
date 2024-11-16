/*
 * Copyright (c) 2010-2022 William Bittle  http://www.dyn4j.org/
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
package org.dyn4j.geometry;

import org.dyn4j.resources.Messages;
import org.jbox2d.collision.shapes.Polygon;
import org.jbox2d.common.Vector2;

/**
 * Contains static methods to perform standard geometric operations.
 * <p>
 * This class can be used to create {@link org.jbox2d.collision.shapes.Shape}s of varying types via the <code>create</code>* methods.
 * While {@link org.jbox2d.collision.shapes.Shape}s can be created using their constructors as well, the methods here can place their
 * centers on the origin and also make copies of the given input to avoid reuse issues.
 * <p>
 * This class also contains various helper methods for cleaning vector arrays and lists and performing
 * various operations on {@link org.jbox2d.collision.shapes.Shape}s.
 *
 * @author William Bittle
 * @version 4.2.2
 * @since 1.0.0
 */
public final class Geometry {
    /**
     * 2 * PI constant
     */
    public static final double TWO_PI = 2.0 * Math.PI;

    /**
     * Returns the winding, Clockwise or Counter-Clockwise, for the given
     * array of points of a polygon.
     *
     * @param points the points of a polygon
     * @return double negative for Clockwise winding; positive for Counter-Clockwise winding
     * @throws NullPointerException     if points is null or an element of points is null
     * @throws IllegalArgumentException if points contains less than 2 elements
     * @since 2.2.0
     */
    public static final double getWinding(Vector2... points) {
        // check for a null list
        if (points == null) throw new NullPointerException(Messages.getString("geometry.nullPointArray"));
        // get the size
        int size = points.length;
        // the size must be larger than 1
        if (size < 2) throw new IllegalArgumentException(Messages.getString("geometry.invalidSizePointArray2"));
        // determine the winding by computing a signed "area"
        double area = 0.0;
        for (int i = 0; i < size; i++) {
            // get the current point and the next point
            Vector2 p1 = points[i];
            Vector2 p2 = points[i + 1 == size ? 0 : i + 1];
            // check for null
            if (p1 == null || p2 == null) throw new NullPointerException(Messages.getString("geometry.nullPointArrayElements"));
            // add the signed area
            area += p1.cross(p2);
        }
        // return the area
        return area;
    }

    /**
     * Reverses the order of the polygon points within the given array.
     * <p>
     * This method performs a simple array reverse.
     *
     * @param points the polygon points
     * @throws NullPointerException if points is null
     * @since 2.2.0
     */
    public static final void reverseWinding(Vector2... points) {
        // check for a null list
        if (points == null) throw new NullPointerException(Messages.getString("geometry.nullPointArray"));
        // get the length
        int size = points.length;
        // check for a length of 1
        if (size == 1 || size == 0) return;
        // otherwise perform the swapping loop
        int i = 0;
        int j = size - 1;
        Vector2 temp = null;
        while (j > i) {
            // swap
            temp = points[j];
            points[j] = points[i];
            points[i] = temp;
            // increment
            j--;
            i++;
        }
    }

    /**
     * Creates a new {@link Polygon} in the shape of an ellipse with count number of vertices centered
     * on the origin.
     * <p>
     * The count should be greater than or equal to 4 and a multiple of 2.  If not, the returned polygon will have count - 1
     * vertices.
     *
     * @param count  the number of vertices to use; must be greater than or equal to 4; should be even, if not, count - 1 vertices will be generated
     * @param width  the width of the ellipse
     * @param height the height of the ellipse
     * @return {@link Polygon}
     * @throws IllegalArgumentException thrown if count is less than 4 or the width or height are less than or equal to zero
     * @since 3.1.5
     */
    public static final Polygon createPolygonalEllipse(int count, double width, double height) {
        // validate the input
        if (count < 4) throw new IllegalArgumentException(Messages.getString("geometry.ellipseInvalidCount"));
        if (width <= 0.0) throw new IllegalArgumentException(Messages.getString("geometry.ellipseInvalidWidth"));
        if (height <= 0.0) throw new IllegalArgumentException(Messages.getString("geometry.ellipseInvalidHeight"));

        final double a = width * 0.5;
        final double b = height * 0.5;

        final int n2 = count / 2;
        // compute the angular increment
        final double pin2 = Math.PI / n2;
        // make sure the resulting output is an even number of vertices
        final Vector2[] vertices = new Vector2[n2 * 2];

        // use the parametric equations:
        // x = a * cos(t)
        // y = b * sin(t)

        int j = 0;
        for (int i = 0; i < n2 + 1; i++) {
            final double t = pin2 * i;
            // since the under side of the ellipse is the same
            // as the top side, only with a negated y, lets save
            // some time by creating the under side at the same time
            final double x = a * Math.cos(t);
            final double y = b * Math.sin(t);
            if (i > 0) {
                vertices[vertices.length - j] = new Vector2((float) x, (float) -y);
            }
            vertices[j++] = new Vector2((float) x, (float) y);
        }

        return new Polygon(vertices);
    }

    /**
     * Returns a scaled version of the given polygon.
     *
     * @param polygon the polygon
     * @param scale   the scale; must be greater than zero
     * @return {@link Polygon}
     * @throws NullPointerException     if the given polygon is null
     * @throws IllegalArgumentException if the given scale is less than or equal to zero
     * @since 3.1.5
     */
    public static final Polygon scale(Polygon polygon, float scale) {
        if (polygon == null) throw new NullPointerException(Messages.getString("geometry.nullShape"));
        if (scale <= 0) throw new IllegalArgumentException(Messages.getString("geometry.invalidScale"));

        Vector2[] oVertices = polygon.vertices;
        int size = oVertices.length;

        Vector2[] vertices = new Vector2[size];
        Vector2 center = polygon.centroid;
        for (int i = 0; i < size; i++) {
            Vector2 vertex = oVertices[i];
            vertices[i] = new Vector2((vertex.x - center.x) * scale + center.x, (vertex.y - center.y) * scale + center.y);
        }

        return new Polygon(vertices);
    }
}