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
import org.jgrapht.experimental.GraphReader;

public class BipartiteMatchingHopcroftKarp<V, E>
    extends IntArrayGraphAlgorithm<V, E>
    implements ExactAlgorithm<Integer, Map<V, V>> {

    //~ Constructors -----------------------------------------------------------

    /**
     * @param g
     */
    public BipartiteMatchingHopcroftKarp(final Graph<V, E> g)
    {
        super(g);
    }

    private long hungarianDag(int[][] part, int[] match, List<Integer> term, List<LinkedList<Integer>> vor) {

        long k = Long.MAX_VALUE;
        LinkedList<Integer> q = new LinkedList<Integer>();
        long[] distance = new long[_neighbors.length];
    
        // initialization
        
        for (int v : part[0]) {
            distance[v] = Long.MAX_VALUE;
            vor.get(v).clear();
        }
        
        term.clear();
        
        for (int u : part[1]) {
            if (match[u] == -1) {
                distance[u] = 0;
                q.add(u);
            }
        }
        
        // hungarian dag

        while (!q.isEmpty()) {
            final int u = q.removeFirst();
            if (distance[u] < k) {
                for (int v : _neighbors[u]) {
                    if (distance[v] > distance[u]) {
                        if (distance[v] == Long.MAX_VALUE) {
                            distance[v] = distance[u] + 1;
                            vor.get(v).add(u);
                            if (match[v] == -1) {
                                term.add(v);
                                if (k == Long.MAX_VALUE) {
                                    k = distance[v];
                                }
                            } else {
                                if (distance[v] < k) {
                                    distance[match[v]] = distance[v] + 1;
                                    q.add(match[v]);
                                }
                            }
                        } else {
                            vor.get(v).add(u);
                        }
                    }
                }
            }
        }
        return k;
    }



    private int[][] getBipartition()
    {
        BitSet unknown = new BitSet(_neighbors.length);
        LinkedList<Integer> queue = new LinkedList<Integer>();
        BitSet odd = new BitSet(_neighbors.length);

        queue.add(0);
        int card = 0;

        while (!unknown.isEmpty()) {
            if (queue.isEmpty()) {
                queue.add(unknown.nextSetBit(0));
            }

            int v = queue.removeFirst();
            unknown.set(v, false);

            for (int n : _neighbors[v]) {
                if (unknown.get(n)) {
                    queue.add(n);
                    if (!odd.get(v)) {
                        odd.set(n);
                        card++;
                    }
                } else if (!(odd.get(v) ^ odd.get(n))) {
                    return null;
                }
            }
        }
        int[][] result = {new int[card], new int[_neighbors.length - card]};
        int idx0=0, idx1=0;
        for (int i = 0; i < _neighbors.length; i++) {
            if (odd.get(i)) {
                result[0][idx0++] = i;
            } else {
                result[1][idx1++] = i;                
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.jgrapht.experimental.alg.ExactAlgorithm#getResult()
     */
    public Integer getResult(Map<V, V> additionalData) {

        final int[][] part = getBipartition();

        if (part != null) {
            final int[] match = new int[_neighbors.length];
            final List<LinkedList<Integer>> vor = new ArrayList<LinkedList<Integer>>(_neighbors.length);
            final List<Integer> term = new ArrayList<Integer>();
            long k;
            
            for (int i = 0; i < match.length; i++) {
                match[i] = -1;
                vor.add(null);
            }

            for (int v : part[0]) {
                vor.set(v, new LinkedList<Integer>());
            }

            while ((k = hungarianDag(part, match, term, vor)) < Long.MAX_VALUE) {
                final boolean[] dead = new boolean[_neighbors.length];
                for (int v : term) { // augment simultan
                    int l = (int) k;
                    final int[] path = new int[l + 1];
                    path[l] = v;
                    boolean done = false;
                    do {
                        final LinkedList<Integer> list = vor.get(path[l]);
                        while (!list.isEmpty() && dead[list.getFirst()]) {
                            list.removeFirst();
                        }
                        if (list.isEmpty()) {
                            if (l == k) {
                                  done = true;
                            } else {
                                dead[path[l + 1]] = true;
                                l += 2;
                            }
                        } else {
                            path[l - 1] = list.removeFirst();
                            dead[path[l - 1]] = true;
                            l--;
                            if (l == 0) {
                                done = true;
                                for (int j = 0; j < path.length; j += 2) {
                                    match[path[j]] = path[j + 1];
                                    match[path[j + 1]] = path[j];
                                }
                            } else {
                                path[l - 1] = match[path[l]];
                                l--;
                            }
                        }
                    } while (!done);
                }
            }
            int result = 0;
            for (int m : match) {
                if (m != -1) {
                    result++;
                    if (additionalData != null) {
                        additionalData.put(_vertices.get(m), _vertices.get(match[m]));
                    }
                }
            }
            return result/2;
        }
        return -1;
    }

    public static void main(String[] args) {
        Graph g = GraphReader.generateIntGraph(args[0]);
        System.out.println(new BipartiteMatchingHopcroftKarp(g).getResult(null));
    }
}
