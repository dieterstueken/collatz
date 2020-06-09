package de.ditz.primes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.06.20
 * Time: 10:43
 */
public class StepList implements AutoCloseable {

    static class Block {
        static final int SIZE = 1<<24;
        static final int BYTES = SIZE;

        final ByteBuffer bytes;

        Block(ByteBuffer bytes) {
            this.bytes = bytes;

            if(bytes==null)
                throw new NullPointerException();

            if(bytes.position()>BYTES)
                throw new IllegalArgumentException("oversized");
        }

        public int size() {
            return bytes.position();
        }

        boolean hasRemaining() {
            return bytes.hasRemaining();
        }

        int get(int index) {
            short value = bytes.getShort(2*index);
            // make unsigned
            return value & 0xff;
        }
    }

    protected ByteBuffer map(long index) throws IOException {
        long pos = index * Block.SIZE;
        if(pos<0 || pos>=size)
            throw new IndexOutOfBoundsException("map");

        int len = (int) Math.min(size - pos, Block.BYTES);
        ByteBuffer bytes = channel.map(FileChannel.MapMode.READ_ONLY, pos, len);
        bytes.position(bytes.limit());
        return bytes;
    }

    final FileChannel channel;

    protected long size;

    final List<Block> blocks = new ArrayList<>();

    public StepList(FileChannel channel) throws IOException {
        this.channel = channel;
        this.size = channel.size();
    }

    public long size() {
        return size;
    }

    protected Block mapBlock(long index) {
        try {
            ByteBuffer buffer = map(index);
            return new Block(buffer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected Block findBlock(long index) {

        // todo: have two stage list
        if((index&0xFFFFFFFFL) != index)
            throw new IndexOutOfBoundsException("overflow");

        return index < blocks.size() ? blocks.get((int)index) : null;
    }

    protected Block getBlock(long index) {

        Block block = findBlock(index);
        if(block!=null)
            return block;

        if(index * Block.SIZE>size)
            throw new IndexOutOfBoundsException("block");

        synchronized(blocks) {
            // double check
            block = findBlock(index);
            if(block!=null)
                return block;
            
            block = mapBlock(index);

            setBlock(index, block);
            return block;
        }
    }

    protected void setBlock(long index, Block block) {

        // todo: have two stage list
        if((index&0xFFFFFFFFL) != index)
            throw new IndexOutOfBoundsException("overflow");

        // fill any intermediate gap
        while (blocks.size() <= index)
            blocks.add(null);

        blocks.set((int) index, block);
    }

    // block index
    public long blix(long index) {
        return (index + Block.SIZE - 1)/ Block.SIZE;
    }

    public int get(long index) {
        int offset = (int) (index% Block.SIZE);
        return getBlock(blix(index)).get(offset);
    }

    public boolean forEachStep(IntPredicate until) {
        int step = 0;
        for (Block block : blocks) {
            int size = block.size();
            for (int i = 0; i < size; ++i) {
                int n = get(i);
                if (n == 0) {
                    step += 254;
                } else {
                    if (!until.test(step+n))
                        return false;
                    step = 0;
                }
            }
        }

        return true;
    }

    public boolean forEachPrime(LongPredicate until) {

        if(!until.test(3))
            return false;

        IntPredicate stepper = new IntPredicate() {
            // skip prime of 2
            long prime = 3;

            @Override
            public boolean test(int step) {
                prime += 2*step;
                return until.test(prime);
            }
        };

        return forEachStep(stepper);
    }

    public void add(int step) {
        throw new UnsupportedOperationException("add");
    }

    public static StepList open(Path path) throws IOException {
        FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
        return new StepList(channel);
    }

    public void flush() {
    }

    public void close() throws IOException{
        channel.close();
    }
}
