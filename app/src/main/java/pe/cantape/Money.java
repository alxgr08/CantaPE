package pe.cantape;

import java.util.Locale;

final class Money {
    private Money() { }
    static String format(long cents) {
        return String.format(Locale.ROOT, "S/ %,d.%02d", cents / 100, cents % 100);
    }
}
