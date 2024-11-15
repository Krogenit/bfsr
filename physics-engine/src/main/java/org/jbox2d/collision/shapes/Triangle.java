package org.jbox2d.collision.shapes;

import org.jbox2d.common.Vector2;

public class Triangle extends Polygon {
    public Triangle(Vector2 point1, Vector2 point2, Vector2 point3) {
        super(new Vector2[]{point1, point2, point3});
    }
}
