package model;

import java.util.ArrayList;
import java.util.List;

public class Booking {

    private final String pnr;
    private final String routeId;
    private final String paymentMethod;
    private final List<Passenger> passengers;

    public Booking(
            String pnr,
            String routeId,
            String paymentMethod,
            List<Passenger> passengers
    ) {
        this.pnr = pnr;
        this.routeId = routeId;
        this.paymentMethod = paymentMethod;
        this.passengers = new ArrayList<>(passengers);
    }

    public String getPnr() {
        return pnr;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public double getTotalFare() {
        return passengers.stream()
                .mapToDouble(Passenger::getFare)
                .sum();
    }
}