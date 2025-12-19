package test.java.classes;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

public class testing {

    private RentalManager manager;
    private User user1;
    private Vehicle car1;
    private Vehicle bike1;
    private Vehicle van1;

    @BeforeEach
    public void setup() {
        manager = new RentalManager();

        // Create users
        user1 = new User(1, "Alice", "alice@example.com", "123456789");
        manager.addUser(user1);

        // Create vehicles
        car1 = new Car(1, "Model S", "Tesla", 2023, 100.0, 4, "Automatic");
        bike1 = new Bike(2, "CBR500R", "Honda", 2022, 50.0, "Sport");
        van1 = new Van(3, "Transit", "Ford", 2021, 150.0, 10.0);

        manager.addVehicle(car1);
        manager.addVehicle(bike1);
        manager.addVehicle(van1);
    }

    @Test
    public void testBookingCreation() {
        Date startDate = new Date();
        Booking booking = manager.bookVehicle(user1.getUserId(), car1.getVehicleId(), startDate, 3);

        assertNotNull(booking, "Booking should be created successfully");
        assertEquals(user1, booking.getUser(), "Booking should reference the correct user");
        assertEquals(car1, booking.getVehicle(), "Booking should reference the correct vehicle");
        assertEquals(300.0, booking.getTotalPrice(), 0.001, "Total price should be dailyRate * days");
        assertFalse(car1.isAvailable(), "Vehicle should not be available after booking");
    }

    @Test
    public void testCancelBooking() {
        Date startDate = new Date();
        Booking booking = manager.bookVehicle(user1.getUserId(), bike1.getVehicleId(), startDate, 2);

        boolean cancelled = manager.cancelBooking(booking.getBookingId());
        assertTrue(cancelled, "Booking cancellation should succeed");
        assertTrue(bike1.isAvailable(), "Vehicle should be available after cancellation");
        assertFalse(booking.isActive(), "Booking should be inactive after cancellation");
    }

    @Test
    public void testMaintenanceWithFutureDate() {
        Date futureDate = new Date(System.currentTimeMillis() + 10000000);
        van1.scheduleMaintenance(futureDate);

        assertTrue(van1.isUnderMaintenance(),
                "Vehicle should be under maintenance when scheduled for future date");
        assertFalse(van1.isAvailable(),
                "Vehicle should NOT be available during scheduled maintenance period");
    }

    @Test
    public void testMaintenanceRejectsPastDate() {
        Date pastDate = new Date(System.currentTimeMillis() - 10000000);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> van1.scheduleMaintenance(pastDate),
                "Should reject past maintenance dates"
        );

        assertEquals("Maintenance date must be in the future", exception.getMessage());
        assertTrue(van1.isAvailable(), "Vehicle should remain available after rejected maintenance");
        assertNull(van1.getMaintenanceDate(), "Maintenance date should not be set");
    }

    @Test
    public void testSortVehicles() {
        List<Vehicle> sorted = manager.sortVehicles(manager.getVehicles());
        assertEquals(bike1, sorted.get(0), "Bike is cheapest ($50), should come first");
        assertEquals(car1, sorted.get(1), "Car is second cheapest ($100)");
        assertEquals(van1, sorted.get(2), "Van is most expensive ($150), should come last");
    }

    @Test
    public void testUserActiveBookings() {
        Date startDate = new Date();
        manager.bookVehicle(user1.getUserId(), car1.getVehicleId(), startDate, 1);
        manager.bookVehicle(user1.getUserId(), bike1.getVehicleId(), startDate, 1);

        List<Booking> activeBookings = user1.getActiveBookings();
        assertEquals(2, activeBookings.size(), "User should have 2 active bookings");
    }

    @Test
    public void testBookingWithZeroDays() {
        Date startDate = new Date();
        Booking booking = manager.bookVehicle(user1.getUserId(), car1.getVehicleId(), startDate, 0);
        assertNull(booking, "Booking with 0 days should fail and return null");
    }

    @Test
    public void testBookingWithUnavailableVehicle() {
        Date startDate = new Date();
        manager.bookVehicle(user1.getUserId(), car1.getVehicleId(), startDate, 1);

        Booking secondBooking = manager.bookVehicle(user1.getUserId(), car1.getVehicleId(), startDate, 1);
        assertNull(secondBooking, "Booking an unavailable vehicle should fail and return null");
        assertEquals(1, manager.getBookings().size(), "Only one booking should exist");
    }

    @Test
    public void testCancelAlreadyCancelledBooking() {
        Date startDate = new Date();
        Booking booking = manager.bookVehicle(user1.getUserId(), bike1.getVehicleId(), startDate, 1);

        assertTrue(manager.cancelBooking(booking.getBookingId()), "First cancellation should succeed");
        assertFalse(manager.cancelBooking(booking.getBookingId()), "Second cancellation should fail");
        assertTrue(bike1.isAvailable(), "Vehicle should remain available after failed cancellation");
    }

    @Test
    public void testBookingWithNonExistentVehicle() {
        Date startDate = new Date();
        Booking booking = manager.bookVehicle(user1.getUserId(), 999, startDate, 2);
        assertNull(booking, "Booking non-existent vehicle should fail and return null");
    }

    @Test
    public void testVehicleCreationWithNegativeDailyRate() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Van(4, "CheapVan", "BrandX", 2020, -50.0, 5.0),
                "Should reject negative daily rates"
        );

        assertEquals("Daily rate must be greater than zero", exception.getMessage());
        assertEquals(3, manager.getVehicles().size(), "No invalid vehicle should be added");
    }

    @Test
    public void testVehicleCreationWithZeroDailyRate() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Car(5, "FreeCar", "BrandY", 2023, 0.0, 4, "Automatic"),
                "Should reject zero daily rates"
        );

        assertEquals("Daily rate must be greater than zero", exception.getMessage());
        assertEquals(3, manager.getVehicles().size(), "No invalid vehicle should be added");
    }

    @Test
    public void testCancelNonExistentBooking() {
        boolean result = manager.cancelBooking(999);
        assertFalse(result, "Cancelling non-existent booking should fail and return false");
    }

    @Test
    public void testBookNonExistentUser() {
        Date startDate = new Date();
        Booking booking = manager.bookVehicle(999, car1.getVehicleId(), startDate, 1);
        assertNull(booking, "Booking for non-existent user should fail and return null");
    }
}