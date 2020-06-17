package de.ditz.primes.compressed;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.LongConsumer;
import java.util.function.LongPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 17.06.20
 * Time: 20:42
 */
public interface Sequence {

    boolean forEachUntil(long seek, LongPredicate until);

    default boolean forEachUntil(LongPredicate until) {
        return forEachUntil(0, until);
    }

    default void forEach(long seek, LongConsumer consumer) {
        forEachUntil(seek, i -> {consumer.accept(i); return false;});
    }

    default Sequence seek(long n) {
        if(n>0)
            return (seek, until) -> forEachUntil(seek + n, until);
        else
            return this;
    }

    default Sequence based(long base) {
        if(base==0)
            return this;
        else
            return (seek, until) -> forEachUntil(seek - base, i -> until.test(base+i));
    }

    List<Sequence> SEQUENCES = IntStream.range(0, 256).mapToObj(Sequences::compact)
            .collect(Collectors.toUnmodifiableList());

    static Sequence sequence(int mask) {
        return SEQUENCES.get(mask);
    }

    static Sequence base() {
        return sequence(0xff);
    }

    static Sequence compact(ByteBuffer buffer) {
        return (seek, until) -> {
            for(long pos=seek/30; pos<buffer.limit(); ++pos) {
                int msk = 0xff & buffer.get((int)pos);
                if(sequence(msk).based(30*pos).forEachUntil(seek, until))
                    return true;
            }
            return false;
        };
    }

}

