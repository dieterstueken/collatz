package de.ditz.primes;


/*
 * Class CompactSequence contains a block of up to 8 numbers below 30=2*3*5
 * which are not a multiple of 2, 3 or 5, but including 1.
 *
 */
abstract public class ByteSequence extends RandomList<Integer> implements Sequence {

   public static final int ROOT = 255;

   public static long SIZE = 2*3*5;

   /**
    * @return the number of prime factors.
    */
   abstract public int size();

   abstract public int count(long limit);

   public <R> R process(long start, Target<? extends R> process, long offset) {
      return null;
   }

   public <R> R process(Target<? extends R> process, long offset) {
      return process(0, process, offset);
   }

   /**
    * Calculate the index of byte sequences for a given offset.
    * @param offset to seek.
    * @return the index of the byte sequence.
    */
   public static long indexOf(long offset) {
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

      if(l>28)
         return 8;

      // get position
      l = 4*((l+1)/2);

      // pick a digit
      l = (int)(0x8777665443221110L >> l) & 0xf;

      return l;
   }

   public static void main(String ... args) {

      for(int i=-1; i<33; ++i) {
         System.out.format("%2d: %d\n", i, pcount(i));
      }
   }
}
