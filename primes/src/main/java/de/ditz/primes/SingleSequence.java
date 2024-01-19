package de.ditz.primes;

import java.util.*;
import java.util.function.LongFunction;

public class SingleSequence extends ByteSequence {

   public static final SingleSequence EMPTY = empty();

   public static final List<? extends SingleSequence> SINGLES = singles();

   final Integer factor;

   final int mask;

   protected SingleSequence(Integer factor, int mask) {
      super(factor);
      this.factor = factor;
      this.mask = mask;
   }

   public int factor() {
      return factor;
   }

   @Override
   public int mask() {
      return mask;
   }

   @Override
   public int size() {
      return 1;
   }

   @Override
   public Integer get(int index) {
      return index==0 ? factor : super.get(index);
   }

   @Override
   public int lastPrime() {
      return factor;
   }

   @Override
   public SingleSequence lastSequence() {
      return this;
   }

   @Override
   public ByteSequence from(long start) {
      return start>this.factor ? EMPTY : this;
   }

   @Override
   public ByteSequence expunge(long prime) {
      return prime == this.factor ? EMPTY : this;
   }

   @Override
   public <R> R process(long start, LongFunction<? extends R> process, long offset) {
      return start>factor ? null : process.apply(factor);
   }

   private static List<SingleSequence> singles() {

      int size = FACTORS.size();

      SingleSequence[] singles = new SingleSequence[size];

      for(int i=0; i<size; ++i) {
         singles[i] = new SingleSequence(FACTORS.get(i), 1<<i);
      }

      return List.of(singles);
   }

   private static SingleSequence empty() {

      // fake single sequence with product = 0
      return new SingleSequence(0,0) {

         /**
          * Empty list of primes.
          * But let get(0) still return 0;
          *
          * @return 0
          */
         @Override
         public int size() {
            return 0;
         }

         @Override
         public ByteSequence expunge(long prime) {
            return this;
         }

         @Override
         public ByteSequence from(long start) {
            return this;
         }
      };
   }
}
