package de.ditz.primes;

public class Sieve {

   final RootBuffer root;

   final BufferedSequence target;

   final Target<BufferedSequence> drop = this::dropFactor;

   final Target<BufferedSequence> sieve = this::sieve;

   long prime;

   int dups;

   public Sieve(RootBuffer root, BufferedSequence target) {
      this.root = root;
      this.target = target;
   }

   public Sieve reset() {
      dups = 0;
      root.fill(target);
      return this;
   }

   public int dups() {
      return dups;
   }

   private BufferedSequence dropFactor(long factor) {
      Boolean dropped = target.drop(factor*prime);
      if(dropped == null)
         return target;    // terminate processing

      if(dropped==false)
         ++dups;

      return null; // continue processing
   }

   public BufferedSequence dropPrimes(long start, long prime) {
      this.prime = prime;
      return root.process(start, drop);
   }

   public Sieve sieve(Sequence primes) {
      primes.process(root.prime+1, sieve);
      return this;
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
