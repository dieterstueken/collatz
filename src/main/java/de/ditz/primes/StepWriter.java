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
public class StepWriter extends StepList {

    class Tail extends Block {

        Tail() throws IOException {
            super(ByteBuffer.allocateDirect(Block.BYTES));

            if((size%Block.SIZE)!=0) {
                long index = size / Block.SIZE;
                ByteBuffer buffer = map(index);
                buffer.flip();
                bytes.put(buffer);
            }
        }

        boolean add(byte value) {
            // just clip last 16 bits
            bytes.put(value);

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
                channel.force(false);
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

    public StepWriter(FileChannel channel) throws IOException {
        super(channel);
        tail = new Tail();
    }

    @Override
    public void flush() {
        tail.flush();
        super.flush();
    }

    public void add(int step) {

        if(step<=0)
            throw new IllegalArgumentException("invalid step");

        while(step>255) {
            tail.add((byte)0);
            step -= 254;
        }

        tail.add((byte)step);
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

    public static StepWriter open(Path path, boolean truncate) throws IOException {

        Set<StandardOpenOption> options = EnumSet.of(StandardOpenOption.CREATE,
                StandardOpenOption.READ, StandardOpenOption.WRITE);

        if(truncate)
            options.add(StandardOpenOption.TRUNCATE_EXISTING);

        FileChannel channel = FileChannel.open(path, options);

        return new StepWriter(channel);
    }

    @Override
    public void close() throws IOException {
        flush();
        super.close();
    }

    public static void main(String ... args) throws IOException {

        File file = new File("steps.dat");
        
        try(StepWriter steps = StepWriter.open(file.toPath(), true)) {
            LongStream.range(0, (1<<30) + 19)
                    .map(l -> (17L*l)%260 + 1)
                    .mapToInt(l -> (int)l)
                    .forEach(steps::add);

            System.out.format("blocks: %d\n", steps.blocks.size());

            System.out.format("value: %d\n", steps.get(500000000));
            System.out.format("tail: %d\n", steps.get(steps.size()-5));

        }
    }
}
