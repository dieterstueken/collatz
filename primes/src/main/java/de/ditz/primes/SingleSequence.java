package de.ditz.primes;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;
import java.util.function.LongFunction;

public class SingleSequence extends ByteSequence {

   public static final SingleSequence EMPTY = new EmptySequence();

   public static final List<? extends SingleSequence> SINGLES = new Singles();

   public static SingleSequence single(int index) {
      return SINGLES.get(index);
   }

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
   public int factor(int index) {
      return index==0 ? factor : super.get(index);
   }

   @Override
   public Integer get(int index) {
      return index==0 ? factor : super.get(index);
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

   static class Singles extends AbstractList<SingleSequence> implements RandomAccess {

      private SingleSequence[] singles = new SingleSequence[8];

      {
         for(int i=0; i<8; ++i) {
            singles[i] = new SingleSequence(FACTORS.get(i), 1<<i);
         }
      }

      @Override
      public SingleSequence get(int i) {
         return singles[i];
      }

      @Override
      public int size() {
         return 8;
      }
   }

   private static class EmptySequence extends SingleSequence {

      EmptySequence() {
         super(0,0);
      };

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
   }
}
