package de.ditz.primes;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;

class Sequences extends AbstractList<CompactSequence> implements RandomAccess {

   public static final Sequences ALL = new Sequences();

   public static CompactSequence sequence(int index) {
      return ALL.get(index);
   }

   public static CompactSequence single(int index) {
      return ALL.singles.get(index);
   }

   public static ByteSequence root() {
      return sequence(ByteSequence.ROOT);
   }

   /**
    * Sequence of real prime numbers < 30
    */
   public static final ByteSequence PRIMES = new ByteSequence() {

      final Integer[] factors = {2,3,5,7,13,17,19,23,29};

      @Override
      public int size() {
         return factors.length;
      }

      @Override
      public Integer get(int index) {
         return factors[index];
      }

      @Override
      public <R> R process(long start, Target<? extends R> target) {

         if (start <= 5) {
            // try 1,2,5
            for (int i = 0; i < 3; ++i) {
               int p = factors[i];
               if (p >= start) {
                  R result = target.apply(p);
                  if (result != null)
                     return result;
               }
            }
         }

         // delegate to root sequence for p>5.
         return root().process(start, target);
      }
   };

   final CompactSequence empty = new SingleSequence.EmptySequence();

   final List<? extends SingleSequence> singles = new SingleSequence.Singles();

   private final CompactSequence[] sequences = new CompactSequence[256];
    
   {
      sequences[0] = empty;

      // transfer 8 single track sequences
      singles.forEach(single -> sequences[single.mask()] = single);

      for(int m=2; m<256; ++m) {
         if(sequences[m]==null)
            sequences[m] = create(m);
      }
   }
   
    @Override
   public int size() {
      return 256;
   }

   @Override
   public CompactSequence get(int index) {
      return sequences[index];
   }


   private CompactSequence create(int mask) {
      long sequence = 0;

      // reversed
      for(int i=7; i>=0; --i) {
         if(((mask>>i)&1) != 0) {
            int p = CompactSequence.FACTORS.get(i);
            sequence = (sequence<<8) + p;
         }
      }

      return new CompactSequence(mask, sequence) {

         final int size = Integer.bitCount(mask);

         @Override
         public ByteSequence expunge(long factor) {
            int m = CompactSequence.expunge(mask, factor);
            return m==mask ? this : sequences[m];
         }

         @Override
         public int size() {
            return size;
         }

         @Override
         public ByteSequence from(long start) {

            if(start<=7) {
               if(start>1 && mask%2!=0) // drop 1
                  return Sequences.sequence(mask&0xfe);
            } else {

               if (start > 29)
                  return empty;

               int m = (int) start;

               // count of factors until start
               m = ByteSequence.pcount(m-1);

               // mask of factors to drop
               m = (1<<m) - 1;

               // invert
               m ^= 0xff;

               // remaining mask
               m &= mask;

               if (m != mask)
                  return sequences[m];
            }

            return this;
         }
      };
   }

   public static void main(String ... args) {
      ALL.forEach(System.out::println);
   }

}
