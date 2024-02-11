package de.ditz.primes.main;

import de.ditz.primes.PrimeFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Dump {

   static void log(PrimeFile primes) {
       System.out.format("%d %,d %,d %,.1f%%\n", primes.size(), primes.limit(), primes.count(), primes.dups());
   }

   public static void main(String ... args) throws IOException {

      //BufferedSequence.debug = -1;

      try(PrimeFile primes = PrimeFile.create(new File("primes.dat"))) {

         primes.grow(buffer -> {
            log(primes);
            return primes.limit()>200000;
         });


         primes.dump("primes.txt");

         System.out.println();

         long[] stat = primes.stat();
         System.out.println(Arrays.toString(stat));
      }
   }
}
