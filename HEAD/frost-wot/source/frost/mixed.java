/*
  mixed.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
import java.util.*;
import java.io.*;

public class mixed {

    /**
     * Compares a String with all elements of an array.
     * Returns true if element exists in array, else false.
     * @param element String to be compared
     * @param array Array of Strings to be compared
     * @return true if element exists in array, else false
     */
    public static boolean isElementOf(String element, String[] array) {
    for (int i = 0; i < array.length; i++)
        if (element.equals(array[i]))
        return true;
    return false;
    }

    /**
     * Copys a file from the jar file to disk
     * @param resource This is the file's name in the jar
     * @param file This is the destination file
     */
    public static void copyFromResource (String resource, File file) throws IOException {
    if (!file.isFile ()) {
        InputStream input = frame1.class.getResourceAsStream(resource);
        FileOutputStream output = new FileOutputStream(file);
        byte[] data = new byte[128];
        int bytesRead;

        while ((bytesRead = input.read(data)) != -1)
        output.write(data, 0, bytesRead);

        input.close();
        output.close();
    }
    }

    /**
     * Waits for a specific number of ms
     * @param time Time to wait in ms
     */
    public static void wait(int time) {
    try {
        Thread.sleep(time);
    }
    catch (InterruptedException e) {}
    }

    /**
     * Replaces characters that are not 0-9, a-z or in 'allowedCharacters'
     * with '_' and returns a lowerCase String
     * @param text original String
     * @return modified String
     */
    public static String makeFilename(String text) {
    StringBuffer newText = new StringBuffer();
    text = text.toLowerCase();

    if (frame1.frostSettings.getBoolValue("allowEvilBert")) {
        // I hope that this allows the display of 2 byte characters
        char[] invalidChars = {'/', '\\', '?', '*', '<', '>', '\"', ':', '|'};

        for (int i = 0; i < invalidChars.length; i++)
        text = text.replace(invalidChars[i], '_');

        newText.append(text);
    }
    else {
        String allowedCharacters = "()-!.";
        for (int i = 0; i < text.length(); i++) {
        int value = Character.getNumericValue(text.charAt(i));
        char character = text.charAt(i);
        if ((value >= 0 && value < 36) || allowedCharacters.indexOf(character) != -1)
            newText.append(character);
        else
        newText.append("_");
        }
    }

    return newText.toString();
    }

    /***** the date functions are all in DateFun now ******/
}
