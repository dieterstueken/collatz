package de.ditz.primes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongPredicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.06.20
 * Time: 10:43
 */
public class ShortList implements LongList {

    public int bits() {
        return 16;
    }

    /**
     * Max size: 2^31 * 2^25 -> 2^56 bytes = 2048 TB
     */
    static class Block extends AbstractList<Short> {
        static final int SIZE = 1<<24;
        static final int BYTES = 2*SIZE;

        final ByteBuffer bytes;

        Block(ByteBuffer bytes) {
            this.bytes = bytes;

            if(bytes==null)
                throw new NullPointerException();

            if(bytes.position()>BYTES)
                throw new IllegalArgumentException("oversized");
        }

        @Override
        public Short get(int index) {
            return bytes.getShort(2*index);
        }

        @Override
        public int size() {
            return bytes.position()/2;
        }

        boolean hasRemaining() {
            return bytes.hasRemaining();
        }

        int getShort(int index) {
            short value = bytes.getShort(2*index);
            // make unsigned
            return value & 0xffff;
        }

        boolean forEach(LongPredicate until) {
            int size = size();
            for(int i=0; i<size; ++i) {
                int value = getShort(i);
                if(!until.test(value))
                    return false;
            }

            return true;
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

    public ShortList(FileChannel channel) throws IOException {
        this.channel = channel;
        this.size = channel.size();
    }

    @Override
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
        return (index + Block.SIZE - 1)/Block.SIZE;
    }

    @Override
    public int get(long index) {
        int offset = (int) (index%Block.SIZE);
        return getBlock(blix(index)).getShort(offset);
    }

    @Override
    public boolean forEach(LongPredicate until) {

        for (Block block : blocks) {
            if(!block.forEach(until))
                return false;
        }

        return true;
    }

    @Override
    public void add(long value) {
        throw new UnsupportedOperationException("add");
    }

    public static ShortList open(Path path) throws IOException {
        FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
        return new ShortList(channel);
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws IOException{
        channel.close();
    }
}
