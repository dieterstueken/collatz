package de.ditz.primes;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongPredicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.06.20
 * Time: 10:34
 */
public class PrimeFiles implements PrimeStream, AutoCloseable {

    class FileEntry {

        final int bias;

        final Path path;

        final long start;

        PrimeFile primes;

        public FileEntry(Path path, int bias, long start) {
            this.bias = bias;
            this.path = path;
            this.start = start;
        }

        public boolean exists() {
            if(primes!=null)
                return true;
            else
                return Files.exists(path);
        }

        public int size() {
            if(primes!=null)
                return primes.size();

            try {
                long size = Files.size(path);
                return (int) (size / 4);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        public long end() {
            return start + size();
        }

        public long putPrime(long prime) {
            try {
                if (this.primes == null)
                    this.primes = PrimeFile.append(path);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            int count = primes.putPrime(prime);
            return start + count;
        }

        public PrimeFile primes() {
            PrimeFile primes = this.primes;
            if(primes!=null)
                return primes;

            synchronized (this) {
                primes = this.primes();
                if(primes!=null)
                    return primes;

                try {
                    primes = PrimeFile.open(path);
                    this.primes = primes;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }

                return primes;
            }
        }

        public void close() throws IOException {
            PrimeFile primes = this.primes();
            if(primes!=null) {
                this.primes=null;
                primes.close();
            }
        }
    }

    final File directory;
    final List<FileEntry> entries = new ArrayList<>();

    PrimeFiles(File directory) {
        this.directory = directory;
    }

    @Override
    public void close() throws IOException {
        for (FileEntry entry : entries) {
            entry.close();
        }
    }

    public long putPrime(long prime) {
        int bias = (int)(prime>>33);
        FileEntry entry = getEntry(bias);
        return entry.putPrime(prime);
    }

    public long count() {
        int size = entries.size();
        return size >0 ? entries.get(size -1).end() : 0;
    }

    private FileEntry getEntry(int bias) {
        int size = entries.size();
        if(bias<size)
            return entries.get(bias);

        if(bias!= size)
            throw new IllegalStateException("bias overrun");

        String name = String.format("primes_%04x", bias);
        Path path = new File(directory, name).toPath();

        if(!Files.exists(path))
            return null;

        FileEntry entry = new FileEntry(path, bias, count());

        entries.add(entry);

        return entry;
    }

    @Override
    public long lastPrime() {
        return 0;
    }

    @Override
    public long forEachPrime(long index, LongPredicate until) {

        for(int i=0; true; ++i) {

        }

    }

}
