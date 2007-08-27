package freenet.crypt;
/*
  This code is part of the Java Adaptive Network Client by Ian Clarke. 
  It is distributed under the GNU Public Licence (GPL) version 2.  See
  http://www.gnu.org/ for further details of the GPL.
*/

public interface Digest {

    /**
     * retrieve the value of a hash, by filling the provided int[] with
     * n elements of the hash (where n is the bitlength of the hash/32)
     * @param digest int[] into which to place n elements
     * @param offset index of first of the n elements
     */
    public void extract(int [] digest, int offset);

     /**
     * Add one byte to the digest. When this is implemented
     * all of the abstract class methods end up calling
     * this method for types other than bytes.
     * @param b byte to add
     */
    public void update(byte b);

    /**
     * Add many bytes to the digest.
     * @param data byte data to add
     * @param offset start byte
     * @param length number of bytes to hash
     */
    public void update(byte[] data, int offset, int length);

    /**
     * Adds the entire contents of the byte array to the digest.
     */
    public void update(byte[] data);
     
    /**
     * Returns the completed digest, reinitializing the hash function.
     * @return the byte array result
     */
    public byte[] digest();

    /**
     * Return completed digest filled into the given buffer.
     * @return the byte array result
     * @param reset If true, the hash function is reinitialized
     */
    public void digest(boolean reset, byte[] buffer, int offset);

    /**
     * Return the hash size of this digest in bits
     */
    public int digestSize();
}




