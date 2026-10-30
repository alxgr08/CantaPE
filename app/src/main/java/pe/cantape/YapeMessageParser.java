package pe.cantape;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses visible notification text only. Never uses account data or phone numbers. */
final class YapeMessageParser {
    private static final Pattern RECEIVED = Pattern.compile(
            "\\b(recibiste|recibido|recibida|recibimos|te\\s+yapearon|te\\s+yapeo|te\\s+hicieron\\s+un\\s+yapeo|te\\s+envio|te\\s+transfirio|te\\s+enviaron|te\\s+ha\\s+enviado|te\\s+han\\s+enviado|te\\s+llego|has\\s+recibido|pago\\s+recibido|yape\\s+recibido)\\b");
    private static final Pattern SENT = Pattern.compile(
            "\\b(enviaste|has\\s+enviado|pagaste|pago\\s+realizado|transferiste|yapeaste|yape\\s+enviado|solicitaste|solicitud|rechazado|anulado)\\b");
    private static final Pattern AMOUNT = Pattern.compile(
            "(?i)(?:S\\s*/\\s*|(?<![A-Za-z])PEN\\s*|(?<![A-Za-z])SOLES?\\s*)([0-9][0-9., ]{0,14}[0-9]|[0-9])");

    private YapeMessageParser() { }

    /** Separate holder so a problem with this pattern can never break amount detection. */
    private static final class Sender {
        // Letters, dots and apostrophes only, so a phone number never becomes a "name".
        // No (?U) flag or \b here: Android's ICU regex rejects (?U), and \b differs around accents.
        static final Pattern SENDER = Pattern.compile(
                "(?i)(?:yape\\s*!?\\s*)?(\\p{L}[\\p{L}.' ]{0,59}?)\\s+te\\s+(?:envi[oóÓ]|yape[oóÓ]|ha\\s+enviado|han\\s+enviado|enviaron|yapearon|transfiri[oóÓ])(?!\\p{L})");
    }

    static Long receivedCents(String text) {
        if (text == null) return null;
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
        if (!RECEIVED.matcher(normalized).find() || SENT.matcher(normalized).find()) return null;
        Matcher matcher = AMOUNT.matcher(normalized);
        if (!matcher.find()) return null;
        Long amount = parseAmount(matcher.group(1));
        if (amount == null) return null;
        while (matcher.find()) {
            // A title and body may repeat the amount. Different amounts are ambiguous.
            if (!amount.equals(parseAmount(matcher.group(1)))) return null;
        }
        return amount;
    }

    /** Name shown before "te envió", e.g. "Juan P.", or "" when the notice has none. */
    static String senderName(String text) {
        if (text == null) return "";
        Matcher matcher = Sender.SENDER.matcher(text);
        if (!matcher.find()) return "";
        String name = matcher.group(1).trim().replaceAll("\\s+", " ");
        return name.equalsIgnoreCase("yape") ? "" : name;
    }

    private static Long parseAmount(String input) {
        String compact = input.replace(" ", "");
        int comma = compact.lastIndexOf(',');
        int dot = compact.lastIndexOf('.');
        int decimalAt = Math.max(comma, dot);
        String whole;
        String fractional = "";
        if (decimalAt >= 0 && compact.length() - decimalAt - 1 <= 2) {
            whole = compact.substring(0, decimalAt).replace(",", "").replace(".", "");
            fractional = compact.substring(decimalAt + 1);
        } else {
            whole = compact.replace(",", "").replace(".", "");
        }
        if (whole.isEmpty() || !whole.matches("[0-9]+") || !fractional.matches("[0-9]{0,2}")) return null;
        try {
            long soles = Long.parseLong(whole);
            long cents = fractional.isEmpty() ? 0 : Long.parseLong(fractional) * (fractional.length() == 1 ? 10 : 1);
            if (soles > 100_000 || (soles == 0 && cents == 0)) return null;
            return soles * 100 + cents;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
