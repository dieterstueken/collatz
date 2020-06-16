package de.ditz.primes.compressed;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.06.20
 * Time: 20:15
 */
public class Sieve {

    private static final byte[] MASKS = new byte[30];

    static {
        for(int i=0; i<8; ++i) {
            int b = PrimeFile.base(i);
            MASKS[b] = (byte)(1<<i);
        }
        for(int i=0; i<30; ++i)
            MASKS[i] ^= 0xff;
    }

    long base = 0;
    final byte[] bytes;
    final ByteBuffer buffer;

    Sieve(int size) {
        bytes = new byte[size];
        buffer = ByteBuffer.wrap(bytes);
    }

    int size()  {
        return 30*buffer.limit();
    }

    void reset(long base) {
        this.base = base;
        buffer.clear();
        Arrays.fill(bytes, (byte)(0xff));
    }

    boolean clear(long index) {

        long pos = (index-base)/30;

        if(pos<buffer.limit()) {
            int i = (int)((index-base)%30);
            int mask = MASKS[i];
            bytes[(int)pos] &= mask;
            return true;
        }

        return false;
    }

    boolean sieve(long prime) {
        long skip = Math.max(base / prime, 5);

        if(prime*prime<base+size()) {
            PrimeFile.forEachOdd(skip, factor -> clear(prime * factor));
            return true;
        } else
            return false;
    }

    void sieve(PrimeFile primes) {

        long length = primes.file.length();
        reset(30*length);

        long limit = 30*length*length;
        if(limit<buffer.limit())
            buffer.limit((int)limit);

        primes.forEachPrime(5, this::sieve);
    }

    static boolean isPrime(long prime) {
        return factor(prime)<2;
    }

    static long factor(long prime) {

        long k = PrimeFile.forEachOdd(5, n -> n*n<=prime && (prime%n)!=0);

        return k*k<prime ? k : 1;
    }

    boolean _sieve(long prime) {
        if(!isPrime(prime))
            throw new IllegalStateException("not a prime: " + prime);
        return sieve(prime);
    }

    public ByteBuffer finish() {

        // find all primes on buffer
        PrimeFile.forEachPrime(base, 0, buffer, this::sieve);

        return buffer;
    }

    public static void main(String ... args) throws IOException {
        File file = new File(args.length > 0 ? args[0] : "primes.dat");

        try(PrimeFile primes = new PrimeFile(BufferedFile.create(file.toPath()))) {
            Sieve sieve = new Sieve(1024);

            for(int i=0; i<1<<27; ++i) {
                sieve.sieve(primes);
                final ByteBuffer buffer = sieve.finish();
                primes.write(buffer);
            }

            primes.forEach(primes.size()-50, System.out::println);
        }

    }

}
