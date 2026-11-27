package service;

public class WaitlistEntry {

    private final String id;
    private final String routeId;
    private final String name;
    private final int age;
    private final String gender;
    private final long createdAt;

    public WaitlistEntry(
            String id,
            String routeId,
            String name,
            int age,
            String gender,
            long createdAt
    ) {
        this.id = id;
        this.routeId = routeId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getRouteId() {
        return routeId;
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

    public long getCreatedAt() {
        return createdAt;
    }
}