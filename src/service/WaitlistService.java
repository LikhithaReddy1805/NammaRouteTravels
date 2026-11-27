package service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WaitlistService {

    private static final Path FILE =
            Path.of("data", "waitlist.csv");

    private static final int MAX_WAITLIST_SIZE = 5;

    private final List<WaitlistEntry> entries =
            new ArrayList<>();

    public WaitlistService() {
        load();
    }

    public boolean add(WaitlistEntry entry) {

        if (remainingSlots(entry.getRouteId()) == 0) {
            return false;
        }

        entries.add(entry);

        save();

        return true;
    }

    public List<WaitlistEntry> getEntriesForRoute(
            String routeId
    ) {

        return entries.stream()
                .filter(entry ->
                        entry.getRouteId().equalsIgnoreCase(routeId))
                .sorted(
                        Comparator.comparingLong(
                                WaitlistEntry::getCreatedAt
                        )
                )
                .toList();
    }

    public int remainingSlots(String routeId) {

        long count = entries.stream()
                .filter(entry ->
                        entry.getRouteId().equalsIgnoreCase(routeId))
                .count();

        return Math.max(
                0,
                MAX_WAITLIST_SIZE - (int) count
        );
    }

    public int positionOf(String id, String routeId) {

        List<WaitlistEntry> routeEntries =
                getEntriesForRoute(routeId);

        for (int i = 0; i < routeEntries.size(); i++) {

            if (routeEntries.get(i).getId().equals(id)) {
                return i + 1;
            }
        }

        return -1;
    }

    public void remove(String id) {

        boolean changed =
                entries.removeIf(entry ->
                        entry.getId().equals(id));

        if (changed) {
            save();
        }
    }

    private void load() {

        if (!Files.exists(FILE)) {
            return;
        }

        try {

            for (String line :
                    Files.readAllLines(FILE, StandardCharsets.UTF_8)) {

                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split("\\|", -1);

                if (fields.length != 6) {
                    continue;
                }

                try {

                    entries.add(
                            new WaitlistEntry(
                                    fields[0],
                                    fields[1],
                                    fields[2],
                                    Integer.parseInt(fields[3]),
                                    fields[4],
                                    Long.parseLong(fields[5])
                            )
                    );

                } catch (NumberFormatException ignored) {
                    // Ignore invalid records.
                }
            }

        } catch (IOException ex) {

            System.out.println(
                    "Warning: Could not read waitlist.csv: "
                            + ex.getMessage()
            );
        }
    }

    private void save() {

        try {

            Files.createDirectories(FILE.getParent());

            List<String> lines = new ArrayList<>();

            for (WaitlistEntry entry : entries) {

                lines.add(
                        String.join(
                                "|",
                                entry.getId(),
                                entry.getRouteId(),
                                entry.getName(),
                                String.valueOf(entry.getAge()),
                                entry.getGender(),
                                String.valueOf(entry.getCreatedAt())
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
                    "Error saving waitlist: " + ex.getMessage()
            );
        }
    }
}