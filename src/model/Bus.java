
package model;

import java.util.ArrayList;
import java.util.List;

public class Bus {
    private String routeId;
    private String origin;
    private String destination;
    private String departure;
    private double price;
    private String busType;
    private List<Seat> seats;

    public Bus(String routeId, String origin, String destination,
               String departure, double price, String busType) {
        this.routeId = routeId;
        this.origin = origin;
        this.destination = destination;
        this.departure = departure;
        this.price = price;
        this.busType = busType;
        this.seats = new ArrayList<>();
        generateSeats();
    }

    private void generateSeats() {
        if (busType.equalsIgnoreCase("Seater")) {
            for (int row = 1; row <= 8; row++) {
                for (char seat = 'A'; seat <= 'D'; seat++) {
                    seats.add(new Seat(row + String.valueOf(seat)));
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

    public String getDeparture() {
        return departure;
    }

    public double getPrice() {
        return price;
    }

    public String getBusType() {
        return busType;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public Seat findSeat(String seatNumber) {
        for (Seat seat : seats) {
            if (seat.getSeatNumber().equalsIgnoreCase(seatNumber)) {
                return seat;
            }
        }
        return null;
    }

    public void displaySeatMap() {
        System.out.println("\n--- Bus " + routeId + ": "
                + origin + " to " + destination + " ---");
        System.out.println("Departure: " + departure);
        System.out.println("Available: Seat Number | Booked: XX\n");

        if (busType.equalsIgnoreCase("Seater")) {
            for (int row = 1; row <= 8; row++) {
                System.out.print("Row " + row + ": ");
                for (char c = 'A'; c <= 'D'; c++) {
                    displaySeat(row + String.valueOf(c));
                }
                System.out.println();
            }
        } else {
            System.out.println("Lower Berths       Upper Berths");
            for (int row = 1; row <= 12; row++) {
                displaySeat(row + "L");
                System.out.print("             ");
                displaySeat(row + "U");
                System.out.println();
            }
        }

        long available = seats.stream()
                .filter(seat -> !seat.isBooked()).count();

        System.out.println("\nAvailable Seats: " + available);
        System.out.println("Booked Seats: " + (seats.size() - available));
    }

    private void displaySeat(String number) {
        Seat seat = findSeat(number);
        String display = seat.isBooked() ? "[XX]" : "[" + number + "]";
        System.out.print(display + " ");
    }
}
