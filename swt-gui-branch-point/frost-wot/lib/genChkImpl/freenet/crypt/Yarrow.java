package freenet.crypt;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/*
  This code is part of the Java Adaptive Network Client by Ian Clarke.
  It is distributed under the GNU Public Licence (GPL) version 2.  See
  http://www.gnu.org/ for further details of the GPL.
*/

/**
 * An implementation of the Yarrow PRNG in Java.
 *
 * This class implements Yarrow-160, a cryptraphically secure PRNG
 * developed by John Kelsey, Bruce Schneier, and Neils Ferguson.  It was
 * designed to follow the specification (www.counterpane.com/labs) given
 * in the paper by the same authors, with the following exceptions:
 *
 * - Instead of 3DES as the output cipher, Rijndael was chosen.
 *   It was my belief that an AES candidate should be selected.  Twofish
 *   was an alternate choice, but the AES implementation does not
 *   allow easy selection of a faster key-schedule, so twofish's
 *   severely impaired performance.
 *
 * - h prime, described as a 'size adaptor' was not used, since
 *   its function is only to constrain the size of a byte array,
 *   our own key generation routine was used instead
 *   (see freenet.crypt.Util.makeKey)
 *
 * - Our own entropy estimation routines are used, as they
 *   use a third-order delta calculation that is quite conservative.
 *   Still, its used along side the global multiplier and program-
 *   supplied guesses, as suggested.
 *
 *
 * @author Scott G. Miller <scgmille@indiana.edu>
 */
public final class Yarrow extends RandomSource {

	private static Logger logger = Logger.getLogger(Yarrow.class.getName());

    /**
     * Security parameters
     */
    private static final int
	Pg = 10;

    private File seedfile;

    public Yarrow() {
	this("prng.seed","SHA1","Rijndael");
    }

    public Yarrow(String seed, String digest, String cipher) {
	this(new File(seed), digest, cipher);
    }

    public Yarrow(File seed, String digest, String cipher) {
        accumulator_init(digest);
        reseed_init(digest);
        seedfile=seed;
        generator_init(cipher);
        entropy_init(seed);
    }

    private void entropy_init(File seed) {
	Properties sys=System.getProperties();
	EntropySource startupEntropy=new EntropySource();

	// Consume the system properties list
	for (Enumeration enum=sys.propertyNames(); enum.hasMoreElements();) {
	    String key=(String)enum.nextElement();
	    consumeString(key);
	    consumeString(sys.getProperty(key));
	}

	// Consume the local IP address
	try {
	    consumeString(java.net.InetAddress.getLocalHost().toString());
	} catch (Exception e) {}

	// Consume the current time
	acceptEntropy(startupEntropy, System.currentTimeMillis(), 0);
	// Free memory
	acceptEntropy(startupEntropy, Runtime.getRuntime().freeMemory(), 0);
	// Total memory
	acceptEntropy(startupEntropy, Runtime.getRuntime().totalMemory(), 0);

	read_seed(seed);
    }

    /**
     * Seed handling
     */
    private void read_seed(File filename) {
	try {
	    DataInputStream dis=new DataInputStream(new FileInputStream(filename));
	    EntropySource seedFile=new EntropySource();
	    try {
		for (int i=0; i<32; i++)
		    acceptEntropy(seedFile, dis.readLong(), 64);
	    } catch (EOFException f) {}
	    dis.close();
	} catch (Exception e) {}
	fast_pool_reseed();
    }

    private void write_seed(File filename) {
	try {
	    DataOutputStream dos=new DataOutputStream(new FileOutputStream(filename));
	    for (int i=0; i<32; i++)
		dos.writeLong(nextLong());
	    dos.close();
	} catch (Exception e) {}
    }

    /**
     * 5.1 Generation Mechanism
     */
    private BlockCipher cipher_ctx;
    private byte[] output_buffer, counter, allZeroString, tmp;
    private int output_count, fetch_counter, block_bytes;

    private void generator_init(String cipher) {
	cipher_ctx=Util.getCipherByName(cipher);
	output_buffer=new byte[cipher_ctx.getBlockSize()/8];
	counter=new byte[cipher_ctx.getBlockSize()/8];
	allZeroString=new byte[cipher_ctx.getBlockSize()/8];
	tmp=new byte[cipher_ctx.getKeySize()/8];

	fetch_counter=output_buffer.length;
    }

    private final void counterInc() {
	for (int i=counter.length-1; i>=0; i--)
	    if (++counter[i] != 0)
		break;
    }

    private final void generateOutput() {
	counterInc();

	output_buffer = new byte[counter.length];
        cipher_ctx.encipher(counter,
                            output_buffer);

	if (output_count++ > Pg) {
	    output_count=0;
	    nextBytes(tmp);
	    rekey(tmp);
	}
    }

    private void rekey(byte[] key) {
	cipher_ctx.initialize(key);
	counter = new byte[allZeroString.length];
        cipher_ctx.encipher(allZeroString, counter);
	Util.wipe(key);
    }

    // Fetches count bytes of randomness into the shared buffer, returning
    // an offset to the bytes
    private synchronized int getBytes(int count) {

	if (fetch_counter+count > output_buffer.length) {
	    fetch_counter=0;
	    generateOutput();
	    return getBytes(count);
	}

	int rv=fetch_counter;
	fetch_counter+=count;
	return rv;
    }

    static final int bitTable[][]=
    { {0, 0x0},
      {1,  0x1}, {1,  0x3}, {1,  0x7}, {1,  0xf},
      {1, 0x1f}, {1, 0x3f}, {1, 0x7f}, {1, 0xff},

      {2,  0x1ff}, {2,  0x3ff}, {2,  0x7ff}, {2,  0xfff},
      {2, 0x1fff}, {2, 0x3fff}, {2, 0x7fff}, {2, 0xffff},

      {3,  0x1ffff}, {3,  0x3ffff}, {3,  0x7ffff}, {3,  0xfffff},
      {3, 0x1fffff}, {3, 0x3fffff}, {3, 0x7fffff}, {3, 0xffffff},

      {4,  0x1ffffff}, {4,  0x3ffffff}, {4,  0x7ffffff}, {4,  0xfffffff},
      {4, 0x1fffffff}, {4, 0x3fffffff}, {4, 0x7fffffff}, {4, 0xffffffff} };

    // This may *look* more complicated than in is, but in fact it is
    // loop unrolled, cache and operation optimized.
    // So don't try to simplify it... Thanks. :)
    // When this was not synchronized, we were getting repeats...
    protected synchronized int next(int bits) {
	int[] parameters=bitTable[bits];
	int offset=getBytes(parameters[0]);

	int val=output_buffer[offset];

	if (parameters[0] == 4)
	    val+=
		((int)output_buffer[offset+1]<<24) +
		((int)output_buffer[offset+2]<<16) +
		((int)output_buffer[offset+3]<< 8);
	else if (parameters[0] == 3)
	    val+=
		((int)output_buffer[offset+1]<<16) +
		((int)output_buffer[offset+2]<< 8);
	else if (parameters[0] == 2)
	    val+=(int)output_buffer[offset+2]<<8;

	return val & parameters[1];
    }

    /**
     * 5.2 Entropy Accumulator
     */
    private Digest fast_pool, slow_pool;
    private int fast_entropy, slow_entropy, digestSize;
    private boolean fast_select;
    private byte[] long_buffer=new byte[8];
    private Hashtable entropySeen;

    private void accumulator_init(String digest) {
	fast_pool=Util.getDigestByName(digest);
	slow_pool=Util.getDigestByName(digest);
	digestSize=fast_pool.digestSize();
	entropySeen=new Hashtable();
    }

    public int acceptEntropy(EntropySource source, long data, int entropyGuess) {
	return accept_entropy(data, source, Math.min(32, Math.min(estimateEntropy(source, data), entropyGuess)));
    }

    private synchronized int accept_entropy(long data, EntropySource source,
                                            int actualEntropy) {
	fast_select = !fast_select;
	Digest pool=(fast_select ? fast_pool : slow_pool);
        pool.update( (byte)  data );
        pool.update( (byte) (data >>  8));
        pool.update( (byte) (data >> 16));
        pool.update( (byte) (data >> 24));
        pool.update( (byte) (data >> 32));
        pool.update( (byte) (data >> 40));
        pool.update( (byte) (data >> 48));
        pool.update( (byte) (data >> 56));


	if (fast_select) {
	    fast_entropy+=actualEntropy;
	    if (fast_entropy > FAST_THRESHOLD)
		fast_pool_reseed();
	} else {
	    slow_entropy += actualEntropy;

	    if (source!=null) {
		Integer contributedEntropy=(Integer)entropySeen.get(source);
		if (contributedEntropy==null)
		    contributedEntropy=new Integer(actualEntropy);
		else
		    contributedEntropy=new Integer(actualEntropy+
						   contributedEntropy.intValue());
		entropySeen.put(source, contributedEntropy);

		if (slow_entropy >= (SLOW_THRESHOLD * 2)) {
		    int kc=0;
		    for (Enumeration enum=entropySeen.keys(); enum.hasMoreElements();) {
			Object key=enum.nextElement();
			Integer v=(Integer)entropySeen.get(key);
			
			logger.fine("Key: <" + key + "> "+ v);
			
			if (v.intValue() > SLOW_THRESHOLD )
			    {
				kc++;
				if (kc >= SLOW_K) {
				    slow_pool_reseed();
				    break;
				}
			    }
		    }
		}
	    }
	}
	logger.fine("Fast pool: " + fast_entropy + "\tSlow pool: " + slow_entropy);
	return actualEntropy;
    }

    private int estimateEntropy(EntropySource source, long newVal) {
	int delta=(int)(newVal-source.lastVal);
	int delta2=delta - source.lastDelta;
	source.lastDelta=delta;

	int delta3=delta2 - source.lastDelta2;
	source.lastDelta2=delta2;

	if (delta < 0)
	    delta = -delta;
	if (delta2 < 0)
	    delta2 = -delta2;
	if (delta3 < 0)
	    delta3 = -delta3;
	if (delta > delta2)
	    delta = delta2;
	if (delta > delta3)
	    delta = delta3;

	/*
	 * delta is now minimum absolute delta.
	 * Round down by 1 bit on general principles,
	 * and limit entropy entimate to 12 bits.
	 */
	delta >>= 1;
	delta &= (1 << 12) - 1;

	/* Smear msbit right to make an n-bit mask */
	delta |= delta >> 8;
	delta |= delta >> 4;
	delta |= delta >> 2;
	delta |= delta >> 1;
	/* Remove one bit to make this a logarithm */
	delta >>= 1;
	/* Count the bits set in the word */
	delta -= (delta >> 1) & 0x555;
	delta = (delta & 0x333) + ((delta >> 2) & 0x333);
	delta += (delta >> 4);
	delta += (delta >> 8);

	source.lastVal=newVal;

	return (int)delta & 15;
    }

    public int acceptTimerEntropy(EntropySource timer) {
	long now=System.currentTimeMillis();
	return acceptEntropy(timer, now-timer.lastVal, 32);
    }

    /**
     * If entropy estimation is supported, this method will block
     * until the specified number of bits of entropy are available.  If
     * estimation isn't supported, this method will return immediately.
     */
    public void waitForEntropy(int bits) {}

    /**
     * 5.3 Reseed mechanism
     */
    private static final int Pt = 5;
    private Digest reseed_ctx;

    private void reseed_init(String digest) {
	reseed_ctx=Util.getDigestByName(digest);
    }

    private void fast_pool_reseed() {
	byte[] v0=fast_pool.digest();
	byte[] vi=v0;

	for (byte i=0; i<Pt; i++) {
            reseed_ctx.update(vi, 0, vi.length);
            reseed_ctx.update(v0, 0, v0.length);
            reseed_ctx.update(i);
            vi=reseed_ctx.digest();
	}

	// vPt=vi
	Util.makeKey(vi, tmp, 0, tmp.length);
	rekey(tmp);
	Util.wipe(v0);
	fast_entropy=0;
	write_seed(seedfile);
    }

    private void slow_pool_reseed() {
	byte[] slow_hash=slow_pool.digest();
        fast_pool.update(slow_hash, 0, slow_hash.length);

        fast_pool_reseed();
	slow_entropy=0;

	Integer ZERO=new Integer(0);
	for (Enumeration enum=entropySeen.keys(); enum.hasMoreElements(); )
	    entropySeen.put(enum.nextElement(), ZERO);
    }

    /**
     * 5.4 Reseed Control parameters
     */
    private static final int
	FAST_THRESHOLD = 100,
	SLOW_THRESHOLD = 160,
	SLOW_K = 2;


    /**
     * If the RandomSource has any resources it wants to close, it can
     * do so when this method is called
     */
    public void close() {};

    /**
     * Test routine
     */
    public static void main(String[] args) throws Exception {
	Yarrow r=new Yarrow(new File("/dev/urandom"), "SHA1","Rijndael");

	byte[] b=new byte[1024];

	if (args.length==0 || args[0].equalsIgnoreCase("latency")) {
	    if (args.length==2) b=new byte[Integer.parseInt(args[1])];
	    long start=System.currentTimeMillis();
	    for (int i=0; i<100; i++)
		r.nextBytes(b);
	    System.out.println((double)(System.currentTimeMillis()-start)/
			       (100*b.length)*1024 + " ms/k");
	    start=System.currentTimeMillis();
	    for (int i=0; i<1000; i++)
		r.nextInt();
	    System.out.println((double)(System.currentTimeMillis()-start)/1000 +
			       " ms/int");
	    start=System.currentTimeMillis();
	    for (int i=0; i<1000; i++)
		r.nextLong();
	    System.out.println((double)(System.currentTimeMillis()-start)/1000 +
			       " ms/long");
	} else if (args[0].equalsIgnoreCase("randomness")) {
	    int kb=Integer.parseInt(args[1]);
	    for (int i=0; i<kb; i++) {
		r.nextBytes(b);
		System.out.write(b);
	    }
	} else if (args[0].equalsIgnoreCase("gathering")) {
	    System.gc();
	    EntropySource t=new EntropySource();
	    long start=System.currentTimeMillis();
	    for (int i=0; i<100000; i++)
		r.acceptEntropy(t,System.currentTimeMillis(),32);
	    System.err.println((double)(System.currentTimeMillis()-start)/100000);
	    System.gc();
	    start=System.currentTimeMillis();
	    for (int i=0; i<100000; i++)
		r.acceptTimerEntropy(t);
		System.err.println((double)(System.currentTimeMillis()-start)/100000);
	} else if (args[0].equalsIgnoreCase("volume")) {
	    b=new byte[1020];
	    long duration=System.currentTimeMillis()+Integer.parseInt(args[1]);
	    while (System.currentTimeMillis() < duration) {
		r.nextBytes(b);
		System.out.write(b);
	    }
	} else if (args[0].equals("stream")) {
	    RandFile f=new RandFile(args[1]);
	    EntropySource rf=new EntropySource();
	    byte[] buffer=new byte[131072];
	    while(true) {
		r.acceptEntropy(rf, f.nextLong(), 32);
		r.nextBytes(buffer);
		System.out.write(buffer);
	    }
	} else if (args[0].equalsIgnoreCase("bitstream")) {
	    while(true) {
		int v=r.nextInt();
		for (int i=0; i<32; i++) {
		    if ( ((v>>i) & 1) == 1)
			System.out.print('1');
		    else
			System.out.print('0');
		}
	    }
	} else if (args[0].equalsIgnoreCase("sample")) {
	    if (args.length==1 || args[1].equals("general")) {
		System.out.println("nextInt(): ");
		for (int i=0;i<3;i++) System.out.println(r.nextInt());
		System.out.println("nextLong(): ");
		for (int i=0;i<3;i++) System.out.println(r.nextLong());
		System.out.println("nextFloat(): ");
		for (int i=0;i<3;i++) System.out.println(r.nextFloat());
		System.out.println("nextDouble(): ");
		for (int i=0;i<3;i++) System.out.println(r.nextDouble());
		System.out.println("nextFullFloat(): ");
		for (int i=0;i<3;i++) System.out.println(r.nextFullFloat());
		System.out.println("nextFullDouble(): ");
		for (int i=0;i<3;i++) System.out.println(r.nextFullDouble());
	    } else if (args[1].equals("normalized")) {
		for (int i=0; i<20; i++)
		    System.out.println(r.nextDouble());
	    }
	}
    }

    private void consumeString(String str) {
	byte[] b=str.getBytes();
	consumeBytes(b);
    }

    private void consumeBytes(byte[] bytes) {
	if (fast_select) {
            fast_pool.update(bytes, 0, bytes.length);
	} else {
            slow_pool.update(bytes, 0, bytes.length);
        }
	fast_select=!fast_select;
    }
}

