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
   R apply(long factor);


   static Target<Boolean> until(LongPredicate process) {
      return p -> process.test(p) ? true : null;
   }

   static Target<Boolean> all(LongConsumer process) {
      return p -> {process.accept(p); return null;};
   }

   default Target<R> shift(long offset) {
      return offset==0 ? this : p -> apply(p+offset);
   }

   default Target<R> from(long start) {
      return start<0 ? this : p -> p<start ? null : apply(p);
   }
}
