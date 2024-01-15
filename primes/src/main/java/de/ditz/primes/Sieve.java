package de.ditz.primes;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.function.LongPredicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.06.20
 * Time: 20:15
 */
public class Sieve extends RecursiveTask<Sieve> {

    final PrimeFile primes;

    final long base;

    final LongPredicate until;

    ConcurrentLinkedQueue<BufferedSequence> unused;

    Sieve(PrimeFile primes, ConcurrentLinkedQueue<BufferedSequence> unused, long base, LongPredicate until) {
        this.primes = primes;
        this.base = base;
        this.until = until;
        this.unused = unused;
    }

    private Sieve next() {
        long next = base + BufferedSequence.SIZE * CompactSequence.SIZE;
        if(until.test(next))
            return null;

        return new Sieve(primes, unused, next, until);
    }

    public Sieve compute() {

        Sieve next = next();

        BufferedSequence sequence = unused.poll();
        if(sequence==null)
            sequence = BufferedSequence.create();

        sequence.sieve();


    }
}
