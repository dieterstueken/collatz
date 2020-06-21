package de.ditz.primes;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.util.concurrent.CancellationException;
import java.util.function.LongPredicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 21.06.20
 * Time: 17:52
 */
class Until implements LongPredicate, AutoCloseable {

    private static final Signal SIGINT = new Signal("INT");

    final long limit;

    Signal signal = null;

    SignalHandler prev;

    Until(long limit) {
        this.limit = limit;
        this.prev = Signal.handle(SIGINT, this::raise);
    }

    @Override
    public boolean test(long value) {
        if (signal != null)
            throw new CancellationException(signal.getName());

        return value >= limit;
    }

    private void raise(Signal signal) {
        this.signal = signal;
    }

    @Override
    public void close() {
        Signal.handle(SIGINT, prev);
    }
}
