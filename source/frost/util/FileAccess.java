/*
  FileAccess.java / File Access
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

package frost.util;

import java.awt.*;
import java.io.*;
import java.nio.channels.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;
import java.util.zip.*;

import javax.swing.*;

import frost.*;

public class FileAccess {

    private static final Logger logger = Logger.getLogger(FileAccess.class.getName());

    public static File createTempFile(final String prefix, final String suffix) {
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile(prefix, suffix, new File(Core.frostSettings.getValue(SettingsClass.DIR_TEMP)));
        } catch( final Throwable ex ) {
        }
        if( tmpFile == null ) {
            do {
                tmpFile = new File(
                        Core.frostSettings.getValue(SettingsClass.DIR_TEMP)+
                        prefix+
                        System.currentTimeMillis()+
                        suffix);
            } while(tmpFile.isFile());
        }
        return tmpFile;
    }

    /**
     * Writes a file to disk after opening a saveDialog window
     * @param parent The parent component, often 'this' can be used
     * @param conten The data to write to disk.
     * @param lastUsedDirectory The saveDialog starts at this directory
     * @param title The saveDialog gets this title
     */
    public static void saveDialog(final Component parent, final String content, final String lastUsedDirectory,   final String title) {

        final JFileChooser fc = new JFileChooser(lastUsedDirectory);
        fc.setDialogTitle(title);
        fc.setFileHidingEnabled(true);
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(false);

        final int returnVal = fc.showSaveDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final File file = fc.getSelectedFile();
            if (file != null) {
                Core.frostSettings.setValue(SettingsClass.DIR_LAST_USED, file.getParent());
                if (!file.isDirectory()) {
                    writeFile(content, file, "UTF-8");
                }
            }
        }
    }

    /**
     * Reads a file and returns it's content in a byte[]
     * @param file the file to read
     * @return byte[] with the files content
     */
    public static byte[] readByteArray(final String filename) {
        return readByteArray(new File(filename));
    }

    public static byte[] readByteArray(final File file) {
        try {
            final byte[] data = new byte[(int)file.length()];
            final FileInputStream fileIn = new FileInputStream(file);
            final DataInputStream din = new DataInputStream(fileIn);
            din.readFully(data);
            fileIn.close();
            return data;
        } catch( final IOException e ) {
            logger.log(Level.SEVERE, "Exception thrown in readByteArray(File file)", e);
        }
        return null;
    }



    /**
     * Returns all files starting from given directory/file.
     */
    public static List<File> getAllEntries(final File file) {
        final List<File> files = new LinkedList<File>();
        getAllFilesRecursive(file, files);
        return files;
    }

    /**
     * Returns all files starting from given directory/file.
     */
    private static void getAllFilesRecursive(final File file, final List<File> filesLst) {
        if( file != null ) {
            if( file.isDirectory() ) {
                final File[] dirfiles = file.listFiles();
                if( dirfiles != null ) {
                    for( final File dirfile : dirfiles ) {
                        getAllFilesRecursive(dirfile, filesLst); // process recursive
                    }
                }
            } else if (!file.isHidden() && file.isFile() && file.length()>0) {
                filesLst.add( file );
            }
        }
    }

    /**
     * Compresses a file into a gzip file.
     *
     * @param inputFile   file to compress
     * @param outputFile  gzip file
     * @return   true if OK
     */
    public static boolean compressFileGZip(final File inputFile, final File outputFile) {

        final int bufferSize = 4096;

        GZIPOutputStream out = null;
        FileInputStream in = null;

        try {
            in = new FileInputStream(inputFile);
            out = new GZIPOutputStream(new FileOutputStream(outputFile));

            final byte[] buf = new byte[bufferSize];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            return true;
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Exception catched", t);
            try { if( in != null) {
                in.close();
            } } catch(final Throwable tt) { }
            try { if( out != null) {
                out.close();
            } } catch(final Throwable tt) { }
            return false;
        }
    }

    /**
     * Decompresses a gzip file.
     *
     * @param inputFile   gzip file
     * @param outputFile  unzipped file
     * @return   true if OK
     */
    public static boolean decompressFileGZip(final File inputFile, final File outputFile) {

        final int bufferSize = 4096;

        GZIPInputStream in = null;
        OutputStream out = null;

        try {
            in = new GZIPInputStream(new FileInputStream(inputFile));
            out = new FileOutputStream(outputFile);

            final byte[] buf = new byte[bufferSize];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            return true;
        } catch(final Throwable t) {
            logger.log(Level.SEVERE, "Exception catched", t);
            try { if( in != null) {
                in.close();
            } } catch(final Throwable tt) { }
            try { if( out != null) {
                out.close();
            } } catch(final Throwable tt) { }
            return false;
        }
    }

    /**
     * Writes zip file
     */
    public static boolean writeZipFile(final byte[] content, final String entry, final File file) {
        if (content == null || content.length == 0) {
            final Exception e = new Exception();
            e.fillInStackTrace();
            logger.log(Level.SEVERE, "Tried to zip an empty file!  Send this output to a dev"+
                                        " and describe what you were doing.", e);
            return false;
        }
        try {
            final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
            zos.setLevel(9); // maximum compression
            final ZipEntry ze = new ZipEntry(entry);
            ze.setSize(content.length);
            zos.putNextEntry(ze);
            zos.write(content);
            zos.flush(); //do this before closeEntry()
            zos.closeEntry();
            zos.close();
            return true;
        } catch( final Throwable e ) {
            logger.log(Level.SEVERE, "Exception thrown in writeZipFile(byte[] content, String entry, File file)", e);
            return false;
        }
    }

    /**
     * Reads first zip file entry and returns content in a byte[].
     */
    public static byte[] readZipFileBinary(final File file) {
        if( !file.isFile() || file.length() == 0 ) {
            return null;
        }

        final int bufferSize = 4096;
        ZipInputStream zis = null;
        ByteArrayOutputStream out = null;
        try {
            zis = new ZipInputStream(new FileInputStream(file));
            out = new ByteArrayOutputStream();
            zis.getNextEntry();

            final byte[] zipData = new byte[bufferSize];
            while( true ) {
                final int len = zis.read(zipData);
                if( len < 0 ) {
                    break;
                }
                out.write(zipData, 0, len);
            }
            zis.close();
            return out.toByteArray();

        } catch( final FileNotFoundException e ) {
            logger.log(Level.SEVERE, "Exception catched", e);
        } catch( final IOException e ) {
            try { if( zis != null) {
                zis.close();
            } } catch(final Throwable t) { }
            try { if( out != null) {
                out.close();
            } } catch(final Throwable t) { }
            logger.log(Level.SEVERE, "Exception thrown in readZipFile(String path) \n" +
                                     "Offending file saved as badfile.zip, send to a dev for analysis", e);
            copyFile(file.getPath(), "badfile.zip");
        }
        return null;
    }

    /**
     * Reads file and returns a List of lines.
     * Encoding "ISO-8859-1" is used.
     */
    public static List<String> readLines(final File file) {
        return readLines(file, "ISO-8859-1");
    }
    /**
     * Reads a File and returns a List of lines
     */
    public static List<String> readLines(final File file, final String encoding) {
        List<String> result = null;
        try {
            final FileInputStream fis = new FileInputStream(file);
            result = readLines(fis, encoding);
            fis.close();
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "Exception thrown in readLines(File file, String encoding)", e);
        }
        return result;
    }

    /**
     * Reads an InputStream and returns a List of lines.
     */
    public static ArrayList<String> readLines(final InputStream is, final String encoding) {
        String line;
        final ArrayList<String> data = new ArrayList<String>();
        try {
            final InputStreamReader iSReader = new InputStreamReader(is, encoding);
            final BufferedReader reader = new BufferedReader(iSReader);
            while( (line = reader.readLine()) != null ) {
                data.add(line.trim());
            }
            reader.close();
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "Exception thrown in readLines(InputStream is, String encoding)", e);
        }
        return data;
    }


    /**
     * Reads a file and returns its contents in a String
     */
    public static String readFile(final File file) {
        String line;
        final StringBuilder sb = new StringBuilder();
        try {
            final BufferedReader f = new BufferedReader(new FileReader(file));
            while( (line = f.readLine()) != null ) {
                sb.append(line).append("\n");
            }
            f.close();
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "Exception thrown in readFile(String path)", e);
        }
        return sb.toString();
    }

    /**
     * Reads a file, line by line, and adds a \n after each one. You can specify the encoding to use when reading.
     *
     * @param path
     * @param encoding
     * @return the contents of the file
     */
    public static String readFile(final File file, final String encoding) {
        String line;
        final StringBuilder sb = new StringBuilder();
        try {
            final InputStreamReader iSReader = new InputStreamReader(new FileInputStream(file), encoding);
            final BufferedReader reader = new BufferedReader(iSReader);
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "Exception thrown in readFile(String path, String encoding)", e);
        }
        return sb.toString();
    }

    /**
     * Writes a file "file" to "path"
     */
    public static boolean writeFile(final String content, final String filename) {
        return writeFile(content, new File(filename));
    }

    /**
     * Writes a file "file" to "path", being able to specify the encoding
     */
    public static boolean writeFile(final String content, final String filename, final String encoding) {
        return writeFile(content, new File(filename), encoding);
    }

    /**
     * Writes a text file in ISO-8859-1 encoding.
     */
    public static boolean writeFile(final String content, final File file) {
        OutputStreamWriter out = null;
        try {
            // write the file in single byte codepage (default could be a DBCS codepage)
            try {
                out = new OutputStreamWriter(new FileOutputStream(file), "ISO-8859-1");
            } catch(final UnsupportedEncodingException e) {
                out = new FileWriter(file);
            }
            out.write(content);
            out.close();
            return true;
        } catch( final Throwable e ) {
            logger.log(Level.SEVERE, "Exception thrown in writeFile(String content, File file)", e);
            try { if( out != null) {
                out.close();
            } } catch(final Throwable tt) { }
            return false;
        }
    }

    public static boolean writeFile(final byte[] content, final File file) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(content);
            out.close();
            return true;
        } catch( final Throwable e ) {
            logger.log(Level.SEVERE, "Exception thrown in writeFile(byte[] content, File file)", e);
            try { if( out != null) {
                out.close();
            } } catch(final Throwable tt) { }
            return false;
        }
    }

    /**
     * Writes a text file in specified encoding. Converts line separators to target platform.
     */
    public static boolean writeFile(final String content, final File file, final String encoding) {
        BufferedReader inputReader = null;
        OutputStreamWriter outputWriter = null;
        try {
            outputWriter = new OutputStreamWriter(new FileOutputStream(file), encoding);

            inputReader = new BufferedReader(new StringReader(content));
            final String lineSeparator = System.getProperty("line.separator");
            String line = inputReader.readLine();

            while (line != null) {
                outputWriter.write(line + lineSeparator);
                line = inputReader.readLine();
            }

            outputWriter.close();
            inputReader.close();
            return true;
        } catch (final Throwable e) {
            logger.log(Level.SEVERE, "Exception thrown in writeFile(String content, File file, String encoding)", e);
            try { if( inputReader != null) {
                inputReader.close();
            } } catch(final Throwable tt) { }
            try { if( outputWriter != null) {
                outputWriter.close();
            } } catch(final Throwable tt) { }
            return false;
        }
    }

    /**
     * Deletes the given directory and ALL FILES/DIRS IN IT !!!
     * USE CAREFUL !!!
     */
    public static boolean deleteDir(final File dir) {
        if( dir.isDirectory() ) {
            final String[] children = dir.list();
            for( final String element : children ) {
                final boolean success = deleteDir(new File(dir, element));
                if( !success ) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }
    
    /**
    * Create given directory
    */
   public static boolean createDir(final java.io.File dir) {
       if( dir.isDirectory() ) {
    	   return true;
       }
       
       if( dir.exists()) {
    	   return false;
       }
       
       return dir.mkdirs();
   }
    
    

    /**
     * This method copies the contents of one file to another. If the destination file didn't exist, it is created. If
     * it did exist, its contents are overwritten.
     *
     * @param sourceName
     *            name of the source file
     * @param destName
     *            name of the destination file
     */
    public static boolean copyFile(final String sourceName, final String destName) {
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;
        boolean wasOk = false;
        try {
            sourceChannel = new FileInputStream(sourceName).getChannel();
            destChannel = new FileOutputStream(destName).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            wasOk = true;
        } catch (final Throwable exception) {
            logger.log(Level.SEVERE, "Exception in copyFile", exception);
        } finally {
            try { if( sourceChannel != null) {
                sourceChannel.close();
            } } catch(final Throwable tt) { }
            try { if( destChannel != null) {
                destChannel.close();
            } } catch(final Throwable tt) { }
        }
        return wasOk;
    }

    /**
     * This method compares 2 file byte by byte.
     * Returns true if they are equals, or false.
     */
    public static boolean compareFiles(final File f1, final File f2) {
        BufferedInputStream s1 = null;
        BufferedInputStream s2 = null;
        try {
            s1 = new BufferedInputStream(new FileInputStream(f1));
            s2 = new BufferedInputStream(new FileInputStream(f2));
            int i1, i2;
            boolean equals = false;
            while(true) {
                i1 = s1.read();
                i2 = s2.read();
                if( i1 != i2 ) {
                    equals = false;
                    break;
                }
                if( i1 < 0 && i2 < 0 ) {
                    equals = true; // both at EOF
                    break;
                }
            }
            s1.close();
            s2.close();
            return equals;
        } catch(final Throwable e) {
            try { if( s1 != null) {
                s1.close();
            } } catch(final Throwable tt) { }
            try { if( s2 != null) {
                s2.close();
            } } catch(final Throwable tt) { }
            return false;
        }
    }

    /**
     * Copys a file from the jar file to disk
     * @param resource This is the file's name in the jar
     * @param file This is the destination file
     */
    public static void copyFromResource(final String resource, final File file) throws IOException {
        if (!file.isFile()) {
            final InputStream input = MainFrame.class.getResourceAsStream(resource);
            final FileOutputStream output = new FileOutputStream(file);
            final byte[] data = new byte[4096];
            int bytesRead;
            while ((bytesRead = input.read(data)) != -1) {
                output.write(data, 0, bytesRead);
            }
            input.close();
            output.close();
        }
    }

    /**
     * Appends a line to the specified text file in UTF-8 encoding.
     */
    public static boolean appendLineToTextfile(final File file, final String line) {
        BufferedWriter out = null;
        boolean wasOk = false;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
            out.write(line);
            out.write("\n");
            wasOk = true;
        } catch (final Throwable e) {
            logger.log(Level.SEVERE, "Exception catched", e);
        } finally {
            try { if( out != null) {
                out.close();
            } } catch(final Throwable tt) { }
        }
        return wasOk;
    }

    public static String appendSeparator(final String path) {
    	final String separator = System.getProperty("file.separator");

    	if (path == null) {
    		return null;
    	}

    	if (!path.endsWith(separator)) {
    		return path + separator;
    	}

    	return path;
    }
}
