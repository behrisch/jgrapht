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
 * IntArrayGraphAlgorithm.java
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

import org.jgrapht.*;

/**
 * @author Michael Behrisch
 */
public abstract class IntArrayApproxAlgorithm<V, E, ResultType, T>
    extends IntArrayGraphAlgorithm<V, E>
    implements ApproximationAlgorithm<ResultType, T>
{
    //~ Instance fields --------------------------------------------------------

    protected ResultType _cachedUpperBound;
    protected ResultType _cachedLowerBound;

    //~ Constructors -----------------------------------------------------------

    /**
     * @param g
     */
    public IntArrayApproxAlgorithm(final Graph<V, E> g)
    {
        super(g);
    }

    /**
     * @param other
     */
    protected IntArrayApproxAlgorithm(final IntArrayGraphAlgorithm<V, E> other)
    {
        super(other);
    }

    protected abstract ResultType internalUpperBound(T optionalData);

    protected abstract ResultType internalLowerBound(T optionalData);

    public final ResultType getUpperBound(T optionalData) {
        if (_cachedUpperBound == null || optionalData != null) {
            _cachedUpperBound = internalUpperBound(optionalData);
        }
        return _cachedUpperBound;
    }

    public final ResultType getLowerBound(T optionalData) {
        if (_cachedLowerBound == null || optionalData != null) {
            _cachedLowerBound = internalLowerBound(optionalData);
        }
        return _cachedLowerBound;
    }

    public final boolean isExact() {
        return getUpperBound(null) == getLowerBound(null);
    }
}

// End IntArrayGraphAlgorithm.java
