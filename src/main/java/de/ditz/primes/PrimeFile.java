package de.ditz.primes;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongPredicate;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  14.06.2020 14:32
 * modified by: $
 * modified on: $
 */
public class PrimeFile implements Sequence, AutoCloseable {

    public static PrimeFile create(File file) throws IOException {
        return new PrimeFile(BufferedFile.create(file.toPath()));
    }

    public static PrimeFile append(File file) throws IOException {
        return new PrimeFile(BufferedFile.append(file.toPath()));
    }

    public static PrimeFile open(File file) throws IOException {
        return new PrimeFile(BufferedFile.open(file.toPath()));
    }

    final BufferedFile file;

    final List<Sequence> sequences = new ArrayList<>();

    public PrimeFile(BufferedFile file) {
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
    public long size() {
        return 30*bytes();
    }

    public long bytes() {
        return file.length();
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    /**
     *
     * @param skip value (exclusive)
     * @param until condition to top
     * @return true if stopped by condition
     */
    @Override
    public boolean forEach(long skip, LongPredicate until) {

        if(skip<5) {
            if(skip<2 && until.test(2))
                return true;

            if(skip<3 && until.test(3))
                return true;

            if(until.test(5))
                return true;
        }

        final int block = 30*file.bytes();

        for(long index = (int)(skip / block); index<file.size(); ++index) {
            if(getSequence((int)index).forEach(skip, until))
                return true;
        }

        return false;
    }

    @Override
    public boolean forEach(long base, long skip, LongPredicate until) {
        if(base==0)
            return forEach(skip, until);
        else
            return forEach(skip, i -> until.test(base+i));
    }

    protected Sequence findSequence(int index) {
         return index < sequences.size() ? sequences.get(index) : null;
    }

    Sequence getSequence(int index) {
        Sequence sequence = findSequence(index);
        if (sequence != null)
            return sequence;

        synchronized (sequences) {
                    // double check
            sequence = findSequence(index);
            if (sequence != null)
                return sequence;

            // extend list on demand
            while (sequences.size() <= index)
                sequences.add(null);

            ByteBuffer buffer = file.get(index);
            sequence = Sequence.compact(buffer).based(30L*index*file.bytes());
            sequences.set(index, sequence);
        }

        return sequence;
    }

    public void write(ByteBuffer buffer) {

        int size = sequences.size();
        long count = file.length / file.bytes();

        // truncate after last complete buffer
        if(size>count) {
            sequences.remove(--size);
        }

        file.write(buffer);
    }

    public static void main(String ... args) throws IOException {
        File file = new File(args.length > 0 ? args[0] : "primes.dat");

        try(PrimeFile primes = PrimeFile.create(file)) {
            primes.forEach(Sequence.each(System.out::println));
        }

        System.out.println();

        Sieve.ODDS.forEach(42, i-> {
            System.out.println(i);
            return i>100;
        });
    }
}
