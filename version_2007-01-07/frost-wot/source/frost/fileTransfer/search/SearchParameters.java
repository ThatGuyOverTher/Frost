/*
  SearchParameters.java / Frost
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
package frost.fileTransfer.search;

import java.util.*;

import frost.util.*;

public class SearchParameters {
    
//    public static void main(String[] args) {
//        SearchParameters s = new SearchParameters();
//        s.setNameString("hello not world \"und so weiter\" aber NOT dieses hier \"und so\"");
//        System.out.println(s.getName());
//        System.out.println(s.getNotName());
//    }
    
    private boolean isSimpleSearch;
    
    public static final int EXTENSIONS_ALL         = 1;
    public static final int EXTENSIONS_AUDIO       = 2;
    public static final int EXTENSIONS_VIDEO       = 3;
    public static final int EXTENSIONS_IMAGES      = 4;
    public static final int EXTENSIONS_DOCUMENTS   = 5;
    public static final int EXTENSIONS_ARCHIVES    = 6;
    public static final int EXTENSIONS_EXECUTABLES = 7;
    
    private int extensions = EXTENSIONS_ALL;
    
    private String tabText = null;
    
    private boolean withKeyOnly = false;
    
    // simple search
    private List<String> simpleSearchStrings = null;
    private List<String> simpleSearchNotStrings = null;
    
    // advanced search
    private List<String> name = null;
    private List<String> comment = null;
    private List<String> keyword = null;
    private List<String> owner = null;

    private List<String> notName = null;
    private List<String> notComment = null;
    private List<String> notKeyword = null;
    private List<String> notOwner = null;

    private static List<String> emptyList = new LinkedList<String>();
    
    public SearchParameters(boolean simpleSearch) {
        isSimpleSearch = simpleSearch;
    }

    /**
     * @return  a descriptive text to display on the search tab
     */
    public String getTabText() {
        if( tabText == null ) {
            List<String> allStrings = new LinkedList<String>();
            if( isSimpleSearch() ) {
                allStrings.addAll(getSimpleSearchStrings());
                if( getSimpleSearchNotStrings().size() > 0 ) {
                    allStrings.add("NOT");
                    allStrings.addAll(getSimpleSearchNotStrings());
                }
            } else {
                allStrings.addAll(getName());
                allStrings.addAll(getComment());
                allStrings.addAll(getKeyword());
                allStrings.addAll(getOwner());
                if( getNotName().size() > 0 
                        || getNotComment().size() > 0 
                        || getNotKeyword().size() > 0 
                        || getNotOwner().size() > 0 ) 
                {
                    allStrings.add("NOT");
                    allStrings.addAll(getNotName());
                    allStrings.addAll(getNotComment());
                    allStrings.addAll(getNotKeyword());
                    allStrings.addAll(getNotOwner());
                }
            }
            
            StringBuffer sb = new StringBuffer();
            for( Iterator i = allStrings.iterator(); i.hasNext() && ( !(sb.length() > 30) ); ) {
                // FIXME: don't append only the 'NOT' to end of final string
                String s = (String) i.next();
                sb.append(s).append(" ");
            }
            tabText = sb.toString().trim();

            if( tabText.length() == 0 ) {
                tabText = "*";
            }
        }
        return tabText;
    }
    
    public void setExtensions(String searchType) {
        extensions = EXTENSIONS_ALL; // default
        
        if( searchType.equals("SearchPane.fileTypes.audio") ) {
            extensions = EXTENSIONS_AUDIO;
        } else if( searchType.equals("SearchPane.fileTypes.video") ) {
            extensions = EXTENSIONS_VIDEO;
        } else if( searchType.equals("SearchPane.fileTypes.images") ) {
            extensions = EXTENSIONS_IMAGES;
        } else if( searchType.equals("SearchPane.fileTypes.documents") ) {
            extensions = EXTENSIONS_DOCUMENTS;
        } else if( searchType.equals("SearchPane.fileTypes.executables") ) {
            extensions = EXTENSIONS_EXECUTABLES;
        } else if( searchType.equals("SearchPane.fileTypes.archives") ) {
            extensions = EXTENSIONS_ARCHIVES;
        }
    }
    public int getExtensions() {
        return extensions;
    }

    public void setSimpleSearchString(String simpleSearchStr) {
        List<String>[] res = TextSearchFun.splitStrings(simpleSearchStr, true);
        simpleSearchStrings = res[0];
        simpleSearchNotStrings = res[1];
    }
    
    public void setCommentString(String commentStr) {
        List<String>[] res = TextSearchFun.splitStrings(commentStr, true);
        comment = res[0];
        notComment = res[1];
    }
    public void setKeywordString(String keywordStr) {
        List<String>[] res = TextSearchFun.splitStrings(keywordStr, true);
        keyword = res[0];
        notKeyword = res[1];
    }
    public void setNameString(String nameStr) {
        List<String>[] res = TextSearchFun.splitStrings(nameStr, true);
        name = res[0];
        notName = res[1];
    }
    public void setOwnerString(String ownerStr) {
        List<String>[] res = TextSearchFun.splitStrings(ownerStr, true);
        owner = res[0];
        notOwner = res[1];
    }
    
    public List<String> getComment() {
        if( comment == null ) {
            return emptyList;
        }
        return comment;
    }

    public List<String> getKeyword() {
        if( keyword == null ) {
            return emptyList;
        }
        return keyword;
    }

    public List<String> getName() {
        if( name == null ) {
            return emptyList;
        }
        return name;
    }

    public List<String> getOwner() {
        if( owner == null ) {
            return emptyList;
        }
        return owner;
    }
    
    public List<String> getNotComment() {
        if( notComment == null ) {
            return emptyList;
        }
        return notComment;
    }

    public List<String> getNotKeyword() {
        if( notKeyword == null ) {
            return emptyList;
        }
        return notKeyword;
    }

    public List<String> getNotName() {
        if( notName == null ) {
            return emptyList;
        }
        return notName;
    }

    public List<String> getNotOwner() {
        if( notOwner == null ) {
            return emptyList;
        }
        return notOwner;
    }
    
    public boolean getWithKeyOnly() {
        return withKeyOnly;
    }
    public void setWithKeyOnly(boolean withKeyOnly) {
        this.withKeyOnly = withKeyOnly;
    }

    public boolean isSimpleSearch() {
        return isSimpleSearch;
    }

    public List<String> getSimpleSearchNotStrings() {
        return simpleSearchNotStrings;
    }

    public List<String> getSimpleSearchStrings() {
        return simpleSearchStrings;
    }
}
