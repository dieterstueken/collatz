package de.ditz.primes;

import java.util.function.LongFunction;

public class ArraySequence implements Sequence {

   final int[] values;

   public ArraySequence(int ... values) {
      this.values = values;
   }

   @Override
   public <R> R process(long start, LongFunction<? extends R> process, long offset) {

      // fast track
      if(start <values[0])
         return process(process, offset);

      // sequence is skipped completely
      if(start >=offset+values[values.length-1])
         return null;

      // partial processing (infrequent).
      return processFrom(0, Sequence.start(process, start), offset);
   }

   public <R> R processFrom(int start, LongFunction<? extends R> process, long offset) {
      R result = null;

      for(int i=start; i<values.length && result==null; ++i) {
         int prime = values[i];
         result = process.apply(prime + offset);
      }

      return result;
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
         public <R> R process(long start, LongFunction<? extends R> process, long offset) {

            long i = start - offset;

            // fast track
            if(i<2)
               return processFrom(0, process, offset);

            // skip values
            if(i<30)
               return processFrom(offsets[(int)i], process, offset);

            // this sequence is skipped completely
            return null;
         }
      };
   }

   public static void main(String ... args) {
      for(int i=0; i<31; ++i) {
         System.out.format("%2d:", i);
         ROOT.process(i, p-> {
            System.out.format(" %2d", p);
            return false;
         });
         System.out.println();
      }
   }
}
