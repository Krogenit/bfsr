package clipper2.engine;

import clipper2.core.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Subject and Clip paths are passed to a Clipper object via AddSubject,
 * AddOpenSubject and AddClip methods. Clipping operations are then initiated by
 * calling Execute. And Execute can be called multiple times (ie with different
 * ClipTypes & FillRules) without having to reload these paths.
 */
public class ClipperBase {
    private ClipType cliptype;
    private FillRule fillrule = FillRule.EvenOdd;
    private Active actives;
    private Active sel;
    private Joiner horzJoiners;
    private final List<LocalMinima> minimaList;
    private final List<IntersectNode> intersectList;
    private final List<OutRec> outrecList;
    private final List<Joiner> joinerList;
    private final NavigableSet<Long> scanlineSet;
    private int currentLocMin;
    private long currentBotY;
    private boolean isSortedMinimaList;
    private boolean succeeded;
    private boolean preserveCollinear;
    private boolean reverseSolution;

    /**
     * Path data structure for clipping solutions.
     */
    static class OutRec {
        int idx;
        @Nullable
        OutRec owner;
        @Nullable
        Active frontEdge;
        @Nullable
        Active backEdge;
        @Nullable
        OutPt pts;
        boolean isOpen;
    }

    static class Active {

        Point64 bot;
        Point64 top;
        long curX; // current (updated at every new scanline)
        double dx;
        int windDx; // 1 or -1 depending on winding direction
        int windCount;
        int windCount2; // winding count of the opposite polytype
        @Nullable
        OutRec outrec;
        // AEL: 'active edge list' (Vatti's AET - active edge table)
        // a linked list of all edges (from left to right) that are present
        // (or 'active') within the current scanbeam (a horizontal 'beam' that
        // sweeps from bottom to top over the paths in the clipping operation).
        @Nullable
        Active prevInAEL;
        @Nullable
        Active nextInAEL;
        // SEL: 'sorted edge list' (Vatti's ST - sorted table)
        // linked list used when sorting edges into their new positions at the
        // top of scanbeams, but also (re)used to process horizontals.
        @Nullable
        Active prevInSEL;
        @Nullable
        Active nextInSEL;
        @Nullable
        Active jump;
        @Nullable
        Vertex vertexTop;
        LocalMinima localMin = new LocalMinima(); // the bottom of an edge 'bound' (also Vatti)
        boolean isLeftBound;

    }

    /**
     * Vertex data structure for clipping solutions
     */
    static class OutPt {

        Point64 pt;
        @Nullable
        OutPt next;
        OutPt prev;
        OutRec outrec;
        @Nullable
        Joiner joiner;

        OutPt(Point64 pt, OutRec outrec) {
            this.pt = pt;
            this.outrec = outrec;
            next = this;
            prev = this;
            joiner = null;
        }

    }

    /**
     * Structure used in merging "touching" solution polygons.
     */
    static class Joiner {

        int idx;
        OutPt op1;
        @Nullable
        OutPt op2;
        @Nullable
        Joiner next1;
        @Nullable
        Joiner next2;
        @Nullable
        Joiner nextH;

        Joiner(OutPt op1, @Nullable OutPt op2, @Nullable Joiner nextH) {
            this.idx = -1;
            this.nextH = nextH;
            this.op1 = op1;
            this.op2 = op2;
            next1 = op1.joiner;
            op1.joiner = this;

            if (op2 != null) {
                next2 = op2.joiner;
                op2.joiner = this;
            } else {
                next2 = null;
            }
        }
    }

    /**
     * A structure representing 2 intersecting edges. Intersections must be sorted
     * so they are processed from the largest y coordinates to the smallest while
     * keeping edges adjacent.
     */
    static final class IntersectNode {

        Point64 pt;
        Active edge1;
        Active edge2;

        IntersectNode(Point64 pt, Active edge1, Active edge2) {
            this.pt = pt.clone();
            this.edge1 = edge1;
            this.edge2 = edge2;
        }
    }

    /**
     * Vertex: a pre-clipping data structure. It is used to separate polygons into
     * ascending and descending 'bounds' (or sides) that start at local minima and
     * ascend to a local maxima, before descending again.
     */
    static class Vertex {

        Point64 pt;
        @Nullable
        Vertex next;
        @Nullable
        Vertex prev;
        int flags;

        Vertex(Point64 pt, int flags, @Nullable Vertex prev) {
            this.pt = pt.clone();
            this.flags = flags;
            next = null;
            this.prev = prev;
        }
    }

    static final class VertexFlags {

        static final int None = 0;
        static final int OpenStart = 1;
        static final int OpenEnd = 2;
        static final int LocalMax = 4;
        static final int LocalMin = 8;

    }

    protected ClipperBase() {
        minimaList = new ArrayList<>();
        intersectList = new ArrayList<>();
        outrecList = new ArrayList<>();
        joinerList = new ArrayList<>();
        scanlineSet = new TreeSet<>();
        preserveCollinear = true;
    }

    /**
     * When adjacent edges are collinear in closed path solutions, the common vertex
     * can safely be removed to simplify the solution without altering path shape.
     * However, because some users prefer to retain these common vertices, this
     * feature is optional. Nevertheless, when adjacent edges in solutions are
     * collinear and also create a 'spike' by overlapping, the vertex creating the
     * spike will be removed irrespective of the PreserveCollinear setting. This
     * property is enabled by default.
     */
    public final void setPreserveCollinear(boolean value) {
        preserveCollinear = value;
    }

    public final void setReverseSolution(boolean value) {
        reverseSolution = value;
    }

    private static boolean IsHotEdge(Active ae) {
        return ae.outrec != null;
    }

    private static Active GetPrevHotEdge(Active ae) {
        Active prev = ae.prevInAEL;
        while (prev != null && (!IsHotEdge(prev))) {
            prev = prev.prevInAEL;
        }
        return prev;
    }

    private static boolean IsFront(Active ae) {
        return (ae == ae.outrec.frontEdge);
    }

    private static double GetDx(Point64 pt1, Point64 pt2) {
        /*-
         *  Dx:                             0(90deg)                                    *
         *                                  |                                           *
         *               +inf (180deg) <--- o --. -inf (0deg)                           *
         *******************************************************************************/
        double dy = pt2.y - pt1.y;
        if (dy != 0) {
            return (pt2.x - pt1.x) / dy;
        }
        if (pt2.x > pt1.x) {
            return Double.NEGATIVE_INFINITY;
        }
        return Double.POSITIVE_INFINITY;
    }

    private static long TopX(Active ae, long currentY) {
        if (currentY == ae.top.y || ae.top.x == ae.bot.x) {
            return ae.top.x;
        }
        if (currentY == ae.bot.y) {
            return ae.bot.x;
        }
        return ae.bot.x + (long) Math.rint(ae.dx * (currentY - ae.bot.y));
    }

    private static boolean IsHorizontal(Active ae) {
        return ae.top.y == ae.bot.y;
    }

    private static boolean IsHeadingRightHorz(Active ae) {
        return (Double.isInfinite(ae.dx));
    }

    private static boolean IsHeadingLeftHorz(Active ae) {
        return (Double.isInfinite(ae.dx));
    }

    private static void SwapActives(RefObject<Active> ae1, RefObject<Active> ae2) {
        Active temp = ae1.argValue;
        ae1.argValue = ae2.argValue;
        ae2.argValue = temp;
    }

    private static PathType GetPolyType(Active ae) {
        return ae.localMin.polytype;
    }

    private static boolean IsSamePolyType(Active ae1, Active ae2) {
        return ae1.localMin.polytype == ae2.localMin.polytype;
    }

    private static Point64 GetIntersectPoint(Active ae1, Active ae2) {
        double b1, b2;
        if (InternalClipper.IsAlmostZero(ae1.dx - ae2.dx)) {
            return ae1.top;
        }

        if (InternalClipper.IsAlmostZero(ae1.dx)) {
            if (IsHorizontal(ae2)) {
                return new Point64(ae1.bot.x, ae2.bot.y);
            }
            b2 = ae2.bot.y - (ae2.bot.x / ae2.dx);
            return new Point64(ae1.bot.x, (long) Math.rint(ae1.bot.x / ae2.dx + b2));
        }

        if (InternalClipper.IsAlmostZero(ae2.dx)) {
            if (IsHorizontal(ae1)) {
                return new Point64(ae2.bot.x, ae1.bot.y);
            }
            b1 = ae1.bot.y - (ae1.bot.x / ae1.dx);
            return new Point64(ae2.bot.x, (long) Math.rint(ae2.bot.x / ae1.dx + b1));
        }
        b1 = ae1.bot.x - ae1.bot.y * ae1.dx;
        b2 = ae2.bot.x - ae2.bot.y * ae2.dx;
        double q = (b2 - b1) / (ae1.dx - ae2.dx);
        return (Math.abs(ae1.dx) < Math.abs(ae2.dx)) ? new Point64((long) (ae1.dx * q + b1), (long) (q))
                : new Point64((long) (ae2.dx * q + b2), (long) (q));
    }

    private static void SetDx(Active ae) {
        ae.dx = GetDx(ae.bot, ae.top);
    }

    private static Vertex NextVertex(Active ae) {
        if (ae.windDx > 0) {
            return ae.vertexTop.next;
        }
        return ae.vertexTop.prev;
    }

    private static Vertex PrevPrevVertex(Active ae) {
        if (ae.windDx > 0) {
            return ae.vertexTop.prev.prev;
        }
        return ae.vertexTop.next.next;
    }

    private static boolean IsMaxima(Vertex vertex) {
        return ((vertex.flags & VertexFlags.LocalMax) != VertexFlags.None);
    }

    private static boolean IsMaxima(Active ae) {
        return IsMaxima(ae.vertexTop);
    }

    private static Active GetMaximaPair(Active ae) {
        Active ae2;
        ae2 = ae.nextInAEL;
        while (ae2 != null) {
            if (ae2.vertexTop == ae.vertexTop) {
                return ae2; // Found!
            }
            ae2 = ae2.nextInAEL;
        }
        return null;
    }

    private static Vertex GetCurrYMaximaVertex(Active ae) {
        Vertex result = ae.vertexTop;
        if (ae.windDx > 0) {
            while (result.next.pt.y == result.pt.y) {
                result = result.next;
            }
        } else {
            while (result.prev.pt.y == result.pt.y) {
                result = result.prev;
            }
        }
        if (!IsMaxima(result)) {
            result = null; // not a maxima
        }
        return result;
    }

    private static Active GetHorzMaximaPair(Active horz, Vertex maxVert) {
        // we can't be sure whether the MaximaPair is on the left or right, so ...
        Active result = horz.prevInAEL;
        while (result != null && result.curX >= maxVert.pt.x) {
            if (result.vertexTop == maxVert) {
                return result; // Found!
            }
            result = result.prevInAEL;
        }
        result = horz.nextInAEL;
        while (result != null && TopX(result, horz.top.y) <= maxVert.pt.x) {
            if (result.vertexTop == maxVert) {
                return result; // Found!
            }
            result = result.nextInAEL;
        }
        return null;
    }

    private static void SetSides(OutRec outrec, Active startEdge, Active endEdge) {
        outrec.frontEdge = startEdge;
        outrec.backEdge = endEdge;
    }

    private static void SwapOutrecs(Active ae1, Active ae2) {
        OutRec or1 = ae1.outrec; // at least one edge has
        OutRec or2 = ae2.outrec; // an assigned outrec
        if (or1 == or2) {
            Active ae = or1.frontEdge;
            or1.frontEdge = or1.backEdge;
            or1.backEdge = ae;
            return;
        }

        if (or1 != null) {
            if (ae1 == or1.frontEdge) {
                or1.frontEdge = ae2;
            } else {
                or1.backEdge = ae2;
            }
        }

        if (or2 != null) {
            if (ae2 == or2.frontEdge) {
                or2.frontEdge = ae1;
            } else {
                or2.backEdge = ae1;
            }
        }

        ae1.outrec = or2;
        ae2.outrec = or1;
    }

    private static double Area(OutPt op) {
        // https://en.wikipedia.org/wiki/Shoelaceformula
        double area = 0.0;
        OutPt op2 = op;
        do {
            area += (op2.prev.pt.y + op2.pt.y) * (op2.prev.pt.x - op2.pt.x);
            op2 = op2.next;
        } while (op2 != op);
        return area * 0.5;
    }

    private static double AreaTriangle(Point64 pt1, Point64 pt2, Point64 pt3) {
        return (pt3.y + pt1.y) * (pt3.x - pt1.x) + (pt1.y + pt2.y) * (pt1.x - pt2.x) + (pt2.y + pt3.y) * (pt2.x - pt3.x);
    }

    private static OutRec GetRealOutRec(OutRec outRec) {
        while ((outRec != null) && (outRec.pts == null)) {
            outRec = outRec.owner;
        }
        return outRec;
    }

    private static void UncoupleOutRec(Active ae) {
        OutRec outrec = ae.outrec;
        if (outrec == null) {
            return;
        }
        outrec.frontEdge.outrec = null;
        outrec.backEdge.outrec = null;
        outrec.frontEdge = null;
        outrec.backEdge = null;
    }

    private static boolean OutrecIsAscending(Active hotEdge) {
        return (hotEdge == hotEdge.outrec.frontEdge);
    }

    private static boolean EdgesAdjacentInAEL(IntersectNode inode) {
        return (inode.edge1.nextInAEL == inode.edge2) || (inode.edge1.prevInAEL == inode.edge2);
    }

    protected final void ClearSolution() {
        while (actives != null) {
            DeleteFromAEL(actives);
        }
        scanlineSet.clear();
        DisposeIntersectNodes();
        joinerList.clear();
        horzJoiners = null;
        outrecList.clear();
    }

    public final void Clear() {
        ClearSolution();
        minimaList.clear();
        currentLocMin = 0;
        isSortedMinimaList = false;
    }

    protected final void Reset() {
        if (!isSortedMinimaList) {
            minimaList.sort((locMin1, locMin2) -> Long.compare(locMin2.vertex.pt.y, locMin1.vertex.pt.y));
            isSortedMinimaList = true;
        }

        for (int i = minimaList.size() - 1; i >= 0; i--) {
            scanlineSet.add(minimaList.get(i).vertex.pt.y);
        }

        currentBotY = 0;
        currentLocMin = 0;
        actives = null;
        sel = null;
        succeeded = true;
    }

    private boolean HasLocMinAtY(long y) {
        return (currentLocMin < minimaList.size() && minimaList.get(currentLocMin).vertex.pt.y == y);
    }

    private LocalMinima PopLocalMinima() {
        return minimaList.get(currentLocMin++);
    }

    private void AddLocMin(Vertex vert, PathType polytype) {
        // make sure the vertex is added only once ...
        if ((vert.flags & VertexFlags.LocalMin) != VertexFlags.None) {
            return;
        }
        vert.flags |= VertexFlags.LocalMin;

        LocalMinima lm = new LocalMinima(vert, polytype);
        minimaList.add(lm);
    }

    protected final void AddPathsToVertexList(Paths64 paths, PathType polytype) {
        for (int i = 0, pathsSize = paths.size(); i < pathsSize; i++) {
            Path64 path = paths.get(i);
            Vertex v0 = null, prevV = null, currV;
            for (int j = 0, pathSize = path.size(); j < pathSize; j++) {
                Point64 pt = path.get(j);
                if (v0 == null) {
                    v0 = new Vertex(pt, VertexFlags.None, null);
                    prevV = v0;
                } else if (prevV.pt.opNotEquals(pt)) { // ie skips duplicates
                    currV = new Vertex(pt, VertexFlags.None, prevV);
                    prevV.next = currV;
                    prevV = currV;
                }
            }
            if (prevV == null || prevV.prev == null) {
                continue;
            }
            if (v0.pt.opEquals(prevV.pt)) {
                prevV = prevV.prev;
            }
            prevV.next = v0;
            v0.prev = prevV;
            if (prevV == prevV.next) {
                continue;
            }

            // OK, we have a valid path
            boolean goingup, goingup0;
            // closed path
            prevV = v0.prev;
            while (!v0.equals(prevV) && prevV.pt.y == v0.pt.y) {
                prevV = prevV.prev;
            }
            if (v0.equals(prevV)) {
                continue; // only open paths can be completely flat
            }
            goingup = prevV.pt.y > v0.pt.y;

            goingup0 = goingup;
            prevV = v0;
            currV = v0.next;
            while (!v0.equals(currV)) {
                if (currV.pt.y > prevV.pt.y && goingup) {
                    prevV.flags |= VertexFlags.LocalMax;
                    goingup = false;
                } else if (currV.pt.y < prevV.pt.y && !goingup) {
                    goingup = true;
                    AddLocMin(prevV, polytype);
                }
                prevV = currV;
                currV = currV.next;
            }

            if (goingup != goingup0) {
                if (goingup0) {
                    AddLocMin(prevV, polytype);
                } else {
                    prevV.flags = prevV.flags | VertexFlags.LocalMax;
                }
            }
        }
    }

    public final void AddSubject(Paths64 paths) {
        paths.forEach(path -> AddPath(path, PathType.SUBJECT));
    }

    public final void AddPath(Path64 path, PathType polytype) {
        Paths64 tmp = new Paths64();
        tmp.add(path);
        AddPaths(tmp, polytype);
    }

    public final void AddPaths(Paths64 paths, PathType polytype) {
        isSortedMinimaList = false;
        AddPathsToVertexList(paths, polytype);
    }

    private boolean IsContributingClosed(Active ae) {
        if (fillrule == FillRule.Positive) {
            if (ae.windCount != 1) {
                return false;
            }
        } else if (fillrule == FillRule.Negative) {
            if (ae.windCount != -1) {
                return false;
            }
        } else if (fillrule == FillRule.NonZero) {
            if (Math.abs(ae.windCount) != 1) {
                return false;
            }
        }

        if (cliptype == ClipType.Intersection) {
            if (fillrule == FillRule.Positive) {
                return ae.windCount2 > 0;
            } else if (fillrule == FillRule.Negative) {
                return ae.windCount2 < 0;
            }
            return ae.windCount2 != 0;
        } else if (cliptype == ClipType.Union) {
            if (fillrule == FillRule.Positive) {
                return ae.windCount2 <= 0;
            } else if (fillrule == FillRule.Negative) {
                return ae.windCount2 >= 0;
            }
            return ae.windCount2 == 0;
        } else if (cliptype == ClipType.Difference) {
            boolean result;
            if (fillrule == FillRule.Positive) {
                result = ae.windCount2 <= 0;
            } else if (fillrule == FillRule.Negative) {
                result = ae.windCount2 >= 0;
            } else {
                result = ae.windCount2 == 0;
            }
            return (GetPolyType(ae) == PathType.SUBJECT) == result;
        } else return cliptype == ClipType.Xor; // XOr is always contributing unless open
    }

    private void SetWindCountForClosedPathEdge(Active ae) {
        /*
         * Wind counts refer to polygon regions not edges, so here an edge's WindCnt
         * indicates the higher of the wind counts for the two regions touching the
         * edge. (nb: Adjacent regions can only ever have their wind counts differ by
         * one. Also, open paths have no meaningful wind directions or counts.)
         */

        Active ae2 = ae.prevInAEL;
        // find the nearest closed path edge of the same PolyType in AEL (heading left)
        PathType pt = GetPolyType(ae);
        while (ae2 != null && GetPolyType(ae2) != pt) {
            ae2 = ae2.prevInAEL;
        }

        if (ae2 == null) {
            ae.windCount = ae.windDx;
            ae2 = actives;
        } else if (fillrule == FillRule.EvenOdd) {
            ae.windCount = ae.windDx;
            ae.windCount2 = ae2.windCount2;
            ae2 = ae2.nextInAEL;
        } else {
            // NonZero, positive, or negative filling here ...
            // when e2's WindCnt is in the SAME direction as its WindDx,
            // then polygon will fill on the right of 'e2' (and 'e' will be inside)
            // nb: neither e2.WindCnt nor e2.WindDx should ever be 0.
            if (ae2.windCount * ae2.windDx < 0) {
                // opposite directions so 'ae' is outside 'ae2' ...
                if (Math.abs(ae2.windCount) > 1) {
                    // outside prev poly but still inside another.
                    if (ae2.windDx * ae.windDx < 0) {
                        // reversing direction so use the same WC
                        ae.windCount = ae2.windCount;
                    } else {
                        // otherwise keep 'reducing' the WC by 1 (i.e. towards 0) ...
                        ae.windCount = ae2.windCount + ae.windDx;
                    }
                } else {
                    // now outside all polys of same polytype so set own WC ...
                    ae.windCount = ae.windDx;
                }
            } else // 'ae' must be inside 'ae2'
                if (ae2.windDx * ae.windDx < 0) {
                    // reversing direction so use the same WC
                    ae.windCount = ae2.windCount;
                } else {
                    // otherwise keep 'increasing' the WC by 1 (i.e. away from 0) ...
                    ae.windCount = ae2.windCount + ae.windDx;
                }

            ae.windCount2 = ae2.windCount2;
            ae2 = ae2.nextInAEL; // i.e. get ready to calc WindCnt2
        }

        // update windCount2 ...
        if (fillrule == FillRule.EvenOdd) {
            while (!ae2.equals(ae)) {
                if (GetPolyType(ae2) != pt) {
                    ae.windCount2 = (ae.windCount2 == 0 ? 1 : 0);
                }
                ae2 = ae2.nextInAEL;
            }
        } else {
            while (!ae2.equals(ae)) {
                if (GetPolyType(ae2) != pt) {
                    ae.windCount2 += ae2.windDx;
                }
                ae2 = ae2.nextInAEL;
            }
        }
    }

    private static boolean IsValidAelOrder(Active resident, Active newcomer) {
        if (newcomer.curX != resident.curX) {
            return newcomer.curX > resident.curX;
        }

        // get the turning direction a1.top, a2.bot, a2.top
        double d = InternalClipper.CrossProduct(resident.top, newcomer.bot, newcomer.top);
        if (d != 0) {
            return (d < 0);
        }

        // edges must be collinear to get here

        // for starting open paths, place them according to
        // the direction they're about to turn
        if (!IsMaxima(resident) && (resident.top.y > newcomer.top.y)) {
            return InternalClipper.CrossProduct(newcomer.bot, resident.top, NextVertex(resident).pt) <= 0;
        }

        if (!IsMaxima(newcomer) && (newcomer.top.y > resident.top.y)) {
            return InternalClipper.CrossProduct(newcomer.bot, newcomer.top, NextVertex(newcomer).pt) >= 0;
        }

        double y = newcomer.bot.y;
        boolean newcomerIsLeft = newcomer.isLeftBound;

        if (resident.bot.y != y || resident.localMin.vertex.pt.y != y) {
            return newcomer.isLeftBound;
        }
        // resident must also have just been inserted
        if (resident.isLeftBound != newcomerIsLeft) {
            return newcomerIsLeft;
        }
        if (InternalClipper.CrossProduct(PrevPrevVertex(resident).pt, resident.bot, resident.top) == 0) {
            return true;
        }
        // compare turning direction of the alternate bound
        return (InternalClipper.CrossProduct(PrevPrevVertex(resident).pt, newcomer.bot, PrevPrevVertex(newcomer).pt) > 0) == newcomerIsLeft;
    }

    private void InsertLeftEdge(Active ae) {
        Active ae2;

        if (actives == null) {
            ae.prevInAEL = null;
            ae.nextInAEL = null;
            actives = ae;
        } else if (!IsValidAelOrder(actives, ae)) {
            ae.prevInAEL = null;
            ae.nextInAEL = actives;
            actives.prevInAEL = ae;
            actives = ae;
        } else {
            ae2 = actives;
            while (ae2.nextInAEL != null && IsValidAelOrder(ae2.nextInAEL, ae)) {
                ae2 = ae2.nextInAEL;
            }
            ae.nextInAEL = ae2.nextInAEL;
            if (ae2.nextInAEL != null) {
                ae2.nextInAEL.prevInAEL = ae;
            }
            ae.prevInAEL = ae2;
            ae2.nextInAEL = ae;
        }
    }

    private static void InsertRightEdge(Active ae, Active ae2) {
        ae2.nextInAEL = ae.nextInAEL;
        if (ae.nextInAEL != null) {
            ae.nextInAEL.prevInAEL = ae2;
        }
        ae2.prevInAEL = ae;
        ae.nextInAEL = ae2;
    }

    private void InsertLocalMinimaIntoAEL(long botY) {
        LocalMinima localMinima;
        Active leftBound, rightBound;
        // Add any local minima (if any) at BotY ...
        // NB horizontal local minima edges should contain locMin.vertex.prev
        while (HasLocMinAtY(botY)) {
            localMinima = PopLocalMinima();
            if ((localMinima.vertex.flags & VertexFlags.OpenStart) != VertexFlags.None) {
                leftBound = null;
            } else {
                leftBound = new Active();
                leftBound.bot = localMinima.vertex.pt;
                leftBound.curX = localMinima.vertex.pt.x;
                leftBound.windDx = -1;
                leftBound.vertexTop = localMinima.vertex.prev;
                leftBound.top = localMinima.vertex.prev.pt;
                leftBound.outrec = null;
                leftBound.localMin = localMinima;
                SetDx(leftBound);
            }

            if ((localMinima.vertex.flags & VertexFlags.OpenEnd) != VertexFlags.None) {
                rightBound = null;
            } else {
                rightBound = new Active();
                rightBound.bot = localMinima.vertex.pt;
                rightBound.curX = localMinima.vertex.pt.x;
                rightBound.windDx = 1;
                rightBound.vertexTop = localMinima.vertex.next;
                rightBound.top = localMinima.vertex.next.pt;
                rightBound.outrec = null;
                rightBound.localMin = localMinima;
                SetDx(rightBound);
            }

            // Currently LeftB is just the descending bound and RightB is the ascending.
            // Now if the LeftB isn't on the left of RightB then we need swap them.
            if (leftBound != null && rightBound != null) {
                if (IsHorizontal(leftBound)) {
                    if (IsHeadingRightHorz(leftBound)) {
                        RefObject<Active> tempRefleftBound = new RefObject<>(leftBound);
                        RefObject<Active> tempRefrightBound = new RefObject<>(rightBound);
                        SwapActives(tempRefleftBound, tempRefrightBound);
                        rightBound = tempRefrightBound.argValue;
                        leftBound = tempRefleftBound.argValue;
                    }
                } else if (IsHorizontal(rightBound)) {
                    if (IsHeadingLeftHorz(rightBound)) {
                        RefObject<Active> tempRefleftBound2 = new RefObject<>(leftBound);
                        RefObject<Active> tempRefrightBound2 = new RefObject<>(rightBound);
                        SwapActives(tempRefleftBound2, tempRefrightBound2);
                        rightBound = tempRefrightBound2.argValue;
                        leftBound = tempRefleftBound2.argValue;
                    }
                } else if (leftBound.dx < rightBound.dx) {
                    RefObject<Active> tempRefleftBound3 = new RefObject<>(leftBound);
                    RefObject<Active> tempRefrightBound3 = new RefObject<>(rightBound);
                    SwapActives(tempRefleftBound3, tempRefrightBound3);
                    rightBound = tempRefrightBound3.argValue;
                    leftBound = tempRefleftBound3.argValue;
                }
                // so when leftBound has windDx == 1, the polygon will be oriented
                // counter-clockwise in Cartesian coords (clockwise with inverted y).
            } else if (leftBound == null) {
                leftBound = rightBound;
                rightBound = null;
            }

            boolean contributing;
            leftBound.isLeftBound = true;
            InsertLeftEdge(leftBound);

            SetWindCountForClosedPathEdge(leftBound);
            contributing = IsContributingClosed(leftBound);

            if (rightBound != null) {
                rightBound.windCount = leftBound.windCount;
                rightBound.windCount2 = leftBound.windCount2;
                InsertRightEdge(leftBound, rightBound); ///////

                if (contributing) {
                    AddLocalMinPoly(leftBound, rightBound, leftBound.bot, true);
                    if (!IsHorizontal(leftBound) && TestJoinWithPrev1(leftBound)) {
                        OutPt op = AddOutPt(leftBound.prevInAEL, leftBound.bot);
                        AddJoin(op, leftBound.outrec.pts);
                    }
                }

                while (rightBound.nextInAEL != null && IsValidAelOrder(rightBound.nextInAEL, rightBound)) {
                    IntersectEdges(rightBound, rightBound.nextInAEL, rightBound.bot);
                    SwapPositionsInAEL(rightBound, rightBound.nextInAEL);
                }

                if (!IsHorizontal(rightBound) && TestJoinWithNext1(rightBound)) {
                    OutPt op = AddOutPt(rightBound.nextInAEL, rightBound.bot);
                    AddJoin(rightBound.outrec.pts, op);
                }

                if (IsHorizontal(rightBound)) {
                    PushHorz(rightBound);
                } else {
                    scanlineSet.add(rightBound.top.y);
                }
            } else if (contributing) {
                StartOpenPath(leftBound, leftBound.bot);
            }

            if (IsHorizontal(leftBound)) {
                PushHorz(leftBound);
            } else {
                scanlineSet.add(leftBound.top.y);
            }
        } // while (HasLocMinAtY())
    }

    private void PushHorz(Active ae) {
        ae.nextInSEL = sel;
        sel = ae;
    }

    private boolean PopHorz(OutObject<Active> ae) {
        ae.argValue = sel;
        if (sel == null) {
            return false;
        }
        sel = sel.nextInSEL;
        return true;
    }

    private boolean TestJoinWithPrev1(Active e) {
        // this is marginally quicker than TestJoinWithPrev2
        // but can only be used when e.PrevInAEL.currX is accurate
        return IsHotEdge(e) && (e.prevInAEL != null) && (e.prevInAEL.curX == e.curX) && IsHotEdge(e.prevInAEL) && (InternalClipper.CrossProduct(e.prevInAEL.top, e.bot, e.top) == 0);
    }

    private boolean TestJoinWithPrev2(Active e, Point64 currPt) {
        return IsHotEdge(e) && (e.prevInAEL != null) && IsHotEdge(e.prevInAEL)
                && (e.prevInAEL.top.y < e.bot.y) && (Math.abs(TopX(e.prevInAEL, currPt.y) - currPt.x) < 2) && (InternalClipper.CrossProduct(e.prevInAEL.top, currPt, e.top) == 0);
    }

    private static boolean TestJoinWithNext1(Active e) {
        // this is marginally quicker than TestJoinWithNext2
        // but can only be used when e.NextInAEL.currX is accurate
        return IsHotEdge(e) && (e.nextInAEL != null) && (e.nextInAEL.curX == e.curX) && IsHotEdge(e.nextInAEL) && (InternalClipper.CrossProduct(e.nextInAEL.top, e.bot, e.top) == 0);
    }

    private static boolean TestJoinWithNext2(Active e, Point64 currPt) {
        return IsHotEdge(e) && (e.nextInAEL != null) && IsHotEdge(e.nextInAEL)
                && (e.nextInAEL.top.y < e.bot.y) && (Math.abs(TopX(e.nextInAEL, currPt.y) - currPt.x) < 2)
                && (InternalClipper.CrossProduct(e.nextInAEL.top, currPt, e.top) == 0);
    }

    private OutPt AddLocalMinPoly(Active ae1, Active ae2, Point64 pt) {
        return AddLocalMinPoly(ae1, ae2, pt, false);
    }

    private OutPt AddLocalMinPoly(Active ae1, Active ae2, Point64 pt, boolean isNew) {
        OutRec outrec = new OutRec();
        outrecList.add(outrec);
        outrec.idx = outrecList.size() - 1;
        outrec.pts = null;
        ae1.outrec = outrec;
        ae2.outrec = outrec;

        // Setting the owner and inner/outer states (above) is an essential
        // precursor to setting edge 'sides' (ie left and right sides of output
        // polygons) and hence the orientation of output paths ...

        outrec.isOpen = false;
        Active prevHotEdge = GetPrevHotEdge(ae1);
        // e.windDx is the winding direction of the **input** paths
        // and unrelated to the winding direction of output polygons.
        // Output orientation is determined by e.outrec.frontE which is
        // the ascending edge (see AddLocalMinPoly).
        if (prevHotEdge != null) {
            outrec.owner = prevHotEdge.outrec;
            if (OutrecIsAscending(prevHotEdge) == isNew) {
                SetSides(outrec, ae2, ae1);
            } else {
                SetSides(outrec, ae1, ae2);
            }
        } else {
            outrec.owner = null;
            if (isNew) {
                SetSides(outrec, ae1, ae2);
            } else {
                SetSides(outrec, ae2, ae1);
            }
        }

        OutPt op = new OutPt(pt, outrec);
        outrec.pts = op;
        return op;
    }

    private OutPt AddLocalMaxPoly(Active ae1, Active ae2, Point64 pt) {
        if (IsFront(ae1) == IsFront(ae2)) {
            succeeded = false;
            return null;
        }

        OutPt result = AddOutPt(ae1, pt);
        if (ae1.outrec == ae2.outrec) {
            OutRec outrec = ae1.outrec;
            outrec.pts = result;
            UncoupleOutRec(ae1);
            CleanCollinear(outrec);
            result = outrec.pts;

            outrec.owner = GetRealOutRec(outrec.owner);
        } else if (ae1.outrec.idx < ae2.outrec.idx) {
            JoinOutrecPaths(ae1, ae2);
        } else {
            JoinOutrecPaths(ae2, ae1);
        }

        return result;
    }

    private static void JoinOutrecPaths(Active ae1, Active ae2) {
        // join ae2 outrec path onto ae1 outrec path and then delete ae2 outrec path
        // pointers. (NB Only very rarely do the joining ends share the same coords.)
        OutPt p1Start = ae1.outrec.pts;
        OutPt p2Start = ae2.outrec.pts;
        OutPt p1End = p1Start.next;
        OutPt p2End = p2Start.next;
        if (IsFront(ae1)) {
            p2End.prev = p1Start;
            p1Start.next = p2End;
            p2Start.next = p1End;
            p1End.prev = p2Start;
            ae1.outrec.pts = p2Start;
            // nb: if IsOpen(e1) then e1 & e2 must be a 'maximaPair'
            ae1.outrec.frontEdge = ae2.outrec.frontEdge;
            if (ae1.outrec.frontEdge != null) {
                ae1.outrec.frontEdge.outrec = ae1.outrec;
            }
        } else {
            p1End.prev = p2Start;
            p2Start.next = p1End;
            p1Start.next = p2End;
            p2End.prev = p1Start;

            ae1.outrec.backEdge = ae2.outrec.backEdge;
            if (ae1.outrec.backEdge != null) {
                ae1.outrec.backEdge.outrec = ae1.outrec;
            }
        }

        // an owner must have a lower idx otherwise
        // it won't be a valid owner
        if (ae2.outrec.owner != null && ae2.outrec.owner.idx < ae1.outrec.idx) {
            if (ae1.outrec.owner == null || ae2.outrec.owner.idx < ae1.outrec.owner.idx) {
                ae1.outrec.owner = ae2.outrec.owner;
            }
        }

        // after joining, the ae2.OutRec must contains no vertices ...
        ae2.outrec.frontEdge = null;
        ae2.outrec.backEdge = null;
        ae2.outrec.pts = null;
        ae2.outrec.owner = ae1.outrec; // this may be redundant

        // and ae1 and ae2 are maxima and are about to be dropped from the Actives list.
        ae1.outrec = null;
        ae2.outrec = null;
    }

    private static OutPt AddOutPt(Active ae, Point64 pt) {
        OutPt newOp;

        // Outrec.OutPts: a circular doubly-linked-list of POutPt where ...
        // opFront[.Prev]* ~~~> opBack & opBack == opFront.Next
        OutRec outrec = ae.outrec;
        boolean toFront = IsFront(ae);
        OutPt opFront = outrec.pts;
        OutPt opBack = opFront.next;

        if (toFront && (pt.opEquals(opFront.pt))) {
            newOp = opFront;
        } else if (!toFront && (pt.opEquals(opBack.pt))) {
            newOp = opBack;
        } else {
            newOp = new OutPt(pt, outrec);
            opBack.prev = newOp;
            newOp.prev = opFront;
            newOp.next = opBack;
            opFront.next = newOp;
            if (toFront) {
                outrec.pts = newOp;
            }
        }
        return newOp;
    }

    private OutPt StartOpenPath(Active ae, Point64 pt) {
        OutRec outrec = new OutRec();
        outrecList.add(outrec);
        outrec.idx = outrecList.size() - 1;
        outrec.owner = null;
        outrec.isOpen = true;
        outrec.pts = null;
        if (ae.windDx > 0) {
            outrec.frontEdge = ae;
            outrec.backEdge = null;
        } else {
            outrec.frontEdge = null;
            outrec.backEdge = ae;
        }

        ae.outrec = outrec;
        OutPt op = new OutPt(pt, outrec);
        outrec.pts = op;
        return op;
    }

    private void UpdateEdgeIntoAEL(Active ae) {
        ae.bot = ae.top;
        ae.vertexTop = NextVertex(ae);
        ae.top = ae.vertexTop.pt;
        ae.curX = ae.bot.x;
        SetDx(ae);
        if (IsHorizontal(ae)) {
            return;
        }
        scanlineSet.add(ae.top.y);
        if (TestJoinWithPrev1(ae)) {
            OutPt op1 = AddOutPt(ae.prevInAEL, ae.bot);
            OutPt op2 = AddOutPt(ae, ae.bot);
            AddJoin(op1, op2);
        }
    }

    private OutPt IntersectEdges(Active ae1, Active ae2, Point64 pt) {
        OutPt resultOp = null;

        // MANAGING CLOSED PATHS FROM HERE ON

        // UPDATE WINDING COUNTS...

        int oldE1WindCount, oldE2WindCount;
        if (ae1.localMin.polytype == ae2.localMin.polytype) {
            if (fillrule == FillRule.EvenOdd) {
                oldE1WindCount = ae1.windCount;
                ae1.windCount = ae2.windCount;
                ae2.windCount = oldE1WindCount;
            } else {
                if (ae1.windCount + ae2.windDx == 0) {
                    ae1.windCount = -ae1.windCount;
                } else {
                    ae1.windCount += ae2.windDx;
                }
                if (ae2.windCount - ae1.windDx == 0) {
                    ae2.windCount = -ae2.windCount;
                } else {
                    ae2.windCount -= ae1.windDx;
                }
            }
        } else {
            if (fillrule != FillRule.EvenOdd) {
                ae1.windCount2 += ae2.windDx;
            } else {
                ae1.windCount2 = (ae1.windCount2 == 0 ? 1 : 0);
            }
            if (fillrule != FillRule.EvenOdd) {
                ae2.windCount2 -= ae1.windDx;
            } else {
                ae2.windCount2 = (ae2.windCount2 == 0 ? 1 : 0);
            }
        }

        if (fillrule == FillRule.Positive) {
            oldE1WindCount = ae1.windCount;
            oldE2WindCount = ae2.windCount;
        } else if (fillrule == FillRule.Negative) {
            oldE1WindCount = -ae1.windCount;
            oldE2WindCount = -ae2.windCount;
        } else {
            oldE1WindCount = Math.abs(ae1.windCount);
            oldE2WindCount = Math.abs(ae2.windCount);
        }

        boolean e1WindCountIs0or1 = oldE1WindCount == 0 || oldE1WindCount == 1;
        boolean e2WindCountIs0or1 = oldE2WindCount == 0 || oldE2WindCount == 1;

        if ((!IsHotEdge(ae1) && !e1WindCountIs0or1) || (!IsHotEdge(ae2) && !e2WindCountIs0or1)) {
            return null;
        }

        // NOW PROCESS THE INTERSECTION ...

        // if both edges are 'hot' ...
        if (IsHotEdge(ae1) && IsHotEdge(ae2)) {
            if ((oldE1WindCount != 0 && oldE1WindCount != 1) || (oldE2WindCount != 0 && oldE2WindCount != 1)
                    || (ae1.localMin.polytype != ae2.localMin.polytype && cliptype != ClipType.Xor)) {
                resultOp = AddLocalMaxPoly(ae1, ae2, pt);
            } else if (IsFront(ae1) || (ae1.outrec == ae2.outrec)) {
                // this 'else if' condition isn't strictly needed but
                // it's sensible to split polygons that ony touch at
                // a common vertex (not at common edges).
                resultOp = AddLocalMaxPoly(ae1, ae2, pt);
                OutPt op2 = AddLocalMinPoly(ae1, ae2, pt);
                if (resultOp != null && resultOp.pt.opEquals(op2.pt) && !IsHorizontal(ae1) && !IsHorizontal(ae2)
                        && (InternalClipper.CrossProduct(ae1.bot, resultOp.pt, ae2.bot) == 0)) {
                    AddJoin(resultOp, op2);
                }
            } else {
                // can't treat as maxima & minima
                resultOp = AddOutPt(ae1, pt);
                AddOutPt(ae2, pt);
                SwapOutrecs(ae1, ae2);
            }
        }

        // if one or other edge is 'hot' ...
        else if (IsHotEdge(ae1)) {
            resultOp = AddOutPt(ae1, pt);
            SwapOutrecs(ae1, ae2);
        } else if (IsHotEdge(ae2)) {
            resultOp = AddOutPt(ae2, pt);
            SwapOutrecs(ae1, ae2);
        }

        // neither edge is 'hot'
        else {
            long e1Wc2, e2Wc2;
            if (fillrule == FillRule.Positive) {
                e1Wc2 = ae1.windCount2;
                e2Wc2 = ae2.windCount2;
            } else if (fillrule == FillRule.Negative) {
                e1Wc2 = -ae1.windCount2;
                e2Wc2 = -ae2.windCount2;
            } else {
                e1Wc2 = Math.abs(ae1.windCount2);
                e2Wc2 = Math.abs(ae2.windCount2);
            }

            if (!IsSamePolyType(ae1, ae2)) {
                resultOp = AddLocalMinPoly(ae1, ae2, pt);
            } else if (oldE1WindCount == 1 && oldE2WindCount == 1) {
                resultOp = null;
                // ClipType.Intersection:
                if (cliptype == ClipType.Union) {
                    if (e1Wc2 > 0 && e2Wc2 > 0) {
                        return null;
                    }
                    resultOp = AddLocalMinPoly(ae1, ae2, pt);
                } else if (cliptype == ClipType.Difference) {
                    if (((GetPolyType(ae1) == PathType.CLIP) && (e1Wc2 > 0) && (e2Wc2 > 0))
                            || ((GetPolyType(ae1) == PathType.SUBJECT) && (e1Wc2 <= 0) && (e2Wc2 <= 0))) {
                        resultOp = AddLocalMinPoly(ae1, ae2, pt);
                    }
                } else if (cliptype == ClipType.Xor) {
                    resultOp = AddLocalMinPoly(ae1, ae2, pt);
                } else {
                    if (e1Wc2 <= 0 || e2Wc2 <= 0) {
                        return null;
                    }
                    resultOp = AddLocalMinPoly(ae1, ae2, pt);
                }
            }
        }

        return resultOp;
    }

    private void DeleteFromAEL(Active ae) {
        Active prev = ae.prevInAEL;
        Active next = ae.nextInAEL;
        if (prev == null && next == null && (!actives.equals(ae))) {
            return; // already deleted
        }
        if (prev != null) {
            prev.nextInAEL = next;
        } else {
            actives = next;
        }
        if (next != null) {
            next.prevInAEL = prev;
        }
        // delete &ae;
    }

    private void AdjustCurrXAndCopyToSEL(long topY) {
        Active ae = actives;
        sel = ae;
        while (ae != null) {
            ae.prevInSEL = ae.prevInAEL;
            ae.nextInSEL = ae.nextInAEL;
            ae.jump = ae.nextInSEL;
            ae.curX = TopX(ae, topY);
            // NB don't update ae.curr.Y yet (see AddNewIntersectNode)
            ae = ae.nextInAEL;
        }
    }

    protected final void executeInternalDifference(FillRule fillRule) {
        fillrule = fillRule;
        cliptype = ClipType.Difference;
        Reset();
        if (scanlineSet.isEmpty()) {
            return;
        }
        long y = scanlineSet.pollLast();
        while (succeeded) {
            InsertLocalMinimaIntoAEL(y);
            Active ae;
            OutObject<Active> tempOutae = new OutObject<>();
            while (PopHorz(tempOutae)) {
                ae = tempOutae.argValue;
                DoHorizontal(ae);
            }
            ConvertHorzTrialsToJoins();
            currentBotY = y; // bottom of scanbeam
            if (scanlineSet.isEmpty()) {
                break; // y new top of scanbeam
            }
            y = scanlineSet.pollLast();
            DoIntersections(y);
            DoTopOfScanbeam(y);
            OutObject<Active> tempOutae2 = new OutObject<>();
            while (PopHorz(tempOutae2)) {
                ae = tempOutae2.argValue;
                DoHorizontal(ae);
            }
        }

        if (succeeded) {
            ProcessJoinList();
        }
    }

    protected final void ExecuteInternal(ClipType ct, FillRule fillRule) {
        if (ct == ClipType.None) {
            return;
        }
        fillrule = fillRule;
        cliptype = ct;
        Reset();
        if (scanlineSet.isEmpty()) {
            return;
        }
        long y = scanlineSet.pollLast();
        while (succeeded) {
            InsertLocalMinimaIntoAEL(y);
            Active ae;
            OutObject<Active> tempOutae = new OutObject<>();
            while (PopHorz(tempOutae)) {
                ae = tempOutae.argValue;
                DoHorizontal(ae);
            }
            ConvertHorzTrialsToJoins();
            currentBotY = y; // bottom of scanbeam
            if (scanlineSet.isEmpty()) {
                break; // y new top of scanbeam
            }
            y = scanlineSet.pollLast();
            DoIntersections(y);
            DoTopOfScanbeam(y);
            OutObject<Active> tempOutae2 = new OutObject<>();
            while (PopHorz(tempOutae2)) {
                ae = tempOutae2.argValue;
                DoHorizontal(ae);
            }
        }

        if (succeeded) {
            ProcessJoinList();
        }
    }

    private void DoIntersections(long topY) {
        if (BuildIntersectList(topY)) {
            ProcessIntersectList();
            DisposeIntersectNodes();
        }
    }

    private void DisposeIntersectNodes() {
        intersectList.clear();
    }

    private void AddNewIntersectNode(Active ae1, Active ae2, long topY) {
        Point64 pt = GetIntersectPoint(ae1, ae2);

        // rounding errors can occasionally place the calculated intersection
        // point either below or above the scanbeam, so check and correct ...
        if (pt.y > currentBotY) {
            // ae.curr.y is still the bottom of scanbeam
            // use the more vertical of the 2 edges to derive pt.x ...
            if (Math.abs(ae1.dx) < Math.abs(ae2.dx)) {
                pt = new Point64(TopX(ae1, currentBotY), currentBotY);
            } else {
                pt = new Point64(TopX(ae2, currentBotY), currentBotY);
            }
        } else if (pt.y < topY) {
            // topY is at the top of the scanbeam
            if (ae1.top.y == topY) {
                pt = new Point64(ae1.top.x, topY);
            } else if (ae2.top.y == topY) {
                pt = new Point64(ae2.top.x, topY);
            } else if (Math.abs(ae1.dx) < Math.abs(ae2.dx)) {
                pt = new Point64(ae1.curX, topY);
            } else {
                pt = new Point64(ae2.curX, topY);
            }
        }

        IntersectNode node = new IntersectNode(pt, ae1, ae2);
        intersectList.add(node);
    }

    private Active ExtractFromSEL(Active ae) {
        Active res = ae.nextInSEL;
        if (res != null) {
            res.prevInSEL = ae.prevInSEL;
        }
        ae.prevInSEL.nextInSEL = res;
        return res;
    }

    private static void Insert1Before2InSEL(Active ae1, Active ae2) {
        ae1.prevInSEL = ae2.prevInSEL;
        if (ae1.prevInSEL != null) {
            ae1.prevInSEL.nextInSEL = ae1;
        }
        ae1.nextInSEL = ae2;
        ae2.prevInSEL = ae1;
    }

    private boolean BuildIntersectList(long topY) {
        if (actives == null || actives.nextInAEL == null) {
            return false;
        }

        // Calculate edge positions at the top of the current scanbeam, and from this
        // we will determine the intersections required to reach these new positions.
        AdjustCurrXAndCopyToSEL(topY);

        // Find all edge intersections in the current scanbeam using a stable merge
        // sort that ensures only adjacent edges are intersecting. Intersect info is
        // stored in FIntersectList ready to be processed in ProcessIntersectList.
        // Re merge sorts see https://stackoverflow.com/a/46319131/359538

        Active left = sel, right, lEnd, rEnd, currBase, prevBase, tmp;

        while (left.jump != null) {
            prevBase = null;
            while (left != null && left.jump != null) {
                currBase = left;
                right = left.jump;
                lEnd = right;
                rEnd = right.jump;
                left.jump = rEnd;
                while (left != lEnd && right != rEnd) {
                    if (right.curX < left.curX) {
                        tmp = right.prevInSEL;
                        for (; ; ) {
                            AddNewIntersectNode(tmp, right, topY);
                            if (left.equals(tmp)) {
                                break;
                            }
                            tmp = tmp.prevInSEL;
                        }

                        tmp = right;
                        right = ExtractFromSEL(tmp);
                        lEnd = right;
                        Insert1Before2InSEL(tmp, left);
                        if (left.equals(currBase)) {
                            currBase = tmp;
                            currBase.jump = rEnd;
                            if (prevBase == null) {
                                sel = currBase;
                            } else {
                                prevBase.jump = currBase;
                            }
                        }
                    } else {
                        left = left.nextInSEL;
                    }
                }

                prevBase = currBase;
                left = rEnd;
            }
            left = sel;
        }

        return !intersectList.isEmpty();
    }

    private void ProcessIntersectList() {
        // We now have a list of intersections required so that edges will be
        // correctly positioned at the top of the scanbeam. However, it's important
        // that edge intersections are processed from the bottom up, but it's also
        // crucial that intersections only occur between adjacent edges.

        // First we do a quicksort so intersections proceed in a bottom up order ...
        intersectList.sort((a, b) -> {
            if (a.pt.y == b.pt.y) {
                if (a.pt.x == b.pt.x) {
                    return 0;
                }
                return (a.pt.x < b.pt.x) ? -1 : 1;
            }
            return (a.pt.y > b.pt.y) ? -1 : 1;
        });

        // Now as we process these intersections, we must sometimes adjust the order
        // to ensure that intersecting edges are always adjacent ...
        for (int i = 0; i < intersectList.size(); ++i) {
            if (!EdgesAdjacentInAEL(intersectList.get(i))) {
                int j = i + 1;
                while (!EdgesAdjacentInAEL(intersectList.get(j))) {
                    j++;
                }
                // swap
                Collections.swap(intersectList, i, j);
            }

            IntersectNode node = intersectList.get(i);
            IntersectEdges(node.edge1, node.edge2, node.pt);
            SwapPositionsInAEL(node.edge1, node.edge2);

            if (TestJoinWithPrev2(node.edge2, node.pt)) {
                OutPt op1 = AddOutPt(node.edge2.prevInAEL, node.pt);
                OutPt op2 = AddOutPt(node.edge2, node.pt);
                if (op1 != op2) {
                    AddJoin(op1, op2);
                }
            } else if (TestJoinWithNext2(node.edge1, node.pt)) {
                OutPt op1 = AddOutPt(node.edge1, node.pt);
                OutPt op2 = AddOutPt(node.edge1.nextInAEL, node.pt);
                if (op1 != op2) {
                    AddJoin(op1, op2);
                }
            }
        }
    }

    private void SwapPositionsInAEL(Active ae1, Active ae2) {
        // preconditon: ae1 must be immediately to the left of ae2
        Active next = ae2.nextInAEL;
        if (next != null) {
            next.prevInAEL = ae1;
        }
        Active prev = ae1.prevInAEL;
        if (prev != null) {
            prev.nextInAEL = ae2;
        }
        ae2.prevInAEL = prev;
        ae2.nextInAEL = ae1;
        ae1.prevInAEL = ae2;
        ae1.nextInAEL = next;
        if (ae2.prevInAEL == null) {
            actives = ae2;
        }
    }

    private static boolean ResetHorzDirection(Active horz, Active maxPair, OutObject<Long> leftX, OutObject<Long> rightX) {
        if (horz.bot.x == horz.top.x) {
            // the horizontal edge is going nowhere ...
            leftX.argValue = horz.curX;
            rightX.argValue = horz.curX;
            Active ae = horz.nextInAEL;
            while (ae != null && !maxPair.equals(ae)) {
                ae = ae.nextInAEL;
            }
            return ae != null;
        }

        if (horz.curX < horz.top.x) {
            leftX.argValue = horz.curX;
            rightX.argValue = horz.top.x;
            return true;
        }
        leftX.argValue = horz.top.x;
        rightX.argValue = horz.curX;
        return false; // right to left
    }

    private static boolean HorzIsSpike(Active horz) {
        Point64 nextPt = NextVertex(horz).pt;
        return (horz.bot.x < horz.top.x) != (horz.top.x < nextPt.x);
    }

    private void TrimHorz(Active horzEdge, boolean preserveCollinear) {
        boolean wasTrimmed = false;
        Point64 pt = NextVertex(horzEdge).pt;

        while (pt.y == horzEdge.top.y) {
            // always trim 180 deg. spikes (in closed paths)
            // but otherwise break if preserveCollinear = true
            if (preserveCollinear && (pt.x < horzEdge.top.x) != (horzEdge.bot.x < horzEdge.top.x)) {
                break;
            }

            horzEdge.vertexTop = NextVertex(horzEdge);
            horzEdge.top = pt;
            wasTrimmed = true;
            if (IsMaxima(horzEdge)) {
                break;
            }
            pt = NextVertex(horzEdge).pt;
        }
        if (wasTrimmed) {
            SetDx(horzEdge); // +/-infinity
        }
    }

    private void DoHorizontal(Active horz)
        /*-
         * Notes: Horizontal edges (HEs) at scanline intersections (i.e. at the top or  *
         * bottom of a scanbeam) are processed as if layered.The order in which HEs     *
         * are processed doesn't matter. HEs intersect with the bottom vertices of      *
         * other HEs[#] and with non-horizontal edges [*]. Once these intersections     *
         * are completed, intermediate HEs are 'promoted' to the next edge in their     *
         * bounds, and they in turn may be intersected[%] by other HEs.                 *
         *                                                                              *
         * eg: 3 horizontals at a scanline:    /   |                     /           /  *
         *              |                     /    |     (HE3)o ========%========== o   *
         *              o ======= o(HE2)     /     |         /         /                *
         *          o ============#=========*======*========#=========o (HE1)           *
         *         /              |        /       |       /                            *
         *******************************************************************************/ {
        Point64 pt;
        double Y = horz.bot.y;

        Vertex vertexmax;
        Active maxPair = null;

        vertexmax = GetCurrYMaximaVertex(horz);
        if (vertexmax != null) {
            maxPair = GetHorzMaximaPair(horz, vertexmax);
            // remove 180 deg.spikes and also simplify
            // consecutive horizontals when preserveCollinear = true
            if (!vertexmax.equals(horz.vertexTop)) {
                TrimHorz(horz, preserveCollinear);
            }
        }

        double leftX;
        OutObject<Long> tempOutleftX = new OutObject<>();
        double rightX;
        OutObject<Long> tempOutrightX = new OutObject<>();
        boolean isLeftToRight = ResetHorzDirection(horz, maxPair, tempOutleftX, tempOutrightX);
        rightX = tempOutrightX.argValue;
        leftX = tempOutleftX.argValue;

        if (IsHotEdge(horz)) {
            AddOutPt(horz, new Point64(horz.curX, Y));
        }

        OutPt op;
        for (; ; ) {
            // loops through consec. horizontal edges (if open)
            Active ae;
            if (isLeftToRight) {
                ae = horz.nextInAEL;
            } else {
                ae = horz.prevInAEL;
            }

            while (ae != null) {
                if (ae == maxPair) {
                    if (IsHotEdge(horz)) {
                        while (horz.vertexTop != ae.vertexTop) {
                            AddOutPt(horz, horz.top);
                            UpdateEdgeIntoAEL(horz);
                        }
                        op = AddLocalMaxPoly(horz, ae, horz.top);
                        if (op != null && op.pt.opEquals(horz.top)) {
                            AddTrialHorzJoin(op);
                        }
                    }

                    DeleteFromAEL(ae);
                    DeleteFromAEL(horz);
                    return;
                }

                // if horzEdge is a maxima, keep going until we reach
                // its maxima pair, otherwise check for break conditions
                if (vertexmax != horz.vertexTop) {
                    // otherwise stop when 'ae' is beyond the end of the horizontal line
                    if (isLeftToRight ? ae.curX > rightX : ae.curX < leftX) {
                        break;
                    }

                    if (ae.curX == horz.top.x && !IsHorizontal(ae)) {
                        pt = NextVertex(horz).pt;
                        if (isLeftToRight) {
                            // otherwise we'll only break when horz's outslope is greater than e's
                            if (TopX(ae, pt.y) >= pt.x) {
                                break;
                            }
                        } else // with open paths we'll only break once past horz's end
                            // otherwise we'll only break when horz's outslope is greater than e's
                            if (TopX(ae, pt.y) <= pt.x) {
                                break;
                            }
                    }
                }

                pt = new Point64(ae.curX, Y);

                if (isLeftToRight) {
                    op = IntersectEdges(horz, ae, pt);
                    SwapPositionsInAEL(horz, ae);

                    if (IsHotEdge(horz) && op != null && op.pt.opEquals(pt)) {
                        AddTrialHorzJoin(op);
                    }

                    if (!IsHorizontal(ae) && TestJoinWithPrev1(ae)) {
                        op = AddOutPt(ae.prevInAEL, pt);
                        OutPt op2 = AddOutPt(ae, pt);
                        AddJoin(op, op2);
                    }

                    horz.curX = ae.curX;
                    ae = horz.nextInAEL;
                } else {
                    op = IntersectEdges(ae, horz, pt);
                    SwapPositionsInAEL(ae, horz);

                    if (IsHotEdge(horz) && op != null && op.pt.opEquals(pt)) {
                        AddTrialHorzJoin(op);
                    }

                    if (!IsHorizontal(ae) && TestJoinWithNext1(ae)) {
                        op = AddOutPt(ae, pt);
                        OutPt op2 = AddOutPt(ae.nextInAEL, pt);
                        AddJoin(op, op2);
                    }

                    horz.curX = ae.curX;
                    ae = horz.prevInAEL;
                }
            } // we've reached the end of this horizontal

            if (NextVertex(horz).pt.y != horz.top.y) {
                break;
            }

            // there must be a following (consecutive) horizontal
            if (IsHotEdge(horz)) {
                AddOutPt(horz, horz.top);
            }
            UpdateEdgeIntoAEL(horz);

            if (preserveCollinear && HorzIsSpike(horz)) {
                TrimHorz(horz, true);
            }

            OutObject<Long> tempOutleftX2 = new OutObject<>();
            OutObject<Long> tempOutrightX2 = new OutObject<>();
            isLeftToRight = ResetHorzDirection(horz, maxPair, tempOutleftX2, tempOutrightX2);
            rightX = tempOutrightX2.argValue;
            leftX = tempOutleftX2.argValue;

        } // end for loop and end of (possible consecutive) horizontals

        if (IsHotEdge(horz)) {
            op = AddOutPt(horz, horz.top);
            AddTrialHorzJoin(op);
        } else {
            op = null;
        }

        if (vertexmax != horz.vertexTop) {
            UpdateEdgeIntoAEL(horz); // this is the end of an intermediate horiz.

            if (isLeftToRight && TestJoinWithNext1(horz)) {
                OutPt op2 = AddOutPt(horz.nextInAEL, horz.bot);
                AddJoin(op, op2);
            } else if (!isLeftToRight && TestJoinWithPrev1(horz)) {
                OutPt op2 = AddOutPt(horz.prevInAEL, horz.bot);
                AddJoin(op2, op);
            }
        } else if (IsHotEdge(horz)) {
            AddLocalMaxPoly(horz, maxPair, horz.top);
        } else {
            DeleteFromAEL(maxPair);
            DeleteFromAEL(horz);
        }
    }

    private void DoTopOfScanbeam(long y) {
        sel = null; // sel is reused to flag horizontals (see PushHorz below)
        Active ae = actives;
        while (ae != null) {
            // NB 'ae' will never be horizontal here
            if (ae.top.y == y) {
                ae.curX = ae.top.x;
                if (IsMaxima(ae)) {
                    ae = DoMaxima(ae); // TOP OF BOUND (MAXIMA)
                    continue;
                }

                // INTERMEDIATE VERTEX ...
                if (IsHotEdge(ae)) {
                    AddOutPt(ae, ae.top);
                }
                UpdateEdgeIntoAEL(ae);
                if (IsHorizontal(ae)) {
                    PushHorz(ae); // horizontals are processed later
                }
            } else { // i.e. not the top of the edge
                ae.curX = TopX(ae, y);
            }

            ae = ae.nextInAEL;
        }
    }

    private Active DoMaxima(Active ae) {
        Active prevE;
        Active nextE, maxPair;
        prevE = ae.prevInAEL;
        nextE = ae.nextInAEL;

        maxPair = GetMaximaPair(ae);
        if (maxPair == null) {
            return nextE; // eMaxPair is horizontal
        }

        // only non-horizontal maxima here.
        // process any edges between maxima pair ...
        while (!nextE.equals(maxPair)) {
            IntersectEdges(ae, nextE, ae.top);
            SwapPositionsInAEL(ae, nextE);
            nextE = ae.nextInAEL;
        }

        // here ae.nextInAel == ENext == EMaxPair ...
        if (IsHotEdge(ae)) {
            AddLocalMaxPoly(ae, maxPair, ae.top);
        }

        DeleteFromAEL(ae);
        DeleteFromAEL(maxPair);
        return (prevE != null ? prevE.nextInAEL : actives);
    }

    private static boolean IsValidPath(OutPt op) {
        return (op.next != op);
    }

    private static boolean AreReallyClose(Point64 pt1, Point64 pt2) {
        return (Math.abs(pt1.x - pt2.x) < 2) && (Math.abs(pt1.y - pt2.y) < 2);
    }

    private static boolean IsValidClosedPath(OutPt op) {
        return (op != null && !op.equals(op.next) && op.next != op.prev
                && !(op.next.next == op.prev && (AreReallyClose(op.pt, op.next.pt) || AreReallyClose(op.pt, op.prev.pt))));
    }

    private static boolean ValueBetween(long val, long end1, long end2) {
        // NB accommodates axis aligned between where end1 == end2
        return ((val != end1) == (val != end2)) && ((val > end1) == (val < end2));
    }

    private static boolean ValueEqualOrBetween(long val, long end1, long end2) {
        return (val == end1) || (val == end2) || ((val > end1) == (val < end2));
    }

    private static boolean PointEqualOrBetween(Point64 pt, Point64 corner1, Point64 corner2) {
        // NB points may not be collinear
        return ValueEqualOrBetween(pt.x, corner1.x, corner2.x) && ValueEqualOrBetween(pt.y, corner1.y, corner2.y);
    }

    private static boolean PointBetween(Point64 pt, Point64 corner1, Point64 corner2) {
        // NB points may not be collinear
        return ValueBetween(pt.x, corner1.x, corner2.x) && ValueBetween(pt.y, corner1.y, corner2.y);
    }

    private static boolean CollinearSegsOverlap(Point64 seg1a, Point64 seg1b, Point64 seg2a, Point64 seg2b) {
        // precondition: seg1 and seg2 are collinear
        if (seg1a.x == seg1b.x) {
            if (seg2a.x != seg1a.x || seg2a.x != seg2b.x) {
                return false;
            }
        } else if (seg1a.x < seg1b.x) {
            if (seg2a.x < seg2b.x) {
                if (seg2a.x >= seg1b.x || seg2b.x <= seg1a.x) {
                    return false;
                }
            } else if (seg2b.x >= seg1b.x || seg2a.x <= seg1a.x) {
                return false;
            }
        } else if (seg2a.x < seg2b.x) {
            if (seg2a.x >= seg1a.x || seg2b.x <= seg1b.x) {
                return false;
            }
        } else if (seg2b.x >= seg1a.x || seg2a.x <= seg1b.x) {
            return false;
        }

        if (seg1a.y == seg1b.y) {
            return seg2a.y == seg1a.y && seg2a.y == seg2b.y;
        } else if (seg1a.y < seg1b.y) {
            if (seg2a.y < seg2b.y) {
                return !(seg2a.y >= seg1b.y) && !(seg2b.y <= seg1a.y);
            } else return !(seg2b.y >= seg1b.y) && !(seg2a.y <= seg1a.y);
        } else if (seg2a.y < seg2b.y) {
            return !(seg2a.y >= seg1a.y) && !(seg2b.y <= seg1b.y);
        } else return !(seg2b.y >= seg1a.y) && !(seg2a.y <= seg1b.y);
    }

    private static boolean HorzEdgesOverlap(double x1a, double x1b, double x2a, double x2b) {
        final double minOverlap = 2;
        if (x1a > x1b + minOverlap) {
            if (x2a > x2b + minOverlap) {
                return !((x1a <= x2b) || (x2a <= x1b));
            }
            return !((x1a <= x2a) || (x2b <= x1b));
        }

        if (x1b > x1a + minOverlap) {
            if (x2a > x2b + minOverlap) {
                return !((x1b <= x2b) || (x2a <= x1a));
            }
            return !((x1b <= x2a) || (x2b <= x1a));
        }
        return false;
    }

    private static Joiner GetHorzTrialParent(OutPt op) {
        Joiner joiner = op.joiner;
        while (joiner != null) {
            if (joiner.op1 == op) {
                if (joiner.next1 != null && joiner.next1.idx < 0) {
                    return joiner;
                }
                joiner = joiner.next1;
            } else {
                if (joiner.next2 != null && joiner.next2.idx < 0) {
                    return joiner;
                }
                joiner = joiner.next1;
            }
        }
        return joiner;
    }

    private boolean OutPtInTrialHorzList(OutPt op) {
        return op.joiner != null && ((op.joiner.idx < 0) || GetHorzTrialParent(op) != null);
    }

    private boolean ValidateClosedPathEx(RefObject<OutPt> op) {
        if (IsValidClosedPath(op.argValue)) {
            return true;
        }
        if (op.argValue != null) {
            SafeDisposeOutPts(op);
        }
        return false;
    }

    private static OutPt InsertOp(Point64 pt, OutPt insertAfter) {
        OutPt result = new OutPt(pt, insertAfter.outrec);
        result.next = insertAfter.next;
        insertAfter.next.prev = result;
        insertAfter.next = result;
        result.prev = insertAfter;
        return result;
    }

    private static OutPt DisposeOutPt(OutPt op) {
        OutPt result = (op.next == op ? null : op.next);
        op.prev.next = op.next;
        op.next.prev = op.prev;
        // op == null;
        return result;
    }

    private void SafeDisposeOutPts(RefObject<OutPt> op) {
        OutRec outRec = GetRealOutRec(op.argValue.outrec);
        if (outRec.frontEdge != null) {
            outRec.frontEdge.outrec = null;
        }
        if (outRec.backEdge != null) {
            outRec.backEdge.outrec = null;
        }

        op.argValue.prev.next = null;
        OutPt op2 = op.argValue;
        while (op2 != null) {
            SafeDeleteOutPtJoiners(op2);
            op2 = op2.next;
        }
        outRec.pts = null;
    }

    private void SafeDeleteOutPtJoiners(OutPt op) {
        Joiner joiner = op.joiner;
        if (joiner == null) {
            return;
        }

        while (joiner != null) {
            if (joiner.idx < 0) {
                DeleteTrialHorzJoin(op);
            } else if (horzJoiners != null) {
                if (OutPtInTrialHorzList(joiner.op1)) {
                    DeleteTrialHorzJoin(joiner.op1);
                }
                if (OutPtInTrialHorzList(joiner.op2)) {
                    DeleteTrialHorzJoin(joiner.op2);
                }
                DeleteJoin(joiner);
            } else {
                DeleteJoin(joiner);
            }
            joiner = op.joiner;
        }
    }

    private void AddTrialHorzJoin(OutPt op) {
        // make sure 'op' isn't added more than once
        if (!op.outrec.isOpen && !OutPtInTrialHorzList(op)) {
            horzJoiners = new Joiner(op, null, horzJoiners);
        }

    }

    private static Joiner FindTrialJoinParent(RefObject<Joiner> joiner, OutPt op) {
        Joiner parent = joiner.argValue;
        while (parent != null) {
            if (op == parent.op1) {
                if (parent.next1 != null && parent.next1.idx < 0) {
                    joiner.argValue = parent.next1;
                    return parent;
                }
                parent = parent.next1;
            } else {
                if (parent.next2 != null && parent.next2.idx < 0) {
                    joiner.argValue = parent.next2;
                    return parent;
                }
                parent = parent.next2;
            }
        }
        return null;
    }

    private void DeleteTrialHorzJoin(OutPt op) {
        if (horzJoiners == null) {
            return;
        }

        Joiner joiner = op.joiner;
        Joiner parentH, parentOp = null;
        while (joiner != null) {
            if (joiner.idx < 0) {
                // first remove joiner from FHorzTrials
                if (horzJoiners.equals(joiner)) {
                    horzJoiners = joiner.nextH;
                } else {
                    parentH = horzJoiners;
                    while (!joiner.equals(parentH.nextH)) {
                        parentH = parentH.nextH;
                    }
                    parentH.nextH = joiner.nextH;
                }

                // now remove joiner from op's joiner list
                if (parentOp == null) {
                    // joiner must be first one in list
                    op.joiner = joiner.next1;
                    // joiner == null;
                    joiner = op.joiner;
                } else {
                    // the trial joiner isn't first
                    if (op == parentOp.op1) {
                        parentOp.next1 = joiner.next1;
                    } else {
                        parentOp.next2 = joiner.next1;
                    }
                    // joiner = null;
                    joiner = parentOp;
                }
            } else {
                // not a trial join so look further along the linked list
                RefObject<Joiner> tempRefjoiner = new RefObject<>(joiner);
                parentOp = FindTrialJoinParent(tempRefjoiner, op);
                joiner = tempRefjoiner.argValue;
                if (parentOp == null) {
                    break;
                }
            }
            // loop in case there's more than one trial join
        }
    }

    private static boolean GetHorzExtendedHorzSeg(RefObject<OutPt> op, OutObject<OutPt> op2) {
        OutRec outRec = GetRealOutRec(op.argValue.outrec);
        op2.argValue = op.argValue;
        if (outRec.frontEdge != null) {
            while (op.argValue.prev != outRec.pts && op.argValue.prev.pt.y == op.argValue.pt.y) {
                op.argValue = op.argValue.prev;
            }
            while (op2.argValue != outRec.pts && op2.argValue.next.pt.y == op2.argValue.pt.y) {
                op2.argValue = op2.argValue.next;
            }
            return op2.argValue != op.argValue;
        }

        while (op.argValue.prev != op2.argValue && op.argValue.prev.pt.y == op.argValue.pt.y) {
            op.argValue = op.argValue.prev;
        }
        while (op2.argValue.next != op.argValue && op2.argValue.next.pt.y == op2.argValue.pt.y) {
            op2.argValue = op2.argValue.next;
        }
        return op2.argValue != op.argValue && op2.argValue.next != op.argValue;
    }

    private void ConvertHorzTrialsToJoins() {
        while (horzJoiners != null) {
            Joiner joiner = horzJoiners;
            horzJoiners = horzJoiners.nextH;
            OutPt op1a = joiner.op1;
            if (joiner.equals(op1a.joiner)) {
                op1a.joiner = joiner.next1;
            } else {
                Joiner joinerParent = FindJoinParent(joiner, op1a);
                if (joinerParent.op1 == op1a) {
                    joinerParent.next1 = joiner.next1;
                } else {
                    joinerParent.next2 = joiner.next1;
                }
            }

            RefObject<OutPt> tempRefop1a = new RefObject<>(op1a);
            OutPt op1b;
            OutObject<OutPt> tempOutop1b = new OutObject<>();
            if (!GetHorzExtendedHorzSeg(tempRefop1a, tempOutop1b)) {
                op1a = tempRefop1a.argValue;
                if (op1a.outrec.frontEdge == null) {
                    CleanCollinear(op1a.outrec);
                }
                continue;
            } else {
                op1b = tempOutop1b.argValue;
                op1a = tempRefop1a.argValue;
            }

            OutPt op2a;
            boolean joined = false;
            joiner = horzJoiners;
            while (joiner != null) {
                op2a = joiner.op1;
                RefObject<OutPt> tempRefop2a = new RefObject<>(op2a);
                OutPt op2b;
                OutObject<OutPt> tempOutop2b = new OutObject<>();
                if (GetHorzExtendedHorzSeg(tempRefop2a, tempOutop2b)
                        && HorzEdgesOverlap(op1a.pt.x, op1b.pt.x, op2a.pt.x, tempOutop2b.argValue.pt.x)) {
                    op2b = tempOutop2b.argValue;
                    op2a = tempRefop2a.argValue;
                    // overlap found so promote to a 'real' join
                    joined = true;
                    if (op1a.pt.opEquals(op2b.pt)) {
                        AddJoin(op1a, op2b);
                    } else if (op1b.pt.opEquals(op2a.pt)) {
                        AddJoin(op1b, op2a);
                    } else if (op1a.pt.opEquals(op2a.pt)) {
                        AddJoin(op1a, op2a);
                    } else if (op1b.pt.opEquals(op2b.pt)) {
                        AddJoin(op1b, op2b);
                    } else if (ValueBetween(op1a.pt.x, op2a.pt.x, op2b.pt.x)) {
                        AddJoin(op1a, InsertOp(op1a.pt, op2a));
                    } else if (ValueBetween(op1b.pt.x, op2a.pt.x, op2b.pt.x)) {
                        AddJoin(op1b, InsertOp(op1b.pt, op2a));
                    } else if (ValueBetween(op2a.pt.x, op1a.pt.x, op1b.pt.x)) {
                        AddJoin(op2a, InsertOp(op2a.pt, op1a));
                    } else if (ValueBetween(op2b.pt.x, op1a.pt.x, op1b.pt.x)) {
                        AddJoin(op2b, InsertOp(op2b.pt, op1a));
                    }
                    break;
                }
                joiner = joiner.nextH;
            }
            if (!joined) {
                CleanCollinear(op1a.outrec);
            }
        }
    }

    private void AddJoin(OutPt op1, OutPt op2) {
        if ((op1.outrec == op2.outrec) && ((op1 == op2) ||
                // unless op1.next or op1.prev crosses the start-end divide
                // don't waste time trying to join adjacent vertices
                ((op1.next == op2) && (op1 != op1.outrec.pts)) || ((op2.next == op1) && (op2 != op1.outrec.pts)))) {
            return;
        }

        Joiner joiner = new Joiner(op1, op2, null);
        joiner.idx = joinerList.size();
        joinerList.add(joiner);
    }

    private static Joiner FindJoinParent(Joiner joiner, OutPt op) {
        Joiner result = op.joiner;
        for (; ; ) {
            if (op == result.op1) {
                if (result.next1 == joiner) {
                    return result;
                }
                result = result.next1;
            } else {
                if (result.next2 == joiner) {
                    return result;
                }
                result = result.next2;
            }
        }
    }

    private void DeleteJoin(Joiner joiner) {
        // This method deletes a single join, and it doesn't check for or
        // delete trial horz. joins. For that, use the following method.
        OutPt op1 = joiner.op1, op2 = joiner.op2;

        Joiner parentJnr;
        if (op1.joiner != joiner) {
            parentJnr = FindJoinParent(joiner, op1);
            if (parentJnr.op1 == op1) {
                parentJnr.next1 = joiner.next1;
            } else {
                parentJnr.next2 = joiner.next1;
            }
        } else {
            op1.joiner = joiner.next1;
        }

        if (op2.joiner != joiner) {
            parentJnr = FindJoinParent(joiner, op2);
            if (parentJnr.op1 == op2) {
                parentJnr.next1 = joiner.next2;
            } else {
                parentJnr.next2 = joiner.next2;
            }
        } else {
            op2.joiner = joiner.next2;
        }

        joinerList.set(joiner.idx, null);
    }

    private void ProcessJoinList() {
        // NB can't use foreach here because list may
        // contain nulls which can't be enumerated
        for (Joiner j : joinerList) {
            if (j == null) {
                continue;
            }
            OutRec outrec = ProcessJoin(j);
            CleanCollinear(outrec);
        }
        joinerList.clear();
    }

    private static boolean CheckDisposeAdjacent(RefObject<OutPt> op, OutPt guard, OutRec outRec) {
        boolean result = false;
        while (op.argValue.prev != op.argValue) {
            if (op.argValue.pt.opEquals(op.argValue.prev.pt) && op.argValue != guard && op.argValue.prev.joiner != null
                    && op.argValue.joiner == null) {
                if (op.argValue == outRec.pts) {
                    outRec.pts = op.argValue.prev;
                }
                op.argValue = DisposeOutPt(op.argValue);
                op.argValue = op.argValue.prev;
            } else {
                break;
            }
        }

        while (op.argValue.next != op.argValue) {
            if (op.argValue.pt.opEquals(op.argValue.next.pt) && op.argValue != guard && op.argValue.next.joiner != null
                    && op.argValue.joiner == null) {
                if (op.argValue == outRec.pts) {
                    outRec.pts = op.argValue.prev;
                }
                op.argValue = DisposeOutPt(op.argValue);
                op.argValue = op.argValue.prev;
            } else {
                break;
            }
        }
        return result;
    }

    private static double DistanceFromLineSqrd(Point64 pt, Point64 linePt1, Point64 linePt2) {
        // perpendicular distance of point (x0,y0) = (a*x0 + b*y0 + C)/Sqrt(a*a + b*b)
        // where ax + by +c = 0 is the equation of the line
        // see https://en.wikipedia.org/wiki/Distancefromapointtoaline
        double a = (linePt1.y - linePt2.y);
        double b = (linePt2.x - linePt1.x);
        double c = a * linePt1.x + b * linePt1.y;
        double q = a * pt.x + b * pt.y - c;
        return (q * q) / (a * a + b * b);
    }

    private static double DistanceSqr(Point64 pt1, Point64 pt2) {
        return (pt1.x - pt2.x) * (pt1.x - pt2.x) + (pt1.y - pt2.y) * (pt1.y - pt2.y);
    }

    private OutRec ProcessJoin(Joiner j) {
        OutPt op1 = j.op1, op2 = j.op2;
        OutRec or1 = GetRealOutRec(op1.outrec);
        OutRec or2 = GetRealOutRec(op2.outrec);
        DeleteJoin(j);

        if (or2.pts == null) {
            return or1;
        }
        if (!IsValidClosedPath(op2)) {
            RefObject<OutPt> tempRefop2 = new RefObject<>(op2);
            SafeDisposeOutPts(tempRefop2);
            return or1;
        }
        if ((or1.pts == null) || !IsValidClosedPath(op1)) {
            RefObject<OutPt> tempRefop1 = new RefObject<>(op1);
            SafeDisposeOutPts(tempRefop1);
            return or2;
        }
        if (or1 == or2 && ((op1 == op2) || (op1.next == op2) || (op1.prev == op2))) {
            return or1;
        }

        RefObject<OutPt> tempRefop12 = new RefObject<>(op1);
        CheckDisposeAdjacent(tempRefop12, op2, or1);
        op1 = tempRefop12.argValue;
        RefObject<OutPt> tempRefop22 = new RefObject<>(op2);
        CheckDisposeAdjacent(tempRefop22, op1, or2);
        op2 = tempRefop22.argValue;
        if (op1.next == op2 || op2.next == op1) {
            return or1;
        }

        OutRec result = or1;
        for (; ; ) {
            if (!IsValidPath(op1) || !IsValidPath(op2) || (or1 == or2 && (op1.prev == op2 || op1.next == op2))) {
                return or1;
            }

            if (op1.prev.pt.opEquals(op2.next.pt) || ((InternalClipper.CrossProduct(op1.prev.pt, op1.pt, op2.next.pt) == 0)
                    && CollinearSegsOverlap(op1.prev.pt, op1.pt, op2.pt, op2.next.pt))) {
                if (or1 == or2) {
                    // SPLIT REQUIRED
                    // make sure op1.prev and op2.next match positions
                    // by inserting an extra vertex if needed
                    if (op1.prev.pt.opNotEquals(op2.next.pt)) {
                        if (PointEqualOrBetween(op1.prev.pt, op2.pt, op2.next.pt)) {
                            op2.next = InsertOp(op1.prev.pt, op2);
                        } else {
                            op1.prev = InsertOp(op2.next.pt, op1.prev);
                        }
                    }

                    // current to new
                    // op1.p[opA] >>> op1 ... opA \ / op1
                    // op2.n[opB] <<< op2 ... opB / \ op2
                    OutPt opA = op1.prev, opB = op2.next;
                    opA.next = opB;
                    opB.prev = opA;
                    op1.prev = op2;
                    op2.next = op1;
                    CompleteSplit(op1, opA, or1);
                } else {
                    // JOIN, NOT SPLIT
                    OutPt opA = op1.prev, opB = op2.next;
                    opA.next = opB;
                    opB.prev = opA;
                    op1.prev = op2;
                    op2.next = op1;

                    // SafeDeleteOutPtJoiners(op2);
                    // DisposeOutPt(op2);

                    if (or1.idx < or2.idx) {
                        or1.pts = op1;
                        or2.pts = null;
                        if (or1.owner != null && (or2.owner == null || or2.owner.idx < or1.owner.idx)) {
                            or1.owner = or2.owner;
                        }
                        or2.owner = or1;
                    } else {
                        result = or2;
                        or2.pts = op1;
                        or1.pts = null;
                        if (or2.owner != null && (or1.owner == null || or1.owner.idx < or2.owner.idx)) {
                            or2.owner = or1.owner;
                        }
                        or1.owner = or2;
                    }
                }
                break;
            }
            if (op1.next.pt.opEquals(op2.prev.pt) || ((InternalClipper.CrossProduct(op1.next.pt, op2.pt, op2.prev.pt) == 0)
                    && CollinearSegsOverlap(op1.next.pt, op1.pt, op2.pt, op2.prev.pt))) {
                if (or1 == or2) {
                    // SPLIT REQUIRED
                    // make sure op2.prev and op1.next match positions
                    // by inserting an extra vertex if needed
                    if (op2.prev.pt.opNotEquals(op1.next.pt)) {
                        if (PointEqualOrBetween(op2.prev.pt, op1.pt, op1.next.pt)) {
                            op1.next = InsertOp(op2.prev.pt, op1);
                        } else {
                            op2.prev = InsertOp(op1.next.pt, op2.prev);
                        }
                    }

                    // current to new
                    // op2.p[opA] >>> op2 ... opA \ / op2
                    // op1.n[opB] <<< op1 ... opB / \ op1
                    OutPt opA = op2.prev, opB = op1.next;
                    opA.next = opB;
                    opB.prev = opA;
                    op2.prev = op1;
                    op1.next = op2;
                    CompleteSplit(op1, opA, or1);
                } else {
                    // JOIN, NOT SPLIT
                    OutPt opA = op1.next, opB = op2.prev;
                    opA.prev = opB;
                    opB.next = opA;
                    op1.next = op2;
                    op2.prev = op1;

                    // SafeDeleteOutPtJoiners(op2);
                    // DisposeOutPt(op2);

                    if (or1.idx < or2.idx) {
                        or1.pts = op1;
                        or2.pts = null;
                        if (or1.owner != null && (or2.owner == null || or2.owner.idx < or1.owner.idx)) {
                            or1.owner = or2.owner;
                        }
                        or2.owner = or1;
                    } else {
                        result = or2;
                        or2.pts = op1;
                        or1.pts = null;
                        if (or2.owner != null && (or1.owner == null || or1.owner.idx < or2.owner.idx)) {
                            or2.owner = or1.owner;
                        }
                        or1.owner = or2;
                    }
                }
                break;
            }

            if (PointBetween(op1.next.pt, op2.pt, op2.prev.pt) && DistanceFromLineSqrd(op1.next.pt, op2.pt, op2.prev.pt) < 2.01) {
                InsertOp(op1.next.pt, op2.prev);
                continue;
            }
            if (PointBetween(op2.next.pt, op1.pt, op1.prev.pt) && DistanceFromLineSqrd(op2.next.pt, op1.pt, op1.prev.pt) < 2.01) {
                InsertOp(op2.next.pt, op1.prev);
                continue;
            }
            if (PointBetween(op1.prev.pt, op2.pt, op2.next.pt) && DistanceFromLineSqrd(op1.prev.pt, op2.pt, op2.next.pt) < 2.01) {
                InsertOp(op1.prev.pt, op2);
                continue;
            }
            if (PointBetween(op2.prev.pt, op1.pt, op1.next.pt) && DistanceFromLineSqrd(op2.prev.pt, op1.pt, op1.next.pt) < 2.01) {
                InsertOp(op2.prev.pt, op1);
                continue;
            }

            // something odd needs tidying up
            RefObject<OutPt> tempRefop13 = new RefObject<>(op1);
            if (CheckDisposeAdjacent(tempRefop13, op2, or1)) {
                op1 = tempRefop13.argValue;
                continue;
            } else {
                op1 = tempRefop13.argValue;
            }
            RefObject<OutPt> tempRefop23 = new RefObject<>(op2);
            if (CheckDisposeAdjacent(tempRefop23, op1, or1)) {
                op2 = tempRefop23.argValue;
                continue;
            } else {
                op2 = tempRefop23.argValue;
            }
            if (op1.prev.pt.opNotEquals(op2.next.pt) && (DistanceSqr(op1.prev.pt, op2.next.pt) < 2.01)) {
                op1.prev.pt = op2.next.pt;
                continue;
            }
            if (op1.next.pt.opNotEquals(op2.prev.pt) && (DistanceSqr(op1.next.pt, op2.prev.pt) < 2.01)) {
                op2.prev.pt = op1.next.pt;
                continue;
            }
            // OK, there doesn't seem to be a way to join after all
            // so just tidy up the polygons
            or1.pts = op1;
            if (or2 != or1) {
                or2.pts = op2;
                CleanCollinear(or2);
            }
            break;
        }
        return result;
    }

    private static void UpdateOutrecOwner(OutRec outrec) {
        OutPt opCurr = outrec.pts;
        for (; ; ) {
            opCurr.outrec = outrec;
            opCurr = opCurr.next;
            if (opCurr == outrec.pts) {
                return;
            }
        }
    }

    private void CompleteSplit(OutPt op1, OutPt op2, OutRec outrec) {
        double area1 = Area(op1);
        double area2 = Area(op2);
        boolean signschange = (area1 > 0) == (area2 < 0);

        // delete trivial splits (with zero or almost zero areas)
        if (area1 == 0 || (signschange && Math.abs(area1) < 2)) {
            RefObject<OutPt> tempRefObject = new RefObject<>(op1);
            SafeDisposeOutPts(tempRefObject);
            outrec.pts = op2;
        } else if (area2 == 0 || (signschange && Math.abs(area2) < 2)) {
            RefObject<OutPt> tempRefObject2 = new RefObject<>(op2);
            SafeDisposeOutPts(tempRefObject2);
            outrec.pts = op1;
        } else {
            OutRec newOr = new OutRec();
            newOr.idx = outrecList.size();
            outrecList.add(newOr);

            if (Math.abs(area1) >= Math.abs(area2)) {
                outrec.pts = op1;
                newOr.pts = op2;
            } else {
                outrec.pts = op2;
                newOr.pts = op1;
            }

            if ((area1 > 0) == (area2 > 0)) {
                newOr.owner = outrec.owner;
            } else {
                newOr.owner = outrec;
            }

            UpdateOutrecOwner(newOr);
            CleanCollinear(newOr);
        }
    }

    private void CleanCollinear(OutRec outrec) {
        outrec = GetRealOutRec(outrec);
        RefObject<OutPt> tempRefpts = new RefObject<>(outrec.pts);
        if (outrec.isOpen || outrec.frontEdge != null || !ValidateClosedPathEx(tempRefpts)) {
            return;
        }

        OutPt startOp = outrec.pts;
        OutPt op2 = startOp;
        for (; ; ) {
            if (op2.joiner != null) {
                return;
            }
            // NB if preserveCollinear == true, then only remove 180 deg. spikes
            if ((InternalClipper.CrossProduct(op2.prev.pt, op2.pt, op2.next.pt) == 0)
                    && ((op2.pt.opEquals(op2.prev.pt)) || (op2.pt.opEquals(op2.next.pt)) || !preserveCollinear
                    || (InternalClipper.DotProduct(op2.prev.pt, op2.pt, op2.next.pt) < 0))) {
                if (op2.equals(outrec.pts)) {
                    outrec.pts = op2.prev;
                }
                op2 = DisposeOutPt(op2);
                RefObject<OutPt> tempRefop2 = new RefObject<>(op2);
                if (!ValidateClosedPathEx(tempRefop2)) {
                    outrec.pts = null;
                    return;
                } else {
                    op2 = tempRefop2.argValue;
                }
                startOp = op2;
                continue;
            }
            op2 = op2.next;
            if (op2.equals(startOp)) {
                break;
            }
        }
        RefObject<OutPt> tempRefObject = new RefObject<>(outrec.pts);
        FixSelfIntersects(tempRefObject);
        outrec.pts = tempRefObject.argValue;
    }

    private OutPt DoSplitOp(RefObject<OutPt> outRecOp, OutPt splitOp) {
        OutPt prevOp = splitOp.prev, nextNextOp = splitOp.next.next;
        PointD ipD = new PointD();
        InternalClipper.GetIntersectPoint(prevOp.pt, splitOp.pt, splitOp.next.pt, nextNextOp.pt, ipD);
        Point64 ip = new Point64(ipD);

        double area1 = Area(outRecOp.argValue);
        double area2 = AreaTriangle(ip, splitOp.pt, splitOp.next.pt);

        if (ip.opEquals(prevOp.pt) || ip.opEquals(nextNextOp.pt)) {
            nextNextOp.prev = prevOp;
            prevOp.next = nextNextOp;
        } else {
            OutPt newOp2 = new OutPt(ip, prevOp.outrec);
            newOp2.prev = prevOp;
            newOp2.next = nextNextOp;
            nextNextOp.prev = newOp2;
            prevOp.next = newOp2;
        }

        SafeDeleteOutPtJoiners(splitOp.next);
        SafeDeleteOutPtJoiners(splitOp);

        if ((Math.abs(area2) >= 1) && ((Math.abs(area2) > Math.abs(area1)) || ((area2 > 0) == (area1 > 0)))) {
            OutRec newOutRec = new OutRec();
            newOutRec.idx = outrecList.size();
            outrecList.add(newOutRec);
            newOutRec.owner = prevOp.outrec.owner;
            splitOp.outrec = newOutRec;
            splitOp.next.outrec = newOutRec;

            OutPt newOp = new OutPt(ip, newOutRec);
            newOp.prev = splitOp.next;
            newOp.next = splitOp;
            newOutRec.pts = newOp;
            splitOp.prev = newOp;
            splitOp.next.next = newOp;
        }
        return prevOp;
    }

    private void FixSelfIntersects(RefObject<OutPt> op) {
        if (!IsValidClosedPath(op.argValue)) {
            return;
        }
        OutPt op2 = op.argValue;
        for (; ; ) {
            // triangles can't self-intersect
            if (op2.prev == op2.next.next) {
                break;
            }
            if (InternalClipper.SegmentsIntersect(op2.prev.pt, op2.pt, op2.next.pt, op2.next.next.pt)) {
                if (op2 == op.argValue || op2.next == op.argValue) {
                    op.argValue = op2.prev;
                }
                op2 = DoSplitOp(op, op2);
                op.argValue = op2;
                continue;
            }

            op2 = op2.next;

            if (op2 == op.argValue) {
                break;
            }
        }
    }

    public static final boolean BuildPath(OutPt op, boolean reverse, boolean isOpen, Path64 path) {
        if (op.next == op || (!isOpen && op.next == op.prev)) {
            return false;
        }
        path.clear();

        Point64 lastPt;
        OutPt op2;
        if (reverse) {
            lastPt = op.pt;
            op2 = op.prev;
        } else {
            op = op.next;
            lastPt = op.pt;
            op2 = op.next;
        }
        path.add(lastPt);

        while (op2 != op) {
            if (op2.pt.opNotEquals(lastPt)) {
                lastPt = op2.pt;
                path.add(lastPt);
            }
            if (reverse) {
                op2 = op2.prev;
            } else {
                op2 = op2.next;
            }
        }
        return true;
    }

    protected void BuildPaths(Paths64 solutionClosed) {
        solutionClosed.clear();

        for (int i = 0, outrecListSize = outrecList.size(); i < outrecListSize; i++) {
            OutRec outrec = outrecList.get(i);
            if (outrec.pts == null) {
                continue;
            }

            Path64 path = new Path64();

            if (BuildPath(outrec.pts, reverseSolution, false, path)) {
                solutionClosed.add(path);
            }
        }
    }
}