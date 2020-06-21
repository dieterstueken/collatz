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

    boolean forEach(long base, long skip, LongPredicate until);

    default boolean forEach(long skip, LongPredicate until) {
        return forEach(0, skip, until);
    }

    default boolean forEach(LongPredicate until) {
        return forEach(0, 0, until);
    }

    default Sequence based(long base) {
        return (b, s, u) -> forEach(base+b, s, p->u.test(base+p));
    }

    default Sequence skipped(long skip) {
        return (b,s,u) -> forEach(b, Math.max(s, skip), u);
    }

    static LongPredicate each(LongConsumer consumer) {
        return i -> {consumer.accept(i); return false;};
    }

    List<Sequence> SEQUENCES = IntStream.range(0, 256).mapToObj(Sequences::compact)
            .collect(Collectors.toUnmodifiableList());

    static Sequence sequence(int mask) {
        return SEQUENCES.get(mask);
    }

    static Sequence base30() {
        return sequence(0xff);
    }

    static Sequence compact(ByteBuffer buffer) {
        return (base, skip, until) -> {

            for(long pos=skip<base?0:(skip-base)/30; pos<buffer.limit(); ++pos) {
                int msk = 0xff & buffer.get((int)pos);
                if(sequence(msk).forEach(30*pos + base, skip, until))
                    return true;
            }
            return false;
        };
    }

    static void main(String ... args) {
        for(int i=0; i<SEQUENCES.size(); ++i){
            System.out.format("%02x:", i);
            sequence(i).forEach(0, 0, each(n->System.out.format(" %d", n)));
            System.out.println();
        }
    }
}

