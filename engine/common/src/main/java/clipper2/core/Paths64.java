package clipper2.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Paths64 represent one or more Path64 structures. While a single path can
 * represent a simple polygon, multiple paths are usually required to define
 * complex polygons that contain one or more holes.
 */
@SuppressWarnings("serial")
public class Paths64 extends ArrayList<Path64> {
    public Paths64() {}

    public Paths64(List<Path64> paths) {
        super(paths);
    }
}