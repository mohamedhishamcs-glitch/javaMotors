package Run.classes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RentalManager {
    private List<Vehicle> vehicles;
    private List<User> users;
    private List<Booking> bookings;

    public RentalManager() {
        this.vehicles = new ArrayList<>();
        this.users = new ArrayList<>();
        this.bookings = new ArrayList<>();
    }

    public void addVehicle(Vehicle v) {
        vehicles.add(v);
        System.out.println("classes.Vehicle added: ID " + v.getVehicleId());
    }

    public boolean removeVehicle(int vehicleId) {
        Vehicle vehicle = findVehicleById(vehicleId);
        if (vehicle != null) {
            vehicles.remove(vehicle);
            System.out.println("classes.Vehicle removed: ID " + vehicleId);
            return true;
        }
        System.out.println("classes.Vehicle not found: ID " + vehicleId);
        return false;
    }

    public Vehicle findVehicleById(int vehicleId) {
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getVehicleId() == vehicleId) {
                return vehicle;
            }
        }
        return null;
    }

    public void addUser(User u) {
        users.add(u);
        System.out.println("classes.User added: " + u.getName());
    }

    public User findUserById(int userId) {
        for (User user : users) {
            if (user.getUserId() == userId) {
                return user;
            }
        }
        return null;
    }

    public Booking bookVehicle(int userId, int vehicleId, Date startDate, int days) {
        try {
            if (startDate == null) {
                throw new IllegalArgumentException("Start date cannot be null.");
            }
            if (days <= 0) {
                throw new IllegalArgumentException("Number of days must be greater than zero.");
            }

            User user = findUserById(userId);
            Vehicle vehicle = findVehicleById(vehicleId);

            if (user == null) {
                throw new IllegalArgumentException("classes.User not found with ID: " + userId);
            }

            if (vehicle == null) {
                throw new IllegalArgumentException("classes.Vehicle not found with ID: " + vehicleId);
            }

            if (!vehicle.isAvailable()) {
                throw new IllegalStateException("classes.Vehicle is not available for booking.");
            }

            Booking booking = user.makeBooking(vehicle, startDate, days);
            if (booking != null) {
                bookings.add(booking);
                System.out.println("classes.Booking created: ID " + booking.getBookingId());
            }
            return booking;
        } catch (IllegalArgumentException e) {
            System.out.println("classes.Booking Error: " + e.getMessage());
            return null;
        } catch (IllegalStateException e) {
            System.out.println("classes.Booking Error: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("Unexpected error during booking: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Date getReturnDateForVehicle(int vehicleId) {
        for (Booking b : bookings) {
            if (b.getVehicle().getVehicleId() == vehicleId && b.isActive() && !b.getVehicle().isUnderMaintenance()) {
                // Calculate return date: startDate + days
                long returnTime = b.getStartDate().getTime() + (long) b.getDays() * 24 * 60 * 60 * 1000;
                return new Date(returnTime);
            }
        }
        return null; // Not rented
    }

    public boolean cancelBooking(int bookingId) {
        try {
            for (Booking booking : bookings) {
                if (booking.getBookingId() == bookingId) {
                    if (!booking.isActive()) {
                        throw new IllegalStateException("classes.Booking is already cancelled.");
                    }
                    booking.cancel();
                    System.out.println("classes.Booking " + bookingId + " successfully cancelled.");
                    return true;
                }
            }
            throw new IllegalArgumentException("classes.Booking not found: ID " + bookingId);
        } catch (IllegalStateException e) {
            System.out.println("Cancellation Error: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.out.println("Cancellation Error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("Unexpected error during cancellation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Vehicle> displayAvailableVehicles() {
        List<Vehicle> availableVehicles = new ArrayList<>();
        System.out.println("\n=== Available Vehicles ===");
        for (Vehicle vehicle : vehicles) {
            if (vehicle.isAvailable()) {
                availableVehicles.add(vehicle);
                vehicle.displayInfo();
                System.out.println("---");
            }
        }
        return availableVehicles;
    }

    public List<Vehicle> sortVehicles(List<Vehicle> list) {
        List<Vehicle> sortedList = new ArrayList<>(list);
        sortedList.sort(null); // uses classes.Vehicle.compareTo
        return sortedList;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Booking> getBookings() {
        return bookings;
    }
}
