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
        Arrays.fill(MASKS, (byte)0xff);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        Sequence.base30().forEach(Sequence.each(i->buffer.put((byte)i)));
        buffer.flip();
        while(buffer.hasRemaining()) {
            int m = 1 << buffer.position();
            byte i = buffer.get();
            MASKS[i] ^= m;
        }
    }

    public static long product(long a, long b) {

        long m = Math.abs(a);
        m |= Math.abs(b);
        if(m>>>31==0)
            return a*b;

        if((a^b)<0)
            return Long.MIN_VALUE;
        else
            return Long.MAX_VALUE;
    }

    static final Sequence ODDS = (base, skip, until) -> {
        Sequence base30 = Sequence.base30();

        if(skip<5)
            skip = 5;

        for(long block = skip/30; block<Long.MAX_VALUE; ++block) {
            if(base30.forEach(30*block, skip, until))
                return true;
        }

        return false;
    };

    static void testPrime(long prime) {
        ODDS.forEach(5, n -> {
            if (n>prime/n)
                return true;

            if ((prime % n) != 0)
                return false;

            throw new IllegalStateException("not a prime: " + prime);
        });
    }

    long base = 0;
    final byte[] bytes;
    final ByteBuffer buffer;

    final PrimeFile primes;

    Sieve(PrimeFile primes, int size) {
        this.bytes = new byte[size];
        this.buffer = ByteBuffer.wrap(bytes);
        this.primes = primes;
    }

    int size()  {
        return 30*buffer.limit();
    }

    long limit() {
        return base + size();
    }

    void reset(long base) {
        this.base = base;
        buffer.clear();
        Arrays.fill(bytes, (byte)(0xff));
        long limit = product(base, base);
        if(limit<limit()) {
            limit -= base;
            limit /= 30;
            buffer.limit((int)limit);
        }
    }

    boolean clear(long index) {

        long pos = (index-base)/30;
        if(pos>=buffer.limit())
            return true; // abort

        int i = (int)((index-base)%30);
        int mask = MASKS[i];
        bytes[(int)pos] &= mask;

        return false; // continue
    }

    /**
     * Sieve all products of a prime.
     * @param prime to clear
     * @return true if no more hits expected.
     */
    boolean sieve(long prime) {

        if(product(prime, prime)>=limit())
            return true;

        // further products to clear
        long skip = Math.max(base / prime, prime-1);

        // for each factor < prime.
        ODDS.forEach(skip, factor -> clear(prime * factor));

        return false;
    }

    void sieve() {

        reset(primes.size());

        primes.forEach(5, this::sieve);
    }

    boolean _sieve(long prime) {
        testPrime(prime);
        return sieve(prime);
    }

    public void finish() {

        // find all primes on buffer
        Sequence.compact(buffer).forEach(base, 0, this::sieve);

        primes.write(buffer);
    }

    public static void main(String ... args) throws IOException {
        File file = new File(args.length > 0 ? args[0] : "primes.dat");

        try(PrimeFile primes = new PrimeFile(BufferedFile.create(file.toPath()))) {
            Sieve sieve = new Sieve(primes, 1<<20);

            for(int i=0; i<100; ++i) {
                sieve.sieve();
                sieve.finish();
            }

            primes.forEach(primes.size()-50, Sequence.each(System.out::println));
        }
    }
}
