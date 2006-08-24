/*
  Mixed.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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
package frost;

import java.util.logging.*;

public final class Mixed {

    private static Logger logger = Logger.getLogger(Mixed.class.getName());

    private static char[] invalidChars = { '/', '\\', '?', '*', '<', '>', '\"', ':', '|', '#', '&' };
    
    /**
     * Creates a new unique ID.
     */
    public static String createUniqueId() {
        
        StringBuffer idStrSb = new StringBuffer();
        idStrSb.append(Long.toString(System.currentTimeMillis())); // millis
        idStrSb.append(DateFun.getExtendedDate());
        idStrSb.append(Long.toString(Runtime.getRuntime().freeMemory())); // free java mem
        idStrSb.append(DateFun.getExtendedTime());
        byte[] idStrPart = idStrSb.toString().getBytes();
        
        // finally add some random bytes
        byte[] idRandomPart = new byte[64];
        Core.getCrypto().getSecureRandom().nextBytes(idRandomPart);

        // concat both parts
        byte[] idBytes = new byte[idStrPart.length + idRandomPart.length];
        System.arraycopy(idStrPart, 0, idBytes, 0, idStrPart.length);
        System.arraycopy(idRandomPart, 0, idBytes, idStrPart.length-1, idRandomPart.length);
        
        String uniqueId = Core.getCrypto().computeChecksumSHA256(idBytes);
        
        return uniqueId;
    }

    /**
     * Waits for a specific number of ms
     * @param time Time to wait in ms
     */
    public static void wait(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Replaces characters that are not 0-9, a-z or in 'allowedCharacters'
     * with '_' and returns a lowerCase String
     *
     * NEW: does not allow the '#' char, because that will be used for internal folders
     *      in keypool, e.g. '#unsent#'
     *
     * @param text original String
     * @return modified String
     */
    public static String makeFilename(String text) {
        if (text == null) {
            logger.severe("ERROR: mixed.makeFilename() was called with NULL!");
            return null;
        }

        StringBuffer newText = new StringBuffer();

        if (text.startsWith("."))
            newText.append("_"); // dont allow a boardfilename like "."

        for (int i = 0; i < invalidChars.length; i++)
            text = text.replace(invalidChars[i], '_');

        newText.append(text);

        return newText.toString();
    }

    /**
     * Filters all non-english characters as well as those filtered by makeFilename
     * @param text the text to be filtered
     * @return the filtered text
     */
    public static String makeASCIIFilename(String text){

        StringBuffer newText = new StringBuffer();
        String allowedCharacters = "()-!.";
        for (int i = 0; i < text.length(); i++)
              {
                   int value = Character.getNumericValue(text.charAt(i));
                   char character = text.charAt(i);
                   if ((value >= 0 && value < 36)
                       || allowedCharacters.indexOf(character) != -1)
                       newText.append(character);
                   else
                       newText.append("_");
             }
        return makeFilename(newText.toString());  //run through the other filter just in case
    }

    /**
     * checks if the string contains non-english characters
     * @param text the string
     * @return whether it contains foreign chars
     */
//    public static boolean containsForeign(String text){
        //REDFLAG: implement?
//        char[] chars = text.toCharArray();
//        Character c = new Character(chars[0]);
//        return false;
//    }
    public static boolean binaryCompare(byte[] src, int offs, String searchTxt)
    {
        int searchLen = searchTxt.length();
        for(int x=0; x < searchLen; x++)
        {
            byte a = (byte)searchTxt.charAt(x);
            byte b = src[offs+x];
            if( a != b )
            {
                return false;
            }
        }
        return true;
    }
}
