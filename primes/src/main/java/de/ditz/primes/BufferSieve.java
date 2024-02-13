package de.ditz.primes;

import java.util.concurrent.RecursiveTask;

public class BufferSieve extends RecursiveTask<BufferedSequence>  {

   final PrimeFile primes;

   final RootBuffer root;

   BufferedSequence target;

   long prime;

   public BufferSieve(RootBuffer root, PrimeFile primes) {
      this.root = root;
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

   public long rebase(long base) {
      if(this.isDone())
         reinitialize();

      return rebase(base, primes.bufferSize(base));
   }

   public long rebase(long base, int capacity) {

      if(target==null || target.capacity()!=capacity)
         target = new BufferedSequence(base, capacity);
      else
         target = new BufferedSequence(base, target.getBuffer());

      return target.base + target.capacity();
   }

   @Override
   public BufferedSequence compute() {
      target.dups = 0;
      root.fill(target);

      // bootstrap very first sequence
      if(target.offset()==0) {
          target.buffer.put(0, (byte) Sequences.root().from(7).mask());
      }

      primes.process(root.prime+1, sieve);

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
      root.process(start, drop);

      // continue with further primes.
      return null;
   }
}
