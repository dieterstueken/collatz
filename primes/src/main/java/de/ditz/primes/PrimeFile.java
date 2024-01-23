package de.ditz.primes;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;
import java.util.function.LongFunction;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  14.06.2020 14:32
 * modified by: $
 * modified on: $
 */
public class PrimeFile extends AbstractList<BufferedSequence> implements Sequence, RandomAccess, AutoCloseable {

    /**
     * Block size in bytes.
     * A single byte of compacted primes represents 2*3*5=30 numbers.
     */

    public static final int BLOCK = 1<<16;

    public static PrimeFile create(File file, int block) throws IOException {
        return new PrimeFile(BufferedFile.create(file.toPath(), block));
    }

    public static PrimeFile append(File file, int block) throws IOException {
        return new PrimeFile(BufferedFile.append(file.toPath(), block));
    }

    public static PrimeFile open(File file) throws IOException {
        return open(file, BLOCK);
    }
    public static PrimeFile open(File file, int block) throws IOException {
        return new PrimeFile(BufferedFile.open(file.toPath(), block));
    }

    final BufferedFile file;

    final List<BufferedSequence> sequences = new ArrayList<>();

    public PrimeFile(BufferedFile file) {
        this.file = file;
    }

    @Override
    public int size() {
        return file.size();
    }

    public long limit() {
        return ByteSequence.SIZE * file.length();
    }

    @Override
    public BufferedSequence get(int i) {
        if(i<sequences.size())
            return sequences.get(i);

        synchronized (sequences) {
            if(i<sequences.size())
                return sequences.get(i);

            BufferedSequence sequence = null;

            for(int k=sequences.size(); k<=i; ++k) {
                long offset = i*file.block*ByteSequence.SIZE;
                sequence = new BufferedSequence(offset, file.get(k));
                if(sequence.buffer.capacity()==file.blockSize())
                    sequences.add(sequence);
            }

            return sequence;
        }

    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    long blocks(long start) {
        return file.blocks(ByteSequence.count(start));
    }

    /**
     * @param start
     * @param process condition to top
     * @return true if stopped by condition
     */
    @Override
    public <R> R process(long start, LongFunction<? extends R> process) {

        final long block = file.block*ByteSequence.SIZE;

        if(start>block*size())
            return null;

        // find blocks to skip.
        int n = (int)(start/(block));

        R result = null;

        // process first block
        start -= n*block;
        if(start>0 && n<size()) {
            BufferedSequence sequence = this.get(n++);
            result = sequence.process(start, process);
        }

        // continue with remaining blocks
        while(result == null && n<size()) {
            BufferedSequence sequence = this.get(n++);
            result = sequence.process(process);
        }

        return result;
    }

    public void write(BufferedSequence sequence) {
        write(sequence.getBuffer());
    }

    public void write(ByteBuffer buffer) {

        int size = sequences.size();
        long count = file.length / file.blockSize();

        // truncate after last complete buffer
        if(size>count) {
            sequences.remove(--size);
        }

        file.write(buffer);
    }

    void grow() {
        int base = file.size();
        BufferedSequence block = new BufferedSequence(base, file.block).init();

        if(base==0)
            block.sieve(block, 7);
        else
            block.sieve(this, 7);

        write(block);
    }

    public long count() {
        return stream().mapToLong(BufferedSequence::count).sum();
    }

    public static void main(String ... args) throws IOException {
        
        try(PrimeFile primes = PrimeFile.create(new File("primes.dat"), 2*7)) {
            primes.grow();

            System.out.format("%,d %,d\n", primes.limit(), primes.count());

            primes.process(Sequence.all(System.out::println));
        }
    }
}
