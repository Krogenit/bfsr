package clipper2.core;

public final class InternalClipper {
    private static final double FLOATING_POINT_TOLERANCE = 1.0E-12;

    public static boolean IsAlmostZero(double value) {
        return (Math.abs(value) <= FLOATING_POINT_TOLERANCE);
    }

    public static double DotProduct(Point64 pt1, Point64 pt2, Point64 pt3) {
        return ((pt2.x - pt1.x) * (pt3.x - pt2.x) + (pt2.y - pt1.y) * (pt3.y - pt2.y));
    }

    public static double CrossProduct(PointD vec1, PointD vec2) {
        return (vec1.y * vec2.x - vec2.y * vec1.x);
    }

    public static double DotProduct(PointD vec1, PointD vec2) {
        return (vec1.x * vec2.x + vec1.y * vec2.y);
    }

    public static boolean GetIntersectPoint(Point64 ln1a, Point64 ln1b, Point64 ln2a, Point64 ln2b, /* out */ PointD ip) {
        double m1, b1, m2, b2;
        if (ln1b.x == ln1a.x) {
            if (ln2b.x == ln2a.x) {
                return false;
            }
            m2 = (double) (ln2b.y - ln2a.y) / (ln2b.x - ln2a.x);
            b2 = ln2a.y - m2 * ln2a.x;
            ip.x = ln1a.x;
            ip.y = m2 * ln1a.x + b2;
        } else if (ln2b.x == ln2a.x) {
            m1 = (double) (ln1b.y - ln1a.y) / (ln1b.x - ln1a.x);
            b1 = ln1a.y - m1 * ln1a.x;
            ip.x = ln2a.x;
            ip.y = m1 * ln2a.x + b1;
        } else {
            m1 = (double) (ln1b.y - ln1a.y) / (ln1b.x - ln1a.x);
            b1 = ln1a.y - m1 * ln1a.x;
            m2 = (double) (ln2b.y - ln2a.y) / (ln2b.x - ln2a.x);
            b2 = ln2a.y - m2 * ln2a.x;
            if (Math.abs(m1 - m2) > FLOATING_POINT_TOLERANCE) {
                ip.x = (b2 - b1) / (m1 - m2);
                ip.y = m1 * ip.x + b1;
            } else {
                ip.x = (ln1a.x + ln1b.x) * 0.5;
                ip.y = (ln1a.y + ln1b.y) * 0.5;
            }
        }

        return true;
    }

    public static boolean SegmentsIntersect(Point64 seg1a, Point64 seg1b, Point64 seg2a, Point64 seg2b) {
        return SegmentsIntersect(seg1a, seg1b, seg2a, seg2b, false);
    }

    public static boolean SegmentsIntersect(Point64 seg1a, Point64 seg1b, Point64 seg2a, Point64 seg2b, boolean inclusive) {
        if (inclusive) {
            double res1 = CrossProduct(seg1a, seg2a, seg2b);
            double res2 = CrossProduct(seg1b, seg2a, seg2b);
            if (res1 * res2 > 0) {
                return false;
            }
            double res3 = CrossProduct(seg2a, seg1a, seg1b);
            double res4 = CrossProduct(seg2b, seg1a, seg1b);
            if (res3 * res4 > 0) {
                return false;
            }
            // ensure NOT collinear
            return (res1 != 0 || res2 != 0 || res3 != 0 || res4 != 0);
        } else {
            double dx1 = seg1a.x - seg1b.x;
            double dy1 = seg1a.y - seg1b.y;
            double dx2 = seg2a.x - seg2b.x;
            double dy2 = seg2a.y - seg2b.y;
            return (((dy1 * (seg2a.x - seg1a.x) - dx1 * (seg2a.y - seg1a.y))
                    * (dy1 * (seg2b.x - seg1a.x) - dx1 * (seg2b.y - seg1a.y)) < 0)
                    && ((dy2 * (seg1a.x - seg2a.x) - dx2 * (seg1a.y - seg2a.y))
                    * (dy2 * (seg1b.x - seg2a.x) - dx2 * (seg1b.y - seg2a.y)) < 0));
        }
    }

    public static boolean PolygonInPolygon(PathD polygon1, PathD polygon2) {
        for (int i = 0; i < polygon1.size() - 1; i++) {
            PointD point1 = polygon1.get(i);
            PointD point2 = polygon1.get(i + 1);

            for (int j = 0; j < polygon2.size() - 1; j++) {
                PointD point3 = polygon2.get(j);
                PointD point4 = polygon2.get(j + 1);

                if (doIntersect(point1, point2, point3, point4)) {
                    return false;
                }
            }
        }

        return PointInPolygonOptimized(polygon1.get(0), polygon2);
    }

    private static int orientation(PointD p, PointD q, PointD r) {
        double val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);
        if (val == 0.0) return 0;
        return val > 0 ? 1 : 2;
    }

    public static boolean doIntersect(PointD p1, PointD q1, PointD p2, PointD q2) {
        return orientation(p1, q1, p2) != orientation(p1, q1, q2) && orientation(p2, q2, p1) != orientation(p2, q2, q1);
    }

    public static boolean PointInPolygonOptimized(PointD pt, PathD polygon) {
        int len = polygon.size(), i = len - 1;

        int val = 0;
        boolean isAbove = polygon.get(i).y < pt.y;
        i = 0;

        while (i < len) {
            if (isAbove) {
                while (i < len && polygon.get(i).y < pt.y) {
                    i++;
                }
                if (i == len) {
                    break;
                }
            } else {
                while (i < len && polygon.get(i).y > pt.y) {
                    i++;
                }
                if (i == len) {
                    break;
                }
            }

            PointD prev;

            PointD curr = polygon.get(i);
            if (i > 0) {
                prev = polygon.get(i - 1);
            } else {
                prev = polygon.get(len - 1);
            }

            if (curr.y == pt.y) {
                if (curr.x == pt.x || (curr.y == prev.y && ((pt.x < prev.x) != (pt.x < curr.x)))) {
                    return false;
                }
                i++;
                continue;
            }

            if (pt.x < curr.x && pt.x < prev.x) {
                // we're only interested in edges crossing on the left
            } else if (pt.x > prev.x && pt.x > curr.x) {
                val = 1 - val; // toggle val
            } else {
                double d = CrossProduct(prev, curr, pt);
                if (d == 0) {
                    return false;
                }
                if ((d < 0) == isAbove) {
                    val = 1 - val;
                }
            }
            isAbove = !isAbove;
            i++;
        }
        return val != 0;
    }

    public static double CrossProduct(PointD pt1, PointD pt2, PointD pt3) {
        return ((pt2.x - pt1.x) * (pt3.y - pt2.y) - (pt2.y - pt1.y) * (pt3.x - pt2.x));
    }

    public static double CrossProduct(Point64 pt1, Point64 pt2, Point64 pt3) {
        return ((pt2.x - pt1.x) * (pt3.y - pt2.y) - (pt2.y - pt1.y) * (pt3.x - pt2.x));
    }

}