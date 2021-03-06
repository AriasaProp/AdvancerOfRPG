package com.ariasaproject.advancerofrpg.math;

import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.Pool;

public class Bresenham2 {
    private final Array<IntegerVector2> points = new Array<IntegerVector2>();
    private final Pool<IntegerVector2> pool = new Pool<IntegerVector2>() {
        @Override
        protected IntegerVector2 newObject() {
            return new IntegerVector2();
        }
    };

    public Array<IntegerVector2> line(IntegerVector2 start, IntegerVector2 end) {
        return line(start.x, start.y, end.x, end.y);
    }

    /**
     * Returns a list of {@link GridPoint2} instances along the given line, at
     * integer coordinates.
     *
     * @param startX the start x coordinate of the line
     * @param startY the start y coordinate of the line
     * @param endX   the end x coordinate of the line
     * @param endY   the end y coordinate of the line
     * @return the list of points on the line at integer coordinates
     */
    public Array<IntegerVector2> line(int startX, int startY, int endX, int endY) {
        pool.freeAll(points);
        points.clear();
        return line(startX, startY, endX, endY, pool, points);
    }

    /**
     * Returns a list of {@link GridPoint2} instances along the given line, at
     * integer coordinates.
     *
     * @param startX the start x coordinate of the line
     * @param startY the start y coordinate of the line
     * @param endX   the end x coordinate of the line
     * @param endY   the end y coordinate of the line
     * @param pool   the pool from which GridPoint2 instances are fetched
     * @param output the output array, will be cleared in this method
     * @return the list of points on the line at integer coordinates
     */
    public Array<IntegerVector2> line(int startX, int startY, int endX, int endY, Pool<IntegerVector2> pool, Array<IntegerVector2> output) {
        int w = endX - startX;
        int h = endY - startY;
        int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;
        if (w < 0) {
            dx1 = -1;
            dx2 = -1;
        } else if (w > 0) {
            dx1 = 1;
            dx2 = 1;
        }
        if (h < 0)
            dy1 = -1;
        else if (h > 0)
            dy1 = 1;
        int longest = Math.abs(w);
        int shortest = Math.abs(h);
        if (longest <= shortest) {
            longest = Math.abs(h);
            shortest = Math.abs(w);
            if (h < 0)
                dy2 = -1;
            else if (h > 0)
                dy2 = 1;
            dx2 = 0;
        }
        int numerator = longest >> 1;
        for (int i = 0; i <= longest; i++) {
            IntegerVector2 point = pool.obtain();
            point.set(startX, startY);
            output.add(point);
            numerator += shortest;
            if (numerator > longest) {
                numerator -= longest;
                startX += dx1;
                startY += dy1;
            } else {
                startX += dx2;
                startY += dy2;
            }
        }
        return output;
    }
}
