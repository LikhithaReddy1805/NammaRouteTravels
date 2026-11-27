
package model;

public class Passenger {

    private final String name;
    private final int age;
    private final String gender;
    private final String seatNumber;
    private final double fare;

    public Passenger(
            String name,
            int age,
            String gender,
            String seatNumber,
            double fare
    ) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.seatNumber = seatNumber;
        this.fare = fare;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public double getFare() {
        return fare;
    }
}