package freenet.crypt;

import freenet.crypt.ciphers.*;
import freenet.support.Loader;
import freenet.support.Bucket;
import java.math.BigInteger;
import java.io.*;
import java.util.Random;

/*
  This code is part of the Java Adaptive Network Client by Ian Clarke. 
  It is distributed under the GNU Public Licence (GPL) version 2.  See
  http://www.gnu.org/ for further details of the GPL.
*/
public class Util {

    // bah, i'm tired of chasing down dynamically loaded classes..
    // this is for getCipherByName() and getDigestByName()
    static {
        SHA1.class.toString();
	JavaSHA1.class.toString();
        Twofish.class.toString();
        Rijndael.class.toString();
    }
    
    //public static char[] HEXCHARSET="0123456789abcdef".toCharArray();
    protected static final int BUFFER_SIZE=32768;
    
/*
    static String quantityToHexString(long n, int quartets) {
        StringBuffer b=new StringBuffer();
        for (int i=0; i<quartets; i++) {
            b.append(HEXCHARSET[(int)(n & 0xf)]);
            n=n>>4;
        }
        b.reverse();
        return b.toString();
    }
    
    public static String intToHexString(int n) {
        return quantityToHexString(n, 8);
    }

    public static String longToHexString(long n) {
        return quantityToHexString(n, 16);
    }

    public static String byteToHexString(byte n) {
        return quantityToHexString(n, 2);
    }
*/
    public static byte[] stringToBytes(String s) throws NumberFormatException {
        char[] c=s.toCharArray();
        byte[] bytes=new byte[c.length*2];
        for (int i=0; i<c.length; i++) {
            bytes[i*2]=(byte)((c[i]>>8) & 0xff);
            bytes[i*2 + 1]=(byte)c[i];
        }
        return bytes;
    }

    public static byte[] hexToBytes(String s) throws NumberFormatException {
        if ((s.length() % 2) != 0) {
            s="0"+s;
        }
            
        byte[] out = new byte[s.length() / 2];
        byte b;
        for (int i=0; i < s.length(); i++) {
            char c = Character.toLowerCase(s.charAt(i));
            if (!((c >= 'a' && c <= 'f') || (c >= '0' && c <='9')))
                throw new NumberFormatException();
            b = (byte) (c >= 'a' && c <='f' ? c - 'a' + 10 : c - '0');
            if (i%2 == 0) {
                out[i/2] = (byte) (b << 4);
            } else {
                out[(i-1)/2] = (byte) (out[(i-1)/2] | b);
            }
        }
        return out;
    }

    /*
    public static String bytesToHex(byte[] bs) {

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bs.length; i++) {
            char c1, c2;
                
            c1 = (char) ((bs[i] >>> 4) & 0xf);
            c2 = (char) (bs[i] & 0xf);
            c1 = (char) ((c1 > 9) ? 'A' + (c1 - 10) : '0' + c1);
            c2 = (char) ((c2 > 9) ? 'A' + (c2 - 10) : '0' + c2);
            sb.append(c1);
            sb.append(c2);
        }
        return sb.toString();
    }
    */
    public static void fillByteArrayFromInts(int[] ints, byte[] bytes) {
        int ic=0;
        for (int i=0; i<ints.length; i++) {
            bytes[ic++]=(byte)(ints[i]>>24);
            bytes[ic++]=(byte)(ints[i]>>16);
            bytes[ic++]=(byte)(ints[i]>>8);
            bytes[ic++]=(byte)ints[i];
        }
    }

    public static void fillByteArrayFromLongs(long[] ints, byte[] bytes) {
        int ic=0;
        for (int i=0; i<ints.length; i++) {
            bytes[ic++]=(byte)(ints[i]>>56);
            bytes[ic++]=(byte)(ints[i]>>48);
            bytes[ic++]=(byte)(ints[i]>>40);
            bytes[ic++]=(byte)(ints[i]>>32);
            bytes[ic++]=(byte)(ints[i]>>24);
            bytes[ic++]=(byte)(ints[i]>>16);
            bytes[ic++]=(byte)(ints[i]>>8);
            bytes[ic++]=(byte)ints[i];
        }
    }
    
    public static void fillIntArrayFromBytes(byte[] bytes, int[] ints) {
        int ic=0;
        for (int i=0; i<(ints.length<<2); i+=4) {
            ints[ic++]= bytes[i] + ((int)bytes[i+1])<<8 +
                ((int)bytes[i+2])<<16 + ((int)bytes[i+3])<<24;
        }
    }

    public static void fillLongArrayFromBytes(byte[] bytes, long[] longs) {
        int ic=0;
        for (int i=0; i<(longs.length<<3); i+=8) {
            longs[ic++]= 
                (long)bytes[i] + ((long)bytes[i+1]<<8) +
                ((long)bytes[i+2]<<16) + ((long)bytes[i+3]<<24) +
                ((long)bytes[i+4]<<32) + ((long)bytes[i+5]<<40) +
                ((long)bytes[i+6]<<48) + ((long)bytes[i+7]<<56);
        }
    }

    public static boolean byteArrayEqual(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        return byteArrayEqual(a, b, 0, a.length);
    }

    public static boolean byteArrayEqual(byte[] a, byte[] b, int offset, int length) {
        int lim = offset + length;
        if (a.length < lim || b.length < lim)
            return false;
        for (int i=offset; i<lim; ++i) 
            if (a[i] != b[i]) return false;
        return true;
    }

    // Crypto utility methods:

    public static final BigInteger
        ONE  = BigInteger.valueOf(1),
        ZERO = BigInteger.valueOf(0),
        TWO  = BigInteger.valueOf(2);


    // we should really try reading the JFC documentation sometime..
    // - the byte array generated by BigInteger.toByteArray() is
    //   compatible with the BigInteger(byte[]) constructor
    // - the byte length is ceil((bitLength()+1) / 8)

    public static byte[] MPIbytes(BigInteger num) {
        int len = num.bitLength();
        byte[] bytes = new byte[ 2 + ((len + 8) >> 3) ];
        System.arraycopy(num.toByteArray(), 0, bytes, 2, bytes.length-2);
        bytes[0] = (byte) (len >> 8);
        bytes[1] = (byte) len;
        return bytes;
    }

    public static void writeMPI(BigInteger num, OutputStream out) throws IOException {
        out.write(MPIbytes(num));
    }

    public static BigInteger readMPI(InputStream in) throws IOException {
        int b1 = in.read();
        int b2 = in.read();
        if (b1 == -1 || b2 == -1) throw new EOFException();
        byte[] data = new byte[ (((b1 << 8) + b2) + 8) >> 3 ];
        readFully(in, data, 0, data.length);
        //(new DataInputStream(in)).readFully(data, 0, data.length);
        return new BigInteger(data);
    }

    /**
     * Creates a large random number (BigInteger) up to <b>bits</b> bits.
     * This differs from the BigInteger constructor, in that it generates 
     * all numbers from the range 2^lower to 2^n, rather than 2^n-1 to 2^n.
     */
    public static BigInteger generateLargeRandom(int lowerBound, 
                                                 int upperBound,
                                                 Random r) {
        if (lowerBound == upperBound) return new BigInteger(lowerBound, r);

        int bl;
        do {
            bl=(r.nextInt() & 0x7fffffff) % upperBound;
        } while (bl < lowerBound);
        BigInteger b=new BigInteger(bl, r);
        return b;
    }

    /**
     * Returns the unsigned BigInteger representation of a byte[]
     * @deprecated -- redundant with BigInteger constructor
     */
    public static BigInteger byteArrayToMPI(byte[] num) {
/*
        byte[] var;
        if (num[0] < 0) {
            var=new byte[num.length+1];
            System.arraycopy(num, 0, var, 1, num.length);
        } else
            var=num;
        return new BigInteger(var);
*/
        return new BigInteger(1, num);
    }
    
    public static byte[] hashBytes(Digest d, byte[] b) {
        return hashBytes(d, b, 0, b.length);
    }

    public static byte[] hashBytes(Digest d, byte[] b, int offset, int length) {
        d.update(b, offset, length);
        return d.digest();
    }
    
    /**
     * Hashes a string in a consistent manner
     */
    public static byte[] hashString(Digest d, String s) {
        try {
            byte[] sbytes=s.getBytes("UTF8");
            d.update(sbytes, 0, sbytes.length);
            return d.digest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Hashes an entire file.
     */
    public static byte[] hashFile(Digest d, File f) throws IOException, FileNotFoundException {
        byte[] buffer=new byte[65536];
        FileInputStream in=new FileInputStream(f);

        int rc=0;
        do {
            rc=in.read(buffer);
            if (rc>0)
                d.update(buffer, 0, rc);
        } while (rc!=-1);
        return d.digest();
    }

    /**
     * Hashes len bytes from an InputStream.
     */
    public static byte[] hashStream(Digest d, InputStream in, long len) throws IOException {
        byte[] buffer=new byte[65536];
        int rc=0;
        do {
            int nBytes = (len > 65536) ? 65536 : (int)len;
            rc=in.read(buffer, 0, nBytes);
            if (rc>0)
                d.update(buffer, 0, rc);
            len -= rc;
        } while ((rc!=-1) && (len > 0));
        return d.digest();
    }

    public static byte[] xor(byte[] b1, byte[] b2) {
	int minl=Math.min(b1.length, b2.length);
	int maxl=Math.max(b1.length, b2.length);

	byte[] rv=new byte[maxl];

	for (int i=0; i<minl; i++) 
	    rv[i]=(byte)(b1[i]^b2[i]);
	return rv;
    }

    private static Digest ctx=SHA1.getInstance();

    public static void makeKey(byte[] entropy, byte[] key, int offset, int len) {
        synchronized(ctx) {
            ctx.digest(); // reinitialize
	    
            int ic=0;
            while (len>0) {
                ic++;
                for (int i=0; i<ic; i++)
                    ctx.update((byte)0);
                ctx.update(entropy, 0, entropy.length);
                int bc;
                if (len>20) {
                    ctx.digest(true, key, offset);
                    bc=20;
                } else {
                    byte[] hash=ctx.digest();
                    bc=Math.min(len, hash.length);
                    System.arraycopy(hash, 0, key, offset, bc);
                }
                offset+=bc;
                len-=bc;
            }
        }
        wipe(entropy);
    }

    public static void makeKey(Bucket entropy, byte[] key, int offset, int len)         throws IOException {

        byte[] buffer = new byte[BUFFER_SIZE];
        synchronized(ctx) {
            ctx.digest();
            int ic=0;
            InputStream in;

            while (len>0) {
                ic++;
                for (int i=0; i<ic; i++)
                    ctx.update((byte)0);                

                in = entropy.getInputStream();
                for (int i = 0 ; (i = in.read(buffer)) > 0 ;)
                    ctx.update(buffer, 0, buffer.length);
                in.close();

                int bc;
                if (len > ctx.digestSize() >> 3) {
                    ctx.digest(true, key, offset);
                    bc = ctx.digestSize() >> 3;
                } else {
                    byte[] hash=ctx.digest();
                    bc=Math.min(len, hash.length);
                    System.arraycopy(hash, 0, key, offset, bc);
                }
                offset+=bc;
                len-=bc;
            }
        }
        wipe(buffer);
    }

    public static BlockCipher getCipherByName(String name) {
                            //throws UnsupportedCipherException {
        try {
            return (BlockCipher) Loader.getInstance("freenet.crypt.ciphers."+name);
        } catch (Exception e) {
            //throw new UnsupportedCipherException(""+e);
            e.printStackTrace();
            return null;
        }
    }

    public static BlockCipher getCipherByName(String name, int keySize) {
                            //throws UnsupportedCipherException {
        try {
            return (BlockCipher) Loader.getInstance("freenet.crypt.ciphers."+name,
                                                    new Class[] { Integer.class },
                                                    new Object[] { new Integer(keySize) });
        } catch (Exception e) {
            //throw new UnsupportedCipherException(""+e);
            e.printStackTrace();
            return null;
        }
    }

    public static Digest getDigestByName(String name) {
                            //throws UnsupportedDigestException {
        try {
            return (Digest) Loader.getInstance("freenet.crypt."+name);
        } catch (Exception e) {
            //throw new UnsupportedDigestException(""+e);
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length==0 || args[0].equals("write")) {
            writeMPI(new BigInteger("9"), System.out);
            writeMPI(new BigInteger("1234567890123456789"), System.out);
            writeMPI(new BigInteger("100200300400500600700800900"), 
                     System.out);
        } else if (args[0].equals("read")) {
            System.out.println("9");
            System.out.println(readMPI(System.in));
            System.out.println("1234567890123456789");
            System.out.println(readMPI(System.in));
            System.out.println("100200300400500600700800900");
            System.out.println(readMPI(System.in));
        } else if (args[0].equals("write-mpi")) {
            writeMPI(new BigInteger(args[1]), System.out);
        } else if (args[0].equals("read-mpi")) {
            System.err.println(readMPI(System.in));
        } else if (args[0].equals("keygen")) {
            byte[] entropy=readMPI(System.in).toByteArray();
            byte[] key=new byte[ (args.length>1 ? 
                                  Integer.parseInt(args[1]) :
                                  16) ];
            makeKey(entropy, key, 0, key.length);
            System.err.println(freenet.support.Fields.bytesToHex(key,0,key.length));
        } else if (args[0].equals("shatest")) {
            synchronized(ctx) {
                ctx.digest();
                ctx.update ((byte)'a');
                ctx.update ((byte)'b');
                ctx.update ((byte)'c');
                byte[] hash = ctx.digest ();
                System.err.println (freenet.support.Fields.bytesToHex (hash,0,hash.length));
            }
        }
    }

    public static byte[] ZERO_ARRAY=new byte[16384];

    public static void wipe(byte[] data) {
        System.arraycopy(ZERO_ARRAY,0,data,0,data.length);
    }

    /** @return log2 of n, rounded up to the nearest integer
      */
    public static int log2(long n) {
        int log2 = 0;
        while (log2 < 63 && 1<<log2 < n) ++log2;
        return log2;
    }

    /** Writes a "rolling-hash pad" of paddingLen bytes to the given
      * output stream, using the provided, _initialized_ Digest.
      *
      * H1   = the hash value of the digest as provided
      * H2   = the hash value of the digest after updating w/ the bytes of H1
      * Hn+1 = the hash value of the digest after updating w/ the bytes of Hn
      *
      * Padding = H1,H1,H2,H1,H2,H3 etc. until enough bytes are generated.
      *
      * @param out         an output stream to write the padding to
      * @param paddingLen  the number of padding bytes to generate and write
      * @param ctx         an SHA1 initialized with the bytes of the data
      *                    that is being padded. Need an SHA1 because it
      *                    MUST support digest(false,...).
      */
    public static void rollingHashPad(OutputStream out, long paddingLen, 
				      SHA1 ctx) throws IOException {
	
        byte[] hashbuf = new byte[ctx.digestSize() >> 3];
        ByteArrayOutputStream pad = new ByteArrayOutputStream();

        while (paddingLen > 0) {
            ctx.digest(false, hashbuf, 0);
            ctx.update(hashbuf, 0, hashbuf.length);
            pad.write(hashbuf, 0, hashbuf.length);
            if (paddingLen < pad.size()) {
                byte[] tmp = pad.toByteArray();
                out.write(tmp, 0, (int) paddingLen);
                paddingLen = 0;
            }
            else {
                pad.writeTo(out);
                paddingLen -= pad.size();
            }
        }
    }

    public static void readFully(InputStream in, byte[] b) throws IOException {
        readFully(in, b, 0, b.length);
    }

    public static void readFully(InputStream in, byte[] b, int off,
                                 int length) throws IOException {
        int total = 0;
	while (total < length) {
            int got = in.read(b, off + total, length - total);
            if (got == -1) {
                throw new EOFException();
            }
            total += got;
	}
    }
}






