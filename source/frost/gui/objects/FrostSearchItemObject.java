package frost.gui.objects;

import frost.KeyClass;
import frost.gui.model.TableMember;


public class FrostSearchItemObject implements FrostSearchItem, TableMember
{
    FrostBoardObject board;
    KeyClass key;
    int state;

    public static final int STATE_NONE        = 1; // set if a search table item is only in search table
    public static final int STATE_DOWNLOADED  = 2; // set if the item is already downloaded and is found in download folder
    public static final int STATE_DOWNLOADING = 3; // set if file is not already downloaded, but in download table
    public static final int STATE_UPLOADING   = 4; // set if file is in upload table

    public FrostSearchItemObject( FrostBoardObject board, KeyClass key, int state )
    {
        this.board = board;
        this.key = key;
        this.state = state;
    }

    /**
     * Returns the object representing value of column. Can be string or icon
     *
     * @param   column  Column to be displayed
     * @return  Object representing table entry.
     */
    public Object getValueAt(int column)
    {
    	boolean offline = (key.getDate() == null || key.getDate().length()==0) &&
				(key.getKey() == null || key.getKey().length() ==0);
        switch(column) {
            case 0: 
	    	if (offline)
			return "<html><font color=\"gray\">"+key.getFilename()+"</font></html>";
		else 
			return "<html><b>"+key.getFilename()+"</b></html>";
            case 1: return key.getSize();
            case 2: 
	    	if (offline)
			return "offline";
		else
	    		return key.getDate();
			
            case 3: //here it is possible to color files from friends in green, later
	    	if (key.getOwner()==null || key.getOwner().length()==0)
	    		return "Anonymous";
		else
			return key.getOwner();
	  
            case 4: return board.toString();
            default: return "*ERR*";
        }
    }

    public int compareTo( TableMember anOther, int tableColumIndex )
    {
        Comparable c1 = (Comparable)getValueAt(tableColumIndex);
        Comparable c2 = (Comparable)anOther.getValueAt(tableColumIndex);
        return c1.compareTo( c2 );
    }

////// Implementing the FrostSearchItem interface //////

    public String getFilename()
    {
        return key.getFilename();
    }

    public Long getSize()
    {
        return key.getSize();
    }

    public String getDate()
    {
        return key.getDate();
    }

    public String getKey()
    {
        return key.getKey();
    }

    public FrostBoardObject getBoard()
    {
        return board;
    }

    public int getState()
    {
        return state;
    }
    
    public String getOwner() {
    	return key.getOwner();
    }
    
    public String getSHA1() {
    	return key.getSHA1();
    }
}
