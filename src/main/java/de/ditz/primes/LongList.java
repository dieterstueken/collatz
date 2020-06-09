package de.ditz.primes;

import java.io.IOException;
import java.util.function.LongPredicate;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  09.06.2020 12:58
 * modified by: $
 * modified on: $
 */
public interface LongList extends AutoCloseable {

    int bits();

    long size();

    int get(long index);

    boolean forEach(LongPredicate until);

    void add(long value);

    void flush();

    void close() throws IOException;
}
