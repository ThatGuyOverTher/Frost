package freenet.crypt;

import java.math.BigInteger;
import java.util.Random;
import java.io.*;

/**
 * Implements the Digital Signature Algorithm (DSA) described in FIPS-186
 */
public class DSA {

    /**
     * Returns a DSA signature given a group, private key (x), a random nonce
     * (k), and the hash of the message (m).
     */
    public static DSASignature sign(DSAGroup g,
				    DSAPrivateKey x,
				    BigInteger k, 
				    BigInteger m) {
		BigInteger r=g.getG().modPow(k, g.getP()).mod(g.getQ());
		
		BigInteger kInv=k.modInverse(g.getQ());
		return sign(g, x, r, kInv, m);
    } 
	
    public static DSASignature sign(DSAGroup g, DSAPrivateKey x, BigInteger m,
				    Random r) {
	BigInteger k;
	do {
	    k=new BigInteger(160, r);
	} while (k.compareTo(g.getQ())>-1 || k.compareTo(Util.ZERO)==0);
	return sign(g, x, k, m);
    }

    /**
     * Precalculates a number of r, kInv pairs given a random source
     */
    public static BigInteger[][] signaturePrecalculate(DSAGroup g,
						       int count, Random r) {
		BigInteger[][] result=new BigInteger[count][2];
		
		for (int i=0; i<count; i++) {
			BigInteger k;
			do {
				k=new BigInteger(160, r);
			} while (k.compareTo(g.getQ())>-1 || k.compareTo(Util.ZERO)==0);
			
			result[i][0] = g.getG().modPow(k, g.getP()); // r 
			result[i][1] = k.modInverse(g.getQ()); // k^-1 
		}
		return result;
    }

    /**
     * Returns a DSA signature given a group, private key (x), 
     * the precalculated values of r and k^-1, and the hash
     * of the message (m)
     */
    public static DSASignature sign(DSAGroup g, DSAPrivateKey x,
				    BigInteger r, BigInteger kInv, 
				    BigInteger m) {
	BigInteger s1=m.add(x.getX().multiply(r)).mod(g.getQ());
	BigInteger s=kInv.multiply(s1).mod(g.getQ());
	return new DSASignature(r,s);
    }

    /**
     * Verifies the message authenticity given a group, the public key
     * (y), a signature, and the hash of the message (m).
     */
    public static boolean verify(DSAPublicKey kp,
				 DSASignature sig,
				 BigInteger m) {
	BigInteger w=sig.getS().modInverse(kp.getQ());
	BigInteger u1=m.multiply(w).mod(kp.getQ());
	BigInteger u2=sig.getR().multiply(w).mod(kp.getQ());
	BigInteger v1=kp.getG().modPow(u1, kp.getP());
	BigInteger v2=kp.getY().modPow(u2, kp.getP());
	BigInteger v=v1.multiply(v2).mod(kp.getP()).mod(kp.getQ());
	return v.equals(sig.getR());
    }

    public static void main(String[] args) throws Exception {
	DSAGroup g=DSAGroup.readFromField(args[0]);
        Yarrow y=new Yarrow();
	DSAPrivateKey pk=new DSAPrivateKey(g, y);
	DSAPublicKey pub=new DSAPublicKey(g, pk);
	DSASignature sig=sign(g, pk, Util.ZERO, y);
	System.err.println(verify(pub, sig, Util.ZERO));
    }
}

	



