/*
 OrderedModelListener.java / Frost
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
package frost.util.model;

import java.util.EventListener;

public interface OrderedModelListener extends EventListener {

	/**
	 * @param position
	 * @param item
	 * @param fieldID
	 * @param oldValue
	 * @param newValue
	 */
	void itemChanged(int position, ModelItem item, int fieldID, Object oldValue, Object newValue);

	/**
	 * @param position
	 * @param item 
	 */
	void itemChanged(int position, ModelItem item);

	/**
	 * @param position
	 * @param item
	 */
	void itemAdded(int position, ModelItem item);

	/**
	 * @param positions
	 * @param items
	 */
	void itemsRemoved(int[] positions, ModelItem[] items);
	
	/**
	 * @param item
	 */
	void modelCleared();

}
