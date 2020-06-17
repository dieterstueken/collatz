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
        Sequence.base().forEach(0, i->MASKS[(int)i]^=((byte)(1<<i)));
    }

    static final Sequence ODDS = (seek, until) -> {
        Sequence base = Sequence.base();

        if(seek<7)
            seek = 7;
        else
            seek %= 30;

        for(long block = seek/30; block<Long.MAX_VALUE; ++block) {
            if(base.based(30*block).forEachUntil(seek, until))
                return true;
        }

        return false;
    };

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

    long limit() {
        return base + size();
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
        long skip = Math.max(base / prime, 7);

        if(prime * skip >= limit())
            return true;

        ODDS.forEachUntil(skip, factor -> clear(prime * factor));

        return false;
    }

    void sieve(PrimeFile primes) {

        reset(primes.size());

        primes.forEachUntil(5, this::sieve);
    }

    static void testPrime(long prime) {
        ODDS.forEachUntil(7, n -> {
            if (n>prime/n)
                return false;

            if ((prime % n) != 0)
                return true;

            throw new IllegalStateException("not a prime: " + prime);
        });
    }

    boolean _sieve(long prime) {
        testPrime(prime);
        return sieve(prime);
    }

    public ByteBuffer finish() {

        // find all primes on buffer
        Sequence.compact(buffer).based(base).forEachUntil(this::_sieve);

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
