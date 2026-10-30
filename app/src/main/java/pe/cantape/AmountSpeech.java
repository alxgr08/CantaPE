package pe.cantape;

final class AmountSpeech {
    static final int MODE_NOTICE = 0;
    static final int MODE_AMOUNT = 1;
    static final int MODE_FULL = 2;

    private AmountSpeech() { }

    static String announcement(int mode, long cents, String sender) {
        switch (mode) {
            case MODE_NOTICE:
                return "Yape confirmado";
            case MODE_FULL:
                if (sender != null && !sender.trim().isEmpty()) {
                    return "Yape confirmado de " + sender.trim() + ", por " + forCents(cents);
                }
                return confirmation(cents);
            default:
                return confirmation(cents);
        }
    }

    static String forCents(long totalCents) {
        if (totalCents <= 0) throw new IllegalArgumentException("El monto debe ser positivo");
        long soles = totalCents / 100;
        long cents = totalCents % 100;
        if (soles == 0) return cents + (cents == 1 ? " céntimo" : " céntimos");
        String result = soles + (soles == 1 ? " sol" : " soles");
        return cents == 0 ? result : result + " y " + cents + (cents == 1 ? " céntimo" : " céntimos");
    }

    static String confirmation(long totalCents) {
        return "Yape confirmado por " + forCents(totalCents);
    }
}
