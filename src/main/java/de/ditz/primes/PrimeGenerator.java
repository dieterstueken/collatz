package de.ditz.primes;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 05.06.20
 * Time: 01:05
 */
public class PrimeGenerator implements AutoCloseable {

    //private static final int[] INITIALS = {2,3,5,7,11,13,17};

    final PrimeWriter primes;

    PrimeGenerator(File file) throws IOException {
        primes = PrimeWriter.open(file.toPath());
        if(primes.isEmpty())
            primes.addPrime(2);
    }

    @Override
    public void close() throws IOException {
        primes.close();
    }

    public void generate(long limit) {
        BasicSieve sieve = new BasicSieve();

        while(primes.size()<limit) {
            long base = (primes.lastPrime()+1)|1;
            sieve.reset(base, 1<<18);
            primes.primes(sieve::sieve);
            sieve.extract(primes::addPrime);
        }
    }

    public static void main(String ... args) throws IOException {
        File file = new File(args.length > 0 ? args[0] : "primes.dat");
        long limit = args.length > 1 ? Long.parseLong(args[1]) : 1<<24;

        try(PrimeGenerator generator = new PrimeGenerator(file)) {
            generator.generate(limit);
        }
    }

}
