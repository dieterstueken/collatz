package de.ditz.primes;

import java.util.function.LongPredicate;

public class ArraySequence implements Sequence {

   final int[] values;

   public ArraySequence(int ... values) {
      this.values = values;
   }

   @Override
   public boolean forEach(long start, LongPredicate until, long offset) {

      // fast track
      if(start <values[0])
         return forEach(until, offset);

      // sequence is skipped completely
      if(start >=offset+values[values.length-1])
         return false;

      // partial processing (infrequent).
      return forEachAt(0, Sequence.start(until, start), offset);
   }

   public boolean forEachAt(int start, LongPredicate until, long offset) {

      for(int i=start; i<values.length; ++i) {
         int prime = values[i];
         if (until.test(prime + offset))
            return true;
      }

      return false;
   }

   public static final ArraySequence ROOT;

   static {

      int[] primes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29};
      int[] offsets = new int[30];

      int j = 0;
      for (int i = 0; i < primes.length; ++i) {
         int prime = primes[i];
         while (j <= prime)
            offsets[j++] = i;
      }
      ROOT = new ArraySequence(primes) {

         public String toString() {
            return "ROOT(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)";
         }
         @Override
         public boolean forEach(long start, LongPredicate until, long offset) {

            long i = start - offset;

            // fast track
            if(i<2)
               return forEachAt(0, until, offset);

            // skip values
            if(i<30)
               return forEachAt(offsets[(int)i], until, offset);

            // sequence is skipped completely
            return false;
         }
      };
   }

   public static void main(String ... args) {
      for(int i=0; i<31; ++i) {
         System.out.format("%2d:", i);
         ROOT.forEach(i, p-> {
            System.out.format(" %2d", p);
            return false;
         });
         System.out.println();
      }
   }
}
