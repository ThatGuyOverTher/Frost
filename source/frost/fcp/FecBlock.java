/*
  FecBlock.java / Frost
  Copyright (C) 2003  Jan-Thomas Czornack <jantho@users.sourceforge.net>

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
package frost.fcp;

import java.io.*;
import java.util.Arrays;
import java.util.logging.*;

import freenet.support.*;

/**
 * Represents a FEC block, either a check block or a data block.
 */
public class FecBlock
{
    public static final int TYPE_DATABLOCK = 1;
    public static final int TYPE_CHECKBLOCK = 2;

    public static final int STATE_TRANSFER_WAITING  = 1; // waits for transfer
    public static final int STATE_TRANSFER_RUNNING  = 2; // is currently transferred
    public static final int STATE_TRANSFER_FINISHED = 3; // transfer is finished
    public static final int STATE_TRANSFER_INVALID  = 4; // dont touch this block, its missing
    
	private static Logger logger = Logger.getLogger(FecBlock.class.getName());

    protected int currentState = -1;
    protected int blockType = -1;

    protected String chkKey = null;
    protected File blockFile; // the File which containes this block
    protected int blockSize = -1; // the block size this block _should_ have

    protected int segmentNo; // segment this block belongs to
    protected int indexInSegment; // index of this block in its segment
    protected int indexInFile; // index of this block in the file (data file _or_ check blocks file)

    protected long fileOffset; // exact offset in file (data or checkblocks) where this block starts

    public FecBlock(int btype, File f, int segno, int ixInSeg, int ixInFile, int bsize, long foffs )
    {
        this.blockType = btype;
        this.blockFile = f;
        this.segmentNo = segno;
        this.indexInSegment = ixInSeg;
        this.indexInFile = ixInFile;
        this.blockSize = bsize;
        this.fileOffset = foffs;

        this.currentState = STATE_TRANSFER_WAITING;
    }
    
    public RandomAccessFileBucket getRandomAccessFileBucket(boolean readOnly)
    {
        RandomAccessFileBucket b = null;
        try {
            b = new RandomAccessFileBucket(this.blockFile, 
                                           this.getFileOffset(),
                                           this.getBlockSize(), 
                                           readOnly);
        }
        catch (IOException e) {
			logger.log(Level.SEVERE, "Exception thrown in getRandomAccessFileBucket(boolean readOnly)", e);
            return null;
        }
        return b;
    }
    
    /**
     * Returns the data of this block padded to blockSize.
     * Padding is needed for blocks at the end of input file.
     * Used by CHK@ key generation.
     */
    public Bucket getPaddedMemoryBucket()
    {
        byte[] data = getPaddedMemoryArray();
        if( data != null )
        {
            return new ArrayBucket( data );
        }
        return null;
    }
    
    /**
     * Returns the data of this block padded to blockSize.
     * Padding is needed for blocks at the end of input file.
     * Used by uploading.
     */
    public byte[] getPaddedMemoryArray()
    {
        byte[] data = null;
        try {
            data = new byte[ getBlockSize() ];
            long filelen = this.blockFile.length();
            if( this.fileOffset > filelen )
            {
                // block is behind end of file -> fill with 0
                Arrays.fill( data, (byte)0 );
            }
            else
            {
                // either file ends before block ends -> pad block to blockSize
                // or block is completely contained in file, read
                RandomAccessFile f = new RandomAccessFile( this.blockFile, "r" );
                f.seek( this.fileOffset );
                int read = f.read( data );
                f.close();
                if( read < 0 )
                    read = 0;
                if( read < blockSize )
                    Arrays.fill( data, read, blockSize, (byte)0 );
            }
            return data;
        }
        catch(Exception ex)
        {
			logger.log(Level.SEVERE, "Error in FecBlock.getPaddedMemoryArray()", ex);
        }
        return null;
    }

    public int getCurrentState()
    {
        return currentState;
    }

    public int getBlockType()
    {
        return blockType;
    }

    public String getChkKey()
    {
        return chkKey;
    }

    public void setChkKey(String key)
    {
        this.chkKey = key;
    }

    public int getBlockSize()
    {
        return blockSize;
    }

    /**
     * @return
     */
    public File getBlockFile()
    {
        return blockFile;
    }

    /**
     * @return
     */
    public long getFileOffset()
    {
        return fileOffset;
    }

    /**
     * @return
     */
    public int getIndexInFile()
    {
        return indexInFile;
    }

    /**
     * @return
     */
    public int getIndexInSegment()
    {
        return indexInSegment;
    }

    /**
     * @return
     */
    public int getSegmentNo()
    {
        return segmentNo;
    }

    /**
     * @param i
     */
    public void setCurrentState(int i)
    {
        currentState = i;
    }

}
