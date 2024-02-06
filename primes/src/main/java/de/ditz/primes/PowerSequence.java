package de.ditz.primes;

import java.util.*;

abstract public class PowerSequence implements Sequence {

   final int n;

   PowerSequence(int n) {
      this.n = n;
   }

   @Override
   public String toString() {
      return "Pow(" + n + ")";
   }

   abstract long pow(long p);

   protected double root(long p) {
      return StrictMath.pow(p, 1.0/n);
   }

   @Override
   public <R> R process(long start, Target<? extends R> target) {
      return target.apply(pow(p));
   }


   abstract class Pow implements Sequence {

      final int n;

      Pow(int n) {
         this.n = n;
      }

      @Override
      public String toString() {
         return "Pow(" + n + ")";
      }

      abstract long pow(long p);

      protected double root(long p) {
         return StrictMath.pow(p, 1.0/n);
      }

      @Override
      public <R> R process(long start, Target<? extends R> target) {
         return null;
      }

      Pow dup() {
         return new Pow(2*n) {

            @Override
            long pow(long p) {
               long pow = Pow.this.pow(p);
               return pow*pow;
            }
         };
      }

      Pow next() {
         return new Pow(2*n) {

            @Override
            long pow(long p) {
               return p * Pow.this.pow(p);
            }
         };
      }
   }

   final List<Pow> powers = new ArrayList<>();

   {
      powers.add(new Pow(0) {
         @Override
         long pow(long p) {
            return 1;
         }
      });
   }

   private Pow pow(int n) {
      if (n < powers.size())
         return powers.get(n);

      if(n%2!=0)
         return pow(n/2).dup();
      else
         return pow(-1).next();
   }

   @Override
   public <R> R process(long start, Target<? extends R> target) {
      return null;
   }
}
