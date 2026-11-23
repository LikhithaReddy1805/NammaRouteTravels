
import model.Bus;
import service.BusService;
import service.BookingService;
import util.InputValidator;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        BusService busService = new BusService();
        BookingService bookingService = new BookingService();

        bookingService.restoreBookedSeats(busService.getBuses());

        while (true) {

            System.out.println("\n====================================");
            System.out.println("       NAMMAROUTE TRAVELS");
            System.out.println("====================================");
            System.out.println("1. Add Bus Service");
            System.out.println("2. View All Bus Services");
            System.out.println("3. View Seat Map");
            System.out.println("4. Book Tickets");
            System.out.println("5. Exit");
            System.out.println("====================================");
            System.out.print("Enter your choice: ");

            String choice = sc.nextLine().trim();

            switch (choice) {

                case "1":
                    addBus(sc, busService);
                    break;

                case "2":
                    busService.viewAllBuses();
                    break;

                case "3":
                    System.out.print("Enter Route ID: ");
                    String routeId = sc.nextLine().trim();

                    busService.displaySeatMap(routeId);
                    break;

                case "4":
                    System.out.print("Enter Route ID: ");
                    String bookingRoute = sc.nextLine().trim();

                    Bus bus = busService.findBus(bookingRoute);

                    if (bus == null) {
                        System.out.println("Invalid Route ID.");
                    } else {
                        bookingService.bookTickets(bus, sc);
                    }
                    break;

                case "5":
                    System.out.println("Thank you for using NammaRoute Travels!");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void addBus(Scanner sc, BusService busService) {

        System.out.println("\n========== ADD BUS SERVICE ==========");

        System.out.print("Enter Route ID (Example NR-101): ");
        String routeId = sc.nextLine().trim();

        if (!InputValidator.isValidRouteId(routeId)) {
            System.out.println("Invalid Route ID format.");
            return;
        }

        if (busService.findBus(routeId) != null) {
            System.out.println("Route ID already exists.");
            return;
        }

        System.out.print("Enter Origin: ");
        String origin = sc.nextLine().trim();

        if (!InputValidator.isValidName(origin)) {
            System.out.println("Invalid origin.");
            return;
        }

        System.out.print("Enter Destination: ");
        String destination = sc.nextLine().trim();

        if (!InputValidator.isValidName(destination)
                || origin.equalsIgnoreCase(destination)) {
            System.out.println("Invalid destination.");
            return;
        }

        System.out.print("Enter Departure Date and Time (yyyy-MM-dd HH:mm): ");
        String departure = sc.nextLine().trim();

        if (!InputValidator.isValidDeparture(departure)) {
            System.out.println("Invalid date or time format.");
            return;
        }

        System.out.print("Enter Base Ticket Price: ");

        double price;

        try {
            price = Double.parseDouble(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ticket price.");
            return;
        }

        if (!InputValidator.isValidPrice(price)) {
            System.out.println("Ticket price must be greater than zero.");
            return;
        }

        System.out.println("\nSelect Bus Type:");
        System.out.println("1. 2x2 Seater (32 Seats)");
        System.out.println("2. 2x1 Sleeper (24 Berths)");
        System.out.print("Enter choice: ");

        String typeChoice = sc.nextLine().trim();

        String busType;

        if (typeChoice.equals("1")) {
            busType = "Seater";
        } else if (typeChoice.equals("2")) {
            busType = "Sleeper";
        } else {
            System.out.println("Invalid bus type.");
            return;
        }

        Bus bus = new Bus(
                routeId, origin, destination,
                departure, price, busType
        );

        if (busService.addBus(bus)) {
            System.out.println("\nBus service added successfully!");
            System.out.println("Total Seats/Berths: " + bus.getSeats().size());
        } else {
            System.out.println("Unable to add bus service.");
        }
    }
}
