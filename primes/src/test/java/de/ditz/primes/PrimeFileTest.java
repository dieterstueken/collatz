package de.ditz.primes;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrimeFileTest {

    @org.junit.jupiter.api.Test
    void create() throws IOException {

        File tmpFile = File.createTempFile( "primes", ".dat");
        tmpFile.deleteOnExit();

        testPrimes(tmpFile, 17, 1<<15);
        testPrimes(tmpFile, 17, 11119);
        testPrimes(tmpFile, 7, 1<<15);
    }

    private void testPrimes(File tmpFile, int root, int block) throws IOException {

        PrimeFile.ROOT = root;
        PrimeFile.BLOCK = block;

        try(PrimeFile primes = PrimeFile.create(tmpFile)) {
            primes.growTo(1<<22);

            assertEquals(0, primes.count(2));
            assertEquals(1, primes.count(3));
            assertEquals(2, primes.count(4));
            assertEquals(2, primes.count(5));
            assertEquals(3, primes.count(6));
            assertEquals(3, primes.count(7));
            assertEquals(4, primes.count(9));
            assertEquals(8, primes.count(20));
            assertEquals(15, primes.count(50));
            assertEquals(172, primes.count(1<<10));
            assertEquals(6542, primes.count(1<<16));
            assertEquals(295947, primes.count(1<<22));

        } finally {
            tmpFile.delete();
        }
    }
}