package de.ditz.primes;


import java.util.*;
import java.util.function.LongFunction;

/*
 * Class CompactSequence contains a block of up to 8 numbers below 30=2*3*5
 * which are not a multiple of 2, 3 or 5, but including 1.
 *
 */
abstract public class ByteSequence extends AbstractList<Integer> implements RandomAccess, Sequence {

   public static int SIZE = 2*3*5;

   public static final List<Integer> FACTORS = List.of(1,7,11,13,17,19,23,29);

   public static final long PROD = 7L * 11 * 13 * 17 * 19 * 23 * 29;

   final long prod;

   protected ByteSequence(long prod) {
      this.prod = prod;
   }

   /**
    * @return a bit mask representing containing primes
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
      throw new IndexOutOfBoundsException();
   }

   /**
    * @return the factor at index as plain int.
    */
   public int getPrime(int index) {
      return get(index);
   }

   abstract public int lastPrime();

   /**
    * @return a byte sequence representing the biggest factor or EMPTY.
    */
   abstract public SingleSequence lastSequence();

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

   @Override
   abstract public <R> R process(long start, LongFunction<? extends R> process, long offset);

   @Override
   public <R> R process(LongFunction<? extends R> process, long offset) {
      return process(0, process, offset);
   }

   /**
    * Calculate the index of byte sequences for a given offset.
    * @param offset to seek.
    * @return the index of the byte sequence.
    */
   public static long count(long offset) {
      return offset<0 ? 0 : offset/ SIZE;
   }

   /**
    * Drop a given factor [0,30] from a mask of factors.
    * @param mask of the compact sequence.
    * @param factor to drop.
    * @return reduced prime factor mask.
    */
   public static int expunge(int mask, int factor) {

      if(factor>0 && PROD%factor==0) {
         // get # of prime by interpolation.
         int n = (5*factor-3)/48;
         mask &= 0xff & (1<<n);
      } else if(factor==1) {
         mask &= 0xfe;
      }

      return mask;
   }
}
