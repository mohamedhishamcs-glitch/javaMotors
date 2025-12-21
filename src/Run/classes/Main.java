package classes;

import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== classes.Vehicle Rental System Test ===\n");
        
        // Create rental manager
        RentalManager manager = new RentalManager();
        
        // Create some vehicles with different prices and years
        Car car1 = new Car(1, "Camry", "Toyota", 2023, 50.0, 4, "Automatic");
        Car car2 = new Car(2, "Civic", "Honda", 2022, 45.0, 4, "Manual");
        Bike bike = new Bike(3, "Street 750", "Harley Davidson", 2021, 35.0, "Cruiser");
        Van van = new Van(4, "Transit", "Ford", 2020, 70.0, 5.0);
        
        // Add vehicles to manager
        manager.addVehicle(car1);
        manager.addVehicle(car2);
        manager.addVehicle(bike);
        manager.addVehicle(van);
        
        System.out.println("\n=== All Vehicles ===");
        for (Vehicle v : manager.getVehicles()) {
            v.displayInfo();
            System.out.println("---");
        }
        
        // Natural order from Comparable: price asc, then newer year, then ID
        System.out.println("\n=== Sorted (Natural Order: Cheapest, Newer First on Ties) ===");
        List<Vehicle> sortedNatural = manager.sortVehicles(manager.getVehicles());
        for (Vehicle v : sortedNatural) {
            System.out.println(v.getVehicleId() + ": $" + v.getDailyRate() + "/day, Year " + v.getYear());
        }

        // Reverse of natural order
        System.out.println("\n=== Sorted (Reverse of Natural Order) ===");
        List<Vehicle> sortedReverse = manager.sortVehicles(manager.getVehicles());
        Collections.reverse(sortedReverse);
        for (Vehicle v : sortedReverse) {
            System.out.println(v.getVehicleId() + ": $" + v.getDailyRate() + "/day, Year " + v.getYear());
        }
        
        System.out.println("\n=== Project is working with Java 21! ===");
    }
}
