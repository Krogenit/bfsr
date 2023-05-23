package clipper2;

import clipper2.core.Path64;
import clipper2.core.PathD;
import clipper2.core.Point64;
import clipper2.core.PointD;

import java.util.Collections;

public final class Clipper {
    public static double Area(Path64 path) {
        // https://en.wikipedia.org/wiki/Shoelace_formula
        int cnt = path.size();
        if (cnt < 3) {
            return 0.0;
        }
        double a = 0.0;
        Point64 prevPt = path.get(cnt - 1);
        for (Point64 pt : path) {
            a += (double) (prevPt.y + pt.y) * (prevPt.x - pt.x);
            prevPt = pt;
        }
        return a * 0.5;
    }

    public static double Area(PathD path) {
        double a = 0.0;
        PointD prevPt = path.get(path.size() - 1);
        for (int i = 0, pathSize = path.size(); i < pathSize; i++) {
            PointD pt = path.get(i);
            a += (prevPt.y + pt.y) * (prevPt.x - pt.x);
            prevPt = pt;
        }
        return a * 0.5;
    }

    public static Path64 ReversePath(Path64 path) {
        Path64 result = new Path64(path);
        Collections.reverse(result);
        return result;
    }

    public static double Sqr(double value) {
        return value * value;
    }

    public static Path64 StripDuplicates(Path64 path, boolean isClosedPath) {
        int cnt = path.size();
        Path64 result = new Path64(cnt);
        if (cnt == 0) {
            return result;
        }
        Point64 lastPt = path.get(0);
        result.add(lastPt);
        for (int i = 1; i < cnt; i++) {
            if (!path.get(i).equals(lastPt)) {
                lastPt = path.get(i);
                result.add(lastPt);
            }
        }
        if (isClosedPath && result.get(0).equals(lastPt)) {
            result.remove(result.size() - 1);
        }
        return result;
    }

    public static Path64 Ellipse(Point64 center, double radiusX, double radiusY) {
        return Ellipse(center, radiusX, radiusY, 0);
    }

    public static Path64 Ellipse(Point64 center, double radiusX, double radiusY, int steps) {
        if (radiusX <= 0) {
            return new Path64();
        }
        if (radiusY <= 0) {
            radiusY = radiusX;
        }
        if (steps <= 2) {
            steps = (int) Math.ceil(Math.PI * Math.sqrt((radiusX + radiusY) / 2));
        }

        double si = Math.sin(2 * Math.PI / steps);
        double co = Math.cos(2 * Math.PI / steps);
        double dx = co, dy = si;
        Path64 result = new Path64(steps);
        result.add(new Point64(center.x + radiusX, center.x));
        for (int i = 1; i < steps; ++i) {
            result.add(new Point64(center.x + radiusX * dx, center.y + radiusY * dy));
            double x = dx * co - dy * si;
            dy = dy * co + dx * si;
            dx = x;
        }
        return result;
    }
}