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
 * GreedyClique.java
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
public class GreedyClique<V, E>
    extends IntArrayApproxAlgorithm<V, E, Integer, Set<V>>
{
    /**
     * @param g
     */
    public GreedyClique(final Graph<V, E> g)
    {
        super(g);
    }

    /**
     * @param other
     */
    public GreedyClique(final IntArrayGraphAlgorithm<V, E> other)
    {
        super(other);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Looks for a clique greedily starting at the first vertex in the array.
     *
     * @param optionalData
     */
    protected Integer internalLowerBound(Set<V> optionalData)
    {
        int maxClique = 0;
        BitSet maxSet = new BitSet(_neighbors.length);
        BitSet members = new BitSet(_neighbors.length);
        BitSet test = new BitSet(_neighbors.length);

        for (int i = 0; i < _neighbors.length; i++) {
            members.clear();
            members.set(i);
            int size = 1;
            for (int j = 0; j < _neighbors[i].length; j++) {
                if (_neighbors[i][j] > i) {
                    members.set(_neighbors[i][j]);
                    size++;
                }
            }
            for (int next = members.nextSetBit(i+1);
                 next != -1 && size > maxClique;
                 next = members.nextSetBit(next+1)) {
                test.clear();
                test.set(next);
                for (int j = 0; j < _neighbors[next].length; j++) {
                    if (_neighbors[next][j] >= i) {
                        test.set(_neighbors[next][j]);
                    }
                }
                members.and(test);
                size = members.cardinality();
            }
            if (size > maxClique) {
                maxClique = size;
                maxSet.clear();
                maxSet.or(members);
            }
        }
        if (optionalData != null) {
            for (int next = maxSet.nextSetBit(0); next != -1;
             next = maxSet.nextSetBit(next+1)) {
                optionalData.add(_vertices.get(next));
            }
        }
        return maxClique;
    }

    /**
     * Employs the fact that for a clique of size k one needs
     * at least k vertices of degree at least k-1.
     *
     * @param optionalData
     */
    protected Integer internalUpperBound(Set<V> optionalData)
    {
        final int [] degrees = new int[_neighbors.length];
        for (int i = 0; i < _neighbors.length; i++) {
            degrees[_neighbors[i].length]++;
        }
        int numHighDegree = 0;
        int c = _neighbors.length - 1;
        for (; c >= numHighDegree; c--) {
            numHighDegree += degrees[c];
        }
        return c+2; //one extra to compensate for the last loop decrement
    }

    public static void main(String[] args) {
        Graph g = GraphReader.generateIntGraph(args[0]);
        System.out.println(new GreedyClique(g).getLowerBound(null));
        System.out.println(new GreedyClique(g).getUpperBound(null));
    }
}
