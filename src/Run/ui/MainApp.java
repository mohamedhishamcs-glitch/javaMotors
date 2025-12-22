package ui;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import classes.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class MainApp extends Application {

    // Core business logic manager - contains all vehicles, users, and bookings
    private final RentalManager manager = new RentalManager();

    // ========== UI COMPONENT REFERENCES ==========
    // These are class-level fields to allow access across methods

    // Show Vehicles tab components
    private TableView<Vehicle> vehicleTable;  // Main table displaying vehicles
    private CheckBox availableCheck, rentedCheck, maintenanceCheck;  // Filter controls
    private RadioButton noSortRB, sortRB;  // Sorting options
    private ToggleGroup sortGroup;  // Ensures only one sort option selected

    // Add Vehicle tab components
    private ComboBox<String> vehicleTypeBox;  // Vehicle type selector (Car/Bike/Van)
    private TextField brandField, modelField, yearField, rateField;  // Basic vehicle info
    private ComboBox<String> typeBox;  // Redundant? (appears to be duplicate of vehicleTypeBox)
    private TextField doorsField, transmissionField, bikeTypeField, cargoField;  // Type-specific fields

    // Show Users tab components
    private TableView<User> usersTable;  // Table of all users
    private ListView<String> userRentalsList;  // Shows rental history for selected user

    // Rent Vehicle tab components
    private ComboBox<User> rentUserBox;  // User selector (includes "Add New User" option)
    private VBox newUserBox;  // Container for new user fields (shown conditionally)
    private TextField newUserNameField, newUserEmailField, newUserPhoneField;  // New user details
    private final User ADD_USER_PLACEHOLDER = new User(-1, "➕ Add New User", "", "");  // Special user for adding new users
    private ComboBox<Vehicle> rentVehicleBox;  // Available vehicles of selected type
    private ComboBox<String> rentVehicleTypeBox;  // Vehicle type filter for rental
    private TextField rentDaysField;  // Rental duration
    private Label rentTotalCost;  // Calculated rental cost display

    // Return Vehicle tab components
    private ComboBox<Vehicle> returnVehicleBox;  // Currently rented vehicles

    // Maintenance tab components
    ComboBox<Vehicle> maintenanceVehicleBox;  // Available vehicles for maintenance

    // Observable list for filtered vehicles (used by vehicleTable)
    ObservableList<Vehicle> filteredVehicles = FXCollections.observableArrayList();

    /**
     * Application entry point - launches the JavaFX application
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Initializes and shows the main application window.
     *
     * Important initialization order:
     * 1. Create tab pane and add most tabs
     * 2. Initialize test data and UI state
     * 3. Add maintenance tab LAST to ensure manager has data
     *
     * This order prevents NullPointerExceptions when accessing manager data during UI creation.
     */
    @Override
    public void start(Stage stage) {
        // Create tab pane and disable tab closing
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Add tabs (except maintenance tab)
        tabPane.getTabs().addAll(
                showVehiclesTab(),
                addVehicleTab(),
                showUsersTab(),
                rentVehicleTab(),
                returnVehicleTab()
        );

        // Initialize test data and UI state BEFORE adding maintenance tab
        initializeTestData();
        initializeUI();

        // Add maintenance tab LAST to ensure manager has vehicles
        tabPane.getTabs().add(maintenanceTab());

        // Create and show scene
        Scene scene = new Scene(tabPane, 1100, 700);
        stage.setScene(scene);
        stage.setTitle("Vehicle Rental System");
        stage.show();
    }

    // ---------------- SHOW VEHICLES TAB ----------------
    /**
     * Creates the "Show Vehicles" tab with:
     * - Left sidebar: filtering and sorting controls
     * - Center: vehicle table with comprehensive information
     *
     * Features:
     * - Real-time filtering by availability status and vehicle type
     * - Sorting by daily rate
     * - Extra info column showing type-specific details
     * - Return date column for rented vehicles
     * - Remove vehicle functionality with confirmation
     */
    private Tab showVehiclesTab() {
        Tab tab = new Tab("Show Vehicles");
        BorderPane root = new BorderPane();

        // Left filters panel
        VBox filters = new VBox(10);
        filters.setPadding(new Insets(10));

        // Availability filters
        availableCheck = new CheckBox("Available Vehicles");
        rentedCheck = new CheckBox("Rented Vehicles");
        maintenanceCheck = new CheckBox("Under Maintenance");
        availableCheck.setSelected(true);
        rentedCheck.setSelected(true);
        maintenanceCheck.setSelected(true);

        // Vehicle type filter
        vehicleTypeBox = new ComboBox<>();
        vehicleTypeBox.getItems().addAll("All", "Car", "Bike", "Van");
        vehicleTypeBox.setValue("All");

        // Sorting options
        noSortRB = new RadioButton("No Sorting");
        sortRB = new RadioButton("Sort by rate");
        sortGroup = new ToggleGroup();
        noSortRB.setToggleGroup(sortGroup);
        sortRB.setToggleGroup(sortGroup);
        noSortRB.setSelected(true);

        filters.getChildren().addAll(
                new Label("Filter Vehicles"),
                availableCheck,
                rentedCheck,
                maintenanceCheck,
                vehicleTypeBox,
                new Separator(),
                new Label("Sort"),
                noSortRB,
                sortRB
        );

        // Center vehicle table
        vehicleTable = new TableView<>();

        // Define table columns
        TableColumn<Vehicle, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("vehicleId"));

        TableColumn<Vehicle, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getClass().getSimpleName())
        );

        TableColumn<Vehicle, String> brandCol = new TableColumn<>("Brand");
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brand"));

        TableColumn<Vehicle, String> modelCol = new TableColumn<>("Model");
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));

        TableColumn<Vehicle, Double> rateCol = new TableColumn<>("Rate");
        rateCol.setCellValueFactory(new PropertyValueFactory<>("dailyRate"));

        // Status column with dynamic status text
        TableColumn<Vehicle, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cell -> {
            Vehicle v = cell.getValue();
            if (v.isUnderMaintenance()) {
                return new SimpleStringProperty("Under Maintenance");
            } else if (v.isAvailable()) {
                return new SimpleStringProperty("Available");
            } else {
                return new SimpleStringProperty("Rented");
            }
        });

        // Extra Info column - shows type-specific details
        TableColumn<Vehicle, String> extraInfoCol = new TableColumn<>("Extra Info");
        extraInfoCol.setCellValueFactory(cell -> {
            Vehicle v = cell.getValue();
            if (v instanceof Car) {
                Car c = (Car) v;
                return new SimpleStringProperty("Doors: " + c.getNumDoors() + ", " + c.getTransmission());
            } else if (v instanceof Bike) {
                Bike b = (Bike) v;
                return new SimpleStringProperty("Type: " + b.getType());
            } else if (v instanceof Van) {
                Van van = (Van) v;
                return new SimpleStringProperty("Cargo: " + van.getCargoVolume() + " m³");
            }
            return new SimpleStringProperty("—");
        });
        extraInfoCol.setPrefWidth(200);  // Wider column for extra info

        // Return Date column - shows when rented vehicles will be returned
        TableColumn<Vehicle, String> returnDateCol = new TableColumn<>("Return Date");
        returnDateCol.setCellValueFactory(cell -> {
            Vehicle v = cell.getValue();
            Date returnDate = manager.getReturnDateForVehicle(v.getVehicleId());
            return new SimpleStringProperty(formatDate(returnDate));
        });

        // Remove column with delete button
        TableColumn<Vehicle, Void> removeCol = new TableColumn<>("Remove");
        removeCol.setPrefWidth(80);
        removeCol.setCellFactory(param -> new TableCell<Vehicle, Void>() {
            private final Button removeBtn = new Button("Remove");

            {
                // Button action handler
                removeBtn.setOnAction(event -> {
                    Vehicle vehicle = getTableView().getItems().get(getIndex());

                    // Confirmation dialog
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirm Removal");
                    confirm.setHeaderText("Remove vehicle: " + vehicle.getBrand() + " " + vehicle.getModel());
                    confirm.setContentText("Are you sure you want to permanently remove this vehicle?");

                    if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                        boolean removed = manager.removeVehicle(vehicle.getVehicleId());
                        if (removed) {
                            filteredVehicles.remove(vehicle);
                            returnVehicle(vehicle);
                        }
                    }
                });
            }

            // Display button only for non-empty rows
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeBtn);
                }
            }
        });

        // Add all columns to table
        vehicleTable.getColumns().addAll(
                idCol, typeCol, brandCol, modelCol, rateCol, statusCol,
                extraInfoCol, returnDateCol, removeCol
        );

        // Bind table to filtered vehicles list
        vehicleTable.setItems(filteredVehicles);

        // Add event listeners to update table when filters change
        availableCheck.setOnAction(e -> updateVehicleTable());
        rentedCheck.setOnAction(e -> updateVehicleTable());
        maintenanceCheck.setOnAction(e -> updateVehicleTable());
        vehicleTypeBox.setOnAction(e -> updateVehicleTable());
        noSortRB.setOnAction(e -> updateVehicleTable());
        sortRB.setOnAction(e -> updateVehicleTable());

        // Layout: filters on left, table in center
        root.setLeft(filters);
        root.setCenter(vehicleTable);
        tab.setContent(root);

        return tab;
    }

    // ---------------- ADD VEHICLE TAB ----------------
    /**
     * Creates the "Add Vehicle" tab with:
     * - Dynamic form that shows type-specific fields based on selection
     * - Input validation for numeric fields
     * - Error handling for invalid inputs
     *
     * Features:
     * - Shows/hides type-specific fields (doors/transmission for cars, etc.)
     * - Validates year, rate, and type-specific values
     * - Prevents adding vehicles with invalid data
     */
    private Tab addVehicleTab() {
        Tab tab = new Tab("Add Vehicle");
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // Vehicle type selector
        typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Car", "Bike", "Van");
        typeBox.setPromptText("Vehicle Type");

        // Basic vehicle info fields (always visible)
        VBox basicBox = new VBox(10,
                new Label("Brand"),
                brandField = new TextField() {{ setPromptText("Brand"); }},
                new Label("Model"),
                modelField = new TextField() {{ setPromptText("Model"); }},
                new Label("Year"),
                yearField = new TextField() {{ setPromptText("Year"); }},
                new Label("Daily Rate"),
                rateField = new TextField() {{ setPromptText("Daily Rate"); }}
        );
        basicBox.setVisible(false);
        basicBox.setManaged(false);

        // Type-specific fields (shown conditionally)
        VBox carBox = new VBox(10,
                new Label("Door Number"),
                doorsField = new TextField() {{ setPromptText("DoorsNumber"); }},
                new Label("Transmission"),
                transmissionField = new TextField() {{ setPromptText("Transmission"); }}
        );
        carBox.setVisible(false);
        carBox.setManaged(false);

        VBox bikeBox = new VBox(10,
                new Label("Bike Type"),
                bikeTypeField = new TextField() {{ setPromptText("Bike Type"); }}
        );
        bikeBox.setVisible(false);
        bikeBox.setManaged(false);

        VBox vanBox = new VBox(10,
                new Label("Cargo Volume (m³)"),
                cargoField = new TextField() {{ setPromptText("Cargo Volume (m³)"); }}
        );
        vanBox.setVisible(false);
        vanBox.setManaged(false);

        // Main form container
        VBox form = new VBox(8,
                basicBox,
                carBox,
                bikeBox,
                vanBox
        );

        // Show/hide type-specific fields based on selection
        typeBox.setOnAction(e -> {
            // Hide all type-specific fields first
            basicBox.setVisible(false);
            basicBox.setManaged(false);
            carBox.setVisible(false);
            carBox.setManaged(false);
            bikeBox.setVisible(false);
            bikeBox.setManaged(false);
            vanBox.setVisible(false);
            vanBox.setManaged(false);

            String type = typeBox.getValue();
            if (type == null) return;

            // Always show basic fields
            basicBox.setVisible(true);
            basicBox.setManaged(true);

            // Show appropriate type-specific fields
            switch (type) {
                case "Car" -> {
                    carBox.setVisible(true);
                    carBox.setManaged(true);
                }
                case "Bike" -> {
                    bikeBox.setVisible(true);
                    bikeBox.setManaged(true);
                }
                case "Van" -> {
                    vanBox.setVisible(true);
                    vanBox.setManaged(true);
                }
            }
        });

        // Add Vehicle button
        Button addBtn = new Button("Add Vehicle");
        addBtn.setOnAction(e -> {
            try {
                // Get form values
                String type = typeBox.getValue();
                String brand = brandField.getText();
                String model = modelField.getText();
                int year = Integer.parseInt(yearField.getText());
                double rate = Double.parseDouble(rateField.getText());

                // Validate basic inputs
                if(rate <= 0) {
                    showAlert("Invalid Input", "please enter a positive non-zero daily rate");
                    return;
                }
                if(year < 1500 || year > 2100){
                    showAlert("Invalid Input", "please enter a valid year");
                    return;
                }

                // Create appropriate vehicle type
                Vehicle v;
                int id = manager.getVehicles().size() + 1;
                switch (type) {
                    case "Car" -> {
                        int doors = Integer.parseInt(doorsField.getText());
                        if(doors <= 0) {
                            showAlert("Invalid Input", "please enter a positive non-zero door numbers");
                            return;
                        }
                        String transmission = transmissionField.getText();
                        doorsField.clear();
                        transmissionField.clear();
                        v = new Car(id, model, brand, year, rate, doors, transmission);
                    }
                    case "Bike" -> {
                        String bikeType = bikeTypeField.getText();
                        bikeTypeField.clear();
                        v = new Bike(id, model, brand, year, rate, bikeType);
                    }
                    case "Van" -> {
                        double cargo = Double.parseDouble(cargoField.getText());
                        if(cargo <= 0) {
                            showAlert("Invalid Input", "please enter a positive non-zero cargo volume");
                            return;
                        }
                        cargoField.clear();
                        v = new Van(id, model, brand, year, rate, cargo);
                    }
                    default ->  v = null;
                }
                if (v != null) manager.addVehicle(v);

                // Clear form fields
                typeBox.getSelectionModel().clearSelection();
                typeBox.setPromptText("Vehicle Type");
                brandField.clear();
                modelField.clear();
                yearField.clear();
                rateField.clear();

            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter valid numbers for year, rate, and vehicle-specific fields");
                return;
            }



            // Update UI after adding vehicle
            updateVehicleTable();
            updateRentVehicleOptions();
        });

        root.getChildren().addAll(typeBox, form, addBtn);
        tab.setContent(root);
        return tab;
    }

    // ---------------- SHOW USERS TAB ----------------
    /**
     * Creates the "Show Users" tab with:
     * - Left: table of all users
     * - Right: rental history for selected user
     *
     * Features:
     * - Split-pane layout for efficient space usage
     * - Real-time rental history display when user is selected
     * - Shows whether each rental was returned or is still active
     */
    private Tab showUsersTab() {
        Tab tab = new Tab("Show Users");

        SplitPane split = new SplitPane();

        // Users table
        usersTable = new TableView<>();
        TableColumn<User, Integer> idCol = new TableColumn<>("User ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("userId"));

        TableColumn<User, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        TableColumn<User, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(200);

        usersTable.getColumns().addAll(idCol, nameCol, emailCol, phoneCol);

        // Rental history list
        userRentalsList = new ListView<>();

        // Layout: users table on left, rentals on right
        split.getItems().addAll(usersTable, userRentalsList);
        tab.setContent(split);

        // Show rentals when user is selected
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser != null) showUserRentals(newUser);
        });

        return tab;
    }

    // ---------------- RENT VEHICLE TAB ----------------
    /**
     * Creates the "Rent Vehicle" tab with:
     * - User selection (existing or new)
     * - Vehicle type and specific vehicle selection
     * - Rental duration and cost calculation
     *
     * Features:
     * - Dynamic form for new user creation
     * - Real-time cost calculation
     * - Comprehensive input validation
     * - New user creation during rental process
     */
    private Tab rentVehicleTab() {
        Tab tab = new Tab("Rent Vehicle");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);

        // User selection
        grid.add(new Label("User"), 0, 0);
        rentUserBox = new ComboBox<>();
        grid.add(rentUserBox, 1, 0);

        // New user fields (shown conditionally)
        newUserNameField = new TextField();
        newUserNameField.setPromptText("Name");

        newUserEmailField = new TextField();
        newUserEmailField.setPromptText("Email");

        newUserPhoneField = new TextField();
        newUserPhoneField.setPromptText("Phone");

        newUserBox = new VBox(5,
                new Label("New User Details"),
                newUserNameField,
                newUserEmailField,
                newUserPhoneField
        );
        newUserBox.setVisible(false);
        newUserBox.setManaged(false);

        grid.add(newUserBox, 1, 1);

        // Show/hide new user fields based on selection
        rentUserBox.setOnAction(e -> {
            User selected = rentUserBox.getValue();
            boolean addingNew = selected != null && selected.getUserId() == -1;
            newUserBox.setVisible(addingNew);
            newUserBox.setManaged(addingNew);
        });

        // Vehicle selection
        grid.add(new Label("Vehicle Type"), 0, 2);
        rentVehicleTypeBox = new ComboBox<>();
        rentVehicleTypeBox.getItems().addAll("Car","Bike","Van");
        grid.add(rentVehicleTypeBox, 1, 2);

        grid.add(new Label("Vehicle"), 0, 3);
        rentVehicleBox = new ComboBox<>();
        grid.add(rentVehicleBox, 1, 3);

        // Rental duration and cost
        grid.add(new Label("Days"), 0, 4);
        rentDaysField = new TextField();
        grid.add(rentDaysField, 1, 4);

        grid.add(new Label("Total Cost"), 0, 5);
        rentTotalCost = new Label("$0.0");
        grid.add(rentTotalCost, 1, 5);

        // Action buttons
        Button calculateCostBtn = new Button("Calculate Cost");
        calculateCostBtn.setOnAction(e -> calculateRentalCost());
        grid.add(calculateCostBtn, 0, 6);

        Button confirmBtn = new Button("Confirm Rental");
        confirmBtn.setOnAction(e -> {
            confirmRental();
        });
        grid.add(confirmBtn, 1, 6);

        // Update available vehicles when type changes
        rentVehicleTypeBox.setOnAction(e -> updateRentVehicleOptions());

        tab.setContent(grid);
        return tab;
    }

    // ---------------- RETURN VEHICLE TAB ----------------
    /**
     * Creates the "Return Vehicle" tab with:
     * - Dropdown of currently rented vehicles
     * - Return button to complete the return process
     *
     * Features:
     * - Simple, focused interface for returning vehicles
     * - Immediate UI updates after return
     */
    private Tab returnVehicleTab() {
        Tab tab = new Tab("Return Vehicle");

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        returnVehicleBox = new ComboBox<>();
        returnVehicleBox.setPromptText("choose a rented Vehicle");

        root.getChildren().addAll(
                new Label("Rented Vehicle"),
                returnVehicleBox
        );

        Button returnBtn = new Button("Return Vehicle");
        returnBtn.setOnAction(e -> {
            returnVehicle(returnVehicleBox.getValue());
            returnVehicleBox.setValue(null);
        });

        root.getChildren().add(returnBtn);
        tab.setContent(root);
        return tab;
    }

    // ---------------- MAINTENANCE TAB ----------------
    /**
     * Creates the "Maintenance" tab with:
     * - Dropdown of available vehicles
     * - Date input for maintenance end date
     * - Schedule button to put vehicle under maintenance
     *
     * Features:
     * - Dynamic vehicle list that updates when vehicles become available
     * - Date validation using Java 8+ time API
     * - Prevents scheduling maintenance for past dates
     */
    private Tab maintenanceTab() {
        Tab tab = new Tab("Maintenance");
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label header = new Label("Schedule Vehicle Maintenance");
        header.setStyle("-fx-font-weight: bold;");

        // Vehicle selector for available vehicles only
        maintenanceVehicleBox = new ComboBox<>();
        maintenanceVehicleBox.setPromptText("Select Vehicle");

        // Initial population of available vehicles
        refreshMaintenanceVehicleCombo();

        // Maintenance end date input
        TextField dateField = new TextField();
        dateField.setPromptText("End Date (YYYY-MM-DD)");

        // Schedule button
        Button scheduleBtn = new Button("Schedule Maintenance");
        scheduleBtn.setOnAction(e -> {
            Vehicle v = maintenanceVehicleBox.getValue();
            String dateInput = dateField.getText().trim();

            // Validate inputs
            if (v == null) {
                showAlert("Missing", "Please select a vehicle.");
                return;
            }
            if (dateInput.isEmpty()) {
                showAlert("Missing", "Enter end date (e.g., 2025-12-25).");
                return;
            }

            try {
                // Parse and validate date using modern Java time API
                LocalDate localDate = LocalDate.parse(dateInput.trim());
                Date maintenanceEnd = Date.from(
                        localDate.atTime(23, 59, 59)
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                );

                // Prevent scheduling maintenance for past dates
                if (maintenanceEnd.before(new Date())) {
                    showAlert("Warning", "Date is in the past — vehicle will be immediately available!");
                    return;
                }

                // Schedule maintenance
                v.scheduleMaintenance(maintenanceEnd);
                showAlert("Done", v.getBrand() + " " + v.getModel() +
                        " is under maintenance until " + dateInput);

                refreshMaintenanceVehicleCombo();

                // Update UI
                updateVehicleTable(); // Refresh status in vehicle table
                maintenanceVehicleBox.setValue(null);
                dateField.clear();

            } catch (Exception ex) {
                showAlert("Invalid Date", "Use real date with format: YYYY-MM-DD");
            }
        });

        // Layout
        root.getChildren().addAll(header,
                new Label("Vehicle:"), maintenanceVehicleBox,
                new Label("Maintenance End:"), dateField,
                scheduleBtn
        );

        tab.setContent(root);
        return tab;
    }

    // -------------------- LOGIC METHODS --------------------
    /**
     * Initializes test data for demonstration purposes.
     * Creates sample users and vehicles to populate the system.
     */
    private void initializeTestData() {
        User u1 = new User(1,"Alice", "sdjs@mail.com", "012005482");
        User u2 = new User(2,"Bob", "sdjs@mail.com", "012005482");
        manager.addUser(u1); manager.addUser(u2);

        Vehicle c1 = new Car(1,"Model S","Tesla",2023,100,4,"Auto");
        Vehicle b1 = new Bike(2,"CBR500","Honda",2022,40,"Sport");
        Vehicle v1 = new Van(3,"Transit","Ford",2021,80,10.0);
        manager.addVehicle(c1); manager.addVehicle(b1); manager.addVehicle(v1);
    }

    /**
     * Initializes UI state after data is loaded.
     * Sets up initial table contents, combo boxes, and converters.
     */
    private void initializeUI() {
        updateVehicleTable();
        updateUsersTable();
        updateReturnVehicleOptions();

        // Converter for return vehicle combo box (formats display text)
        returnVehicleBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Vehicle v) {
                if (v == null) return "";
                return v.getClass().getSimpleName() +
                        " | ID: " + v.getVehicleId() +
                        " | " + v.getBrand() +
                        " " + v.getModel();
            }

            @Override
            public Vehicle fromString(String s) {
                return null;
            }
        });

        // Initialize rent user combo box with users + "Add New User" option
        ObservableList<User> users = FXCollections.observableArrayList(manager.getUsers());
        users.add(ADD_USER_PLACEHOLDER);
        rentUserBox.setItems(users);
    }

    /**
     * Refreshes the rent user combo box (used after adding new users).
     */
    private void initializeUserCombo() {
        ObservableList<User> users =
                FXCollections.observableArrayList(manager.getUsers());
        users.add(ADD_USER_PLACEHOLDER);
        rentUserBox.setItems(users);
    }

    /**
     * Updates the vehicle table based on current filter settings.
     *
     * Key logic:
     * - Clears and repopulates filteredVehicles list
     * - Applies availability filters (available, rented, maintenance)
     * - Applies vehicle type filter
     * - Applies sorting if selected
     * - Automatically makes vehicles available when maintenance period ends
     */
    private void updateVehicleTable() {
        System.out.println("updating vehicle table");
        filteredVehicles.clear();

        // Get current filter settings
        boolean showAvailable = availableCheck.isSelected();
        boolean showRented = rentedCheck.isSelected();
        boolean showMaintenance = maintenanceCheck.isSelected();
        String selectedType = vehicleTypeBox.getValue();

        // Get base vehicle list
        List<Vehicle> baseList = manager.getVehicles();

        // Apply sorting if selected
        if (sortRB.isSelected()) {
            baseList = manager.sortVehicles(baseList);
        }

        // Filter vehicles based on current settings
        for (Vehicle v : baseList) {
            // Auto-expire maintenance: if maintenance period ended, make available
            if (v.getMaintenanceDate() != null && !v.isUnderMaintenance()) {
                v.setAvailable(true);
            }

            // Determine if vehicle matches current status filters
            boolean statusMatch = false;
            if (v.isUnderMaintenance()) {
                statusMatch = showMaintenance;
            } else if (v.isAvailable()) {
                statusMatch = showAvailable;
            } else { // rented
                statusMatch = showRented;
            }

            // Type filter
            boolean typeMatch = selectedType == null ||
                    selectedType.equals("All") ||
                    v.getClass().getSimpleName().equals(selectedType);

            // Add to filtered list if both filters match
            if (statusMatch && typeMatch) {
                filteredVehicles.add(v);
            }
        }
    }

    /**
     * Updates the users table with current user data.
     */
    private void updateUsersTable() {
        ObservableList<User> data = FXCollections.observableArrayList(manager.getUsers());
        usersTable.setItems(data);
    }

    /**
     * Shows rental history for the specified user in the user rentals list.
     * Displays whether each rental was returned or is still active.
     */
    private void showUserRentals(User user) {
        ObservableList<String> rentalDetails = FXCollections.observableArrayList();

        for (Booking b : manager.getBookings()) {
            if (b.getUser().equals(user)) {
                String vehicleInfo = b.getVehicle().getBrand() + " " + b.getVehicle().getModel();
                String status = b.isActive() ? "Rented" : "Returned";
                String dateInfo = " (Booked: " + formatDate(b.getStartDate()) + ", " + b.getDays() + " days)";

                rentalDetails.add(vehicleInfo + " — " + status + dateInfo);
            }
        }

        userRentalsList.setItems(rentalDetails);
    }

    /**
     * Updates the rent vehicle combo box with available vehicles of the selected type.
     */
    private void updateRentVehicleOptions() {
        if (rentVehicleTypeBox.getValue() == null) {
            rentVehicleBox.setItems(FXCollections.observableArrayList());
            return;
        }
        String type = rentVehicleTypeBox.getValue();
        ObservableList<Vehicle> vehicles = FXCollections.observableArrayList();
        for (Vehicle v : manager.getVehicles()) {
            if (v.isAvailable() && v.getClass().getSimpleName().equals(type)) vehicles.add(v);
        }
        rentVehicleBox.setItems(vehicles);
    }

    /**
     * Updates the return vehicle combo box with currently rented vehicles.
     */
    private void updateReturnVehicleOptions() {
        ObservableList<Vehicle> rentedVehicles = FXCollections.observableArrayList();

        for (Vehicle v : manager.getVehicles()) {
            if (!v.isAvailable()) {
                rentedVehicles.add(v);
            }
        }

        returnVehicleBox.setItems(rentedVehicles);
    }

    /**
     * Refreshes the maintenance vehicle combo box with currently available vehicles.
     * Call this after any operation that changes vehicle availability (rent, return, maintenance).
     */
    private void refreshMaintenanceVehicleCombo() {
        if (maintenanceVehicleBox == null) return; // Safety check

        ObservableList<Vehicle> available = FXCollections.observableArrayList();
        for (Vehicle v : manager.getVehicles()) {
            if (v.isAvailable()) {
                available.add(v);
            }
        }
        maintenanceVehicleBox.setItems(available);
    }

    /**
     * Calculates and displays the rental cost based on selected vehicle and days.
     * Includes input validation.
     */
    private void calculateRentalCost() {
        try{
            Vehicle v = rentVehicleBox.getValue();
            if (v == null || rentDaysField.getText().isEmpty()) {
                showAlert("Invalid Input", "Please choose a vehicle or how many days for renting the vehicle");
                return;
            }
            int days = Integer.parseInt(rentDaysField.getText());
            if (days <= 0){
                showAlert("Invalid Input", "Please a positive number of renting days");
                return;
            }
            rentTotalCost.setText("$" + v.calculateRentalPrice(days));
        } catch (NumberFormatException ex) {
            rentTotalCost.setText("Invalid days");
        }
    }

    /**
     * Confirms a rental after validating all inputs.
     *
     * Handles:
     * - New user creation during rental
     * - Input validation for all fields
     * - Booking creation through RentalManager
     * - UI updates after successful booking
     */
    private void confirmRental() {
        try{
            // Validate user selection
            User selectedUser = rentUserBox.getValue();
            if (selectedUser == null) {
                showAlert("Missing Selection", "Please select a user.");
                return;
            }

            // Validate vehicle selection
            Vehicle v = rentVehicleBox.getValue();
            if (v == null) {
                showAlert("Missing Selection", "Please select a vehicle.");
                return;
            }

            // Handle new user creation
            User finalUser;
            if (selectedUser.getUserId() == -1) {
                int newId = manager.getUsers().size() + 1;

                // Validate new user fields
                boolean newUserError = newUserNameField.getText().trim().isEmpty()
                        || newUserEmailField.getText().trim().isEmpty()
                        || newUserPhoneField.getText().trim().isEmpty();

                if (newUserError) {
                    showAlert("Invalid Input", "Please enter name, email, and phone for the new user.");
                    return;
                }

                // Create new user
                finalUser = new User(
                        newId,
                        newUserNameField.getText(),
                        newUserEmailField.getText(),
                        newUserPhoneField.getText()
                );

                manager.addUser(finalUser);

                // Refresh UI
                updateUsersTable();
                initializeUserCombo();
            } else {
                finalUser = selectedUser;
            }

            // Create booking
            int days = Integer.parseInt(rentDaysField.getText());
            if (days <= 0){
                showAlert("Invalid Input", "Please a positive number of renting days");
                return;
            }
            manager.bookVehicle(finalUser.getUserId(), v.getVehicleId(), new Date(), days);

            // Update UI
            refreshMaintenanceVehicleCombo();
        } catch (NumberFormatException ex) {
            showAlert("Invalid Input", "Please enter valid amount of renting days");
            return;
        }

        // Clear form and update UI
        rentUserBox.getSelectionModel().clearSelection();
        newUserNameField.clear();
        newUserEmailField.clear();
        newUserPhoneField.clear();
        rentVehicleTypeBox.getSelectionModel().clearSelection();
        rentVehicleBox.getSelectionModel().clearSelection();
        rentDaysField.clear();
        rentTotalCost.setText("$0.0");

        updateVehicleTable();
        updateUsersTable();
        updateRentVehicleOptions();
        updateReturnVehicleOptions();
    }

    /**
     * Returns the selected vehicle and updates UI accordingly.
     */
    private void returnVehicle(Vehicle v) {
        if (v == null) return;

        for (Booking b : manager.getBookings()) {
            if (b.getVehicle().equals(v) && b.isActive()) {
                b.checkOut();
                refreshMaintenanceVehicleCombo();
                break;
            }
        }

        updateVehicleTable();
        updateUsersTable();
        updateRentVehicleOptions();
        updateReturnVehicleOptions();
    }

    /**
     * Shows an error alert dialog with the specified title and message.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Formats a Date object as "MMM dd, yyyy" (e.g., "Dec 15, 2025").
     * Returns "—" for null dates.
     */
    private String formatDate(Date date) {
        if (date == null) return "—";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy");
        return sdf.format(date);
    }
}