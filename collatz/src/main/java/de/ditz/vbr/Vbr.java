package de.ditz.vbr;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 25.06.25
 * Time: 13:10
 */
public class Vbr {
    static final int BLOCK = 1 <<12;

    static final byte[] VBR = new byte[] {
            (byte)0xEB, (byte)0x58, (byte)0x90,
            'N', 'T', 'F', 'S', ' ', ' '};

    final FileChannel dev;

    final byte[] sector = new byte[BLOCK];

    Vbr(FileChannel dev) {
        this.dev = dev;
    }

    public void run() throws IOException {
        final ByteBuffer buffer = ByteBuffer.wrap(sector);
        while(true) {
            long k = dev.position()/BLOCK;
            buffer.clear();
            int n = dev.read(buffer);
            if(n<0)
                break;

            if(isVBR())
                System.out.format("%,d VBR\n", k);
            else
            if(k%(1024*1024)==0)
                System.out.format("%,d mB\n", k/256);
        }

    }

    private boolean isVBR() {
        return Arrays.equals(
                VBR, 3, 9,
                sector, 3, 9);
    }

    public static void main(String ... args) throws IOException {
        String dev = args.length>0 ? args[0] : "/dev/sdf";
        try(FileChannel fch = FileChannel.open(new File(dev).toPath(), StandardOpenOption.READ)) {
            new Vbr(fch).run();
        }
    }
}
