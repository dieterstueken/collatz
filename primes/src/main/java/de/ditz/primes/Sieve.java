package de.ditz.primes;

public class Sieve {

   final RootBuffer root;

   final BufferedSequence target;

   final Target<BufferedSequence> drop = this::drop;

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

   public BufferedSequence sieve(Sequence primes) {
      return primes.process(root.prime+1, sieve);
   }

   public BufferedSequence drop(long factor) {
      return target.drop(factor*prime);
   }

   public BufferedSequence sieve(long prime) {
      this.prime = prime;

      long factor = target.offset() / root.prime;
      if(factor<prime)
         factor = prime;

      return root.process(factor, drop);
   }

   public BufferedSequence dropAll(long prime) {
      this.prime = prime;
      return root.process(drop);
   }
}
