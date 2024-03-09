package de.ditz.primes.main;

import de.ditz.primes.ByteSequence;
import de.ditz.primes.PrimeFile;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 21.06.20
 * Time: 20:13
 */
public class Count {

    final PrimeFile primes;

    int j=1;
    long limit = 8;

    long pos;
    long count = 3;

    public Count(PrimeFile primes) {
        this.primes = primes;
    }

    void count(ByteSequence seq) {
        pos += ByteSequence.SIZE;
        count += seq.size();
        if(count>limit) {
            stat();
            limit *= 2;
        }
    }

    void stat() {
        long prime = primes.process(pos, p->p);
        double pn =  count * Math.log(count);
        System.out.format("%,23d %,23d %,23.1f %5.1f\n", count, prime, pn, 100*(prime/pn-1));
    }

    public void run() {
        pos = 0;
        count = 3;
        limit = 8;

        primes.sequences().forEach(this::count);

        stat();
    }

    public static void main(String ... args) throws IOException {
        File file = new File(args.length > 0 ? args[0] : "primes.dat");
        PrimeFile.BLOCK = 1<<24;
        try(PrimeFile primes = PrimeFile.open(file)) {
            System.out.format("total: %,16d\n", primes.size());
            Count count = new Count(primes);
            count.run();
        }
    }
}
