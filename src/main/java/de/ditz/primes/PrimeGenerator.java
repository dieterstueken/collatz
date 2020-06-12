package de.ditz.primes;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ForkJoinTask;
import java.util.function.BooleanSupplier;
import java.util.function.LongPredicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 05.06.20
 * Time: 01:05
 */
public class PrimeGenerator implements PrimeWriter, AutoCloseable {

    // 393615962
    //8589934583 0x1FFFFFFF7
    //8589934609 0x200000011

    final IntFile primes;

    PrimeGenerator(File file) throws IOException {
        primes = IntFile.create(file.toPath());

        int[] initials = {3,5,7,11,13,17,19};
        for(int i=0; i<initials.length; ++i) {
            if(primes.size()<=i)
                addPrime(initials[i]);
        }

        long test = getPrime(2);
    }

    public long size() {
        return primes.size();
    }

    @Override
    public long addPrime(long prime) {
        if(prime<3)
            throw new IllegalArgumentException("not a prime");

        int value = (int) (prime/2);
        if(2*(value&0xffffffffL)+1 != prime)
            throw new IllegalArgumentException("overflow");

        return primes.putInt(value);
    }
    
    public long getPrime(long index) {
        long prime = 0xffffffffL & primes.getInt((int) index);
        return 1+2*prime;
    }

    @Override
    public long lastPrime() {
        return primes.isEmpty() ? 0 : getPrime(primes.size()-1);
    }

    @Override
    public long forEachPrime(long index, LongPredicate until) {
        return primes.forEachInt((int) index, i -> until.test(1+2L*i));
    }

    @Override
    public void close() throws IOException {
        primes.close();
    }

    public void generate(BooleanSupplier abort) {
        Sieves sieves = new Sieves(this);

        ForkJoinTask<Runnable> pending = null;

         do {
            ForkJoinTask<Runnable> runner = sieves.nextTask().fork();

            if(pending!=null)
                pending.join().run();

            pending = runner;
            } while(!abort.getAsBoolean());

        if(pending!=null)
            pending.join().run();
    }

    public void generateS(BooleanSupplier abort) {
        Sieves sieves = new Sieves(this, 1);

         do {
            ForkJoinTask<Runnable> runner = sieves.nextTask();
            runner.invoke().run();
         } while(!abort.getAsBoolean());

    }

    public static void main(String ... args) throws IOException {
        File file = new File(args.length > 0 ? args[0] : "primes.dat");
        long limit = args.length > 1 ? Long.parseLong(args[1]) : 10000000000L;

        try(PrimeGenerator generator = new PrimeGenerator(file)) {

            BooleanSupplier abort = () -> {

                long count = generator.size();
                long range = Math.min(count, 1000);
                long lastPrime = generator.lastPrime();
                long last1 = generator.getPrime(count-range);
                int buffers = generator.primes.buffers.size();

                double f1 = (double) (lastPrime-3) / (count-1);
                double f2 = (double) (lastPrime-last1) / (range-1);

                System.out.format("%,13d %,13d %6d %6.3f %6.3f\n",
                        count, lastPrime, buffers, f1, f2);

                return generator.size() > limit;
            };

            generator.generate(abort);

            System.out.format("%,13d %,13d\n", limit, generator.getPrime(limit));
        }
    }

}
