package de.ditz.primes;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Dump {

   public static void log(PrimeFile primes) {
       System.out.format("%d %,d %,d %,d\n", primes.size(), primes.limit(), primes.count(), primes.dup);
   }

   public static void main(String ... args) throws IOException {

      try(PrimeFile primes = PrimeFile.create(new File("primes.dat"))) {

         while(primes.limit()<200000) {
            primes.grow();
            log(primes);
         }

         primes.dump("primes.txt");

         System.out.println();

         long[] stat = primes.stat();
         System.out.println(Arrays.toString(stat));
      }
   }
}
