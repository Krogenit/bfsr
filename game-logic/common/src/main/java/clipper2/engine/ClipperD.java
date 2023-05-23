package clipper2.engine;

import clipper2.core.ClipType;
import clipper2.core.FillRule;
import clipper2.core.Paths64;

public class ClipperD extends ClipperBase {
    public final void executeDifference(FillRule fillRule, Paths64 solutionClosed) {
        solutionClosed.clear();
        try {
            executeInternalDifference(fillRule);
            BuildPaths(solutionClosed);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ClearSolution();
    }

    public void Execute(ClipType clipType, FillRule fillRule, Paths64 solution) {
        solution.clear();
        try {
            ExecuteInternal(clipType, fillRule);
            BuildPaths(solution);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ClearSolution();
    }
}