package de.ditz.primes;

import java.util.AbstractList;
import java.util.RandomAccess;

class Sequences extends AbstractList<ByteSequence> implements RandomAccess {

   public static final AbstractList<ByteSequence> ALL = new Sequences();

   public static final ByteSequence ROOT = ALL.get(255);

   public static ByteSequence sequence(int index) {
      return ALL.get(index);
   }

   private final ByteSequence[] sequences = new ByteSequence[256];

   @Override
   public int size() {
      return 256;
   }

   @Override
   public ByteSequence get(int index) {
      return sequences[index];
   }

   {
      sequences[0] = SingleSequence.EMPTY;

      // transfer 8 single track sequences
      SingleSequence.SINGLES.forEach(single -> sequences[single.factor()] = single);

      for(int m=2; m<256; ++m) {
         if(sequences[m]==null)
            sequences[m] = create(m);
      }
   }

   private CompactSequence create(int mask) {
      long sequence = 0;
      long prod = 1;

      // reversed
      for(int i=7; i>0; --i) {
         if(((mask>>i)&1) != 0) {
            int p = ByteSequence.FACTORS.get(i);
            sequence = (sequence<<8) + p;
            prod *= p;
         }
      }

      return new CompactSequence(mask, prod, sequence);
   }


   public static void main(String ... args) {
      ALL.forEach(System.out::println);
   }
}
