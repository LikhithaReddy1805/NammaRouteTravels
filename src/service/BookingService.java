
package service;

import model.Booking;
import model.Bus;
import model.Passenger;
import model.Seat;
import util.InputValidator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

public class BookingService {

    private static final Path FILE = Path.of("data", "bookings.csv");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final List<Booking> bookings = new ArrayList<>();
    private final WaitlistService waitlistService = new WaitlistService();

    public BookingService(List<Bus> buses) {
        loadBookings(buses);
    }

    public void bookTickets(List<Bus> buses, Scanner sc) {

        String routeId = InputValidator.readRouteId(sc);
        Bus bus = findBus(buses, routeId);

        if (bus == null) {
            System.out.println("Bus route not found.");
            return;
        }

        if (!bus.getDeparture().isAfter(java.time.LocalDateTime.now())) {
            System.out.println("Bookings are not available for a past departure.");
            return;
        }

        if (bus.getAvailableCount() == 0) {
            joinWaitlist(bus, sc);
            return;
        }

        bus.displaySeatMap();

        int count = InputValidator.readInt(
                sc, "How many seats do you want to book? ",
                1, bus.getAvailableCount()
        );

        List<String> requestedSeats = new ArrayList<>();
        Set<String> uniqueSeats = new HashSet<>();

        for (int i = 1; i <= count; i++) {

            String seatNumber = InputValidator.readNonEmpty(
                    sc, "Enter seat number " + i + ": "
            ).toUpperCase();

            Seat seat = bus.findSeat(seatNumber);

            if (seat == null || seat.isBooked()
                    || !uniqueSeats.add(seatNumber)) {
                System.out.println(
                        "Booking stopped: invalid, already booked, "
                                + "or duplicate seat " + seatNumber + "."
                );
                return;
            }

            requestedSeats.add(seatNumber);
        }

        List<Passenger> passengers = new ArrayList<>();

        for (String seatNumber : requestedSeats) {

            System.out.println("\nPassenger details for seat " + seatNumber);

            String name = InputValidator.readNonEmpty(sc, "Passenger name: ");
            int age = InputValidator.readInt(sc, "Passenger age: ", 1, 120);
            String gender = InputValidator.readGender(sc);

            passengers.add(new Passenger(
                    name, age, gender, seatNumber, 0.0
            ));
        }

        String safetyMessage = getCoSeatSafetyError(bus, passengers);

        if (safetyMessage != null) {
            System.out.println(safetyMessage);
            System.out.println("No seats have been booked.");
            return;
        }

        int simulatedBooked = bus.getBookedCount();
        List<Passenger> pricedPassengers = new ArrayList<>();

        System.out.println("\n========== FARE SUMMARY ==========");

        for (Passenger passenger : passengers) {

            double fare = calculateFare(
                    bus, passenger.getSeatNumber(), simulatedBooked
            );

            System.out.printf(
                    "%s - %s: Rs. %.2f%s%n",
                    passenger.getName(),
                    passenger.getSeatNumber(),
                    fare,
                    isSurgeActive(bus, simulatedBooked)
                            ? " (20% surge applied)" : ""
            );

            pricedPassengers.add(new Passenger(
                    passenger.getName(),
                    passenger.getAge(),
                    passenger.getGender(),
                    passenger.getSeatNumber(),
                    fare
            ));

            simulatedBooked++;
        }

        double totalFare = pricedPassengers.stream()
                .mapToDouble(Passenger::getFare)
                .sum();

        System.out.printf("Total fare: Rs. %.2f%n", totalFare);

        String confirm = InputValidator.readNonEmpty(
                sc, "Confirm booking? (Y/N): "
        );

        if (!confirm.equalsIgnoreCase("Y")) {
            System.out.println("Booking cancelled. No seats were reserved.");
            return;
        }

        String paymentMethod = choosePaymentMethod(sc);

        Booking booking = new Booking(
                generatePnr(),
                routeId,
                paymentMethod,
                pricedPassengers
        );

        for (String seatNumber : requestedSeats) {
            bus.findSeat(seatNumber).setBooked(true);
        }

        bookings.add(booking);
        saveBookings();
        printReceipt(booking, bus);
    }

    private void joinWaitlist(Bus bus, Scanner sc) {

        System.out.println("This bus is fully booked.");

        if (waitlistService.remainingSlots(bus.getRouteId()) == 0) {
            System.out.println("The waitlist is full (maximum 5 passengers).");
            return;
        }

        String join = InputValidator.readNonEmpty(
                sc, "Would you like to join the waitlist? (Y/N): "
        );

        if (!join.equalsIgnoreCase("Y")) {
            return;
        }

        String name = InputValidator.readNonEmpty(sc, "Passenger name: ");
        int age = InputValidator.readInt(sc, "Passenger age: ", 1, 120);
        String gender = InputValidator.readGender(sc);

        WaitlistEntry entry = new WaitlistEntry(
                UUID.randomUUID().toString(),
                bus.getRouteId(),
                name,
                age,
                gender,
                System.currentTimeMillis()
        );

        if (waitlistService.add(entry)) {
            System.out.println(
                    "Added to waitlist. Your position is "
                            + waitlistService.positionOf(
                                    entry.getId(), bus.getRouteId()
                            )
                            + ". The queue is saved."
            );
        } else {
            System.out.println("The waitlist is full (maximum 5 passengers).");
        }
    }

    public void displayWaitlist(List<Bus> buses, Scanner sc) {

        String routeId = InputValidator.readRouteId(sc);
        Bus bus = findBus(buses, routeId);

        if (bus == null) {
            System.out.println("Bus route not found.");
            return;
        }

        List<WaitlistEntry> entries =
                waitlistService.getEntriesForRoute(routeId);

        System.out.println("\n========== WAITLIST: " + routeId + " ==========");

        if (entries.isEmpty()) {
            System.out.println("No passengers are currently waitlisted.");
            return;
        }

        int position = 1;

        for (WaitlistEntry entry : entries) {
            System.out.printf(
                    "%d. %-22s Age: %-3d Gender: %s%n",
                    position++,
                    entry.getName(),
                    entry.getAge(),
                    entry.getGender()
            );
        }
    }

    public void cancelTickets(List<Bus> buses, Scanner sc) {

        String bookingNumber = normalizeBookingNumber(
                InputValidator.readNonEmpty(sc, "Enter booking number: ")
        );

        Booking booking = findBooking(bookingNumber);

        if (booking == null) {
            System.out.println("No active booking found for this number.");
            return;
        }

        Bus bus = findBus(buses, booking.getRouteId());

        if (bus == null) {
            System.out.println("The bus for this booking could not be found.");
            return;
        }

        System.out.println("\nPassengers under " + bookingNumber + ":");
        printPassengerRows(booking.getPassengers());

        System.out.println("\n1. Cancel complete booking");
        System.out.println("2. Cancel selected seats");

        int choice = InputValidator.readInt(sc, "Choose option: ", 1, 2);

        List<Passenger> cancelled = new ArrayList<>();

        if (choice == 1) {
            cancelled.addAll(booking.getPassengers());
        } else {

            int count = InputValidator.readInt(
                    sc,
                    "How many seats do you want to cancel? ",
                    1,
                    booking.getPassengers().size()
            );

            Set<String> selected = new HashSet<>();

            for (int i = 1; i <= count; i++) {

                String seatNumber = InputValidator.readNonEmpty(
                        sc, "Enter seat number to cancel: "
                ).toUpperCase();

                boolean belongs = booking.getPassengers().stream()
                        .anyMatch(p -> p.getSeatNumber()
                                .equalsIgnoreCase(seatNumber));

                if (!belongs || !selected.add(seatNumber)) {
                    System.out.println(
                            "Invalid or duplicate seat. Cancellation stopped; "
                                    + "no changes made."
                    );
                    return;
                }
            }

            for (Passenger passenger : booking.getPassengers()) {
                if (selected.contains(
                        passenger.getSeatNumber().toUpperCase())) {
                    cancelled.add(passenger);
                }
            }
        }

        double cancelledAmount = cancelled.stream()
                .mapToDouble(Passenger::getFare)
                .sum();

        double cancellationCharge = roundMoney(cancelledAmount * 0.10);
        double refund = roundMoney(cancelledAmount - cancellationCharge);

        List<String> releasedSeats = new ArrayList<>();

        for (Passenger passenger : cancelled) {

            Seat seat = bus.findSeat(passenger.getSeatNumber());

            if (seat != null) {
                seat.setBooked(false);
                releasedSeats.add(passenger.getSeatNumber());
            }

            booking.getPassengers().remove(passenger);
        }

        if (booking.getPassengers().isEmpty()) {
            bookings.remove(booking);
        }

        saveBookings();

        for (String releasedSeat : releasedSeats) {
            promoteWaitlistedPassenger(bus, releasedSeat);
        }

        saveBookings();

        System.out.println("\n========== CANCELLATION CONFIRMATION ==========");
        System.out.println("Booking number: " + bookingNumber);

        System.out.println(
                "Cancelled seats: "
                        + cancelled.stream()
                        .map(Passenger::getSeatNumber)
                        .toList()
        );

        System.out.printf(
                "Cancelled ticket amount: Rs. %.2f%n", cancelledAmount
        );

        System.out.printf(
                "Cancellation charge (10%%): Rs. %.2f%n",
                cancellationCharge
        );

        System.out.printf("Refund amount (90%%): Rs. %.2f%n", refund);

        System.out.println(
                "Payment method on original booking: "
                        + booking.getPaymentMethod()
        );

        System.out.println(
                "Released seats were checked against the waitlist. "
                        + "Eligible passengers were automatically confirmed."
        );
    }

    public void generateManifest(List<Bus> buses, Scanner sc) {

        String routeId = InputValidator.readRouteId(sc);
        Bus bus = findBus(buses, routeId);

        if (bus == null) {
            System.out.println("Bus route not found.");
            return;
        }

        List<ManifestRow> rows = new ArrayList<>();

        for (Booking booking : bookings) {

            if (booking.getRouteId().equalsIgnoreCase(routeId)) {

                for (Passenger passenger : booking.getPassengers()) {
                    rows.add(new ManifestRow(
                            booking.getPnr(), passenger
                    ));
                }
            }
        }

        rows.sort(
                Comparator.comparingInt(
                        (ManifestRow row) ->
                                seatRow(row.passenger().getSeatNumber())
                ).thenComparing(
                        row -> seatSuffix(row.passenger().getSeatNumber())
                )
        );

        System.out.println(
                "\n========== PASSENGER MANIFEST: " + routeId + " =========="
        );

        System.out.println(
                bus.getOrigin() + " -> " + bus.getDestination()
                        + " | " + bus.getDepartureDisplay()
        );

        System.out.printf(
                "%-8s %-12s %-22s %-5s %-8s%n",
                "Seat", "Booking No.", "Passenger Name", "Age", "Gender"
        );

        System.out.println(
                "---------------------------------------------------------------"
        );

        for (ManifestRow row : rows) {

            Passenger p = row.passenger();

            System.out.printf(
                    "%-8s %-12s %-22s %-5d %-8s%n",
                    p.getSeatNumber(),
                    row.bookingNumber(),
                    p.getName(),
                    p.getAge(),
                    p.getGender()
            );
        }

        System.out.println(
                "---------------------------------------------------------------"
        );

        System.out.println(
                "Total Booked: " + rows.size()
                        + " | Available: " + bus.getAvailableCount()
        );

        if (rows.isEmpty()) {
            System.out.println("No active passengers booked for this route.");
        }
    }

    private void promoteWaitlistedPassenger(Bus bus, String seatNumber) {

        List<WaitlistEntry> entries =
                waitlistService.getEntriesForRoute(bus.getRouteId());

        for (WaitlistEntry entry : entries) {

            Passenger candidate = new Passenger(
                    entry.getName(),
                    entry.getAge(),
                    entry.getGender(),
                    seatNumber,
                    0.0
            );

            String safetyError =
                    getCoSeatSafetyError(bus, List.of(candidate));

            if (safetyError != null) {
                continue;
            }

            double fare = calculateFare(
                    bus, seatNumber, bus.getBookedCount()
            );

            Passenger confirmedPassenger = new Passenger(
                    entry.getName(),
                    entry.getAge(),
                    entry.getGender(),
                    seatNumber,
                    fare
            );

            String bookingNumber = generatePnr();

            Booking promotedBooking = new Booking(
                    bookingNumber,
                    bus.getRouteId(),
                    "Waitlist Auto-Promotion",
                    List.of(confirmedPassenger)
            );

            bus.findSeat(seatNumber).setBooked(true);
            bookings.add(promotedBooking);
            waitlistService.remove(entry.getId());

            System.out.println("\n========== WAITLIST AUTO-PROMOTION ==========");
            System.out.println("Passenger: " + entry.getName());
            System.out.println("Seat: " + seatNumber);
System.out.println("New booking number: " + bookingNumber);
            System.out.printf("Ticket fare: Rs. %.2f%n", fare);

            System.out.println("Passenger removed from the waitlist.");
            return;
        }
    }

    private String getCoSeatSafetyError(
            Bus bus, List<Passenger> requestedPassengers
    ) {

        Map<String, String> existingGenders = new LinkedHashMap<>();

        for (Booking booking : bookings) {

            if (booking.getRouteId().equalsIgnoreCase(bus.getRouteId())) {

                for (Passenger passenger : booking.getPassengers()) {
                    existingGenders.put(
                            passenger.getSeatNumber().toUpperCase(),
                            normalizeGender(passenger.getGender())
                    );
                }
            }
        }

        Map<String, String> requestedGenders = new LinkedHashMap<>();

        for (Passenger passenger : requestedPassengers) {
            requestedGenders.put(
                    passenger.getSeatNumber().toUpperCase(),
                    normalizeGender(passenger.getGender())
            );
        }

        for (Passenger passenger : requestedPassengers) {

            String seat = passenger.getSeatNumber().toUpperCase();
            String adjacent = adjacentSeat(bus, seat);

            if (adjacent == null) {
                continue;
            }

            String otherGender = requestedGenders.get(adjacent);

            if (otherGender != null) {
                continue;
            }

            otherGender = existingGenders.get(adjacent);

            if (otherGender == null) {
                continue;
            }

            String passengerGender = normalizeGender(passenger.getGender());

            if (otherGender.equals("F") && !passengerGender.equals("F")) {
                return "Sorry, seat " + seat
                        + " is reserved for a female co-passenger. "
                        + "Please choose another seat.";
            }

            if (passengerGender.equals("F") && otherGender.equals("M")) {
                return "Sorry, seat " + seat
                        + " is next to a male passenger. "
                        + "Please choose another seat.";
            }
        }

        return null;
    }

    private String adjacentSeat(Bus bus, String seatNumber) {

        String numberPart = seatNumber.replaceAll("[^0-9]", "");
        String suffix = seatNumber.replaceAll("[0-9]", "");

        if (numberPart.isEmpty()) {
            return null;
        }

        int row = Integer.parseInt(numberPart);

        if (bus.getBusType().equalsIgnoreCase("Seater")) {

            if (suffix.equalsIgnoreCase("A")) return row + "B";
            if (suffix.equalsIgnoreCase("B")) return row + "A";
            if (suffix.equalsIgnoreCase("C")) return row + "D";
            if (suffix.equalsIgnoreCase("D")) return row + "C";

            return null;
        }

        int position = (row - 1) % 3;

        if (position == 0) return (row + 1) + suffix;
        if (position == 1) return (row - 1) + suffix;

        return null;
    }

    private String normalizeGender(String gender) {

        if (gender == null) {
            return "";
        }

        String value = gender.trim().toUpperCase();

        if (value.equals("F") || value.equals("FEMALE")) return "F";
        if (value.equals("M") || value.equals("MALE")) return "M";

        return "OTHER";
    }

    private boolean isSurgeActive(Bus bus, int bookedCount) {

        int threshold = (int) Math.ceil(bus.getSeats().size() * 0.70);
        return bookedCount >= threshold;
    }

    private double calculateFare(
            Bus bus, String seatNumber, int bookedCount
    ) {

        double normalFare = bus.getBasePrice();

        if (bus.getBusType().toLowerCase().contains("sleeper")
                && seatNumber.toUpperCase().endsWith("U")) {
            normalFare *= 0.95;
        }

        if (isSurgeActive(bus, bookedCount)) {
            normalFare *= 1.20;
        }

        return roundMoney(normalFare);
    }

    private String choosePaymentMethod(Scanner sc) {

        System.out.println("\nPayment method:");
        System.out.println("1. Cash");
        System.out.println("2. Card");
        System.out.println("3. UPI");

        return switch (InputValidator.readInt(
                sc, "Choose payment method: ", 1, 3
        )) {
            case 1 -> "Cash";
            case 2 -> "Card";
            default -> "UPI";
        };
    }

    private void printReceipt(Booking booking, Bus bus) {

        System.out.println(
                "\n================ BOOKING RECEIPT ================"
        );

System.out.println("PNR: " + booking.getPnr());
        System.out.println(
                "Route: " + bus.getOrigin() + " -> " + bus.getDestination()
        );

        System.out.println("Route ID: " + bus.getRouteId());
        System.out.println("Departure: " + bus.getDepartureDisplay());
        System.out.println("Bus type: " + bus.getBusType());

        System.out.println(
                "Payment method: " + booking.getPaymentMethod()
        );

        System.out.println(
                "-------------------------------------------------"
        );

        printPassengerRows(booking.getPassengers());

        System.out.printf(
                "Total amount paid: Rs. %.2f%n",
                booking.getTotalFare()
        );

        System.out.println(
                "================================================="
        );
    }

    private void printPassengerRows(List<Passenger> passengers) {

        System.out.printf(
                "%-22s %-5s %-8s %-8s %-10s%n",
                "Passenger Name", "Age", "Gender", "Seat", "Fare"
        );

        for (Passenger p : passengers) {

            System.out.printf(
                    "%-22s %-5d %-8s %-8s Rs.%-8.2f%n",
                    p.getName(),
                    p.getAge(),
                    p.getGender(),
                    p.getSeatNumber(),
                    p.getFare()
            );
        }
    }

    private String generatePnr() {

        String bookingNumber;

        do {
            bookingNumber = String.valueOf(
                    10000 + RANDOM.nextInt(90000)
            );
        } while (findBooking(bookingNumber) != null);

        return bookingNumber;
    }

    private String normalizeBookingNumber(String value) {

        String number = value.trim().toUpperCase();

        if (number.startsWith("PNR-")) {
            number = number.substring(4);
        } else if (number.startsWith("PNR:")) {
            number = number.substring(4).trim();
        }

        return number;
    }

    private Booking findBooking(String bookingNumber) {

        String normalized = normalizeBookingNumber(bookingNumber);

        for (Booking booking : bookings) {
            if (normalizeBookingNumber(booking.getPnr())
                    .equalsIgnoreCase(normalized)) {
                return booking;
            }
        }

        return null;
    }

    private Bus findBus(List<Bus> buses, String routeId) {

        for (Bus bus : buses) {
            if (bus.getRouteId().equalsIgnoreCase(routeId)) {
                return bus;
            }
        }

        return null;
    }

    private void loadBookings(List<Bus> buses) {

        if (!Files.exists(FILE)) {
            return;
        }

        Map<String, List<String>> grouped = new LinkedHashMap<>();

        try {

            for (String line :
                    Files.readAllLines(FILE, StandardCharsets.UTF_8)) {

                if (line.isBlank()) {
                    continue;
                }

                String[] p = line.split("\\|", -1);

                if (p.length != 8 && p.length != 7 && p.length != 6) {
                    continue;
                }

                String bookingNumber = normalizeBookingNumber(p[0]);

                grouped.computeIfAbsent(
                        bookingNumber, key -> new ArrayList<>()
                ).add(line);
            }

            for (Map.Entry<String, List<String>> entry :
                    grouped.entrySet()) {

                List<Passenger> passengers = new ArrayList<>();

                String routeId = null;
                String payment = null;

                for (String line : entry.getValue()) {

                    String[] p = line.split("\\|", -1);
                    routeId = p[1];

                    if (p.length == 8) {

                        payment = p[2];

                        passengers.add(new Passenger(
                                p[4],
                                Integer.parseInt(p[5]),
                                p[6],
                                p[3],
                                Double.parseDouble(p[7])
                        ));

                    } else if (p.length == 7) {

                        payment = "Cash";

                        double oldBookingTotal =
                                Double.parseDouble(p[2]);

                        double passengerFare =
                                oldBookingTotal / entry.getValue().size();

                        passengers.add(new Passenger(
                                p[4],
                                Integer.parseInt(p[5]),
                                p[6],
                                p[3],
                                passengerFare
                        ));

                    } else {

                        payment = "Cash";

                        Bus oldBus = findBus(buses, routeId);

                        double passengerFare =
                                oldBus == null ? 0.0 : oldBus.getBasePrice();

                        passengers.add(new Passenger(
                                p[3],
                                Integer.parseInt(p[4]),
                                p[5],
                                p[2],
                                passengerFare
                        ));
                    }
                }

                Booking booking = new Booking(
                        entry.getKey(),
                        routeId,
                        payment,
                        passengers
                );

                bookings.add(booking);

                Bus bus = findBus(buses, routeId);

                if (bus != null) {
                    for (Passenger passenger : passengers) {

                        Seat seat = bus.findSeat(
                                passenger.getSeatNumber()
                        );

                        if (seat != null) {
                            seat.setBooked(true);
                        }
                    }
                }
            }

        } catch (Exception ex) {
            System.out.println(
                    "Warning: Could not fully read bookings.csv: "
                            + ex.getMessage()
            );
        }
    }

    private void saveBookings() {

        try {

            Files.createDirectories(FILE.getParent());

            List<String> lines = new ArrayList<>();

            for (Booking booking : bookings) {

                for (Passenger p : booking.getPassengers()) {

                    lines.add(String.join(
                            "|",
                            normalizeBookingNumber(booking.getPnr()),
                            booking.getRouteId(),
                            booking.getPaymentMethod(),
                            p.getSeatNumber(),
                            p.getName(),
                            String.valueOf(p.getAge()),
                            p.getGender(),
                            String.valueOf(p.getFare())
                    ));
                }
            }

            Files.write(FILE, lines, StandardCharsets.UTF_8);

        } catch (IOException ex) {
            System.out.println("Error saving bookings: " + ex.getMessage());
        }
    }

    private static int seatRow(String seatNumber) {

        int i = 0;

        while (i < seatNumber.length()
                && Character.isDigit(seatNumber.charAt(i))) {
            i++;
        }

        return Integer.parseInt(seatNumber.substring(0, i));
    }

    private static String seatSuffix(String seatNumber) {

        int i = 0;

        while (i < seatNumber.length()
                && Character.isDigit(seatNumber.charAt(i))) {
            i++;
        }

        return seatNumber.substring(i);
    }

    private static double roundMoney(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }

    private record ManifestRow(
            String bookingNumber,
            Passenger passenger
    ) {
    }
}
