package pe.cantape;

public final class ParserCheck {
    public static void main(String[] args) {
        check("Recibiste un Yape de S/ 2.00", 200L);
        check("¡Te yapearon S/ 2.50!", 250L);
        check("Te llegó S/ 0.30", 30L);
        check("Recibiste un Yape de S/ 1,234.56", 123456L);
        check("Recibiste S/ 1.234,56", 123456L);
        check("Te han enviado S/ 0.01", 1L);
        check("María te yapeó S/ 2.00", 200L);
        check("Te hicieron un yapeo por S/ 2.50", 250L);
        check("S/ 2.00 Recibiste un Yape de S/ 2.00", 200L);
        check("Yape! Juan P. te envió un pago por S/ 0.1. El cód. de seguridad es: 427", 10L);
        check("Confirmación de Pago Yape! Ana te envió un pago por S/ 25", 2500L);
        check("Ana te ha enviado S/ 3.00", 300L);
        check("Enviaste un Yape de S/ 2.50", null);
        check("Solicitaste S/ 2.50", null);
        check("Tu saldo es S/ 2.50", null);
        check("Recibiste S/ 2.50. Saldo S/ 20.00", null);
        sender("Yape! Juan P. te envió un pago por S/ 0.1. El cód. de seguridad es: 427", "Juan P.");
        sender("María José Quispe te yapeó S/ 5.00", "María José Quispe");
        sender("Yape! 987654321 te envió un pago por S/ 1", "");
        sender("Recibiste un Yape de S/ 2.00", "");
        assertSpeech(200, "Yape confirmado por 2 soles");
        assertSpeech(250, "Yape confirmado por 2 soles y 50 céntimos");
        assertSpeech(30, "Yape confirmado por 30 céntimos");
        System.out.println("23 verificaciones correctas");
    }

    private static void check(String message, Long expected) {
        Long actual = YapeMessageParser.receivedCents(message);
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + ": esperado=" + expected + ", obtenido=" + actual);
        }
    }

    private static void sender(String message, String expected) {
        String actual = YapeMessageParser.senderName(message);
        if (!expected.equals(actual)) throw new AssertionError(message + ": nombre=" + actual);
    }

    private static void assertSpeech(long cents, String expected) {
        String actual = AmountSpeech.confirmation(cents);
        if (!expected.equals(actual)) throw new AssertionError(actual);
    }
}
