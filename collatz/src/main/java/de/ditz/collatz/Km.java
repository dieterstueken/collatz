package de.ditz.collatz;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 10.07.25
 * Time: 14:24
 */
public class Km implements AutoCloseable {

    static int BUFFER = 1<<12;

    static long[] POW3 = new long[40];

    static {
        long[] POW3 = new long[40];
        long p3 = 1;
        for(int i=0; i<40; ++i) {
            POW3[i] = p3;
            p3 *= 3;
        }
        Km.POW3 = POW3;
    }

    void grow() throws IOException {
        long size = file.size();
        // truncate odd buffers
        size &= (1<<12)-1;


    }

    static int kl(long m) {
        int l = 0;
        int k = 0;
        while(m>2) {

            ++m;
            int i = Long.numberOfTrailingZeros(m);
            m >>= i;
            m *= POW3[i];
            k += i;
            --m;

            i = Long.numberOfTrailingZeros(m);
            m >>= i;
            l += i;
        }

        if(Math.max(k, l)>Short.MAX_VALUE)
            throw new IndexOutOfBoundsException();

        return (k<<16) + l;
    }

    final FileChannel file;
    final AtomicLong count;

    public Km(FileChannel file) throws IOException {
        this.file = file;
        this.count = new AtomicLong(file.size() / BUFFER);
    }

    @Override
    public void close() throws Exception {
        file.close();
    }

    void grow(ByteBuffer buffer) throws IOException {
        long m = count.getAndIncrement() << 12;
        buffer.clear();
        for(int j=0; j<buffer.capacity()/2; ++j) {
            int l = kl(m+j)>>16;
            buffer.putShort((short) l);
        }
        buffer.clear();
        long count = file.write(buffer, m);
    }

    void grow(long maxCount) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER);
        while(count.get()<maxCount) {
            grow(buffer);
        }
    }

    RecursiveAction growTask(long maxCount) {
        return new RecursiveAction() {
            @Override
            protected void compute() {
                try {
                    grow(maxCount);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
    }

    void growConcurrently(long maxCount) {
        int n = ForkJoinPool.getCommonPoolParallelism();
        List<RecursiveAction> tasks = new ArrayList(n);
        for(int i=0; i<n; ++i)
            tasks.add(growTask(maxCount));

        ForkJoinTask.invokeAll(tasks);
    }

    static Km open(String name) throws IOException {
        Path path = new File(name).toPath();
        FileChannel file = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        return new Km(file);
    }

    final static String DEFAULT_FILE = "km.dat";
    final static long DEFAULT_LENGTH = 1L<<30;

    public static void main(String ... args) throws Exception {
        String file = args.length>0? args[0] : DEFAULT_FILE;
        long maxCount = args.length>1? Long.parseLong(args[1]) : DEFAULT_LENGTH;

        try(Km km = Km.open(file)) {

            ForkJoinTask<?> grow = ForkJoinTask.adapt(() -> km.growConcurrently(maxCount));

            grow.fork();

            while (!grow.isDone()) {
                System.out.format("%,d\n", km.count.get());
                Thread.sleep(10000);
            }

            grow.join();
        }
    }

}
