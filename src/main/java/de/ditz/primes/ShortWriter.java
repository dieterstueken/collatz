package de.ditz.primes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.06.20
 * Time: 11:24
 */
public class ShortWriter extends ShortList {

    public static ShortWriter open(Path path, boolean truncate) throws IOException {

        Set<StandardOpenOption> options = EnumSet.of(StandardOpenOption.CREATE,
                StandardOpenOption.READ, StandardOpenOption.WRITE);

        if(truncate)
            options.add(StandardOpenOption.TRUNCATE_EXISTING);

        FileChannel channel = FileChannel.open(path, options);

        return new ShortWriter(channel);
    }

    public ShortWriter(FileChannel channel) throws IOException {
        super(channel);
    }

    protected ByteBuffer map(long index) throws IOException {
        ByteBuffer bytes = super.map(index);

        // make partial block writeable
        if(bytes==null || bytes.position()<Block.SIZE) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(Block.SIZE);

            if(bytes!=null) {
                bytes.flip();
                buffer.put(bytes);
            }

            bytes = buffer;
        }

        return bytes;
    }
}
