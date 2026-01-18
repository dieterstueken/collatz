package de.ditz.array;

import java.util.Arrays;
import java.util.function.LongConsumer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 10.07.25
 * Time: 14:57
 */
public class GrowingLongArray implements LongArray {

    private long values[];
    private int size = 0;

    public GrowingLongArray(int initialLen) {
        values = new long[initialLen];
    }

    public GrowingLongArray() {
        this(1024);
    }
    @Override
    public long size() {
        return size;
    }

    @Override
    public long getLong(int index) {
        return index<size ? values[index] : 0L;
    }

    private long[] grow(int minCapacity) {
        if(values.length<=minCapacity) {
            int newSize = minCapacity;
            newSize += newSize / 2;
            values = Arrays.copyOf(values, newSize);
        }
        return values;
    }

    @Override
    public long setLong(int index, long value) {
        long prevValue = 0;

        long[] values = grow(index);
        if(index<size) {
            prevValue = values[index];
            values[index] = value;
        } else if(value!=0) {
            size = index+1;
            values[index] = value;
        }

        return prevValue;
    }

    @Override
    public void forEach(LongConsumer consumer) {
        for(int i=0; i<size; ++i)
            consumer.accept(values[i]);
    }
}
