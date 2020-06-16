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

    public static long forEachOdd(long skip, LongPredicate until) {
        for(long n=skip/30; true; ++n) {
            long base = 30*n;
            for (byte b : BASE30) {
                long number = base + b;
                if(number>skip)
                    if(!until.test(number))
                        return number;
            }
        }
    }

    public static boolean forEachPrime(long base, int seek, ByteBuffer buffer, LongPredicate until) {
        int skip = seek%30;
        for(int pos=seek/30;pos<buffer.limit(); ++pos) {
            int bits = 0xff & buffer.get(pos);
            byte[] seq = SEQUENCES.get(bits);
            for (byte b : seq) {
                if(b>skip) {
                    long prime = base + 30 * pos + b;
                    if (!until.test(prime))
                        return false;
                }
            }
            skip = 0;
        }

        return true;
    }

    /**
     *
     * @param skip value (exclusive)
     * @param until condition to top
     * @return true if stopped by condition
     */
    boolean forEachPrime(long skip, LongPredicate until) {

        if(skip<2 && !until.test(2))
            return false;

        if(skip<3 && !until.test(3))
            return false;

        if(skip<5 && !until.test(5))
            return false;

        final int block = 30*file.bytes();
        int pos = (int)(skip % block);

        for(long index = (int)(skip / block); index<file.size(); ++index, pos=0) {
            ByteBuffer buffer = file.get((int)index);
            if(!forEachPrime(index * block, pos, buffer, until))
                return false;
        }

        return true;
    }

    void forEach(long start, LongConsumer consumer) {
        forEachPrime(start, prime -> {consumer.accept(prime); return true;});
    }

    public void write(ByteBuffer buffer) throws IOException {
        file.write(buffer);
    }

    public static void main(String ... args) throws IOException {
        File file = new File(args.length > 0 ? args[0] : "primes.dat");

        try(PrimeFile primes = new PrimeFile(BufferedFile.create(file.toPath()))) {
            primes.forEach(7, System.out::println);
        }

    }
}
