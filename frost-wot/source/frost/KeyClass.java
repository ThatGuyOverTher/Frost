/*
KeyClass.java / Frost
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

public class KeyClass
{
    private boolean DEBUG = false;
    private final static String[] invalidChars = {"/", "\\", "?", "*", "<", ">", "\"", ":", "|"};

    String key = new String(); // Name of this key
    String date = new String(); // Last access
    String size = new String(); // Filesize
    String filename = new String();
    boolean exchange;

    public GregorianCalendar getCal()
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        int firstPoint = date.indexOf(".");
        int secondPoint = date.lastIndexOf(".");
        if( firstPoint != -1 && secondPoint != -1 && firstPoint != secondPoint )
        {
            int year = Integer.parseInt(date.substring(0, firstPoint));
            int month = Integer.parseInt(date.substring(firstPoint + 1, secondPoint));
            int day = Integer.parseInt(date.substring(secondPoint + 1, date.length()));
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month - 1);
            cal.set(Calendar.DATE, day - 1);
        }
        return cal;
    }

    /**Returns true if key is outdated*/
    public boolean checkDate()
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        int firstPoint = date.indexOf(".");
        int secondPoint = date.lastIndexOf(".");
        int maxAge = frame1.frostSettings.getIntValue("maxAge");
        if( firstPoint != -1 && secondPoint != -1 && firstPoint != secondPoint )
        {
            try
            {
                int year = Integer.parseInt(date.substring(0, firstPoint));
                int month = Integer.parseInt(date.substring(firstPoint + 1, secondPoint));
                int day = Integer.parseInt(date.substring(secondPoint + 1, date.length()));
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month - 1);
                cal.set(Calendar.DATE, day + maxAge);

                GregorianCalendar today = new GregorianCalendar();
                today.setTimeZone(TimeZone.getTimeZone("GMT"));

                if( today.after(cal) )
                {
                    if( DEBUG ) System.out.println(filename + " is outdated");
                    return false;
                }

                today.add(Calendar.DATE, (maxAge + 2)); // Accept one day into future
                if( cal.after(today) )
                {
                    if( DEBUG ) System.out.println("Future date of " + filename + " " + year + month + day);
                    return false;
                }

                return true;
            }
            catch( NumberFormatException e )
            {
                if( DEBUG ) System.out.println("Date of " + filename + " is invalid");
                return false;
            }
        }
        else
        {
            if( DEBUG ) System.out.println(filename + " has invalid date");
            return false;
        }
    }

    /**Tests if the filename is valid*/
    public boolean checkFilename()
    {
        if( filename.length() == 0 || filename.length() > 255 )
            return false;

        for( int i = 0; i < invalidChars.length; i++ )
        {
            if( filename.indexOf(invalidChars[i]) != -1 )
            {
                if( DEBUG ) System.out.println(filename + " has invalid filename");
                return false;
            }
        }
        return true;
    }

    /** Tests, if size is a valid integer value*/
    public boolean checkSize()
    {
        try {
            int tmp = Integer.parseInt(size);
        }
        catch( NumberFormatException e ) {
            if( DEBUG ) System.out.println("Invalid size in key " + filename);
            return false;
        }
        return true;
    }

    /**Tests if key is valid*/
    public boolean checkKey()
    {
        if( key.startsWith("CHK@") && key.length() == 58 ) return true;
        //  if (DEBUG) System.out.println("Invalid key in " + filename);
        return false;
    }

    /**Returns true if key is valid*/
    public boolean isValid()
    {
        boolean valid = checkDate();
        valid = valid && checkSize();
        valid = valid && checkFilename();
        valid = valid && checkKey();
        return valid;
    }

    /**Set filename*/
    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    /**Get filename*/
    public String getFilename()
    {
        return filename.trim();
    }

    /**Set key*/
    public void setKey(String key)
    {
        this.key = key;
    }

    /**Get key*/
    public String getKey()
    {
        return key.trim();
    }

    /**Set date*/
    public void setDate(String date)
    {
        this.date = date;
    }

    /**Get date*/
    public String getDate()
    {
        return date.trim();
    }

    /**Set size*/
    public void setSize(String size)
    {
        this.size = size;
    }

    /**Set size*/
    public void setSize(long size)
    {
        this.size = String.valueOf(size);
    }

    /**Get size*/
    public String getSize()
    {
        return size.trim();
    }

    public boolean getExchange()
    {
        return exchange;
    }
    public void setExchange(boolean exchange)
    {
        this.exchange = exchange;
    }

    /** Constructor*/
    public KeyClass(String key)
    {
        this.key = key;
        this.exchange = true;
    }
}
