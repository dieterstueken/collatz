package de.ditz.primes;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.LongStream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.06.20
 * Time: 11:24
 */
public class ShortWriter extends ShortList {

    class Tail extends Block {

        Tail() throws IOException {
            super(ByteBuffer.allocateDirect(2*Block.SIZE));

            if((size%Block.SIZE)!=0) {
                long index = size / Block.SIZE;
                ByteBuffer buffer = map(index);
                buffer.flip();
                bytes.put(buffer);
            }
        }

        @Override
        public boolean add(Short value) {
            return add((long)value);
        }

        boolean add(long value) {
            // just clip last 16 bits
            bytes.putShort((short)value);

            if(!bytes.hasRemaining()) {
                flush();
                // tail was never cached on block list
                assert lastBlock()!=tail;
            }

            ++size;

            return true;
        }

        public void flush() {

            // data to push
            int pos = bytes.position();
            if(pos==0)
                return;

            try {
                long index = (size-1) / Block.SIZE;
                bytes.flip();
                channel.write(bytes, index * Block.SIZE);

                // switch tail
                if(!bytes.hasRemaining())
                    pos = 0;

            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                bytes.position(pos);
            }
        }
    }

    // last partial writeable buffer
    final Tail tail;

    public ShortWriter(FileChannel channel) throws IOException {
        super(channel);
        tail = new Tail();
    }

    public void flush() {
        tail.flush();
    }

    public void add(long value) {
        tail.add(value);
    }

    @Override
    protected Block findBlock(long index) {
        // return last block as tail
        if(index == blix(size))
            return tail;
        else
            return super.findBlock(index);
    }

    protected Block lastBlock() {
        int n = blocks.size();
        return n>0 ? blocks.get(n-1) : null;
    }

    public static ShortWriter open(Path path, boolean truncate) throws IOException {

        Set<StandardOpenOption> options = EnumSet.of(StandardOpenOption.CREATE,
                StandardOpenOption.READ, StandardOpenOption.WRITE);

        if(truncate)
            options.add(StandardOpenOption.TRUNCATE_EXISTING);

        FileChannel channel = FileChannel.open(path, options);

        return new ShortWriter(channel);
    }

    @Override
    public void close() throws IOException {
        flush();
        super.close();
    }

    public static void main(String ... args) throws IOException {

        File file = new File("shorts.dat");
        
        try(ShortWriter shorts = ShortWriter.open(file.toPath(), true)) {
            LongStream.range(0, (1L<<30) + 19)
                    .map(l -> l*17)
                    .forEach(shorts::add);

            System.out.format("blocks: %d\n", shorts.blocks.size());

            System.out.format("value: %d\n", shorts.get(500000000));
            System.out.format("tail: %d\n", shorts.get(shorts.size()-5));

        }


    }
}
