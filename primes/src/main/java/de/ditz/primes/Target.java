package de.ditz.primes;

import java.util.function.LongConsumer;
import java.util.function.LongPredicate;

public interface Target<R> {

   /**
    * Test the target for a given factor.
    * Return null if further testes > factor are required.
    * Else return some result if no further factors are required.
    *
    * @param factor to test.
    * @return some result.
    */
   R process(long factor);

   static Target<Boolean> until(LongPredicate process) {
      return p -> process.test(p) ? true : null;
   }

   static Target<Boolean> all(LongConsumer process) {
      return p -> {process.accept(p); return null;};
   }

   default Target<R> start(long start) {
      return new Target<>() {

         @Override
         public R process(long factor) {
            return factor<start ? null : Target.this.process(factor);
         }

         @Override
         public Target<R> start(long skip) {
            return skip > start ? Target.super.start(skip) : this;
         }
      };
   }
}