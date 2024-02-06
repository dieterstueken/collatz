package de.ditz.primes;

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
     * @param target to generate a result to stop the processing.
     * @return R result if the processor or null if the stream run to its end.
     */
    <R> R process(long start, Target<? extends R> target);

    default <R> R process(Target<? extends R> process) {
        return process(0, process);
    }

    default Sequence from(long start) {
        return new Sequence() {

            public <R> R process(Target<? extends R> target) {
                return Sequence.this.process(target.from(start));
            }

            @Override
            public <R> R process(long skip, Target<? extends R> target) {
                return Sequence.this.process(target.from(Math.max(skip, start)));
            }
        };
    }
}