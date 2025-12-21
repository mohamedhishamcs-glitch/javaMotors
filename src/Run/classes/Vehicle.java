package classes;

import java.util.Date;

public abstract class Vehicle implements Maintainable, Comparable<Vehicle> {
    private int vehicleId;
    private String model;
    private String brand;
    private int year;
    private double dailyRate;
    private boolean isAvailable;
    private Date maintenanceDate;

    public Vehicle(int vehicleId, String model, String brand, int year, double dailyRate) {
        if (dailyRate <= 0) {
            throw new IllegalArgumentException("Daily rate must be greater than zero");
        }
        this.vehicleId = vehicleId;
        this.model = model;
        this.brand = brand;
        this.year = year;
        this.dailyRate = dailyRate;
        this.isAvailable = true;
        this.maintenanceDate = null;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean flag) {
        this.isAvailable = flag;
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public abstract void displayInfo();

    public double calculateRentalPrice(int days) {
        return dailyRate * days;
    }

    @Override
    public void scheduleMaintenance(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("Maintenance date cannot be null");
        }

        Date now = new Date();
        if (!date.after(now)) {
            throw new IllegalArgumentException("Maintenance date must be in the future");
        }

        this.maintenanceDate = date;
        this.isAvailable = false;
    }

    @Override
    public boolean isUnderMaintenance() {
        if (maintenanceDate == null) {
            return false;
        }
        Date now = new Date();
        return maintenanceDate.after(now);
    }

    public String getModel() {
        return model;
    }

    public String getBrand() {
        return brand;
    }

    public int getYear() {
        return year;
    }


    @Override
    public int compareTo(Vehicle other) {
        // Natural order: cheapest daily rate first, tie-breaker by newer year, then ID for stability
        int byPrice = Double.compare(this.dailyRate, other.dailyRate);
        if (byPrice != 0) {
            return byPrice;
        }

        int byYear = Integer.compare(other.year, this.year); // newer first on ties
        if (byYear != 0) {
            return byYear;
        }

        return Integer.compare(this.vehicleId, other.vehicleId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + " - " + brand
                + " " + model
                + " (ID: " + vehicleId + ")";
    }

    public Date getMaintenanceDate() {
        return maintenanceDate;
    }
}
