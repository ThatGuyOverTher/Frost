/*
  MessageUploaderResult.java / Frost
  Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
package frost.messaging.frost.transfer;

public class MessageUploaderResult {

    private boolean isSuccess = false;
    private int uploadIndex = -1;
    private boolean keepMessage = false;

    /**
     * For successful uploads
     */
    public MessageUploaderResult(int index) {
        uploadIndex = index;
        isSuccess = true;
    }

    /**
     * For failed uploads.
     */
    public MessageUploaderResult(boolean keepMsg) {
        keepMessage = keepMsg;
        isSuccess = false;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public boolean isKeepMessage() {
        return keepMessage;
    }

    public int getUploadIndex() {
        return uploadIndex;
    }
}
