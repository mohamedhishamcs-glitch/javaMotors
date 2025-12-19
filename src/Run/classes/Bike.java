package Run.classes;

public class Bike extends Vehicle {
    private String type;

    public Bike(int vehicleId, String model, String brand, int year, double dailyRate, 
                String type) {
        super(vehicleId, model, brand, year, dailyRate);
        this.type = type;
    }

    @Override
    public void displayInfo() {
        System.out.println("classes.Bike Details:");
        System.out.println("  ID: " + getVehicleId());
        System.out.println("  Brand: " + getBrand());
        System.out.println("  Model: " + getModel());
        System.out.println("  Year: " + getYear());
        System.out.println("  Type: " + type);
        System.out.println("  Daily Rate: $" + getDailyRate());
        System.out.println("  Available: " + isAvailable());
    }

    public String getType() {
        return type;
    }
}
