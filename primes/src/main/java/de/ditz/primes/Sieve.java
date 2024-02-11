package de.ditz.primes;

public class Sieve {

   final PrimeFile primes;

   BufferedSequence target;

   long prime;

   public Sieve(PrimeFile primes) {
      this.primes = primes;
   }

   final Target<BufferedSequence> drop = this::dropFactor;

   private BufferedSequence dropFactor(long factor) {
      return target.process(factor*prime);
   }
   
   public long base() {
      return target==null ? 0 : target.base;
   }

   public long length() {
      return target==null ? 0 : target.base + target.capacity();
   }

   public Sieve rebase(long base) {
      return rebase(base, primes.bufferSize(base));
   }

   public Sieve rebase(long base, int capacity) {

      if(target==null || target.size()!=capacity)
         target = new BufferedSequence(base, capacity);
      else
         target = new BufferedSequence(base, target.getBuffer());

      return this;
   }

   public Sieve sieve(BufferedSequence target) {
      this.target = target;
      sieve();
      return this;
   }

   public BufferedSequence sieve() {
      target.dups = 0;
      primes.root.fill(target);

      // bootstrap very first sequence
      if(target.offset()==0) {
          target.buffer.put(0, (byte) Sequences.root().from(7).mask());
      }

      primes.process(primes.root.prime+1, sieve);

      return target;
   }

   private final Target<BufferedSequence> sieve = this::sieve;

   private BufferedSequence sieve(long prime) {
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
