package de.ditz.primes;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.LongPredicate;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  14.06.2020 14:32
 * modified by: $
 * modified on: $
 */
public class PrimeFile extends AbstractList<BufferedSequence> implements Sequence, RandomAccess, AutoCloseable {

    public static PrimeFile create(File file) throws IOException {
        return new PrimeFile(BufferedFile.create(file.toPath(), Sieve.BLOCK));
    }

    public static PrimeFile append(File file) throws IOException {
        return new PrimeFile(BufferedFile.append(file.toPath(), Sieve.BLOCK));
    }

    public static PrimeFile open(File file) throws IOException {
        return new PrimeFile(BufferedFile.open(file.toPath(), Sieve.BLOCK));
    }

    final BufferedFile file;

    final List<BufferedSequence> sequences = new ArrayList<>();

    public PrimeFile(BufferedFile file) {
        this.file = file;
    }

    public int size() {
        return file.size();
    }

    public BufferedSequence get(int i) {
        if(i<sequences.size())
            return sequences.get(i);

        synchronized (sequences) {
            if(i<sequences.size())
                return sequences.get(i);

            BufferedSequence sequence = null;

            for(int k=sequences.size(); k<=i; ++k) {
                sequence = new BufferedSequence(file.get(k));
                if(sequence.buffer.capacity()==file.bytes())
                    sequences.add(sequence);
            }

            return sequence;
        }

    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    /**
     * @param start
     * @param until condition to top
     * @return true if stopped by condition
     */
    @Override
    public boolean forEach(long start, LongPredicate until, long offset) {

        // substitute first compact sequence since it does not contain any primes < 17.
        if(ArraySequence.ROOT.forEach(start, until, offset))
            return true;

        if(start <30) // skip since root sequence already done
            start = 30;

        for (BufferedSequence sequence : this) {
            if(sequence.forEach(start, until, offset))
                return true;
            offset += sequence.size();
        }

        return false;
    }

    public void write(BufferedSequence sequence) {
        write(sequence.getBuffer());
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
}
