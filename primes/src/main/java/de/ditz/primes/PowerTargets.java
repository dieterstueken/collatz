package de.ditz.primes;

import java.util.*;

abstract public class PowerTargets extends PowerTarget {

   final Sequence primes;

   final Target<?> target;

   final List<PowerTarget> powers = new ArrayList<>();

   public PowerTargets(final Sequence primes, Target<?> target) {
      super(0);
      this.primes = primes;
      this.target = target;
      powers.add(null);
      powers.add(this);
   }

   PowerTarget pow(int n) {
      if(n<powers.size())
         return powers.get(n);

      return new PowerTarget(n) {

         @Override
         boolean test(long start) {
            return false;
         }
      };
   }

   public boolean test(int p) {

   }

   public void process(long start) {

      for(int i=1; true; ++i) {
         PowerTargets<R> pow = pow(i);
         if(!pow.test(start))

      }
   }
}
