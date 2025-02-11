package net.bfsr.engine.pack.maxrects;

import net.bfsr.engine.pack.RectanglesPackingAlgorithm;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MaxRectanglesBinPack implements RectanglesPackingAlgorithm {
    private int binWidth;
    private int binHeight;

    private boolean binAllowFlip;

    private List<Rectangle> usedRectangles;
    private List<Rectangle> freeRectangles;

    public MaxRectanglesBinPack(int width, int height, boolean allowFlip) {
        init(width, height, allowFlip);
    }

    public void init(int width, int height, boolean allowFlip) {
        binAllowFlip = allowFlip;
        binWidth = width;
        binHeight = height;

        Rectangle n = new Rectangle(0, 0, width, height);
        usedRectangles = new ArrayList<>();
        freeRectangles = new ArrayList<>();
        freeRectangles.add(n);
    }

    public Rectangle insert(int width, int height, FreeRectChoiceHeuristic method) {
        ScoreResult scoreResult = new ScoreResult();
        scoreResult.setScore1(Integer.MAX_VALUE);
        scoreResult.setScore2(Integer.MAX_VALUE);

        Rectangle newNode = switch (method) {
            case RECT_BEST_SHORT_SIDE_FIT -> findPositionForNewNodeBestShortSideFit(width, height, scoreResult);
            case RECT_BOTTOM_LEFT_RULE -> findPositionForNewNodeBottomLeft(width, height, scoreResult);
            case RECT_CONTACT_POINT_RULE -> FindPositionForNewNodeContactPoint(width, height, scoreResult);
            case RECT_BEST_LONG_SIDE_FIT -> findPositionForNewNodeBestLongSideFit(width, height, scoreResult);
            case RECT_BEST_AREA_FIT -> findPositionForNewNodeBestAreaFit(width, height, scoreResult);
            case RECT_BEST_SQUARE_FIT -> FindPositionForNewNodeBestSquareFit(width, height, scoreResult);
        };

        if (newNode.getHeight() == 0)
            return newNode;

        int numRectanglesToProcess = freeRectangles.size();
        for (int i = 0; i < numRectanglesToProcess; ++i) {
            if (splitFreeNode(freeRectangles.get(i), newNode)) {
                freeRectangles.remove(i--);
                numRectanglesToProcess--;
            }
        }

        pruneFreeList();

        usedRectangles.add(newNode);
        return newNode;
    }

    @Override
    public @Nullable Rectangle insert(int width, int height) {
        Rectangle newNode = scoreRect(width, height, FreeRectChoiceHeuristic.RECT_BOTTOM_LEFT_RULE, new ScoreResult());

        if (newNode == null)
            return null;

        placeRect(newNode);
        return newNode;
    }

    public void insert(List<RectangleSize> rectangleSizes, List<Rectangle> dst, FreeRectChoiceHeuristic method) {
        dst.clear();

        while (rectangleSizes.size() > 0) {
            ScoreResult scoreResult = new ScoreResult();
            int bestScore1 = Integer.MAX_VALUE;
            int bestScore2 = Integer.MAX_VALUE;
            int bestRectIndex = -1;
            Rectangle bestNode = null;

            for (int i = 0; i < rectangleSizes.size(); ++i) {
                Rectangle newNode = scoreRect(rectangleSizes.get(i).width(), rectangleSizes.get(i).height(), method, scoreResult);

                if (scoreResult.getScore1() < bestScore1 ||
                        (scoreResult.getScore1() == bestScore1 && scoreResult.getScore2() < bestScore2)) {
                    bestScore1 = scoreResult.getScore1();
                    bestScore2 = scoreResult.getScore2();
                    bestNode = newNode;
                    bestRectIndex = i;
                }
            }

            if (bestRectIndex == -1)
                return;

            placeRect(bestNode);
            dst.add(bestNode);
            rectangleSizes.remove(bestRectIndex);
        }
    }

    private void placeRect(Rectangle node) {
        int numRectanglesToProcess = freeRectangles.size();
        for (int i = 0; i < numRectanglesToProcess; ++i) {
            if (splitFreeNode(freeRectangles.get(i), node)) {
                freeRectangles.remove(i--);
                numRectanglesToProcess--;
            }
        }

        pruneFreeList();

        usedRectangles.add(node);
    }

    private Rectangle scoreRect(int width, int height, FreeRectChoiceHeuristic method, ScoreResult scoreResult) {
        Rectangle newNode;
        scoreResult.setScore1(Integer.MAX_VALUE);
        scoreResult.setScore2(Integer.MAX_VALUE);
        switch (method) {
            case RECT_BEST_SHORT_SIDE_FIT:
                newNode = findPositionForNewNodeBestShortSideFit(width, height, scoreResult);
                break;
            case RECT_BOTTOM_LEFT_RULE:
                newNode = findPositionForNewNodeBottomLeft(width, height, scoreResult);
                break;
            case RECT_CONTACT_POINT_RULE:
                newNode = FindPositionForNewNodeContactPoint(width, height, scoreResult);
                scoreResult.setScore1(-scoreResult.getScore1());
                break;
            case RECT_BEST_LONG_SIDE_FIT:
                newNode = findPositionForNewNodeBestLongSideFit(width, height, scoreResult);
                break;
            case RECT_BEST_AREA_FIT:
                newNode = findPositionForNewNodeBestAreaFit(width, height, scoreResult);
                break;
            case RECT_BEST_SQUARE_FIT:
                newNode = FindPositionForNewNodeBestSquareFit(width, height, scoreResult);
                break;
            default:
                newNode = null;
                break;
        }

        if (newNode == null || newNode.getHeight() == 0) {
            scoreResult.setScore1(Integer.MAX_VALUE);
            scoreResult.setScore2(Integer.MAX_VALUE);
        }

        return newNode;
    }

    public float occupancy() {
        int usedSurfaceArea = 0;
        for (int i = 0; i < usedRectangles.size(); ++i)
            usedSurfaceArea += usedRectangles.get(i).getWidth() * usedRectangles.get(i).getHeight();

        return (float) usedSurfaceArea / (binWidth * binHeight);
    }

    private Rectangle findPositionForNewNodeBottomLeft(int width, int height, ScoreResult scoreResult) {
        Rectangle bestNode = new Rectangle();

        scoreResult.setScore1(Integer.MAX_VALUE);
        scoreResult.setScore2(Integer.MAX_VALUE);

        for (int i = 0; i < freeRectangles.size(); ++i) {
            Rectangle rectangle = freeRectangles.get(i);
            if (rectangle.getWidth() >= width && rectangle.getHeight() >= height) {
                int topSideY = rectangle.getY() + height;
                if (topSideY < scoreResult.getScore1() ||
                        (topSideY == scoreResult.getScore1() && rectangle.getX() < scoreResult.getScore2())) {
                    bestNode.setX(rectangle.getX());
                    bestNode.setY(rectangle.getY());
                    bestNode.setWidth(width);
                    bestNode.setHeight(height);
                    scoreResult.setScore1(topSideY);
                    scoreResult.setScore2(rectangle.getX());
                }
            }

            if (binAllowFlip && rectangle.getWidth() >= height && rectangle.getHeight() >= width) {
                int topSideY = rectangle.getY() + width;
                if (topSideY < scoreResult.getScore1() ||
                        (topSideY == scoreResult.getScore1() && rectangle.getX() < scoreResult.getScore2())) {
                    bestNode.setX(rectangle.getX());
                    bestNode.setY(rectangle.getY());
                    bestNode.setWidth(height);
                    bestNode.setHeight(width);
                    scoreResult.setScore1(topSideY);
                    scoreResult.setScore2(rectangle.getX());
                }
            }
        }

        return bestNode;
    }

    private Rectangle findPositionForNewNodeBestShortSideFit(int width, int height, ScoreResult scoreResult) {
        Rectangle bestNode = new Rectangle();

        scoreResult.setScore1(Integer.MAX_VALUE);
        scoreResult.setScore2(Integer.MAX_VALUE);

        for (int i = 0; i < freeRectangles.size(); ++i) {
            Rectangle rectangle = freeRectangles.get(i);
            if (rectangle.getWidth() >= width && rectangle.getHeight() >= height) {
                int leftoverHoriz = Math.abs(rectangle.getWidth() - width);
                int leftoverVert = Math.abs(rectangle.getHeight() - height);
                int shortSideFit = Math.min(leftoverHoriz, leftoverVert);
                int longSideFit = Math.max(leftoverHoriz, leftoverVert);

                if (shortSideFit < scoreResult.getScore1()
                        || (shortSideFit == scoreResult.getScore1() && longSideFit < scoreResult.getScore2())) {
                    bestNode.setX(rectangle.getX());
                    bestNode.setY(rectangle.getY());
                    bestNode.setWidth(width);
                    bestNode.setHeight(height);
                    scoreResult.setScore1(shortSideFit);
                    scoreResult.setScore2(longSideFit);
                }
            }

            if (binAllowFlip && rectangle.getWidth() >= height && rectangle.getHeight() >= width) {
                int flippedLeftoverHoriz = Math.abs(rectangle.getWidth() - height);
                int flippedLeftoverVert = Math.abs(rectangle.getHeight() - width);
                int flippedShortSideFit = Math.min(flippedLeftoverHoriz, flippedLeftoverVert);
                int flippedLongSideFit = Math.max(flippedLeftoverHoriz, flippedLeftoverVert);

                if (flippedShortSideFit < scoreResult.getScore1()
                        || (flippedShortSideFit == scoreResult.getScore1() && flippedLongSideFit < scoreResult.getScore2())) {
                    bestNode.setX(rectangle.getX());
                    bestNode.setY(rectangle.getY());
                    bestNode.setWidth(height);
                    bestNode.setHeight(width);
                    scoreResult.setScore1(flippedShortSideFit);
                    scoreResult.setScore2(flippedLongSideFit);
                }
            }
        }

        return bestNode;
    }

    private Rectangle findPositionForNewNodeBestLongSideFit(int width, int height, ScoreResult scoreResult) {
        Rectangle bestNode = new Rectangle();

        scoreResult.setScore1(Integer.MAX_VALUE);
        scoreResult.setScore2(Integer.MAX_VALUE);

        for (int i = 0; i < freeRectangles.size(); ++i) {
            Rectangle rectangle = freeRectangles.get(i);
            if (rectangle.getWidth() >= width && rectangle.getHeight() >= height) {
                int leftoverHoriz = Math.abs(rectangle.getWidth() - width);
                int leftoverVert = Math.abs(rectangle.getHeight() - height);
                int shortSideFit = Math.min(leftoverHoriz, leftoverVert);
                int longSideFit = Math.max(leftoverHoriz, leftoverVert);

                if (longSideFit < scoreResult.getScore2() ||
                        (longSideFit == scoreResult.getScore2() && shortSideFit < scoreResult.getScore1())) {
                    bestNode.setX(rectangle.getX());
                    bestNode.setY(rectangle.getY());
                    bestNode.setWidth(width);
                    bestNode.setHeight(height);
                    scoreResult.setScore1(shortSideFit);
                    scoreResult.setScore2(longSideFit);
                }
            }

            if (binAllowFlip && rectangle.getWidth() >= height && rectangle.getHeight() >= width) {
                int leftoverHoriz = Math.abs(rectangle.getWidth() - height);
                int leftoverVert = Math.abs(rectangle.getHeight() - width);
                int shortSideFit = Math.min(leftoverHoriz, leftoverVert);
                int longSideFit = Math.max(leftoverHoriz, leftoverVert);

                if (longSideFit < scoreResult.getScore2() ||
                        (longSideFit == scoreResult.getScore2() && shortSideFit < scoreResult.getScore1())) {
                    bestNode.setX(rectangle.getX());
                    bestNode.setY(rectangle.getY());
                    bestNode.setWidth(height);
                    bestNode.setHeight(width);
                    scoreResult.setScore1(shortSideFit);
                    scoreResult.setScore2(longSideFit);
                }
            }
        }

        return bestNode;
    }

    private Rectangle findPositionForNewNodeBestAreaFit(int width, int height, ScoreResult scoreResult) {
        Rectangle bestNode = new Rectangle();

        scoreResult.setScore1(Integer.MAX_VALUE);
        scoreResult.setScore2(Integer.MAX_VALUE);

        for (int i = 0; i < freeRectangles.size(); ++i) {
            Rectangle rectangle = freeRectangles.get(i);
            int areaFit = rectangle.getWidth() * rectangle.getHeight() - width * height;

            if (rectangle.getWidth() >= width && rectangle.getHeight() >= height) {
                int leftoverHoriz = Math.abs(rectangle.getWidth() - width);
                int leftoverVert = Math.abs(rectangle.getHeight() - height);
                int shortSideFit = Math.min(leftoverHoriz, leftoverVert);

                if (areaFit < scoreResult.getScore1() || (areaFit == scoreResult.getScore1() && shortSideFit < scoreResult.getScore2())) {
                    bestNode.setX(rectangle.getX());
                    bestNode.setY(rectangle.getY());
                    bestNode.setWidth(width);
                    bestNode.setHeight(height);
                    scoreResult.setScore2(shortSideFit);
                    scoreResult.setScore1(areaFit);
                }
            }

            if (binAllowFlip && rectangle.getWidth() >= height && rectangle.getHeight() >= width) {
                int leftoverHoriz = Math.abs(rectangle.getWidth() - height);
                int leftoverVert = Math.abs(rectangle.getHeight() - width);
                int shortSideFit = Math.min(leftoverHoriz, leftoverVert);

                if (areaFit < scoreResult.getScore1() || (areaFit == scoreResult.getScore1() && shortSideFit < scoreResult.getScore2())) {
                    bestNode.setX(rectangle.getX());
                    bestNode.setY(rectangle.getY());
                    bestNode.setWidth(height);
                    bestNode.setHeight(width);
                    scoreResult.setScore2(shortSideFit);
                    scoreResult.setScore1(areaFit);
                }
            }
        }

        return bestNode;
    }

    private Rectangle FindPositionForNewNodeBestSquareFit(int width, int height, ScoreResult scoreResult) {
        Rectangle bestNode = new Rectangle();

        scoreResult.setScore1(Integer.MAX_VALUE);
        scoreResult.setScore2(Integer.MAX_VALUE);

        for (int i = 0; i < freeRectangles.size(); ++i) {
            Rectangle rectangle = freeRectangles.get(i);
            if (rectangle.getWidth() >= width && rectangle.getHeight() >= height) {
                int xbound = rectangle.getX() + width;
                int ybound = rectangle.getY() + height;
                int shortSideFit = Math.min(xbound, ybound);
                int longSideFit = Math.max(xbound, ybound);
                if (longSideFit < scoreResult.getScore2() || (longSideFit == scoreResult.getScore2() && shortSideFit <
                        scoreResult.getScore1())) {
                    bestNode.setX(rectangle.getX());
                    bestNode.setY(rectangle.getY());
                    bestNode.setWidth(width);
                    bestNode.setHeight(height);
                    scoreResult.setScore1(shortSideFit);
                    scoreResult.setScore2(longSideFit);
                }
            }
        }

        return bestNode;
    }

    private int commonIntervalLength(int i1start, int i1end, int i2start, int i2end) {
        if (i1end < i2start || i2end < i1start) {
            return 0;
        }

        return Math.min(i1end, i2end) - Math.max(i1start, i2start);
    }

    private int contactPointScoreNode(int x, int y, int width, int height) {
        int score = 0;

        if (x == 0 || x + width == binWidth)
            score += height;
        if (y == 0 || y + height == binHeight)
            score += width;

        for (int i = 0; i < usedRectangles.size(); ++i) {
            Rectangle rectangle = usedRectangles.get(i);
            if (rectangle.getX() == x + width || rectangle.getX() + rectangle.getWidth() == x)
                score += commonIntervalLength(rectangle.getY(), rectangle.getY() + rectangle.getHeight(), y, y + height);
            if (rectangle.getY() == y + height || rectangle.getY() + rectangle.getHeight() == y)
                score += commonIntervalLength(rectangle.getX(), rectangle.getX() + rectangle.getWidth(), x, x + width);
        }

        return score;
    }

    private Rectangle FindPositionForNewNodeContactPoint(int width, int height, ScoreResult scoreResult) {
        Rectangle bestNode = new Rectangle();

        scoreResult.setScore1(-1);

        for (int i = 0; i < freeRectangles.size(); ++i) {
            Rectangle rectangle = freeRectangles.get(i);

            if (rectangle.getWidth() >= width && rectangle.getHeight() >= height) {
                int score = contactPointScoreNode(rectangle.getX(), rectangle.getY(), width, height);
                if (score > scoreResult.getScore1()) {
                    bestNode.setX(rectangle.getX());
                    bestNode.setY(rectangle.getY());
                    bestNode.setWidth(width);
                    bestNode.setHeight(height);
                    scoreResult.setScore1(score);
                }
            }

            if (rectangle.getWidth() >= height && rectangle.getHeight() >= width) {
                int score = contactPointScoreNode(rectangle.getX(), rectangle.getY(), height, width);
                if (score > scoreResult.getScore1()) {
                    bestNode.setX(rectangle.getX());
                    bestNode.setY(rectangle.getY());
                    bestNode.setWidth(height);
                    bestNode.setHeight(width);
                    scoreResult.setScore1(score);
                }
            }
        }

        return bestNode;
    }

    private boolean splitFreeNode(Rectangle freeNode, Rectangle usedNode) {
        Rectangle newNode;

        if (usedNode.getX() >= freeNode.getX() + freeNode.getWidth() || usedNode.getX() + usedNode.getWidth() <= freeNode.getX()
                || usedNode.getY() >= freeNode.getY() + freeNode.getHeight() || usedNode.getY() + usedNode.getHeight() <= freeNode.getY())
            return false;

        if (usedNode.getX() < freeNode.getX() + freeNode.getWidth() && usedNode.getX() + usedNode.getWidth() > freeNode.getX()) {
            if (usedNode.getY() > freeNode.getY() && usedNode.getY() < freeNode.getY() + freeNode.getHeight()) {
                newNode = new Rectangle(freeNode);
                newNode.setHeight(usedNode.getY() - newNode.getY());
                freeRectangles.add(newNode);
            }

            if (usedNode.getY() + usedNode.getHeight() < freeNode.getY() + freeNode.getHeight()) {
                newNode = new Rectangle(freeNode);
                newNode.setY(usedNode.getY() + usedNode.getHeight());
                newNode.setHeight(freeNode.getY() + freeNode.getHeight() - (usedNode.getY() + usedNode.getHeight()));
                freeRectangles.add(newNode);
            }
        }

        if (usedNode.getY() < freeNode.getY() + freeNode.getHeight() && usedNode.getY() + usedNode.getHeight() > freeNode.getY()) {
            if (usedNode.getX() > freeNode.getX() && usedNode.getX() < freeNode.getX() + freeNode.getWidth()) {
                newNode = new Rectangle(freeNode);
                newNode.setWidth(usedNode.getX() - newNode.getX());
                freeRectangles.add(newNode);
            }

            if (usedNode.getX() + usedNode.getWidth() < freeNode.getX() + freeNode.getWidth()) {
                newNode = new Rectangle(freeNode);
                newNode.setX(usedNode.getX() + usedNode.getWidth());
                newNode.setWidth(freeNode.getX() + freeNode.getWidth() - (usedNode.getX() + usedNode.getWidth()));
                freeRectangles.add(newNode);
            }
        }

        return true;
    }

    private void pruneFreeList() {
        for (int i = 0; i < freeRectangles.size(); ++i) {
            for (int j = i + 1; j < freeRectangles.size(); ++j) {
                if (isContainedIn(freeRectangles.get(i), freeRectangles.get(j))) {
                    freeRectangles.remove(i--);
                    break;
                }

                if (isContainedIn(freeRectangles.get(j), freeRectangles.get(i))) {
                    freeRectangles.remove(j--);
                }
            }
        }
    }

    private boolean isContainedIn(Rectangle a, Rectangle b) {
        return a.getX() >= b.getX() && a.getY() >= b.getY() && a.getX() + a.getWidth() <= b.getX() + b.getWidth() &&
                a.getY() + a.getHeight() <= b.getY() + b.getHeight();
    }
}