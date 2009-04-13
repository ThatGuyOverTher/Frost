/*
 ReverseComparator.java / Frost
 Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as
 published by the Free Software Foundation; either version 2 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package frost.util;

import java.util.*;

/**
 * This class implements a comparator that is the reverse of the one passed as the
 * parameter of the constructor.
 */
public class ReverseComparator implements Comparator {

	private Comparator delegate;

	/**
	 * @param newDelegate 
	 */
	public ReverseComparator(Comparator newDelegate) {
		super();
		delegate = newDelegate;
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		return -delegate.compare(o1, o2);
	}

}
