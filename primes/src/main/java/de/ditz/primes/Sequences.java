package de.ditz.primes;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;

class Sequences extends AbstractList<ByteSequence> implements RandomAccess {

   public static final Sequences ALL = new Sequences();

   public static ByteSequence sequence(int index) {
      return ALL.get(index);
   }
   
   final ByteSequence empty = new SingleSequence.EmptySequence();

   final List<? extends SingleSequence> singles = new SingleSequence.Singles();

   private final ByteSequence[] sequences = new ByteSequence[256];
    
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
   public ByteSequence get(int index) {
      return sequences[index];
   }


   private CompactSequence create(int mask) {
      long sequence = 0;
      long prod = 1;

      // reversed
      for(int i=7; i>=0; --i) {
         if(((mask>>i)&1) != 0) {
            int p = ByteSequence.FACTORS.get(i);
            sequence = (sequence<<8) + p;
            prod *= p;
         }
      }

      return new CompactSequence(mask, prod, sequence) {

         @Override
         public ByteSequence expunge(long factor) {
            int m = ByteSequence.expunge(mask, factor);
            return m==mask ? this : sequences[m];
         }

         @Override
         public ByteSequence from(long start) {

            if(start<=7) {
               if(mask>1 && mask%2!=0) // drop 1
                  return Sequences.sequence(mask&0xfe);
            } else {

               if (start > 28)
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
