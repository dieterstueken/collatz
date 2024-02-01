package de.ditz.primes;

public class Sieve {

   final RootBuffer root;

   final BufferedSequence target;

   final Target<BufferedSequence> drop = this::dropFactor;

   final Target<BufferedSequence> sieve = this::sieve;

   long prime;

   public Sieve(RootBuffer root, BufferedSequence target) {
      this.root = root;
      this.target = target;
   }

   public Sieve reset() {
      root.fill(target);
      return this;
   }

   private BufferedSequence dropFactor(long factor) {
      return target.drop(factor*prime);
   }

   public BufferedSequence dropPrimes(long start, long prime) {
      this.prime = prime;
      return root.process(start, drop);
   }

   public BufferedSequence sieve(Sequence primes) {
      return primes.process(root.prime+1, sieve);
   }

   public BufferedSequence sieve(long prime) {
      long factor = target.offset() / prime;
      if(factor<prime)
         factor = prime;

      if(factor*prime>target.limit())
         return target;

      dropPrimes(factor, prime);

      // continue with further primes.
      return null;
   }
}
