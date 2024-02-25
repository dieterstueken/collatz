package de.ditz.primes.main;

import de.ditz.primes.BufferedSequence;
import de.ditz.primes.PrimeFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 25.02.24
 * Time: 19:02
 */
public class Stat {

    public static void main(String ... args) throws IOException {

        PrimeFile.BLOCK = 1<<30;
        long[] stat = new long[10];

        try(PrimeFile primes = PrimeFile.open(new File("primes.dat"))) {
            List<BufferedSequence> buffers =  primes.buffers();
            int start = 0; //buffers.size()-500;

            for(int i=start; i<buffers.size(); ++i) {
                BufferedSequence buffer = buffers.get(i);
                System.out.format("%5d %s\n", i, Arrays.toString(buffer.stat(stat)));

                Arrays.fill(stat, 0);
            }
        }
    }
}
