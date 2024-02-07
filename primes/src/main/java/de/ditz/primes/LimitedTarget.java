package de.ditz.primes;

public interface LimitedTarget<R> extends Target<R> {

   long offset();

   long limit();
}
