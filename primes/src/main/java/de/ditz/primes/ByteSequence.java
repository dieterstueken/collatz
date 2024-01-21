package de.ditz.primes;


import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;
import java.util.function.LongFunction;

/*
 * Class CompactSequence contains a block of up to 8 numbers below 30=2*3*5
 * which are not a multiple of 2, 3 or 5, but including 1.
 *
 */
abstract public class ByteSequence extends AbstractList<Integer> implements RandomAccess, Sequence {

   public static long SIZE = 2*3*5;

   public static final List<Integer> FACTORS = List.of(1,7,11,13,17,19,23,29);

   public static final long PROD = 7L * 11 * 13 * 17 * 19 * 23 * 29;

   final long prod;

   String asString;

   protected ByteSequence(long prod) {
      this.prod = prod;
   }

   @Override
   public String toString() {
      if(asString==null)
         asString = asString();
      return asString;
   }

   /**
    * @return a bit mask representing containing factors
    */
   abstract public int mask();

   public byte getByte() {
      return (byte) mask();
   }

   /**
    * @return the number of prime factors.
    */
   abstract public int size();

   /**
    * @return the factor at index
    */
   public Integer get(int index) {
      return factor(index);
   }

   /**
    * @return the factor at index as plain int.
    */
   public int factor(int index) {
      throw new IndexOutOfBoundsException();
   }

   /**
    * Return a truncated ByteSequence omitting all factors < start.
    * @param start limit of first prime.
    * @return a truncated ByteSequence.
    */
   abstract public ByteSequence from(long start);

   /**
    * Return a sequence without the given factor.
    * Return this sequence again if the sequence does not contain the given factor
    * or the factor is not a prime at all.
    *
    * @param factor to expunge.
    * @return a sequence without the given factor, or this.
    */
   abstract public ByteSequence expunge(long factor);

   abstract public <R> R process(long start, LongFunction<? extends R> process, long offset);

   public <R> R process(LongFunction<? extends R> process, long offset) {
      return process(0, process, offset);
   }

   /**
    * Calculate the index of byte sequences for a given offset.
    * @param offset to seek.
    * @return the index of the byte sequence.
    */
   public static long count(long offset) {
      return offset<0 ? 0 : offset/SIZE;
   }

   /**
    * Calculate the # of (prime) factors <= l.
    * @param l limit of factors to count .
    * @return # of factors <= l.
    */
   public static int pcount(int l) {
      if(l<7)
         return l<1 ? 0 : 1;

      if(l>23)
         return 8;

      // get position
      l = 4*((l+1)/2);

      // pick a digit
      l = (int)(0x8777665443221110L >> l) & 0xf;

      return l;
   }

   /**
    * Drop a given factor [0,30] from a mask of factors.
    * @param mask of the compact sequence.
    * @param factor to drop.
    * @return reduced prime factor mask.
    */
   public static int expunge(int mask, long factor) {

      if(factor<7) {
         if(factor>0 && mask%2!=0) // drop 1
            mask &= 0xfe;
      } else if(factor<30) {
         // bit # to drop
         int m = ByteSequence.pcount((int)factor) - 1;

         // as bitmask
         m = 1 << m;

         // drop
         mask &= ~m;
      }

      return mask;
   }

   protected String asString() {
      StringBuilder sb = new StringBuilder();
      sb.append("%02x".formatted(mask()));
      char c='[';
      for(int i=0; i<size(); ++i) {
           sb.append(c).append(get(size()-i-1));
           c = '*';
      }
      sb.append(']');
      return sb.toString();
   }
   
   public static void main(String ... args) {
      
      for(int i=-1; i<33; ++i) {
         System.out.format("%2d: %d\n", i, pcount(i));
      }
   }
}
