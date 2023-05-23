package clipper2.offset;

import clipper2.Clipper;
import clipper2.core.*;
import clipper2.engine.ClipperD;
import tangible.RefObject;

import java.util.ArrayList;
import java.util.List;

public class ClipperOffset {

    private static final double TWO_PI = Math.PI * 2;

    private final ClipperD clipper = new ClipperD();
    private final List<Group> groups = new ArrayList<>();
    private final PathD normals = new PathD();
    private final Paths64 solution = new Paths64();
    private double group_delta, abs_group_delta, tmpLimit, stepsPerRad;
    private JoinType joinType;
    private final double arcTolerance;
    private final boolean mergeGroups;
    private final double miterLimit;
    private final boolean preserveCollinear;
    private final boolean reverseSolution;

    /**
     * @see #ClipperOffset(double, double, boolean, boolean)
     */
    public ClipperOffset(double miterLimit) {
        this(miterLimit, 0.25, false, false);
    }

    /**
     * Creates a ClipperOffset object, using default parameters.
     *
     * @see #ClipperOffset(double, double, boolean, boolean)
     */
    public ClipperOffset() {
        this(2.0, 0.25, false, false);
    }

    public ClipperOffset(double miterLimit, double arcTolerance, boolean preserveCollinear, boolean reverseSolution) {
        this.miterLimit = miterLimit;
        this.arcTolerance = arcTolerance;
        mergeGroups = true;
        this.preserveCollinear = preserveCollinear;
        this.reverseSolution = reverseSolution;
    }

    public final void Clear() {
        groups.clear();
    }

    public final void AddPath(Path64 path, JoinType joinType, EndType endType) {
        int cnt = path.size();
        if (cnt == 0) {
            return;
        }
        Paths64 pp = new Paths64();
        pp.add(path);
        AddPaths(pp, joinType, endType);
    }

    public final void AddPaths(Paths64 paths, JoinType joinType, EndType endType) {
        int cnt = paths.size();
        if (cnt == 0) {
            return;
        }
        groups.add(new Group(paths, joinType, endType));
    }

    public final Paths64 Execute(double delta) {
        solution.clear();
        if (Math.abs(delta) < 0.5) {
            for (int i = 0, groupsSize = groups.size(); i < groupsSize; i++) {
                Group group = groups.get(i);
                Paths64 inPaths = group.inPaths;
                for (int j = 0, inPathsSize = inPaths.size(); j < inPathsSize; j++) {
                    Path64 path = inPaths.get(j);
                    solution.add(path);
                }
            }
            return solution;
        }

        tmpLimit = (miterLimit <= 1 ? 2.0 : 2.0 / Clipper.Sqr(miterLimit));

        for (int i = 0, groupsSize = groups.size(); i < groupsSize; i++) {
            Group group = groups.get(i);
            DoGroupOffset(group, delta);
        }

        if (mergeGroups && !groups.isEmpty()) {
            // clean up self-intersections ...
            clipper.Clear();
            clipper.setPreserveCollinear(preserveCollinear);
            clipper.setReverseSolution(reverseSolution != groups.get(0).pathsReversed);
            clipper.AddSubject(solution);
            if (groups.get(0).pathsReversed) {
                clipper.Execute(ClipType.Union, FillRule.Negative, solution);
            } else {
                clipper.Execute(ClipType.Union, FillRule.Positive, solution);
            }
        }
        return solution;
    }

    private static PointD GetUnitNormal(Point64 pt1, Point64 pt2) {
        double dx = (pt2.x - pt1.x);
        double dy = (pt2.y - pt1.y);
        if ((dx == 0) && (dy == 0)) {
            return new PointD();
        }

        double f = 1.0 / Math.sqrt(dx * dx + dy * dy);
        dx *= f;
        dy *= f;

        return new PointD(dy, -dx);
    }

    private static int GetLowestPolygonIdx(Paths64 paths) {
        Point64 lp = new Point64(0, Long.MIN_VALUE);
        int result = -1;
        for (int i = 0; i < paths.size(); i++) {
            for (Point64 pt : paths.get(i)) {
                if (pt.y < lp.y || (pt.y == lp.y && pt.x >= lp.x)) {
                    continue;
                }
                result = i;
                lp = pt;
            }
        }
        return result;
    }

    private static PointD TranslatePoint(PointD pt, double dx, double dy) {
        return new PointD(pt.x + dx, pt.y + dy);
    }

    private static PointD ReflectPoint(PointD pt, PointD pivot) {
        return new PointD(pivot.x + (pivot.x - pt.x), pivot.y + (pivot.y - pt.y));
    }

    private static boolean AlmostZero(double value) {
        return AlmostZero(value, 0.001);
    }

    private static boolean AlmostZero(double value, double epsilon) {
        return Math.abs(value) < epsilon;
    }

    private static double Hypotenuse(double x, double y) {
        return Math.sqrt(x * x + y * y);
    }

    private static PointD NormalizeVector(PointD vec) {
        double h = Hypotenuse(vec.x, vec.y);
        if (AlmostZero(h)) {
            return new PointD(0, 0);
        }
        double inverseHypot = 1 / h;
        return new PointD(vec.x * inverseHypot, vec.y * inverseHypot);
    }

    private static PointD GetAvgUnitVector(PointD vec1, PointD vec2) {
        return NormalizeVector(new PointD(vec1.x + vec2.x, vec1.y + vec2.y));
    }

    private static PointD IntersectPoint(PointD pt1a, PointD pt1b, PointD pt2a, PointD pt2b) {
        if (InternalClipper.IsAlmostZero(pt1a.x - pt1b.x)) { // vertical
            if (InternalClipper.IsAlmostZero(pt2a.x - pt2b.x)) {
                return new PointD(0, 0);
            }
            double m2 = (pt2b.y - pt2a.y) / (pt2b.x - pt2a.x);
            double b2 = pt2a.y - m2 * pt2a.x;
            return new PointD(pt1a.x, m2 * pt1a.x + b2);
        }

        if (InternalClipper.IsAlmostZero(pt2a.x - pt2b.x)) { // vertical
            double m1 = (pt1b.y - pt1a.y) / (pt1b.x - pt1a.x);
            double b1 = pt1a.y - m1 * pt1a.x;
            return new PointD(pt2a.x, m1 * pt2a.x + b1);
        } else {
            double m1 = (pt1b.y - pt1a.y) / (pt1b.x - pt1a.x);
            double b1 = pt1a.y - m1 * pt1a.x;
            double m2 = (pt2b.y - pt2a.y) / (pt2b.x - pt2a.x);
            double b2 = pt2a.y - m2 * pt2a.x;
            if (InternalClipper.IsAlmostZero(m1 - m2)) {
                return new PointD(0, 0);
            }
            double x = (b2 - b1) / (m1 - m2);
            return new PointD(x, m1 * x + b1);
        }
    }

    private Point64 GetPerpendic(Point64 pt, PointD norm) {
        return new Point64(pt.x + norm.x * group_delta, pt.y + norm.y * group_delta);
    }

    private PointD GetPerpendicD(Point64 pt, PointD norm) {
        return new PointD(pt.x + norm.x * group_delta, pt.y + norm.y * group_delta);
    }

    private void DoSquare(Group group, Path64 path, int j, int k) {
        PointD vec;
        if (j == k) {
            vec = new PointD(normals.get(0).y, -normals.get(0).x);
        } else {
            vec = GetAvgUnitVector(new PointD(-normals.get(k).y, normals.get(k).x),
                    new PointD(normals.get(j).y, -normals.get(j).x));
        }

        // now offset the original vertex delta units along unit vector
        PointD ptQ = new PointD(path.get(j));
        ptQ = TranslatePoint(ptQ, abs_group_delta * vec.x, abs_group_delta * vec.y);

        // get perpendicular vertices
        PointD pt1 = TranslatePoint(ptQ, group_delta * vec.y, group_delta * -vec.x);
        PointD pt2 = TranslatePoint(ptQ, group_delta * -vec.y, group_delta * vec.x);
        // get 2 vertices along one edge offset
        PointD pt3 = GetPerpendicD(path.get(k), normals.get(k));

        if (j == k) {
            PointD pt4 = new PointD(pt3.x + vec.x * group_delta, pt3.y + vec.y * group_delta);
            PointD pt = IntersectPoint(pt1, pt2, pt3, pt4);
            // get the second intersect point through reflecion
            group.outPath.add(new Point64(ReflectPoint(pt, ptQ)));
            group.outPath.add(new Point64(pt));
        } else {
            PointD pt4 = GetPerpendicD(path.get(j), normals.get(k));
            PointD pt = IntersectPoint(pt1, pt2, pt3, pt4);
            group.outPath.add(new Point64(pt));
            // get the second intersect point through reflecion
            group.outPath.add(new Point64(ReflectPoint(pt, ptQ)));
        }
    }

    private void DoMiter(Group group, Path64 path, int j, int k, double cosA) {
        double q = group_delta / (cosA + 1);
        group.outPath.add(new Point64(path.get(j).x + (normals.get(k).x + normals.get(j).x) * q,
                path.get(j).y + (normals.get(k).y + normals.get(j).y) * q));
    }

    private void DoRound(Group group, Path64 path, int j, int k, double angle) {
        // even though angle may be negative this is a convex join
        Point64 pt = path.get(j);
        PointD pt2 = new PointD(normals.get(k).x * group_delta, normals.get(k).y * group_delta);
        if (j == k) {
            pt2.Negate();
        }

        int steps = (int) Math.ceil(stepsPerRad * Math.abs(angle));
        double stepSin = Math.sin(angle / steps);
        double stepCos = Math.cos(angle / steps);

        group.outPath.add(new Point64(pt.x + pt2.x, pt.y + pt2.y));
        for (int i = 0; i < steps; i++) {
            pt2 = new PointD(pt2.x * stepCos - stepSin * pt2.y, pt2.x * stepSin + pt2.y * stepCos);
            group.outPath.add(new Point64(pt.x + pt2.x, pt.y + pt2.y));
        }
        group.outPath.add(GetPerpendic(pt, normals.get(j)));
    }

    private void BuildNormals(Path64 path) {
        int cnt = path.size();
        normals.clear();

        for (int i = 0; i < cnt - 1; i++) {
            normals.add(GetUnitNormal(path.get(i), path.get(i + 1)));
        }
        normals.add(GetUnitNormal(path.get(cnt - 1), path.get(0)));
    }

    private void OffsetPoint(Group group, Path64 path, int j, RefObject<Integer> k) {
        // Let A = change in angle where edges join
        // A == 0: ie no change in angle (flat join)
        // A == PI: edges 'spike'
        // sin(A) < 0: right turning
        // cos(A) < 0: change in angle is more than 90 degree
        double sinA = InternalClipper.CrossProduct(normals.get(j), normals.get(k.argValue));
        double cosA = InternalClipper.DotProduct(normals.get(j), normals.get(k.argValue));
        if (sinA > 1.0) {
            sinA = 1.0;
        } else if (sinA < -1.0) {
            sinA = -1.0;
        }
        boolean almostNoAngle = (AlmostZero(sinA) && cosA > 0);
        if (almostNoAngle || (sinA * group_delta < 0)) {
            group.outPath.add(GetPerpendic(path.get(j), normals.get(k.argValue)));
            if (!almostNoAngle) {
                group.outPath.add(path.get(j));
            }
            group.outPath.add(GetPerpendic(path.get(j), normals.get(j)));
        } else {
            // convex
            if (joinType == JoinType.Round) {
                DoRound(group, path, j, k.argValue, Math.atan2(sinA, cosA));
            } else if (joinType == JoinType.Miter) {
                // miter unless the angle is so acute the miter would exceeds ML
                if (cosA > tmpLimit - 1) {
                    DoMiter(group, path, j, k.argValue, cosA);
                } else {
                    DoSquare(group, path, j, k.argValue);
                }
            }
            // don't bother squaring angles that deviate < ~20 degrees because
            // squaring will be indistinguishable from mitering and just be a lot slower
            else if (cosA > 0.9) {
                DoMiter(group, path, j, k.argValue, cosA);
            } else {
                DoSquare(group, path, j, k.argValue);
            }
        }

        k.argValue = j;
    }

    private void OffsetPolygon(Group group, Path64 path) {
        group.outPath = new Path64();
        int cnt = path.size();
        RefObject<Integer> prev = new RefObject<>(cnt - 1);
        for (int i = 0; i < cnt; i++) {
            OffsetPoint(group, path, i, prev);
        }
        group.outPaths.add(group.outPath);
    }

    private void OffsetOpenJoined(Group group, Path64 path) {
        OffsetPolygon(group, path);
        path = Clipper.ReversePath(path);
        BuildNormals(path);
        OffsetPolygon(group, path);
    }

    private void OffsetOpenPath(Group group, Path64 path, EndType endType) {
        group.outPath = new Path64();
        int highI = path.size() - 1;

        // do the line start cap
        if (endType == EndType.Butt) {
            group.outPath.add(new Point64(path.get(0).x - normals.get(0).x * group_delta,
                    path.get(0).y - normals.get(0).y * group_delta));
            group.outPath.add(GetPerpendic(path.get(0), normals.get(0)));
        } else if (endType == EndType.Round) {
            DoRound(group, path, 0, 0, Math.PI);
        } else {
            DoSquare(group, path, 0, 0);
        }

        // offset the left side going forward
        RefObject<Integer> k = new RefObject<>(0);
        for (int i = 1; i < highI; i++) {
            OffsetPoint(group, path, i, k);
        }

        // reverse normals ...
        for (int i = highI; i > 0; i--) {
            normals.set(i, new PointD(-normals.get(i - 1).x, -normals.get(i - 1).y));
        }
        normals.set(0, normals.get(highI));

        // do the line end cap
        if (endType == EndType.Butt) {
            group.outPath.add(new Point64(path.get(highI).x - normals.get(highI).x * group_delta,
                    path.get(highI).y - normals.get(highI).y * group_delta));
            group.outPath.add(GetPerpendic(path.get(highI), normals.get(highI)));
        } else if (endType == EndType.Round) {
            DoRound(group, path, highI, highI, Math.PI);
        } else {
            DoSquare(group, path, highI, highI);
        }

        // offset the left side going back
        k = new RefObject<>(0);
        for (int i = highI; i > 0; i--) {
            OffsetPoint(group, path, i, k);
        }

        group.outPaths.add(group.outPath);
    }

    private boolean IsFullyOpenEndType(EndType et) {
        return (et != EndType.Polygon) && (et != EndType.Joined);
    }

    private void DoGroupOffset(Group group, double delta) {
        if (group.endType != EndType.Polygon) {
            delta = Math.abs(delta) / 2;
        }
        boolean isClosedPaths = !IsFullyOpenEndType(group.endType);

        if (isClosedPaths) {
            // the lowermost polygon must be an outer polygon. So we can use that as the
            // designated orientation for outer polygons (needed for tidy-up clipping)
            int lowestIdx = GetLowestPolygonIdx(group.inPaths);
            if (lowestIdx < 0) {
                return;
            }
            // nb: don't use the default orientation here ...
            double area = Clipper.Area(group.inPaths.get(lowestIdx));
            if (area == 0) {
                return;
            }
            group.pathsReversed = (area < 0);
            if (group.pathsReversed) {
                delta = -delta;
            }
        } else {
            group.pathsReversed = false;
        }

        this.group_delta = delta;
        abs_group_delta = Math.abs(this.group_delta);
        joinType = group.joinType;

        // calculate a sensible number of steps (for 360 deg for the given offset
        if (group.joinType == JoinType.Round || group.endType == EndType.Round) {
            double arcTol = arcTolerance > 0.01 ? arcTolerance : Math.log10(2 + abs_group_delta) * 0.25; // empirically
            // derived
            // get steps per 180 degrees (see offset_triginometry2.svg)
            stepsPerRad = Math.PI / Math.acos(1 - arcTol / abs_group_delta) / TWO_PI;
        }

        Paths64 inPaths = group.inPaths;
        for (int i = 0, inPathsSize = inPaths.size(); i < inPathsSize; i++) {
            Path64 p = inPaths.get(i);
            Path64 path = Clipper.StripDuplicates(p, isClosedPaths);
            int cnt = path.size();
            if (cnt == 0 || (cnt < 3 && !IsFullyOpenEndType(group.endType))) {
                continue;
            }

            if (cnt == 1) {
                group.outPath = new Path64();
                // single vertex so build a circle or square ...
                if (group.endType == EndType.Round) {
                    double r = abs_group_delta;
                    group.outPath = Clipper.Ellipse(path.get(0), r, r);
                } else {
                    int d = (int) Math.ceil(group_delta);
                    Rect64 r = new Rect64(path.get(0).x - d, path.get(0).y - d, path.get(0).x - d, path.get(0).y - d);
                    group.outPath = r.AsPath();
                }
                group.outPaths.add(group.outPath);
            } else {
                BuildNormals(path);
                if (group.endType == EndType.Polygon) {
                    OffsetPolygon(group, path);
                } else if (group.endType == EndType.Joined) {
                    OffsetOpenJoined(group, path);
                } else {
                    OffsetOpenPath(group, path, group.endType);
                }
            }
        }

        if (!mergeGroups) {
            // clean up self-intersections
            clipper.Clear();
            clipper.setPreserveCollinear(preserveCollinear);
            clipper.setReverseSolution(reverseSolution != group.pathsReversed);
            clipper.AddSubject(group.outPaths);
            if (group.pathsReversed) {
                clipper.Execute(ClipType.Union, FillRule.Negative, group.outPaths);
            } else {
                clipper.Execute(ClipType.Union, FillRule.Positive, group.outPaths);
            }
        }
        solution.addAll(group.outPaths);
        group.outPaths.clear();
    }
}