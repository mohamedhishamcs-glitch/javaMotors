package classes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class User {
    private int userId;
    private String name;
    private String email;
    private String phone;
    private List<Booking> rentals;

    public User(int userId, String name, String email, String phone) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.rentals = new ArrayList<>();
    }

    public Booking makeBooking(Vehicle vehicle, Date startDate, int days) {
        try {
            if (vehicle == null) {
                throw new IllegalArgumentException("classes.Vehicle cannot be null.");
            }
            if (startDate == null) {
                throw new IllegalArgumentException("Start date cannot be null.");
            }
            if (days <= 0) {
                throw new IllegalArgumentException("Days must be greater than zero.");
            }
            if (!vehicle.isAvailable()) {
                throw new IllegalStateException("classes.Vehicle is not available for booking.");
            }
            
            int bookingId = rentals.size() + 1;
            Booking booking = new Booking(bookingId, this, vehicle, startDate, days);
            rentals.add(booking);
            vehicle.setAvailable(false);
            System.out.println("classes.Booking " + bookingId + " created successfully for user " + this.name);
            return booking;
        } catch (IllegalArgumentException e) {
            System.out.println("classes.Booking Error: " + e.getMessage());
            return null;
        } catch (IllegalStateException e) {
            System.out.println("classes.Booking Error: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("Unexpected error while making booking: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean cancelBooking(int bookingId) {
        for (Booking booking : rentals) {
            if (booking.getBookingId() == bookingId && booking.isActive()) {
                booking.cancel();
                return true;
            }
        }
        return false;
    }

    public List<Booking> getActiveBookings() {
        List<Booking> activeBookings = new ArrayList<>();
        for (Booking booking : rentals) {
            if (booking.isActive()) {
                activeBookings.add(booking);
            }
        }
        return activeBookings;
    }

    public void displayInfo() {
        System.out.println("classes.User Information:");
        System.out.println("  classes.User ID: " + userId);
        System.out.println("  Name: " + name);
        System.out.println("  Email: " + email);
        System.out.println("  Phone: " + phone);
        System.out.println("  Total Bookings: " + rentals.size());
        System.out.println("  Active Bookings: " + getActiveBookings().size());
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public List<Booking> getRentals() {
        return rentals;
    }

    public String toString() {
        return name + " (ID: " + userId + ")";
    }
}
