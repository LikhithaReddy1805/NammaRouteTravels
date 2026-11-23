
package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class InputValidator {

    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty()
                && !name.contains("|");
    }

    public static boolean isValidAge(int age) {
        return age > 0 && age <= 120;
    }

    public static boolean isValidGender(String gender) {
        return gender.equalsIgnoreCase("M")
                || gender.equalsIgnoreCase("F")
                || gender.equalsIgnoreCase("Other");
    }

    public static boolean isValidPrice(double price) {
        return price > 0 && Double.isFinite(price);
    }

    public static boolean isValidRouteId(String routeId) {
        return routeId != null
                && routeId.matches("NR-[A-Za-z0-9]+");
    }

    public static boolean isValidDeparture(String departure) {
        try {
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime.parse(departure, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
