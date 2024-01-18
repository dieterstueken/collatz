package de.ditz.primes;

import java.util.*;
import java.util.function.LongFunction;

public class Sequences {

   public static final List<CompactSequence> SEQUENCES;

   public static final CompactSequence EMPTY;

   public static final CompactSequence ROOT;

   public static CompactSequence sequence(int index) {
      return SEQUENCES.get(index&0xff);
   }

   static {

      int[] BASE = {1,7,11,13,17,19,23,29};

      EMPTY = new CompactSequence(0, 0) {

         @Override
         public int get(int index) {
            throw new IndexOutOfBoundsException();
         }

         @Override
         public int size() {
            return 0;
         }

         @Override
         public <R> R process(long start, LongFunction<? extends R> process, long offset) {
            return null;
         }

         @Override
         public <R> R process(LongFunction<? extends R> process, long offset) {
            return null;
         }

         @Override
         public CompactSequence drop(int factor) {
            return this;
         }

         @Override
         public CompactSequence from(long start) {
            return this;
         }
      };

      // setup sequences.
      CompactSequence[] sequences = new CompactSequence[256];

      int[] from = new int[30];

      sequences[0] = EMPTY;

      // 8 single track sequences
      for(int k=1, i=1; i<BASE.length; ++i) {
         int p = BASE[i];
         long sequence = 1<<i;
         if(i>0)
            sequence |= p<<8;

         CompactSequence value = new CompactSequence(sequence, p) {

            @Override
            public int get(int index) {
               if(index==0)
                  return p;

               throw new IndexOutOfBoundsException();
            }

            @Override
            public int size() {
               return 1;
            }

            @Override
            public <R> R process(long start, LongFunction<? extends R> process, long offset) {
               return start>p ? null : process.apply(p+offset);
            }

            @Override
            public <R> R process(LongFunction<? extends R> process, long offset) {
               return process.apply(p+offset);
            }

            @Override
            public CompactSequence drop(int factor) {
               return factor==p ? EMPTY : this;
            }

            @Override
            public CompactSequence from(long start) {
               return p<start ? EMPTY : this;
            }
         };

         sequences[1<<i] = value;

         Arrays.fill(from, k, p, (1<<i)-1);
         k = p;
      }

      from[29] = 0xff;

      // generate 256 remaining sequences
      for(int m=1; m<256; ++m) {
         if(sequences[m]!=null)
            continue;

         long sequence = 0;
         long prod = 1;

         for(int i=7; i>0; --i) {
            if(((m>>i)&1) != 0) {
               int p = BASE[i];
               sequence = (sequence<<8) + p;
               prod *= p;
            }
         }

         sequence = (sequence<<8) | (m&0xff);

         sequences[m] = new CompactSequence(sequence, prod) {
            public CompactSequence from(long start) {

               if(start>0 && start<29) {
                  int m = from[(int)start];
                  return sequences[m];
               }

               return this;
            }

         };
      }

      SEQUENCES = List.of(sequences);
      ROOT = sequences[255];
   }

   public static void main(String ... args) {
      SEQUENCES.forEach(System.out::println);
   }
}
