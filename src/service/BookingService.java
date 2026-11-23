
package service;

import model.Bus;
import model.Seat;
import model.Passenger;
import model.Booking;

import java.io.*;
import java.util.*;

public class BookingService {
    private List<Booking> bookings = new ArrayList<>();
    private final String FILE = "data/bookings.csv";

    public BookingService() {
        loadBookings();
    }

    public void bookTickets(Bus bus, Scanner sc) {
        if (bus == null) {
            System.out.println("Invalid Route ID.");
            return;
        }

        bus.displaySeatMap();

        System.out.print("\nEnter seat numbers separated by commas: ");
        String input = sc.nextLine().trim();

        if (input.isEmpty()) {
            System.out.println("Please enter seat numbers.");
            return;
        }

        String[] requestedSeats = input.split(",");
        List<Seat> selectedSeats = new ArrayList<>();
        Set<String> uniqueSeats = new HashSet<>();

        for (String requested : requestedSeats) {
            String seatNumber = requested.trim().toUpperCase();

            if (!uniqueSeats.add(seatNumber)) {
                System.out.println("Duplicate seat entered: " + seatNumber);
                return;
            }

            Seat seat = bus.findSeat(seatNumber);

            if (seat == null) {
                System.out.println("Invalid seat number: " + seatNumber);
                return;
            }

            if (seat.isBooked()) {
                System.out.println("Seat already booked: " + seatNumber);
                return;
            }

            selectedSeats.add(seat);
        }

        List<Passenger> passengers = new ArrayList<>();

        for (Seat seat : selectedSeats) {
            System.out.println("\nPassenger details for " + seat.getSeatNumber());

            System.out.print("Name: ");
            String name = sc.nextLine().trim();

            if (!isValidName(name)) {
                System.out.println("Invalid passenger name.");
                return;
            }

            System.out.print("Age: ");
            int age;

            try {
                age = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid age.");
                return;
            }

            if (age <= 0 || age > 120) {
                System.out.println("Age must be between 1 and 120.");
                return;
            }

            System.out.print("Gender (M/F/Other): ");
            String gender = sc.nextLine().trim();

            if (!gender.equalsIgnoreCase("M")
                    && !gender.equalsIgnoreCase("F")
                    && !gender.equalsIgnoreCase("Other")) {
                System.out.println("Invalid gender.");
                return;
            }

            passengers.add(new Passenger(
                    name, age, gender, seat.getSeatNumber()
            ));
        }

        String pnr = generatePNR();
        double totalFare = bus.getPrice() * passengers.size();

        Booking booking = new Booking(
                pnr, bus.getRouteId(), passengers, totalFare
        );

        bookings.add(booking);

        for (Seat seat : selectedSeats) {
            seat.setBooked(true);
        }

        saveBookings();
        printReceipt(booking, bus);
    }

    private String generatePNR() {
        String pnr;

        do {
            int number = 10000 + new Random().nextInt(90000);
            pnr = "PNR-" + number;
        } while (pnrExists(pnr));

        return pnr;
    }

    private boolean pnrExists(String pnr) {
        for (Booking booking : bookings) {
            if (booking.getPnr().equals(pnr)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty()
                && !name.contains("|");
    }

    private void printReceipt(Booking booking, Bus bus) {
        System.out.println("\n========================================");
        System.out.println("          NAMMAROUTE TRAVELS");
        System.out.println("             BOOKING RECEIPT");
        System.out.println("========================================");

        System.out.println("PNR Number : " + booking.getPnr());
        System.out.println("Route ID   : " + bus.getRouteId());
        System.out.println("Route      : " + bus.getOrigin()
                + " -> " + bus.getDestination());
        System.out.println("Departure  : " + bus.getDeparture());

        System.out.println("----------------------------------------");
        System.out.printf("%-6s %-18s %-5s %-8s%n",
                "Seat", "Passenger Name", "Age", "Gender");
        System.out.println("----------------------------------------");

        for (Passenger passenger : booking.getPassengers()) {
            System.out.printf("%-6s %-18s %-5d %-8s%n",
                    passenger.getSeatNumber(),
                    passenger.getName(),
                    passenger.getAge(),
                    passenger.getGender());
        }

        System.out.println("----------------------------------------");
        System.out.println("Number of Seats : " + booking.getPassengers().size());
        System.out.println("Price per Seat  : Rs." + bus.getPrice());
        System.out.println("Total Fare      : Rs." + booking.getTotalFare());

        System.out.println("========================================");
        System.out.println("       BOOKING CONFIRMED!");
        System.out.println("========================================");
    }

    private void saveBookings() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE))) {
            for (Booking booking : bookings) {
                for (Passenger passenger : booking.getPassengers()) {
                    writer.println(
                            booking.getPnr() + "|" +
                            booking.getRouteId() + "|" +
                            passenger.getSeatNumber() + "|" +
                            passenger.getName() + "|" +
                            passenger.getAge() + "|" +
                            passenger.getGender()
                    );
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving bookings: " + e.getMessage());
        }
    }

    private void loadBookings() {
        File file = new File(FILE);

        if (!file.exists()) {
            return;
        }

        Map<String, List<Passenger>> passengerMap = new LinkedHashMap<>();
        Map<String, String> routeMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|", -1);

                if (data.length != 6) {
                    continue;
                }

                String pnr = data[0];
                String routeId = data[1];

                passengerMap.computeIfAbsent(
                        pnr, key -> new ArrayList<>()
                ).add(new Passenger(
                        data[3],
                        Integer.parseInt(data[4]),
                        data[5],
                        data[2]
                ));

                routeMap.put(pnr, routeId);
            }

            for (String pnr : passengerMap.keySet()) {
                List<Passenger> passengers = passengerMap.get(pnr);

                double fare = 0;

                Booking booking = new Booking(
                        pnr, routeMap.get(pnr),
                        passengers, fare
                );

                bookings.add(booking);
            }

        } catch (IOException | NumberFormatException e) {
            System.out.println("Error loading bookings: " + e.getMessage());
        }
    }

    public void restoreBookedSeats(List<Bus> buses) {
        for (Booking booking : bookings) {
            for (Bus bus : buses) {
                if (bus.getRouteId().equalsIgnoreCase(booking.getRouteId())) {
                    for (Passenger passenger : booking.getPassengers()) {
                        Seat seat = bus.findSeat(passenger.getSeatNumber());

                        if (seat != null) {
                            seat.setBooked(true);
                        }
                    }
                }
            }
        }
    }
}
