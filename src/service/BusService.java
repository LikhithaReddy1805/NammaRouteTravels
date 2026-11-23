
package service;

import model.Bus;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BusService {
    private List<Bus> buses = new ArrayList<>();
    private final String FILE = "data/buses.csv";

    public BusService() {
        loadBuses();
    }

    public boolean addBus(Bus bus) {
        if (findBus(bus.getRouteId()) != null) {
            return false;
        }

        buses.add(bus);
        saveBuses();
        return true;
    }

    public Bus findBus(String routeId) {
        for (Bus bus : buses) {
            if (bus.getRouteId().equalsIgnoreCase(routeId)) {
                return bus;
            }
        }
        return null;
    }

    public List<Bus> getBuses() {
        return buses;
    }

    public void viewAllBuses() {
        if (buses.isEmpty()) {
            System.out.println("No bus services available.");
            return;
        }

        System.out.println("\n========== BUS SERVICES ==========");

        for (Bus bus : buses) {
            System.out.println("Route ID: " + bus.getRouteId());
            System.out.println("Route: " + bus.getOrigin()
                    + " -> " + bus.getDestination());
            System.out.println("Departure: " + bus.getDeparture());
            System.out.println("Price: Rs." + bus.getPrice());
            System.out.println("Bus Type: " + bus.getBusType());
            System.out.println("Total Seats: " + bus.getSeats().size());
            System.out.println("----------------------------------");
        }
    }

    public void displaySeatMap(String routeId) {
        Bus bus = findBus(routeId);

        if (bus == null) {
            System.out.println("Invalid Route ID.");
            return;
        }

        bus.displaySeatMap();
    }

    private void saveBuses() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE))) {
            for (Bus bus : buses) {
                writer.println(
                        bus.getRouteId() + "|" +
                        bus.getOrigin() + "|" +
                        bus.getDestination() + "|" +
                        bus.getDeparture() + "|" +
                        bus.getPrice() + "|" +
                        bus.getBusType()
                );
            }
        } catch (IOException e) {
            System.out.println("Error saving bus details: " + e.getMessage());
        }
    }

    private void loadBuses() {
        File file = new File(FILE);

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|", -1);

                if (data.length == 6) {
                    Bus bus = new Bus(
                            data[0], data[1], data[2],
                            data[3], Double.parseDouble(data[4]), data[5]
                    );
                    buses.add(bus);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error loading buses: " + e.getMessage());
        }
    }
}
