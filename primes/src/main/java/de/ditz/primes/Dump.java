package de.ditz.primes;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Dump {


   public static void main(String ... args) throws IOException {

      try(PrimeFile primes = PrimeFile.create(new File("primes.dat"))) {

         while(primes.buffers.size()<2) {
            primes.grow();
            System.out.format("%,d %,d %,d\n", primes.limit(), primes.count(), primes.dup);
         }

         primes.dump("primes.txt");

         System.out.println();
         System.out.format("%,d %,d %,d\n", primes.limit(), primes.count(), primes.dup);

         long[] stat = primes.stat();
         System.out.println(Arrays.toString(stat));
      }
   }
}
