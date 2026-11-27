
package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Bus {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");

    private final String routeId;
    private final String origin;
    private final String destination;
    private final LocalDateTime departure;
    private final double basePrice;
    private final String busType;

    private final List<Seat> seats = new ArrayList<>();

    public Bus(
            String routeId,
            String origin,
            String destination,
            LocalDateTime departure,
            double basePrice,
            String busType
    ) {
        this.routeId = routeId;
        this.origin = origin;
        this.destination = destination;
        this.departure = departure;
        this.basePrice = basePrice;
        this.busType = busType;

        createSeats();
    }

    private void createSeats() {

        if (busType.equalsIgnoreCase("Seater")) {

            for (int row = 1; row <= 8; row++) {
                for (char col = 'A'; col <= 'D'; col++) {
                    seats.add(new Seat(row + String.valueOf(col)));
                }
            }

        } else {

            for (int row = 1; row <= 12; row++) {
                seats.add(new Seat(row + "L"));
                seats.add(new Seat(row + "U"));
            }
        }
    }

    public String getRouteId() {
        return routeId;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDateTime getDeparture() {
        return departure;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public String getBusType() {
        return busType;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public int getAvailableCount() {
        return (int) seats.stream()
                .filter(seat -> !seat.isBooked())
                .count();
    }

    public int getBookedCount() {
        return seats.size() - getAvailableCount();
    }

    public String getDepartureDisplay() {
        return departure.format(DISPLAY_FORMAT);
    }

    public Seat findSeat(String seatNumber) {

        for (Seat seat : seats) {
            if (seat.getSeatNumber()
                    .equalsIgnoreCase(seatNumber)) {
                return seat;
            }
        }

        return null;
    }

    public void displaySeatMap() {

        System.out.println(
                "\n========== SEAT MAP: " + routeId + " =========="
        );

        System.out.println(
                origin + " -> " + destination
                        + " | " + getDepartureDisplay()
        );

        System.out.println(
                "Available seat = seat number | Booked seat = XX\n"
        );

        if (busType.equalsIgnoreCase("Seater")) {

            for (int row = 1; row <= 8; row++) {

                System.out.printf("Row %-2d: ", row);

                for (char col = 'A'; col <= 'D'; col++) {

                    String number = row + String.valueOf(col);

                    printSeat(number);

                    if (col == 'B') {
                        System.out.print("    ");
                    } else {
                        System.out.print(" ");
                    }
                }

                System.out.println();
            }

        } else {

            displaySleeperLevel("LOWER BERTHS", "L");
            displaySleeperLevel("UPPER BERTHS", "U");
        }

        System.out.println(
                "\nAvailable: " + getAvailableCount()
                        + " | Booked: " + getBookedCount()
        );
    }

    private void displaySleeperLevel(String title, String suffix) {

        System.out.println("\n========== " + title + " ==========");

        System.out.println(
                "Left Side          AISLE          Right Side"
        );

        for (int row = 1; row <= 4; row++) {

            int first = (row - 1) * 3 + 1;
            int second = first + 1;
            int third = first + 2;

            System.out.printf(
                    "Row %-2d: [%-3s] [%-3s]       [%-3s]%n",
                    row,
                    seatLabel(first + suffix),
                    seatLabel(second + suffix),
                    seatLabel(third + suffix)
            );
        }
    }

    private void printSeat(String number) {
        System.out.printf("[%-2s]", seatLabel(number));
    }

    private String seatLabel(String number) {

        Seat seat = findSeat(number);

        if (seat != null && seat.isBooked()) {
            return "XX";
        }

        return number;
    }
}
