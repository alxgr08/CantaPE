package pe.cantape;

final class ProfileName {
    private ProfileName() { }

    static String shortName(String fullName) {
        if (fullName == null) return "";
        String trimmed = fullName.trim().replaceAll("\\s+", " ");
        if (trimmed.isEmpty()) return "";
        String[] parts = trimmed.split(" ");
        if (parts.length == 1) return parts[0];
        // The common four-part Peruvian format is given name, given name, surname, surname.
        return parts[0] + " " + parts[parts.length >= 4 ? 2 : parts.length - 1];
    }
}
