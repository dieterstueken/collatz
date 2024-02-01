package de.ditz.primes;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 21.06.20
 * Time: 20:13
 */
public class Dump {

    public static void main(String ... args) throws IOException {
        File file = new File(args.length > 0 ? args[0] : "primes.dat");

        try(PrimeFile primes = PrimeFile.open(file)) {
            System.out.format("total: %,16d\n", primes.size());

            try (PrintWriter out = new PrintWriter(new File("primes.log"))) {
                primes.process(Target.all(out::println));
            }
        }
    }
}
