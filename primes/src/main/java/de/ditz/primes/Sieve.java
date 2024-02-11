package de.ditz.primes;

public class Sieve {

   final PrimeFile primes;

   BufferedSequence target;

   long prime;

   int dups;

   public Sieve(PrimeFile primes) {
      this.primes = primes;
   }

   public int dups() {
      return dups;
   }


   final Target<BufferedSequence> drop = this::dropFactor;

   private BufferedSequence dropFactor(long factor) {
      Boolean dropped = target.drop(factor*prime);
      if(dropped == null)
         return target;    // terminate processing

      if(dropped==false)
         ++dups;

      return null; // continue processing
   }

   public Sieve sieve(BufferedSequence target) {
      this.target = target;
      this.dups = 0;
      primes.root.fill(target);

      primes.process(primes.root.prime+1, sieve);

      return this;
   }

   final Target<BufferedSequence> sieve = this::sieve;

   public BufferedSequence sieve(long prime) {
      long start = (target.offset() + prime-1) / prime;
      if(start<prime)
         start = prime;

      if(start*prime>target.limit())
         return target;

      this.prime = prime;
      primes.root.process(start, drop);

      // continue with further primes.
      return null;
   }
}
