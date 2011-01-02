// REDFLAG: test and javadoc
package freenet.support;

import java.io.*;
import java.util.*;

/**
 * Bucket implementation that can efficiently access any arbitrary byte-range
 * of a file.
 **/
public class RandomAccessFileBucket2 implements Bucket {
    RandomAccessFile raf = null;

    public RandomAccessFileBucket2(final File file, final long offset, final long len, boolean readOnly, final RandomAccessFile r)
        throws IOException {
        if (!(file.exists() && file.canRead())) {
            throw new IOException("Can't read file: " + file.getAbsolutePath());
        }

        if ((!file.canWrite()) && (!readOnly)) {
            throw new IOException("Can't write to file: " + file.getAbsolutePath());
        }

        this.file = file;
        this.readOnly = readOnly;
        this.raf = r;
        setRange(offset, len);
    }

    public synchronized void setRange(final long offset, long len) throws IOException {
        if (isReleased()) {
            throw new IOException("Attempt to use a released RandomAccessFileBucket: " + getName() );
        }

        if (streams.size() > 0) {
            throw new IllegalStateException("Can't reset range.  There are open streams.");
        }
        if ( (offset < 0) || (len < 0) )//|| (offset + len > file.length()))
        {
            throw new IllegalArgumentException("Bad range arguments.");
        }
        if (offset + len > file.length()) {
            len = file.length() - offset; // change to correct len silently
        }
        this.offset = offset;
        this.len = len;
        localOffset = 0;
    }

    public static class Range {
        Range(final long offset, final long len) {
            this.offset = offset;
            this.len = len;
        }

        public long offset;
        public long len;
    }

    public final synchronized Range getRange() {
        return new Range(offset, len);
    }

    // hmmm make protected???
    public final synchronized boolean hasOpenStreams() {
        return streams.size() > 0;
    }

    // Wrap non-const members so we can tell
    // when code touches the Bucket after it
    // has been released.
    public synchronized InputStream getInputStream() throws IOException {
        if (isReleased()) {
            throw new IOException("Attempt to use a released RandomAccessFileBucket: " + getName() );
        }

        final InputStream newIn = new RAInputStream(this, file.getAbsolutePath());
        streams.addElement(newIn);
        return newIn;
    }

    public synchronized OutputStream getOutputStream() throws IOException {
        if (isReleased()) {
            throw new IOException("Attempt to use a released RandomAccessBucket: " + getName() );
        }

        if (readOnly) {
            throw new IOException("Tried to write a read-only Bucket.");
        }

        final OutputStream newOut = new RAOutputStream(this, file.getAbsolutePath());
        streams.addElement(newOut);
        return newOut;
    }

    public String getName() {
        return file.getAbsolutePath() + " [" + offset + ", " +
            (offset + len - 1) + "]";
    }

    public synchronized void resetWrite() {
        if (isReleased()) {
            throw new RuntimeException("Attempt to use a released RandomAccessFileBucket: " + getName() );
        }
        // REDFLAG: implicit assumptions
        // 0) Bucket is only written to at a time.
        // 1) The output stream is closed before the
        //    next is open. Ouch. This may cause problems...
        localOffset = 0;
    }

    public long size() { return len; }

    public synchronized boolean release() {
        if (released) {
            return true;
        }

        // Force all open streams closed.
        // Windows won't let us delete the file unless we
        // do this.
        for (int i =0; i < streams.size(); i++) {
            try {
                if (streams.elementAt(i) instanceof InputStream) {
                    ((InputStream)streams.elementAt(i)).close();
                } else if (streams.elementAt(i) instanceof OutputStream) {
                    ((OutputStream)streams.elementAt(i)).close();
                }
            } catch (final IOException ioe) {
            }
        }
        streams.removeAllElements();
        streams.trimToSize();
        // We don't delete anything because we don't own anything.
        released = true;
        return true;
    }

    public synchronized final boolean isReleased() { return released; }

    @Override
    public void finalize() throws Throwable {
        if (!released) {
            release();
        }
    }

    // REDFLAG: RETEST
    // set blocks = -1 for until end.
    // last block may have length < blockSize
    public static Bucket[] segment(
            final File file,
            final int blockSize,
            final long offset,
            int blocks,
            boolean readOnly,
            final RandomAccessFile r)
    throws IOException
    {
        if (!(file.exists() && file.canRead())) {
            throw new IOException("Can't read file: " + file.getAbsolutePath());
        }

        if ((!file.canWrite()) && (!readOnly)) {
            throw new IOException("Can't write to file: " + file.getAbsolutePath());
        }

        if ((offset < 0) || (offset >= file.length() - 1)) {
            throw new IllegalArgumentException("offset: " + offset);
        }

        final long length = file.length() - offset;
        int nBlocks = (int) (length / blockSize);
        if ((length % blockSize) != 0) {
            nBlocks++;
        }

        if (blocks == -1) {
            blocks = nBlocks;
        }
        else if ((blocks > nBlocks) || (blocks < 1)) {
            throw new IllegalArgumentException("blocks: " + blocks+"; nBlocks: "+nBlocks+"; blockSize: "+blockSize+
                    "; offset: "+offset+"; fileLen: "+file.length()+"; length: "+length);
        }

        final Bucket[] ret = new Bucket[blocks];

        for (int i = 0; i < blocks; i++) {
            final long localOffset = i * blockSize + offset;
            int blockLen = blockSize;
            if (i == nBlocks - 1) {
                blockLen = (int) (length - (nBlocks - 1) * blockSize);
            }
            ret[i] = new RandomAccessFileBucket2(file, localOffset, blockLen, readOnly, r);
        }

        return ret;
    }

    ////////////////////////////////////////////////////////////
    // InputStream and OutputStream implementations
    //
//    private final static boolean vociferous = false;

    class RAInputStream extends InputStream  {
        public RAInputStream(final RandomAccessFileBucket2 rafb, final String prefix) throws IOException {
            this.rafb = rafb;
//            raf = new RandomAccessFile(rafb.file, "r");
//System.out.println("rafin, seek="+offset+"  len="+raf.length());
            raf.seek(offset);
//System.out.println("rafin,end");
//            println(" -- Created new InputStream [" + rafb.offset +
//                    ", " + (rafb.offset + rafb.len -1) + "]" );
        }

        ////////////////////////////////////////////////////////////
        // FilterInput implementation

        private final int bytesLeft() throws IOException {
            return (int)(rafb.offset + rafb.len - raf.getFilePointer());
        }

        @Override
        public int read() throws java.io.IOException {
            synchronized (rafb) {
//                println(".read()");
//                checkValid();
                if (bytesLeft() < 1) {
                    return -1; // EOF
                }
                return raf.read();
            }
        }

        @Override
        public int read(final byte[] bytes) throws java.io.IOException {
            synchronized (rafb) {
//                println(".read(byte[])");
//                checkValid();
                int nAvailable = bytesLeft();
                if (nAvailable < 1) {
                    return -1; // EOF
                }
                if (nAvailable > bytes.length) {
                    nAvailable = bytes.length;
                }
                return raf.read(bytes, 0, nAvailable);
            }
        }

        @Override
        public int read(final byte[] bytes, final int a, final int b) throws java.io.IOException {
            synchronized (rafb) {
//                println(".read(byte[], int, int)");
//                checkValid();
                int nAvailable = bytesLeft();
                if (nAvailable < 1) {
                    return -1; // EOF
                }
                if (nAvailable > b) {
                    nAvailable = b;
                }
                return raf.read(bytes, a, nAvailable);
            }
        }

        @Override
        public long skip(final long a) throws java.io.IOException {
            synchronized (rafb) {
//                println(".skip(long)");
//                checkValid();
                int nAvailable = bytesLeft();
                if (nAvailable < 1) {
                    return -1; // EOF
                }
                if (nAvailable > a) {
                    nAvailable = (int)a;
                }

                return raf.skipBytes(nAvailable);
            }
        }

        @Override
        public int available() throws java.io.IOException {
            synchronized (rafb) {
//                println(".available()");
//                checkValid();
                return bytesLeft();
            }
        }

        @Override
        public void close() throws java.io.IOException {
            synchronized (rafb) {
//                println(".close()");
//                checkValid();
//                raf.close();
                if (rafb.streams.contains(RAInputStream.this)) {
                    rafb.streams.removeElement(RAInputStream.this);
                }
                rafb.streams.trimToSize();
            }
        }

        // LATER: support if really needed.
        @Override
        public  void mark(final int a) {
            // NOP
        }

        @Override
        public void reset() throws java.io.IOException {
            // NOP
        }

        @Override
        public boolean markSupported() {
            return false;
        }

/*        private final void checkValid() throws IOException {
            if (rafb.released) {
                throw new IOException("Attempt to use a released RandomAccessFileBucket: " + prefix);
            }
        }*/

        ////////////////////////////////////////////////////////////
        private RandomAccessFileBucket2 rafb = null;
//        private RandomAccessFile raf = null;
//        private String prefix = "";
    }

    private class RAOutputStream extends OutputStream {
        public RAOutputStream(final RandomAccessFileBucket2 rafb, final String pref) throws IOException {
            this.rafb = rafb;
//            raf = new RandomAccessFile(rafb.file, "rw");
//System.out.println("rafout="+raf.toString()+"  seek="+(rafb.offset + rafb.localOffset));
            raf.seek(rafb.offset + rafb.localOffset);
//            println(" -- Created new OutputStream [" + rafb.offset + ", "
//                    + (rafb.offset + rafb.len -1) + "]" );
        }

        ////////////////////////////////////////////////////////////
        // OutputStream implementation
        @Override
        public void write(final int b) throws IOException {
            synchronized (rafb) {
//                println(".write(b)");
                checkValid();
                final int nAvailable = bytesLeft();
                if (nAvailable < 1) {
                    throw new IOException("Attempt to write past end of Bucket.");
                }
                raf.write(b);
            }
        }

        @Override
        public void write(final byte[] buf) throws IOException {
            synchronized (rafb) {
//                println(".write(buf)");
                checkValid();
                final int nAvailable = bytesLeft();
                if (nAvailable < buf.length) {
                    throw new IOException("Attempt to write past end of Bucket.");
                }
                raf.write(buf);
            }
        }

        @Override
        public void write(final byte[] buf, final int off, final int tlen) throws IOException {
            synchronized (rafb) {
//                println(".write(buf,off,len)");
                checkValid();
                final int nAvailable = bytesLeft();
                if (nAvailable < tlen) {
                    throw new IOException("Attempt to write past end of Bucket.");
                }
                raf.write(buf, off, tlen);
            }
        }

        @Override
        public void flush() throws IOException {
            synchronized (rafb) {
//                println(".flush()");
                checkValid();
                // NOP? Bytes written immediately?
                // REDFLAG: double check.
            }
        }

        @Override
        public void close() throws IOException {
            synchronized (rafb) {
//                println(".close()");
                checkValid();
                if (rafb.streams.contains(RAOutputStream.this)) {
                    rafb.streams.removeElement(RAOutputStream.this);
                }
                rafb.streams.trimToSize();
                final long added = raf.getFilePointer() - rafb.offset;
                if (added > 0) {
                    // To get proper append behavior.
                    rafb.localOffset = added;
                }

//                raf.close();
            }
        }

        ////////////////////////////////////////////////////////////
        private final void checkValid() throws IOException {
            if (rafb.isReleased()) {
                throw new IOException("Attempt to use a released RandomAccessFileBucket: " + prefix);
            }
        }
        private final int bytesLeft() throws IOException {
            return (int)(rafb.offset + rafb.len - raf.getFilePointer());
        }

        private RandomAccessFileBucket2 rafb = null;
//        private RandomAccessFile raf = null;
        private final String prefix = "";

    }
    ////////////////////////////////////////////////////////////

    private File file = null;
    private long offset = -1;
    private long localOffset = 0;
    private long len = -1;
    private boolean readOnly = false;
    private boolean released = false;
    private final Vector streams = new Vector();
}
