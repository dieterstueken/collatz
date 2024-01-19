package de.ditz.primes;

import java.util.*;

public class Sequences {

   public static final List<? extends ByteSequence> SEQUENCES = sequences();

   public static final ByteSequence EMPTY = SEQUENCES.get(0);

   public static final ByteSequence ROOT = SEQUENCES.get(255);

   public static ByteSequence sequence(int index) {
      return SEQUENCES.get(index&0xff);
   }

   private static List<ByteSequence> sequences() {

      ByteSequence[] sequences = new ByteSequence[256];

      sequences[0] = SingleSequence.EMPTY;

      // transfer 8 single track sequences
      SingleSequence.SINGLES.forEach(single -> {
         sequences[single.factor()] = single;
      });

      // for() clip masks
      int[] clips = new int[30];

      for(int i=0, j=0; i<8; ++i) {
         int p = ByteSequence.FACTORS.get(i);
         int m = (2 << i) - 1;
         Arrays.fill(clips, j, i + 1, m);
      }

      for(int m=2; m<256; ++m) {

         if(sequences[m]!=null)
            continue;

         // index of last 1 bit (-1 if empty)
         int l = 31 - Integer.numberOfLeadingZeros(m);
         SingleSequence last = SingleSequence.SINGLES.get(l);

         long sequence = 0;
         long prod = 1;
         int size = Integer.bitCount(m);

         for(int i=7; i>0; --i) {
            if(((m>>i)&1) != 0) {
               int p = ByteSequence.FACTORS.get(i);
               sequence = (sequence<<8) + p;
               prod *= p;
            }
         }

         int mask = m;

         sequences[m] = new CompactSequence(sequence, prod) {
            @Override
            public int mask() {
               return mask;
            }

            @Override
            public int size() {
               return size;
            }

            @Override
            public int lastPrime() {
               return last.factor();
            }

            @Override
            public SingleSequence lastSequence() {
               return last;
            }

            @Override
            public ByteSequence from(long start) {
               if (start>0 && start < 30) {
                  int m = mask();
                  int k = m&clips[(int)start];
                  if(m!=k)
                     return sequence(k);
               }

               return this;
            }
         };
      }

      return List.of(sequences);
   }

   public static void main(String ... args) {
      SEQUENCES.forEach(System.out::println);
   }
}
