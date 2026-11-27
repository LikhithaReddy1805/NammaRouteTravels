
package service;

import model.Bus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BusService {

    private static final Path FILE =
            Path.of("data", "buses.csv");

    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
    };

    private static final DateTimeFormatter SAVE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final List<Bus> buses = new ArrayList<>();

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
        return new ArrayList<>(buses);
    }

    public void displayUpcomingBuses() {

        List<Bus> upcoming = buses.stream()
                .filter(bus ->
                        bus.getDeparture().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Bus::getDeparture))
                .toList();

        if (upcoming.isEmpty()) {
            System.out.println("No upcoming bus services found.");
            return;
        }

        System.out.println(
                "\n================ UPCOMING BUS SERVICES ================"
        );

        System.out.printf(
                "%-12s %-18s %-18s %-22s %-12s %-12s %-10s%n",
                "Route ID",
                "Origin",
                "Destination",
                "Departure",
                "Type",
                "Fare",
                "Available"
        );

        for (Bus bus : upcoming) {

            System.out.printf(
                    "%-12s %-18s %-18s %-22s %-12s Rs.%-9.2f %-10d%n",
                    bus.getRouteId(),
                    bus.getOrigin(),
                    bus.getDestination(),
                    bus.getDepartureDisplay(),
                    bus.getBusType(),
                    bus.getBasePrice(),
                    bus.getAvailableCount()
            );
        }
    }

    private void loadBuses() {

        if (!Files.exists(FILE)) {
            return;
        }

        try {

            List<String> lines =
                    Files.readAllLines(FILE, StandardCharsets.UTF_8);

            for (String line : lines) {

                if (line.isBlank()) {
                    continue;
                }

                try {
                    line = line.replace("\uFEFF", "").trim();

                    String[] p;

                    if (line.contains("|")) {
                        p = line.split("\\|", -1);
                    } else {
                        p = line.split(",", -1);
                    }

                    if (p.length != 6) {
                        System.out.println(
                                "Skipping invalid bus record: " + line
                        );
                        continue;
                    }

                    LocalDateTime departure = parseDate(p[3].trim());

                    Bus bus = new Bus(
                            p[0].trim(),
                            p[1].trim(),
                            p[2].trim(),
                            departure,
                            Double.parseDouble(p[4].trim()),
                            p[5].trim()
                    );

                    if (findBus(bus.getRouteId()) == null) {
                        buses.add(bus);
                    }

                } catch (Exception ex) {

                    System.out.println(
                            "Skipping invalid bus record: " + line
                    );
                }
            }

        } catch (IOException ex) {

            System.out.println(
                    "Warning: Could not read buses.csv: "
                            + ex.getMessage()
            );
        }
    }

    private LocalDateTime parseDate(String date) {

        for (DateTimeFormatter formatter : DATE_FORMATS) {

            try {
                return LocalDateTime.parse(date, formatter);
            } catch (Exception ignored) {
            }
        }

        throw new IllegalArgumentException(
                "Invalid departure date: " + date
        );
    }

    private void saveBuses() {

        try {

            Files.createDirectories(FILE.getParent());

            List<String> lines = new ArrayList<>();

            for (Bus bus : buses) {

                lines.add(
                        String.join(
                                "|",
                                bus.getRouteId(),
                                bus.getOrigin(),
                                bus.getDestination(),
                                bus.getDeparture().format(SAVE_FORMAT),
                                String.valueOf(bus.getBasePrice()),
                                bus.getBusType()
                        )
                );
            }

            Files.write(
                    FILE,
                    lines,
                    StandardCharsets.UTF_8
            );

        } catch (IOException ex) {

            System.out.println(
                    "Error saving buses: " + ex.getMessage()
            );
        }
    }
}
