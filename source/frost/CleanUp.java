/*
 * CleanUp.java
 *
 * Created on January 22, 2003, 8:24 PM
 */

package frost;
import java.io.*;
import java.util.*;
                      
/**
 * @author FillaMent
 */
public class CleanUp {

   private String folder;
   private int daysOld;

   /**
    * @param Folder Folder for cleaning
    * @param DaysOld Days of files to keep
    */   
   public CleanUp(String Folder, int DaysOld) {
	this(Folder, DaysOld, false);
   }

   /**
    * @param Folder Folder for cleaning
    * @param DaysOld Days of files to keep
    * @param goNow Clean immediately
    */   
   public CleanUp(String Folder, int DaysOld, boolean goNow) {
	folder = Folder;
	daysOld = DaysOld;
	if (goNow) {
		doCleanup();
	}
   }

   public void doCleanup() {
	recursDir(folder);
   }

   /**
    * @param dirItem Folder for recursion
    */   
   private void recursDir(String dirItem) 
   {    
      String list[];
      File file = new File(dirItem);
      if (file.isDirectory() && file.listFiles().length > 0) {
          System.out.println("\n"+file+":");
          Vector vd = new Vector();
          Vector vf = new Vector();
          list = file.list();
          Arrays.sort(list,String.CASE_INSENSITIVE_ORDER);          
          
          for (int i = 0; i < list.length; i++)
          {
             File f = new File(dirItem + File.separatorChar + list[i]);
             if (f.isDirectory()) vd.add(list[i]); else vf.add(list[i]);
          }
          for (int a=0; a < vf.size(); a++) 
            recursDir (dirItem + File.separatorChar + vf.get(a));
          for (int d=0; d < vd.size(); d++)
            recursDir (dirItem + File.separatorChar + vd.get(d));
      } else
      processItem(dirItem);
      list=null;
   //   vd=null;
 //     vf=null;
    }   
    
   /**
    * @param dirItem Item for inspection/deletion
    */   
    private void processItem(String dirItem)
    {
        File f = new File(dirItem);
	long expiration = new Date().getTime() - (daysOld*24 * 60 * 60 * 1000);
	if (f.lastModified() < expiration || f.isDirectory() && !f.getName().endsWith(".key")) {
		f.delete();
	}
	f=null;
    }
    
}
