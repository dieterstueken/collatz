package de.ditz.array;

import java.util.function.IntConsumer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 29.05.25
 * Time: 15:49
 */
public class IndirectIntArray implements IntArray {

    static final long FRAGMENTS = 256;

    final long base;

    final IntArray[] fragments = new IntArray[(int)FRAGMENTS];

    IndirectIntArray(long base) {
        this.base = base;
    }

    IndirectIntArray(IntArray array) {
         this.base = array.size();
         fragments[0] = array;
    }

    @Override
    public long size() {
        return FRAGMENTS*base;
    }

    private int fragIndex(long index) {
        if(index<0 || index>=size())
            throw new IndexOutOfBoundsException("index: " + index);
        return (int) (index/base);
    }

    private IntArray fragment(long index) {
        int i = fragIndex(index);
        return fragments[i];
    }

    @Override
    public int getInt(long index) {
        IntArray fragment = fragment(index);
        return fragment==null ? 0 : fragment.getInt(index%FRAGMENTS);
    }

    @Override
    public int setInt(long index, int value) {
        int i = fragIndex(index);
        IntArray fragment = fragments[i];
        if(fragment==null) {
            // avoid allocation new array
            if(value==0)
                return 0;

            synchronized (fragments) {
                fragment = fragments[i];
                if(fragment==null)
                    fragments[i] = fragment = allocate(base);
            }
        }

        return fragment.setInt(index%base, value);
    }

    public void forEach(IntConsumer consumer) {
        for (IntArray fragment : fragments) {
            if(fragment!=null)
                fragment.forEach(consumer);
        }

    }

    public static IntArray allocate(long size) {
        if(size <= 1<<16)
            return new DirectShortArray((int)size);

        long base = 1<<16;
        while(size>base*FRAGMENTS)
            base *= FRAGMENTS;

        return new IndirectIntArray(base);
    }
}
