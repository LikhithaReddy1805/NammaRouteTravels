package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class InputValidator {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static String readNonEmpty(
            Scanner sc, String prompt
    ) {

        while (true) {

            System.out.print(prompt);

            String value = sc.nextLine().trim();

            if (!value.isEmpty() && !value.contains("|")) {
                return value;
            }

            System.out.println(
                    "Please enter a valid value. "
                            + "The field cannot be empty "
                            + "or contain '|'."
            );
        }
    }

    public static String readRouteId(Scanner sc) {

        while (true) {

            String value =
                    readNonEmpty(
                            sc,
                            "Enter Route ID (e.g. NR-101): "
                    ).toUpperCase();

            if (value.matches("NR-[A-Z0-9]+")) {
                return value;
            }

            System.out.println(
                    "Invalid Route ID. Use the format NR-101."
            );
        }
    }

    public static int readInt(
            Scanner sc,
            String prompt,
            int min,
            int max
    ) {

        while (true) {

            System.out.print(prompt);

            String value = sc.nextLine().trim();

            try {

                int number = Integer.parseInt(value);

                if (number >= min && number <= max) {
                    return number;
                }

            } catch (NumberFormatException ignored) {
            }

            System.out.println(
                    "Enter a whole number between "
                            + min + " and " + max + "."
            );
        }
    }

    public static double readPositiveDouble(
            Scanner sc, String prompt
    ) {

        while (true) {

            System.out.print(prompt);

            try {

                double value =
                        Double.parseDouble(
                                sc.nextLine().trim()
                        );

                if (Double.isFinite(value) && value > 0) {
                    return value;
                }

            } catch (NumberFormatException ignored) {
            }

            System.out.println(
                    "Enter a valid amount greater than zero."
            );
        }
    }

    public static LocalDateTime readFutureDateTime(
            Scanner sc
    ) {

        while (true) {

            System.out.print(
                    "Departure date and time "
                            + "(yyyy-MM-dd HH:mm): "
            );

            try {

                LocalDateTime value =
                        LocalDateTime.parse(
                                sc.nextLine().trim(),
                                DATE_FORMAT
                        );

                if (value.isAfter(LocalDateTime.now())) {
                    return value;
                }

                System.out.println(
                        "Departure must be in the future."
                );

            } catch (DateTimeParseException ex) {

                System.out.println(
                        "Invalid date/time. "
                                + "Example: 2026-11-27 21:30"
                );
            }
        }
    }

    public static String readGender(Scanner sc) {

        while (true) {

            String value =
                    readNonEmpty(
                            sc,
                            "Gender (M/F/Other): "
                    );

            if (value.equalsIgnoreCase("M")) {
                return "M";
            }

            if (value.equalsIgnoreCase("F")) {
                return "F";
            }

            if (value.equalsIgnoreCase("Other")) {
                return "Other";
            }

            System.out.println(
                    "Enter M, F or Other."
            );
        }
    }
}