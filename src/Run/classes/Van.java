package Run.classes;

public class Van extends Vehicle {
    private double cargoVolume;

    public Van(int vehicleId, String model, String brand, int year, double dailyRate, 
               double cargoVolume) {
        super(vehicleId, model, brand, year, dailyRate);
        this.cargoVolume = cargoVolume;
    }

    @Override
    public void displayInfo() {
        System.out.println("classes.Van Details:");
        System.out.println("  ID: " + getVehicleId());
        System.out.println("  Brand: " + getBrand());
        System.out.println("  Model: " + getModel());
        System.out.println("  Year: " + getYear());
        System.out.println("  Cargo Volume: " + cargoVolume + " m³");
        System.out.println("  Daily Rate: $" + getDailyRate());
        System.out.println("  Available: " + isAvailable());
    }

    public double getCargoVolume() {
        return cargoVolume;
    }
}
