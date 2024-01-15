package de.ditz.primes;

import java.util.function.LongConsumer;
import java.util.function.LongPredicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 17.06.20
 * Time: 20:42
 */
public interface Sequence {

    /**
     * Feed all numbers through a test until it fails.
     * @param until test to stop processing.
     * @return true if the stream was ended by until.test or was aborted otherwise.
     */
    boolean forEach(LongPredicate until);

    static LongPredicate forAll(LongConsumer consumer) {
        return i -> {
            consumer.accept(i);
            return false;
        };
    }

    static LongPredicate shift(LongPredicate until, long offset) {
        return offset==0 ? until : p -> until.test(p+offset);
    }

    static LongPredicate skip(LongPredicate until, long skip) {
        return p -> p>skip && until.test(p);
    }

    /**
     * Create a sequence shifted by an offset.
     * @param offset to add.
     * @return a sequence with a given offset.
     */
    default Sequence based(long offset) {
        return offset==0 ? this : until -> forEach(shift(until, offset));
    }

    /**
     * Create a sequence skipping entries.
     * @param skip limit to skip.
     * @return a skipping sequence
     */
    default Sequence skipped(long skip) {
        return until -> forEach(skip(until, skip));
    }
}

