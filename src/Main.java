import model.Bus;
import service.BookingService;
import service.BusService;
import util.InputValidator;

import java.time.LocalDateTime;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        BusService busService = new BusService();
        BookingService bookingService =
                new BookingService(busService.getBuses());

        System.out.println("\n===============================================");
        System.out.println("             NAMMAROUTE TRAVELS");
        System.out.println("        BUS TICKET MANAGEMENT SYSTEM");
        System.out.println("===============================================");

        boolean running = true;

        while (running) {
            System.out.println("\n1. Add Bus Service");
            System.out.println("2. View Upcoming Bus Services");
            System.out.println("3. View Seat Map");
            System.out.println("4. Book Tickets");
            System.out.println("5. Cancel Tickets");
            System.out.println("6. Generate Passenger Manifest");
            System.out.println("7. View Waitlist");
            System.out.println("8. Exit");

            int choice = InputValidator.readInt(
                    sc, "\nEnter your choice: ", 1, 8
            );

            switch (choice) {
                case 1 -> addBus(busService, sc);
                case 2 -> busService.displayUpcomingBuses();
                case 3 -> viewSeatMap(busService, sc);
                case 4 -> bookingService.bookTickets(
                        busService.getBuses(), sc
                );
                case 5 -> bookingService.cancelTickets(
                        busService.getBuses(), sc
                );
                case 6 -> bookingService.generateManifest(
                        busService.getBuses(), sc
                );
                case 7 -> bookingService.displayWaitlist(
                        busService.getBuses(), sc
                );
                case 8 -> {
                    running = false;
                    System.out.println(
                            "Thank you for using NammaRoute Travels. Goodbye!"
                    );
                }
            }
        }

        sc.close();
    }

    private static void addBus(BusService busService, Scanner sc) {
        System.out.println("\n========== ADD BUS SERVICE ==========");

        String routeId = InputValidator.readRouteId(sc);
        String origin = InputValidator.readNonEmpty(sc, "Origin: ");
        String destination = InputValidator.readNonEmpty(sc, "Destination: ");

        if (origin.equalsIgnoreCase(destination)) {
            System.out.println(
                    "Origin and destination cannot be the same."
            );
            return;
        }

        LocalDateTime departure =
                InputValidator.readFutureDateTime(sc);

        double price =
                InputValidator.readPositiveDouble(
                        sc, "Base ticket price (Rs.): "
                );

        System.out.println("\nBus type:");
        System.out.println("1. 2x2 Seater (32 seats)");
        System.out.println("2. 2x1 Sleeper (24 berths)");

        int typeChoice =
                InputValidator.readInt(sc, "Choose bus type: ", 1, 2);

        String type = typeChoice == 1 ? "Seater" : "Sleeper";

        Bus bus = new Bus(
                routeId,
                origin,
                destination,
                departure,
                price,
                type
        );

        if (busService.addBus(bus)) {
            System.out.println("Bus service added successfully.");
        } else {
            System.out.println(
                    "A bus with this Route ID already exists."
            );
        }
    }

    private static void viewSeatMap(
            BusService busService, Scanner sc
    ) {
        String routeId = InputValidator.readRouteId(sc);
        Bus bus = busService.findBus(routeId);

        if (bus == null) {
            System.out.println("Bus route not found.");
        } else {
            bus.displaySeatMap();
        }
    }
}