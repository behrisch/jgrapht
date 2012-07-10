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
 * SortedBuckets.java
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


/**
 * @author Michael Behrisch
 */
public class SortedBuckets {

    final int[] _buckets;
    final int[] _keys;
    final int[] _positions;
    final int[] _borders;
    int _maxKey;

    public SortedBuckets(final int capacity) {
        this(capacity, capacity);
    }

    public SortedBuckets(final int capacity, final int maxKey) {
        _buckets = new int[capacity];
        _keys = new int[capacity];
        _positions = new int[capacity];
        Arrays.fill(_positions, -1);
        _borders = new int[maxKey+1];
        _maxKey = 0;
    }

    public boolean add(int elem, int key) {
        if (_positions[elem] != -1) {
            return false;
        }
        _keys[elem] = key;
        while (key < _maxKey) {
            final int next = _buckets[_borders[key]];
            _buckets[_borders[key]] = elem;
            _positions[elem] = _buckets[_borders[key]];
            while (key < _keys[next]) {
                _borders[key]++;
                key++;
            }
            elem = next;
        }
        while (key > _maxKey) {
            _borders[_maxKey+1] = _borders[_maxKey];
            _maxKey++;
        }
        _buckets[_borders[key]] = elem;
        _positions[elem] = _buckets[_borders[key]];
        _borders[key]++;
        return true;
    }

    public int getKey(final int elem) {
        return _keys[elem];
    }

    public int getMaxKey() {
        return _maxKey;
    }

    public boolean increaseKey(final int elem) {
        final int pos = _positions[elem];
        if (pos == -1) {
            return false;
        }
        if (pos != _borders[_keys[elem]] - 1) {
            _buckets[pos] = _buckets[_borders[_keys[elem]] - 1];
            _buckets[_borders[_keys[elem]] - 1] = elem;
            _positions[elem] = _borders[_keys[elem]] - 1;
            _positions[_buckets[pos]]= pos;
        }
        _borders[_keys[elem]]--;
        _keys[elem]++;
        if (_keys[elem] > _maxKey) {
            _maxKey++;
            _borders[_maxKey] = _borders[_maxKey-1] + 1;
        }
        return true;
    }

    public int pop() {
        _borders[_maxKey]--;
        final int result = _buckets[_borders[_maxKey]];
        while (_maxKey > 0 && _borders[_maxKey] == _borders[_maxKey-1]) {
            _maxKey--;
        }
        _positions[result] = -1;
        return result;
    }

    public int peek() {
        return _buckets[_borders[_maxKey]-1];
    }

    public boolean isEmpty() {
        return _borders[_maxKey] == 0;
    }
}
