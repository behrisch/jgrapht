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
 * MinBisection.java
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


/**
 * @author Michael Behrisch
 */
public class MinBisection<V, E>
    extends IntArrayApproxAlgorithm<V, E, Integer, Map<V, Integer> > {

    private final int[][]   _cumulDegree;
    private final int[]     _order;
    private final int[]     _invOrder;
    private final int[]     _part1;
    private final boolean[] _inPart1;
    private final int[]     _inDegree;
    private final int[]     _totalOut;
    private final int[]     _countByInDegree;
    private int             _numEdges;
    private int             _upperBound;

    public MinBisection(final Graph<V, E> g) {
        super(g);
        _cumulDegree = new int[_neighbors.length][_neighbors.length];
        _order = new int[_neighbors.length];
        _invOrder = new int[_neighbors.length];
        _part1 = new int[_neighbors.length/2];
        _inPart1 = new boolean[_neighbors.length];
        _inDegree = new int[_neighbors.length];
        _totalOut = new int[_neighbors.length/2];
        for (int i = 0; i < _totalOut.length; i++) {
            _totalOut[i] = -1;
        }
        _countByInDegree = new int[(_neighbors.length+1)/2];
        for (int i = 0; i < _neighbors.length; i++) {
            _numEdges += _neighbors[i].length;
        }
        _numEdges /= 2;
        _upperBound = _numEdges;
    }
    
    public int orderNodes() {
        int maxDegree = 0;
        final List<List<Integer>> buckets = new ArrayList<List<Integer>>(_neighbors.length);
        for (int i = 0; i < _neighbors.length; i++) {
            buckets.add(new ArrayList<Integer>());
        }
        for (int i = 0; i < _neighbors.length; i++) {
            final int[] nb = _neighbors[i];
            buckets.get(nb.length).add(i);
            if (nb.length > maxDegree) maxDegree = nb.length;
        }
        int idx = 0;
        for (int i = _neighbors.length - 1; i >= 0; i--) {
            for (int v: buckets.get(i)) {
                _invOrder[v] = idx;
                _order[idx++] = v;
            }
        }
        return maxDegree;
    }

    private int greedyBisection() {
        final SortedBuckets nodeHeap = new SortedBuckets(_neighbors.length);
        for (int i = 0; i < _neighbors.length; i++) {
            nodeHeap.add(i, _neighbors.length - _neighbors[i].length);
        }
        int idx = 0;
        while (idx < _part1.length) {
            final int v = nodeHeap.pop();
            final int[] nb = _neighbors[v];
            _part1[idx++] = v;
            for (int i = 0; i < nb.length; i++) {
                nodeHeap.increaseKey(nb[i]);
            }
        }
        int result = 0;
        for (int i = 0; i < _part1.length; i++) {
            result += _neighbors.length - nodeHeap.getKey(_part1[i]);
        }
        return result;
    }
    
    private int totalOutDegree(final int idx1) {
        final int freePart2 = (_neighbors.length+1)/2 - (_part1[idx1] - idx1);
        int minInDeg = 0;
        for (int i = 0, count = 0; count < freePart2; count += _countByInDegree[i++]) {
            minInDeg += i * Math.min(freePart2 - count, _countByInDegree[i]);
        }
        return _totalOut[idx1] + minInDeg;
    }
    
    private void add(final int idx1) {
        final int vNr = _order[_part1[idx1]];
        final int[] nb = _neighbors[vNr];
        _inPart1[vNr] = true;
        _countByInDegree[_inDegree[vNr]]--;
        if (_totalOut[idx1] == -1) {
            _totalOut[idx1] = 0;
            if (idx1 > 0) {
                _totalOut[idx1] = _totalOut[idx1-1];
            }
        } else {
            final int nr = _order[_part1[idx1]-1];
            _totalOut[idx1] += _inDegree[nr] - (_cumulDegree[nr][_part1[idx1]-1] - _inDegree[nr]);
        }
        for (int i = 0; i < nb.length; i++) {
            final int nr = nb[i];
            _inDegree[nr]++;
            if (!_inPart1[nr] && _invOrder[nr] < _part1[idx1]) {
                _totalOut[idx1]++;
            }
            if (_invOrder[nr] > _part1[idx1]) {
                _countByInDegree[_inDegree[nr]-1]--;
                _countByInDegree[_inDegree[nr]]++;
            }
        }
    }
   
    private void remove(int idx1) {
        final int vNr = _order[_part1[idx1]];
        final int[] nb = _neighbors[vNr];
        _inPart1[vNr] = false;
        for (int i = 0; i < nb.length; i++) {
            final int nr = nb[i];
            _inDegree[nr]--;
            if (_invOrder[nr] > _part1[idx1]) {
                _countByInDegree[_inDegree[nr]+1]--;
                _countByInDegree[_inDegree[nr]]++;
            }
        }
    }
   
    protected Integer internalLowerBound(Map<V, Integer> optionalData) {
        List<Integer> optional = new ArrayList<Integer>();
        Integer lower = new EdgeConnectivity<V, E>(this).getResult(optional);
        _upperBound = Math.min(_upperBound, optional.get(0));
        return lower;
    }

    protected Integer internalUpperBound(Map<V, Integer> optionalData) {
        if (4*_numEdges <= _neighbors.length) return 0;
        if (2*_numEdges == _neighbors.length * (_neighbors.length - 1)) {
            if (_neighbors.length % 2 == 0) {
                return _neighbors.length * _neighbors.length / 4;
            } else {
                return (_neighbors.length-1) * (_neighbors.length+1) / 4;
            }
        }
        int lowerBound = getLowerBound(null);
        final int maxDegree = orderNodes();
        for (int i = 0; i < _neighbors.length; i++) {
            final int[] nb = _neighbors[i];
            for (int j = 0; j < nb.length; j++) {
                _cumulDegree[i][_invOrder[nb[j]]] = 1;
            }
            for (int j = 1; j < _neighbors.length; j++) {
                _cumulDegree[i][j] += _cumulDegree[i][j-1];
            }
        }
        
        if (maxDegree < 3 && _upperBound >= 3) _upperBound = maxDegree;
        _upperBound = Math.min(_upperBound, greedyBisection());
//         System.out.println(new cern.colt.list.IntArrayList(_order));
        System.out.println(lowerBound+" "+_upperBound);
//         if (_upperBound == lowerBound) return lowerBound;
        _countByInDegree[0] = _neighbors.length;
        int idx = 1;
        _part1[0] = 0;
        add(0);
        _part1[1] = 1;
        while (idx > 0) {
            add(idx);
//                         System.out.println(new cern.colt.list.IntArrayList(_part1));
//             System.out.println(new cern.colt.list.IntArrayList(_inDegree));
//             System.out.println(_neighbors.length-_part1[idx]-1+" "+new cern.colt.list.IntArrayList(_countByInDegree));
            if (_totalOut[idx] < _upperBound) {
                final int outDegree = totalOutDegree(idx);
                if (outDegree < _upperBound) {
                    if (idx < _part1.length - 1) {
                        idx++;
                        _part1[idx] = -1;
                    } else {
//                         System.out.println(new cern.colt.list.IntArrayList(_part1));
                        _upperBound = outDegree;
                        if (_upperBound == lowerBound) return lowerBound;
                        System.out.println(lowerBound+" "+_upperBound);
                    }
                }
            }
            while (idx > 0) {
                if (_part1[idx] != -1) {
                    remove(idx);
                } else {
                    _part1[idx] = _part1[idx-1];
                }
                if (++_part1[idx] <= _neighbors.length - _part1.length + idx) break;
                _totalOut[idx] = -1;
                for (int i = _part1[idx-1]+1; i < _part1[idx]; i++) {
                    _countByInDegree[_inDegree[_order[i]]]++;
                }
                idx--;
            }
        }
        return _upperBound;
    }
}
