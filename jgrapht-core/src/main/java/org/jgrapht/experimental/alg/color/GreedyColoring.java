/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2012, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/* -------------------
 * GreedyColoring.java
 * -------------------
 * (C) Copyright 2010-2012, by Michael Behrisch and Contributors.
 *
 * Original Author:  Michael Behrisch
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Dec-2008 : Initial revision (AN);
 *
 */
package org.jgrapht.experimental.alg.color;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.experimental.GraphReader;
import org.jgrapht.experimental.alg.*;


/**
 * @author Michael Behrisch
 */
public class GreedyColoring<V, E>
    extends IntArrayApproxAlgorithm<V, E, Integer, Map<V, Integer>>
{
    //~ Static fields/initializers ---------------------------------------------

    public static final int BEST_ORDER = 0;
    public static final int NATURAL_ORDER = 1;
    public static final int SMALLEST_DEGREE_LAST_ORDER = 2;
    public static final int LARGEST_SATURATION_FIRST_ORDER = 3;

    //~ Instance fields --------------------------------------------------------

    private int _order = BEST_ORDER;

    //~ Constructors -----------------------------------------------------------

    /**
     * @param g
     */
    public GreedyColoring(final Graph<V, E> g)
    {
        this(g, BEST_ORDER);
    }

    /**
     * @param g
     */
    public GreedyColoring(final Graph<V, E> g, final int method)
    {
        super(g);
        _order = method;
    }

    //~ Methods ----------------------------------------------------------------

    int color(int [] order)
    {
        final int [] color = new int[_neighbors.length];
        int maxColor = 1;
        BitSet usedColors = new BitSet(_neighbors.length);

        for (int i = 0; i < _neighbors.length; i++) {
            final int v = (order == null) ? i : order[i];
            usedColors.clear();
            for (int j = 0; j < _neighbors[v].length; j++) {
                final int nb = _neighbors[v][j];
                if (color[nb] > 0) {
                    usedColors.set(color[nb]);
                }
            }
            color[v] = usedColors.nextClearBit(1);
            if (color[v] > maxColor) {
                maxColor = color[v];
            }
        }
        return maxColor;
    }

    int [] smallestDegreeLastOrder()
    {
        final int [] order = new int[_neighbors.length];
        final int [] degree = new int[_neighbors.length];
        final List<List<Integer>> buckets =
            new ArrayList<List<Integer>>(_neighbors.length);
        int index = _neighbors.length - 1;

        for (int i = 0; i < _neighbors.length; i++) {
            buckets.add(new ArrayList<Integer>());
            degree[i] = _neighbors[i].length;
        }
        for (int i = 0; i < _neighbors.length; i++) {
            buckets.get(degree[i]).add(i);
        }
        for (int i = 0; i < _neighbors.length; i++) {
            while (buckets.get(i).size() > 0) {
                final int s = buckets.get(i).size() - 1;
                final int vertex = (Integer) buckets.get(i).get(s);
                buckets.get(i).remove(s);
                degree[vertex] = -1;
                order[index--] = vertex;
                for (int j = 0; j < _neighbors[vertex].length; j++) {
                    final int nb = _neighbors[vertex][j];
                    if (degree[nb] >= 0) {
                        buckets.get(degree[nb]).remove(new Integer(nb));
                        degree[nb]--;
                        buckets.get(degree[nb]).add(nb);
                        if (degree[nb] < i) {
                            i = degree[nb];
                        }
                    }
                }
            }
        }
        return order;
    }

    int [] largestSaturationFirstOrder()
    {
        final int [] satur = new int[_neighbors.length];
        final int [] buckets = new int[_neighbors.length];
        final int [] cumBucketSize = new int[_neighbors.length];
        final int [] bucketIndex = new int[_neighbors.length];
        int index = 0;
        int maxSat = 0;

        for (int i = 0; i < _neighbors.length; i++) {
            buckets[i] = i;
            bucketIndex[i] = i;
        }
        cumBucketSize[0] = _neighbors.length;
        while (index < _neighbors.length) {
            while (
                (maxSat > 0)
                && (cumBucketSize[maxSat] == cumBucketSize[maxSat - 1]))
            {
                cumBucketSize[maxSat--] = 0;
            }
            final int v = buckets[cumBucketSize[maxSat] - 1];
            cumBucketSize[maxSat]--;
            satur[v] = -1;
            index++;
            for (int j = 0; j < _neighbors[v].length; j++) {
                final int nb = (int) _neighbors[v][j];
                final int bi = bucketIndex[nb];
                if (satur[nb] >= 0) {
                    if (bi != (cumBucketSize[satur[nb]] - 1)) {
                        buckets[bi] = buckets[cumBucketSize[satur[nb]] - 1];
                        buckets[cumBucketSize[satur[nb]] - 1] = nb;
                        bucketIndex[nb] = cumBucketSize[satur[nb]] - 1;
                        bucketIndex[buckets[bi]] = bi;
                    }
                    cumBucketSize[satur[nb]]--;
                    satur[nb]++;
                    if (cumBucketSize[satur[nb]] == 0) {
                        cumBucketSize[satur[nb]] =
                            cumBucketSize[satur[nb] - 1] + 1;
                    }
                    if (satur[nb] > maxSat) {
                        maxSat = satur[nb];
                    }
                }
            }
        }
        Collections.reverse(Arrays.asList(buckets));
        return buckets;
    }

    protected Integer internalLowerBound(Map<V, Integer> optionalData)
    {
        return new GreedyClique<V, E>(this).getLowerBound(null);
    }

    protected Integer internalUpperBound(Map<V, Integer> optionalData)
    {
        switch (_order) {
        case BEST_ORDER:
            return Math.min(
                Math.min(color(null), color(smallestDegreeLastOrder())),
                color(largestSaturationFirstOrder()));
        case NATURAL_ORDER:
            return color(null);
        case SMALLEST_DEGREE_LAST_ORDER:
            return color(smallestDegreeLastOrder());
        case LARGEST_SATURATION_FIRST_ORDER:
            return color(largestSaturationFirstOrder());
        }
        return _neighbors.length;
    }

    public static void main(String[] args) {
        Graph g = GraphReader.generateIntGraph(args[0]);
        System.out.println(new GreedyColoring(g).getLowerBound(null));
        System.out.println(new GreedyColoring(g).getUpperBound(null));
    }
}
