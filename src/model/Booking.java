
package model;

import java.util.List;

public class Booking {
    private String pnr;
    private String routeId;
    private List<Passenger> passengers;
    private double totalFare;

    public Booking(String pnr, String routeId,
                   List<Passenger> passengers, double totalFare) {
        this.pnr = pnr;
        this.routeId = routeId;
        this.passengers = passengers;
        this.totalFare = totalFare;
    }

    public String getPnr() {
        return pnr;
    }

    public String getRouteId() {
        return routeId;
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public double getTotalFare() {
        return totalFare;
    }
}
