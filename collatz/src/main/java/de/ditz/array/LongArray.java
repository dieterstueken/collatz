package de.ditz.array;

import java.util.function.LongConsumer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 29.05.25
 * Time: 15:45
 */
public interface LongArray {

    long size();

    long getLong(int index);

    long setLong(int index, long value);

    void forEach(LongConsumer consumer);
}
