package de.ditz.primes;

import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 17.06.20
 * Time: 20:42
 */
public interface Sequence {

    /**
     * Feed all numbers through a processor until it returns a result.
     *
     * @param start to suppress all values < start.
     * @param process to generate a result to stop the processing.
     * @return R result if the processor or null if the stream run to its end.
     */
    <R> R process(long start, LongFunction<? extends R> process);

    default <R> R process(LongFunction<? extends R> process) {
        return process(0, process);
    }

    static LongFunction<Boolean> until(LongPredicate process) {
        return p -> process.test(p) ? true : null;
    }

    static LongFunction<Boolean> all(LongConsumer process) {
        return p -> {process.accept(p); return null;};
    }

    static <R> LongFunction<R> shift(LongFunction<R> process, long offset) {
        return offset==0 ? process : p -> process.apply(p+offset);
    }

    static <R> LongFunction<R> from(LongFunction<R> process, long start) {
        return start<0 ? process : p -> p<start ? null : process.apply(p);
    }
}