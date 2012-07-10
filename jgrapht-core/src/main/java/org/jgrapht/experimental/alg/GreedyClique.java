/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
 * GreedyColoringjava
 * -------------------
 * (C) Copyright 2010-2010, by Michael Behrisch and Contributors.
 *
 * Original Author:  Michael Behrisch
 * Contributor(s):   -
 *
 * $Id: GraphReaderTest.java 711 2010-05-21 21:18:52Z behrisch $
 *
 * Changes
 * -------
 * 24-Dec-2008 : Initial revision (AN);
 *
 */
package org.jgrapht.experimental.alg;

import java.util.*;

import org.jgrapht.*;


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

    protected Integer internalLowerBound(Set<V> optionalData)
    {
        int maxClique = 1;
        BitSet members = new BitSet(_neighbors.length);
        BitSet test = new BitSet(_neighbors.length);
        int size = 1;

        for (int i = 0; i < _neighbors.length; i++) {
            members.clear();
            members.set(i);
            size = 1;
            for (int j = 0; j < _neighbors[i].length; j++) {
                if (_neighbors[i][j] > i) {
                    members.set(_neighbors[i][j]);
                    size++;
                }
            }
            if (size > maxClique) {
                int next = members.nextSetBit(i+1);
                while (next != -1) {
                    test.clear();
                    for (int j = 0; j < _neighbors[next].length; j++) {
                        if (_neighbors[next][j] >= i) {
                            test.set(_neighbors[next][j]);
                        }
                    }
                    members.and(test);
                    next = members.nextSetBit(next+1);
                }
            }
        }
        return maxClique;
    }

    protected Integer internalUpperBound(Set<V> optionalData)
    {
        final int [] degrees = new int[_neighbors.length];
        for (int i = 0; i < _neighbors.length; i++) {
            degrees[_neighbors[i].length]++;
        }
        int numHighDegree = 0;
        int c = _neighbors.length - 1;
        for (; c >= 0; c--) {
            numHighDegree += degrees[c];
            if (numHighDegree > c) {
                break;
            }
        }
        return c+1;
    }
}

// End GreedyColoring.java
