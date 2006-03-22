/*
 MessageCreationException.java / Frost
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
package frost.messages;

/**
 * @author $Author$
 * @version $Revision$
 */
public class MessageCreationException extends Exception {

	private boolean empty = false;
	
	/**
	 * @param message
	 * @param cause
	 */
	public MessageCreationException(String message, Throwable cause) {
		super(message, cause);
	}
	/**
	 * This method creates a new MessageCreationException with the given
	 * message.
	 * @param message a string describing the exception
	 */
	public MessageCreationException(String message) {
		super(message);
	}
	
	/**
	 * This method creates a new MessageCreationException with the given
	 * message and empty property.
	 * @param message a string describing the exception
	 * @param empty true if message creation failed because the file only contained
	 * 		  the word "Empty". False if the reason was different.
	 */
	public MessageCreationException(String message, boolean empty) {
		super(message);
		this.empty = empty;
	}
	
	/**
	 * This method returns true if message creation failed because the file 
	 * only contained the word "Empty"
	 * @return true if message creation failed because the file only contained
	 * 				the word "Empty". False if the reason was different.
	 */
	public boolean isEmpty() {
		return empty;
	}
	
	/**
	 * This method sets the empty property of the exception, used to distinguish
	 * between the file only containing the word "Empty" and other reasons for
	 * the failure.
	 * @param empty true if message creation failed because the file only contained
	 * 				the word "Empty". False if the reason was different.
	 */
	public void setEmpty(boolean empty) {
		this.empty = empty;
	}
}
