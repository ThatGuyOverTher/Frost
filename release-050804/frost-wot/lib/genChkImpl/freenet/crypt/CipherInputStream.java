package freenet.crypt;
/*
  This code is part of the Java Adaptive Network Client by Ian Clarke. 
  It is distributed under the GNU Public Licence (GPL) version 2.  See
  http://www.gnu.org/ for further details of the GPL.
*/

import java.io.*;

/**
 * Implements a Java InputStream that is encrypted with any symmetric block
 * cipher (implementing the BlockCipher interface).
 * 
 * This stream operates in Periodic Cipher Feedback Mode (PCFB), allowing 
 * byte at a time encryption with no additional encryption workload.
 */

public class CipherInputStream extends FilterInputStream {

    private final PCFBMode ctx;
    private boolean needIV = false;

    public CipherInputStream(BlockCipher c, InputStream in) throws IOException {
        this(new PCFBMode(c), in);
    }

    public CipherInputStream(BlockCipher c, InputStream in, boolean readIV) 
        throws IOException {

        this(new PCFBMode(c), in);
        if (readIV) ctx.readIV(this.in);
    }

    /**
     * This constructor causes the IV to be read of the connection the
     * first time one of the read messages is read (if later is set).
     */
    public CipherInputStream(BlockCipher c, InputStream in, boolean readIV,
                             boolean later) throws IOException {
        this(new PCFBMode(c), in);
        if (readIV && later)
            needIV = true;
        else if (readIV)
            ctx.readIV(this.in);
    }

    public CipherInputStream(BlockCipher c, InputStream in, byte[] iv) throws IOException {
        this(new PCFBMode(c, iv), in);
    }

    public CipherInputStream(PCFBMode ctx, InputStream in) throws IOException {
        super(in);
        this.ctx = ctx;
    }

    //int read = 0;
    public int read() throws IOException {
        if (needIV) {
            ctx.readIV(in);
            needIV = false;
        }
        //System.err.println("CIS READING");
        int rv=in.read();

        //if ((read++ % 5) == 0)
        //    System.err.println("CIS READ " + read);
        return (rv==-1 ? -1 : ctx.decipher(rv));
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (needIV) {
            ctx.readIV(in);
            needIV = false;
        }
        //System.err.println("CIS READING IN: " + in.toString() + " LEN: " + 
        //                   len);
        int rv=in.read(b, off, len);
        //System.err.println("CIS READ " + (read += rv));
        if (rv != -1) {
            ctx.blockDecipher(b, off, rv);
            return rv;
        } else 
            return -1;
    }

    public int available() throws IOException {
        int r = in.available();
        return (needIV ? Math.max(0, r - ctx.lengthIV()) : r);
    }


}








