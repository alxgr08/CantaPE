package pe.cantape;

public final class ProfileCheck {
    public static void main(String[] args) {
        check("Diego Alexis Gil Rojas", "Diego Gil");
        check("  Diego   Alexis   Gil   Rojas  ", "Diego Gil");
        check("Diego Gil", "Diego Gil");
        check("Diego Alexis Gil", "Diego Gil");
        check("", "");
        if (!"S/ 2.50".equals(Money.format(250))) throw new AssertionError("Formato de importe");
        System.out.println("6 verificaciones correctas");
    }

    private static void check(String input, String expected) {
        String actual = ProfileName.shortName(input);
        if (!expected.equals(actual)) throw new AssertionError(input + " => " + actual);
    }
}
