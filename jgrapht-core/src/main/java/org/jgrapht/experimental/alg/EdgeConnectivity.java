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
 * EdgeConnectivity.java
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
package org.jgrapht.experimental.alg;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.experimental.GraphReader;


/**
 * @author Michael Behrisch
 */
public class EdgeConnectivity<V, E>
    extends IntArrayGraphAlgorithm<V, E>
    implements ExactAlgorithm<Integer, List<Integer>> {

    private int _upperBound;

    public EdgeConnectivity(final Graph<V, E> g) {
        super(g);
    }

    /**
     * @param other
     */
    public EdgeConnectivity(final IntArrayGraphAlgorithm<V, E> other) {
        super(other);
    }

    private int maxAdjOrder(List<List<Integer>> nb, int size, int[] ident, int[] order, int[] mult) {
        final SortedBuckets nodeHeap = new SortedBuckets(_neighbors.length);
        for (int i = 0; i < _neighbors.length; i++) {
            if (ident[i] == i) {
                nodeHeap.add(i, 0);
            }
        }
        int idx = 0;
        int count = 0;
        int result = 0;
        int cutsize = 0;
        while (!nodeHeap.isEmpty()) {
            result = nodeHeap.getMaxKey();
            final int v = nodeHeap.pop();
            order[idx++] = v;
            cutsize -= result;
            count += mult[v];
            for (int nr: nb.get(v)) {
                int u = ident[nr];
                while (u != ident[u]) u = ident[u];
                ident[nr] = u;
                if (nodeHeap.increaseKey(u)) {
                    cutsize++;
                }
            }
            if (count == nb.size() / 2 || count == (nb.size()+1) / 2) {
                _upperBound = Math.min(_upperBound, cutsize);
            }
        }
        return result;
    }

    public Integer getResult(List<Integer> optionalData) {
        final List<List<Integer>> nb = new ArrayList<List<Integer>>(_neighbors.length);
        final int[] ident = new int[_neighbors.length];
        final int[] order = new int[_neighbors.length];
        final int[] mult = new int[_neighbors.length];
        _upperBound = _neighbors.length - 1;
        int lambda = _neighbors.length - 1;
        for (int i = 0; i < _neighbors.length; i++) {
            if (_neighbors[i].length < lambda) {
                lambda = _neighbors[i].length;
            }
            nb.add(new ArrayList<Integer>());
            ident[i] = i;
            mult[i] = 1;
        }
        for (int k = _neighbors.length; k > 1; k--) {
            lambda = Math.min(maxAdjOrder(nb, k, ident, order, mult), lambda);
            nb.get(order[k-2]).addAll(nb.get(order[k-1]));
            ident[order[k-1]] = ident[order[k-2]];
            mult[order[k-2]] += mult[order[k-1]];
        }
        if (optionalData != null) {
            optionalData.add(_upperBound);
        }
        return lambda;
    }

    public static void main(String[] args) {
        Graph g = GraphReader.generateIntGraph(args[0]);
        System.out.println(new EdgeConnectivity(g).getResult(null));
    }
}
