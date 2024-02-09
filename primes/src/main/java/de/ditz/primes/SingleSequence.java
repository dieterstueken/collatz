package de.ditz.primes;

import java.util.AbstractList;
import java.util.RandomAccess;

public class SingleSequence extends CompactSequence {

   public static final CompactSequence EMPTY = new EmptySequence();

   final Integer factor;

   protected SingleSequence(Integer factor, int mask) {
      super(mask, factor);
      this.factor = factor;
   }

   public int factor() {
      return factor;
   }

   @Override
   public int size() {
      return 1;
   }

   @Override
   public int count(long limit) {
      return limit<this.factor ? 0 : 1;
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
   public CompactSequence from(long start) {
      return start>this.factor ? EMPTY : this;
   }

   @Override
   public CompactSequence expunge(long prime) {
      return prime == this.factor ? EMPTY : this;
   }

   @Override
   public <R> R process(long start, Target<? extends R> process, long offset) {
      return start-offset>factor ? null : process.process(factor+offset);
   }

   @Override
   public <R> R process(long start, Target<? extends R> target) {
      return start>factor ? null : target.process(factor);
   }

   @Override
   public <R> R process(Target<? extends R> process, long offset) {
      return process.process(factor+offset);
   }

   @Override
   public <R> R process(Target<? extends R> process) {
      return process.process(factor);
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

   static class EmptySequence extends CompactSequence {

      EmptySequence() {
         super(0, 0);
      }

      @Override
      public String toString() {
         return "00[]";
      }

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
      public int count(long limit) {
         return 0;
      }

      @Override
      public CompactSequence expunge(long prime) {
         return this;
      }

      @Override
      public CompactSequence from(long start) {
         return this;
      }

      @Override
      public <R> R process(long start, Target<? extends R> target) {
         return null;
      }
   }
}
