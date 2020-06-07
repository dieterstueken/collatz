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

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.06.20
 * Time: 10:43
 */
public class ShortList implements AutoCloseable {

    static class Block extends AbstractList<Short> {
        static final int SIZE = 1<<24;

        final ByteBuffer bytes;

        Block(ByteBuffer bytes) {
            this.bytes = bytes;

            if(bytes==null)
                throw new NullPointerException();

            if(bytes.position()>SIZE)
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

        int getShort(int index) {
            short value = bytes.getShort(2*index);
            // make unsigned
            return value & 0xffff;
        }

        void addShort(int index, int value) {
            if((value&0xFFFF) != value)
                throw new IllegalArgumentException("short overflow");

            bytes.putShort((short)value);
        }
    }

    protected ByteBuffer map(long index) throws IOException {
        long pos = index * Block.SIZE;
        if(pos<0 || pos>size)
            throw new IndexOutOfBoundsException("map");

        int len = (int) Math.min(size - pos, Block.SIZE);

        // special case of allocating new writable block.
        if(len==0)
            return null;

        ByteBuffer bytes = channel.map(FileChannel.MapMode.READ_ONLY, pos, len);
        bytes.position(bytes.limit());
        return bytes;
    }

    public static ShortList open(Path path) throws IOException {
        FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
        return new ShortList(channel);
    }

    final FileChannel channel;

    protected long size;

    final List<Block> blocks = new ArrayList<>();

    public ShortList(FileChannel channel) throws IOException {
        this.channel = channel;
        this.size = channel.size();
        if(size>0)
            blocks.add(mapBlock(0));
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

    protected Block findBlock(int index) {
        return index < blocks.size() ? blocks.get(index) : null;
    }

    protected Block getBlock(int index) {

        Block block = findBlock(index);
        if(block!=null)
            return block;

        if(index * Block.SIZE>=size)
            throw new IndexOutOfBoundsException("block");

        synchronized(blocks) {
            // double check
            block = findBlock(index);
            if(block!=null)
                return block;
            block = mapBlock(index);

            // fill any intermediate gap
            while (blocks.size() < index)
                blocks.add(null);

            blocks.set(index, block);
            return block;
        }
    }

    public int getShort(long index) {
        int offset = (int) (index%Block.SIZE);

        index /= Block.SIZE;
        if((index&0xFFFFFFFFL) != index)
            throw new IndexOutOfBoundsException("overflow");

        return getBlock((int)index).getShort(offset);
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
