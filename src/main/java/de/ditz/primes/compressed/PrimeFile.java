package de.ditz.primes.compressed;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;
import java.util.function.LongPredicate;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  14.06.2020 14:32
 * modified by: $
 * modified on: $
 */
public class PrimeFile implements AutoCloseable {

    private static final byte[] BASE30 = {1,7,11,13,17,19,23,29};

    public static int base(int i) {
        int j = i/30;
        int k = i%30;
        return 30*j + PrimeFile.BASE30[k];
    }

    private static final List<byte[]> SEQUENCES;

    static {
        List<byte[]> sequences = new ArrayList<>(256);
        ByteBuffer buffer = ByteBuffer.allocate(8);
        for(int i=0; i<256; ++i) {
            buffer.clear();
            int m=i;
            while(m!=0) {
                byte bit = (byte)Integer.numberOfTrailingZeros(m);
                buffer.put(BASE30[bit]);
                m ^= 1<<bit;
            }

            buffer.flip();
            byte[] seq = new byte[buffer.limit()];
            buffer.get(seq);
            sequences.add(seq);
        }

        SEQUENCES = List.copyOf(sequences);
    }

    final BufferedFile file;

    public PrimeFile(BufferedFile file) throws IOException {
        this.file = file;

        if(file.length()<4) {
            if(file.length()!=0)
                throw new IllegalStateException();

            byte[] initial = {
                    (byte)0xfe, // 1
                    (byte)0xdf, // 49
                    (byte)0xef, // 77
                    (byte)0x7e  // 91, 119
            };
            ByteBuffer buffer = ByteBuffer.wrap(initial);
            write(buffer);
        }
    }

    /**
     *
     * @return number of checked numbers so far.
     */
    long size() {
        return file.length() * 30;
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    public static boolean forEachPrime(long base, ByteBuffer buffer, int pos, LongPredicate until) {

        for(;pos<buffer.limit(); ++pos, base+=30) {
            int bits = 0xff & buffer.get(pos);
            byte[] seq = SEQUENCES.get(bits);
            for (byte b : seq) {
                long prime = base + b;
                if (until.test(prime))
                    return true;
            }
        }

        return false;
    }

    /**
     *
     * @param start value (inclusive)
     * @param until condition to top
     * @return true if stopped by condition
     */
    boolean forEachPrime(long start, LongPredicate until) {

        if(start<=2 && until.test(2))
            return true;

        if(start<=3 && until.test(3))
            return true;

        if(start<=5 && until.test(5))
            return true;
        
        final int block = 30*file.bytes();
        int pos = (int)(start % 30);

        LongPredicate predicate = pos==0 ? until : prime->prime>=start && until.test(prime);

        for(long index = (int)(start / block); index<file.size(); ++index) {
            ByteBuffer buffer = file.get((int)index);
            long base = index * block + 30*pos;

            if(forEachPrime(base, buffer, pos, predicate))
                return true;

            pos = 0;
            predicate = until;
        }

        return false;
    }

    void forEach(long start, LongConsumer consumer) {
        forEachPrime(start, prime -> {consumer.accept(prime); return false;});
    }

    public void write(ByteBuffer buffer) throws IOException {
        file.write(buffer);
    }

    public static void main(String ... args) throws IOException {
        File file = new File(args.length > 0 ? args[0] : "primes.dat");

        try(PrimeFile primes = new PrimeFile(BufferedFile.create(file.toPath()))) {
            primes.forEach(11, System.out::println);
        }

    }
}
