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

    boolean forEachUntil(long skip, LongPredicate until);

    default boolean forEachUntil(LongPredicate until) {
        return forEachUntil(0, until);
    }

    default void forEach(long skip, LongConsumer consumer) {
        forEachUntil(skip, i -> {consumer.accept(i); return false;});
    }

    default void forEach(LongConsumer consumer) {
        forEachUntil(0, i -> {consumer.accept(i); return false;});
    }

    default Sequence based(long base) {
        if(base==0)
            return this;
        else
            return (skip, until) -> forEachUntil(skip - base, i -> until.test(base+i));
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
        return (skip, until) -> {
            for(long pos=skip>0?skip/30:0; pos<buffer.limit(); ++pos) {
                int msk = 0xff & buffer.get((int)pos);
                if(sequence(msk).based(30*pos).forEachUntil(skip, until))
                    return true;
            }
            return false;
        };
    }

    static void main(String ... args) {
        for(int i=0; i<SEQUENCES.size(); ++i){
            System.out.format("%02x:", i);
            sequence(i).forEach(0, n->System.out.format(" %d", n));
            System.out.println();
        }
    }
}

