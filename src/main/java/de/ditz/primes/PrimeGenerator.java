package de.ditz.primes;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ForkJoinTask;
import java.util.function.BooleanSupplier;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 05.06.20
 * Time: 01:05
 */
public class PrimeGenerator implements AutoCloseable {

    final PrimeWriter primes;

    PrimeGenerator(File file) throws IOException {
        primes = PrimeWriter.open(file.toPath(), true);

        int[] initials = {2,3,5,7,11,13,17,19};
        for(int i=0; i<initials.length; ++i) {
            if(primes.size()<=i)
                primes.addPrime(initials[i]);
        }
    }

    @Override
    public void close() throws IOException {
        primes.close();
    }

    public void generate(BooleanSupplier abort) {
        Sieves sieves = new Sieves(primes);

        ForkJoinTask<Runnable> pending = null;

        while(!abort.getAsBoolean()) {
            ForkJoinTask<Runnable> runner = sieves.nextTask().fork();

            if(pending!=null)
                pending.join().run();

            pending = runner;
        }

        if(pending!=null)
            pending.join().run();
    }

    public static void main(String ... args) throws IOException {
        File file = new File(args.length > 0 ? args[0] : "primes.dat");
        long limit = args.length > 1 ? Long.parseLong(args[1]) : 10000000;

        try(PrimeGenerator generator = new PrimeGenerator(file)) {

            BooleanSupplier abort = () -> {
                System.out.format("%,13d %,13d %13d\n",
                        generator.primes.size(),
                        generator.primes.lastPrime(),
                        generator.primes.buffers.size());
                return generator.primes.size() > limit;
            };

            generator.generate(abort);

            System.out.format("%,13d %,13d\n", limit, generator.primes.getPrime(limit));
        }
    }

}
