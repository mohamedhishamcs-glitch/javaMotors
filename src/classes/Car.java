package classes;

public class Car extends Vehicle {
    private int numDoors;
    private String transmission;

    public Car(int vehicleId, String model, String brand, int year, double dailyRate, 
               int numDoors, String transmission) {
        super(vehicleId, model, brand, year, dailyRate);
        this.numDoors = numDoors;
        this.transmission = transmission;
    }

    @Override
    public void displayInfo() {
        System.out.println("classes.Car Details:");
        System.out.println("  ID: " + getVehicleId());
        System.out.println("  Brand: " + getBrand());
        System.out.println("  Model: " + getModel());
        System.out.println("  Year: " + getYear());
        System.out.println("  Number of Doors: " + numDoors);
        System.out.println("  Transmission: " + transmission);
        System.out.println("  Daily Rate: $" + getDailyRate());
        System.out.println("  Available: " + isAvailable());
    }

    public int getNumDoors() {
        return numDoors;
    }

    public String getTransmission() {
        return transmission;
    }
}
