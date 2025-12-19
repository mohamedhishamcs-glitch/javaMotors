package Run.classes;

import java.util.Date;

public class Booking {
    private int bookingId;
    private User user;
    private Vehicle vehicle;
    private Date startDate;
    private int days;
    private double totalPrice;
    private boolean isActive;
    private String paymentStatus; // "PAID" or "UNPAID"

    public Booking(int bookingId, User user, Vehicle vehicle, Date startDate, int days) {
        this.bookingId = bookingId;
        this.user = user;
        this.vehicle = vehicle;
        this.startDate = startDate;
        this.days = days;
        this.isActive = true;
        this.paymentStatus = "UNPAID";
        this.totalPrice = calculatePrice();
    }

    public double calculatePrice() {
        try {
            if (vehicle == null) {
                throw new IllegalStateException("classes.Vehicle is null for booking " + bookingId);
            }
            if (days <= 0) {
                throw new IllegalArgumentException("Invalid number of days: " + days);
            }
            double price = vehicle.calculateRentalPrice(days);
            if (price < 0) {
                throw new IllegalStateException("Calculated price cannot be negative.");
            }
            return price;
        } catch (IllegalArgumentException e) {
            System.out.println("Price Calculation Error: " + e.getMessage());
            return 0.0;
        } catch (IllegalStateException e) {
            System.out.println("Price Calculation Error: " + e.getMessage());
            return 0.0;
        } catch (Exception e) {
            System.out.println("Unexpected error calculating price: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    public void cancel() {
        this.isActive = false;
        vehicle.setAvailable(true);
        System.out.println("classes.Booking " + bookingId + " has been cancelled.");
    }

    public void checkIn() {
        System.out.println("classes.Vehicle checked in for booking " + bookingId);
        System.out.println("classes.User: " + user.getName());
        System.out.println("classes.Vehicle ID: " + vehicle.getVehicleId());
    }

    public void checkOut() {
        System.out.println("classes.Vehicle checked out for booking " + bookingId);
        this.isActive = false;
        vehicle.setAvailable(true);
    }

    public int getBookingId() {
        return bookingId;
    }

    public User getUser() {
        return user;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public Date getStartDate() {
        return startDate;
    }

    public int getDays() {
        return days;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
