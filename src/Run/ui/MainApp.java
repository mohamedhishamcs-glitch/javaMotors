package Run.ui;

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

    private final RentalManager manager = new RentalManager();

    // UI references for controller
    //show vehicles tab
    private TableView<Vehicle> vehicleTable;
    private CheckBox availableCheck, rentedCheck, maintenanceCheck;
    private RadioButton noSortRB, sortRB;
    private ToggleGroup sortGroup;
    //add vehicle tab
    private ComboBox<String> vehicleTypeBox;
    private TextField brandField;
    private TextField modelField;
    private TextField yearField;
    private TextField rateField;
    private ComboBox<String> typeBox;
    private TextField doorsField;
    private TextField transmissionField;
    private TextField bikeTypeField;
    private TextField cargoField;
    //show users tab
    private TableView<User> usersTable;
    private ListView<String> userRentalsList;
    //rent vehicle tab
    private ComboBox<User> rentUserBox;
    private VBox newUserBox;
    private TextField newUserNameField;
    private TextField newUserEmailField;
    private TextField newUserPhoneField;
    private final User ADD_USER_PLACEHOLDER = new User(-1, "➕ Add New User", "", "");
    private ComboBox<Vehicle> rentVehicleBox;
    private ComboBox<String> rentVehicleTypeBox;
    private TextField rentDaysField;
    private Label rentTotalCost;
    //return vehicle tab
    private ComboBox<Vehicle> returnVehicleBox;
    //maintenance tab
    ComboBox<Vehicle> maintenanceVehicleBox;

    ObservableList<Vehicle> filteredVehicles = FXCollections.observableArrayList();


    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {

        // --- TAB PANE ---
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().addAll(
                showVehiclesTab(),
                addVehicleTab(),
                showUsersTab(),
                rentVehicleTab(),
                returnVehicleTab()
        );

        // --- Initialize test data ---
        initializeTestData();

        // --- Initialize UI ---
        initializeUI();

        tabPane.getTabs().add(maintenanceTab());

        // --- Scene ---
        Scene scene = new Scene(tabPane, 1100, 700);
        stage.setScene(scene);
        stage.setTitle("Vehicle Rental System");
        stage.show();

    }

    // ---------------- SHOW VEHICLES TAB ----------------
    private Tab showVehiclesTab() {
        Tab tab = new Tab("Show Vehicles");
        BorderPane root = new BorderPane();

        // Left filters
        VBox filters = new VBox(10);
        filters.setPadding(new Insets(10));

        availableCheck = new CheckBox("Available Vehicles");
        rentedCheck = new CheckBox("Rented Vehicles");
        maintenanceCheck = new CheckBox("Under Maintenance");
        availableCheck.setSelected(true);
        rentedCheck.setSelected(true);
        maintenanceCheck.setSelected(true);

        vehicleTypeBox = new ComboBox<>();
        vehicleTypeBox.getItems().addAll("All", "Car", "Bike", "Van");
        vehicleTypeBox.setValue("All");

        noSortRB = new RadioButton("No Sorting");
        sortRB = new RadioButton("Sort by rate");
        sortGroup = new ToggleGroup();
        noSortRB.setToggleGroup(sortGroup);
        sortRB.setToggleGroup(sortGroup);
        noSortRB.setSelected(true);

        filters.getChildren().addAll(new Label("Filter Vehicles"), availableCheck, rentedCheck, maintenanceCheck, vehicleTypeBox
                                    ,new Separator(), new Label("Sort"), noSortRB,sortRB);




        // Center table
        vehicleTable = new TableView<>();

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

        // Extra Info column (doors, cargo, bike type, etc.)
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
        extraInfoCol.setPrefWidth(200);

        // Return Date column (only shown if rented)
        TableColumn<Vehicle, String> returnDateCol = new TableColumn<>("Return Date");
        returnDateCol.setCellValueFactory(cell -> {
            Vehicle v = cell.getValue();
            Date returnDate = manager.getReturnDateForVehicle(v.getVehicleId());
            return new SimpleStringProperty(formatDate(returnDate));
        });

        TableColumn<Vehicle, Void> removeCol = new TableColumn<>("Remove");
        removeCol.setPrefWidth(80); // Fits button nicely
        removeCol.setCellFactory(param -> new TableCell<Vehicle, Void>() {
            private final Button removeBtn = new Button("Remove");

            {
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
                        }
                    }
                });
            }

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


        vehicleTable.getColumns().addAll(idCol, typeCol, brandCol, modelCol, rateCol, statusCol,
                                         extraInfoCol, returnDateCol, removeCol);

        vehicleTable.setItems(filteredVehicles);

        availableCheck.setOnAction(e -> updateVehicleTable());
        rentedCheck.setOnAction(e -> updateVehicleTable());
        maintenanceCheck.setOnAction(e -> updateVehicleTable());
        vehicleTypeBox.setOnAction(e -> updateVehicleTable());
        noSortRB.setOnAction(e -> updateVehicleTable());
        sortRB.setOnAction(e -> updateVehicleTable());

        root.setLeft(filters);
        root.setCenter(vehicleTable);
        tab.setContent(root);

        return tab;
    }

    // ---------------- ADD VEHICLE TAB ----------------
    private Tab addVehicleTab() {
        Tab tab = new Tab("Add Vehicle");
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Car", "Bike", "Van");
        typeBox.setPromptText("Vehicle Type");

        VBox basicBox = new VBox(10,
                brandField = new TextField() {{ setPromptText("Brand"); }},
                modelField = new TextField() {{ setPromptText("Model"); }},
                yearField = new TextField() {{ setPromptText("Year"); }},
                rateField = new TextField() {{ setPromptText("Daily Rate"); }}
        );
        basicBox.setVisible(false);
        basicBox.setManaged(false);

        //car specific fields
        VBox carBox = new VBox(10,
                doorsField = new TextField() {{ setPromptText("DoorsNumber"); }},
                transmissionField = new TextField() {{ setPromptText("Transmission"); }}
        );
        carBox.setVisible(false);
        carBox.setManaged(false);

        //bike specific fields
        VBox bikeBox = new VBox(10,
                bikeTypeField = new TextField() {{ setPromptText("Bike Type"); }}
        );
        bikeBox.setVisible(false);
        bikeBox.setManaged(false);

        //van specific fields
        VBox vanBox = new VBox(10,
                cargoField = new TextField() {{ setPromptText("Cargo Volume (m³)"); }}
        );
        vanBox.setVisible(false);
        vanBox.setManaged(false);


        VBox form = new VBox(8,
                basicBox,
                carBox,
                bikeBox,
                vanBox
        );

        typeBox.setOnAction(e -> {
            carBox.setVisible(false);
            carBox.setManaged(false);

            bikeBox.setVisible(false);
            bikeBox.setManaged(false);

            vanBox.setVisible(false);
            vanBox.setManaged(false);

            String type = typeBox.getValue();
            if (type == null) return;
            basicBox.setVisible(true);
            basicBox.setManaged(true);

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


        Button addBtn = new Button("Add Vehicle");
        addBtn.setOnAction(e -> {
            try {
                String type = typeBox.getValue();
                String brand = brandField.getText();
                String model = modelField.getText();
                int year = Integer.parseInt(yearField.getText());
                double rate = Double.parseDouble(rateField.getText());
                typeBox.getSelectionModel().clearSelection();
                brandField.clear();
                modelField.clear();
                yearField.clear();
                rateField.clear();

                Vehicle v;
                int id = manager.getVehicles().size() + 1;
                switch (type) {
                    case "Car" -> {
                        int doors = Integer.parseInt(doorsField.getText());
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
                        cargoField.clear();
                        v = new Van(id, model, brand, year, rate, cargo);
                    }
                    default ->  v = null;
                }
                if (v != null) manager.addVehicle(v);

            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter valid numbers for year, rate, and vehicle-specific fields");
                return;
            }

            updateVehicleTable();
            updateRentVehicleOptions();
        });

        root.getChildren().addAll(typeBox, form, addBtn);
        tab.setContent(root);
        return tab;
    }

    // ---------------- SHOW USERS TAB ----------------
    private Tab showUsersTab() {
        Tab tab = new Tab("Show Users");

        SplitPane split = new SplitPane();

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

        userRentalsList = new ListView<>();

        split.getItems().addAll(usersTable, userRentalsList);
        tab.setContent(split);

        // Show rentals on click
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser != null) showUserRentals(newUser);
        });

        return tab;
    }

    // ---------------- RENT VEHICLE TAB ----------------
    private Tab rentVehicleTab() {
        Tab tab = new Tab("Rent Vehicle");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);


        //show a list of users and can create a new user
        grid.add(new Label("User"), 0, 0);
        rentUserBox = new ComboBox<>();
        grid.add(rentUserBox, 1, 0);

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

        rentUserBox.setOnAction(e -> {
            User selected = rentUserBox.getValue();

            boolean addingNew = selected != null && selected.getUserId() == -1;

            newUserBox.setVisible(addingNew);
            newUserBox.setManaged(addingNew);
        });



        grid.add(new Label("Vehicle Type"), 0, 2);
        rentVehicleTypeBox = new ComboBox<>();
        rentVehicleTypeBox.getItems().addAll("Car","Bike","Van");
        grid.add(rentVehicleTypeBox, 1, 2);

        grid.add(new Label("Vehicle"), 0, 3);
        rentVehicleBox = new ComboBox<>();
        grid.add(rentVehicleBox, 1, 3);

        grid.add(new Label("Days"), 0, 4);
        rentDaysField = new TextField();
        grid.add(rentDaysField, 1, 4);

        grid.add(new Label("Total Cost"), 0, 5);
        rentTotalCost = new Label("$0.0");
        grid.add(rentTotalCost, 1, 5);

        Button calculateCostBtn = new Button("Calculate Cost");
        calculateCostBtn.setOnAction(e -> calculateRentalCost());
        grid.add(calculateCostBtn, 0, 6);

        Button confirmBtn = new Button("Confirm Rental");
        confirmBtn.setOnAction(e -> {
            confirmRental();

        });
        grid.add(confirmBtn, 1, 6);

        // Update vehicles when type changes
        rentVehicleTypeBox.setOnAction(e -> updateRentVehicleOptions());

        tab.setContent(grid);
        return tab;
    }

    // ---------------- RETURN VEHICLE TAB ----------------
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
            returnVehicle();
        });

        root.getChildren().add(returnBtn);
        tab.setContent(root);
        return tab;
    }

    private Tab maintenanceTab() {
        Tab tab = new Tab("Maintenance");
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label header = new Label("Schedule Vehicle Maintenance");
        header.setStyle("-fx-font-weight: bold;");

        maintenanceVehicleBox = new ComboBox<>();
        maintenanceVehicleBox.setPromptText("Select Vehicle");

        // Initial population
        refreshMaintenanceVehicleCombo(maintenanceVehicleBox);

        ObservableList<Vehicle> allVehicles = FXCollections.observableList(manager.getVehicles());
        allVehicles.addListener((ListChangeListener.Change<? extends Vehicle> c) -> {
            refreshMaintenanceVehicleCombo(maintenanceVehicleBox);
        });


        TextField dateField = new TextField();
        dateField.setPromptText("End Date (YYYY-MM-DD)");

        Button scheduleBtn = new Button("Schedule Maintenance");
        scheduleBtn.setOnAction(e -> {
            Vehicle v = maintenanceVehicleBox.getValue();
            String dateInput = dateField.getText().trim();

            if (v == null) {
                showAlert("Missing", "Please select a vehicle.");
                return;
            }
            if (dateInput.isEmpty()) {
                showAlert("Missing", "Enter end date (e.g., 2025-12-25).");
                return;
            }

            try {
                // Parse date safely
                LocalDate localDate = LocalDate.parse(dateInput.trim());
                Date maintenanceEnd = Date.from(
                        localDate.atTime(23, 59, 59)
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                );

                if (maintenanceEnd.before(new Date())) {
                    showAlert("Warning", "Date is in the past — vehicle will be immediately available!");
                    return;
                }

                v.scheduleMaintenance(maintenanceEnd);
                showAlert("Done", v.getBrand() + " " + v.getModel() +
                        " is under maintenance until " + dateInput);

                updateVehicleTable(); // Refresh status
                maintenanceVehicleBox.setValue(null);
                dateField.clear();

            } catch (Exception ex) {
                showAlert("Invalid Date", "Use real date with format: YYYY-MM-DD");
            }
        });

        root.getChildren().addAll(header,
                new Label("Vehicle:"), maintenanceVehicleBox,
                new Label("Maintenance End:"), dateField,
                scheduleBtn
        );

        tab.setContent(root);
        return tab;
    }


    // -------------------- LOGIC METHODS --------------------
    private void initializeTestData() {
        User u1 = new User(1,"Alice", "sdjs@mail.com", "012005482");
        User u2 = new User(2,"Bob", "sdjs@mail.com", "012005482");
        manager.addUser(u1); manager.addUser(u2);

        Vehicle c1 = new Car(1,"Model S","Tesla",2023,100,4,"Auto");
        Vehicle b1 = new Bike(2,"CBR500","Honda",2022,40,"Sport");
        Vehicle v1 = new Van(3,"Transit","Ford",2021,80,10.0);
        manager.addVehicle(c1); manager.addVehicle(b1); manager.addVehicle(v1);
    }

    private void initializeUI() {
        updateVehicleTable();
        updateUsersTable();
        updateReturnVehicleOptions();
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

        ObservableList<User> users = FXCollections.observableArrayList(manager.getUsers());
        users.add(ADD_USER_PLACEHOLDER);
        rentUserBox.setItems(users);
    }

    private void initializeUserCombo() {
        ObservableList<User> users =
                FXCollections.observableArrayList(manager.getUsers());
        users.add(ADD_USER_PLACEHOLDER);
        rentUserBox.setItems(users);
    }

    private void updateVehicleTable() {
        System.out.println("updating vehicle table");
        filteredVehicles.clear();

        boolean showAvailable = availableCheck.isSelected();
        boolean showRented = rentedCheck.isSelected();
        boolean showMaintenance = maintenanceCheck.isSelected();  // ← new
        String selectedType = vehicleTypeBox.getValue();

        List<Vehicle> baseList = manager.getVehicles();

        if (sortRB.isSelected()) {
            baseList = manager.sortVehicles(baseList);
        }

        for (Vehicle v : baseList) {
            if (v.getMaintenanceDate() != null && !v.isUnderMaintenance()) {
                v.setAvailable(true);
            }

            //Status matching logic:
            boolean statusMatch = false;

            if (v.isUnderMaintenance()) {
                System.out.println("under maintenance");
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

            if (statusMatch && typeMatch) {
                filteredVehicles.add(v);
            }
        }
    }

    private void updateUsersTable() {
        ObservableList<User> data = FXCollections.observableArrayList(manager.getUsers());
        usersTable.setItems(data);
    }

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

    private void updateReturnVehicleOptions() {
        ObservableList<Vehicle> rentedVehicles = FXCollections.observableArrayList();

        for (Vehicle v : manager.getVehicles()) {
            if (!v.isAvailable()) {
                rentedVehicles.add(v);
            }
        }

        returnVehicleBox.setItems(rentedVehicles);
    }

    private void refreshMaintenanceVehicleCombo(ComboBox<Vehicle> combo) {
        ObservableList<Vehicle> available = FXCollections.observableArrayList();
        for (Vehicle v : manager.getVehicles()) {
            if (v.isAvailable()) {
                available.add(v);
            }
        }
        combo.setItems(available);
    }

    private void calculateRentalCost() {
        try{
            Vehicle v = rentVehicleBox.getValue();
            if (v == null || rentDaysField.getText().isEmpty()) {
                showAlert("Invalid Input", "Please choose a vehicle or how many days for renting the vehicle");
                return;
            }
            int days = Integer.parseInt(rentDaysField.getText());
            rentTotalCost.setText("$" + v.calculateRentalPrice(days));
        } catch (NumberFormatException ex) {
            rentTotalCost.setText("Invalid days");
        }
    }

    private void confirmRental() {
        try{
            User selectedUser = rentUserBox.getValue();
            if (selectedUser == null) {
                showAlert("Missing Selection", "Please select a user.");
                return;
            }
            Vehicle v = rentVehicleBox.getValue();
            if (v == null) {
                showAlert("Missing Selection", "Please select a vehicle.");
                return;
            }

            User finalUser;
            if (selectedUser.getUserId() == -1) {
                int newId = manager.getUsers().size() + 1;

                boolean newUserError = newUserNameField.getText().trim().isEmpty()
                                    || newUserEmailField.getText().trim().isEmpty()
                                    || newUserPhoneField.getText().trim().isEmpty();

                if (newUserError) {
                    showAlert("Invalid Input", "Please enter name, email, and phone for the new user.");
                    return;
                }

                finalUser = new User(
                        newId,
                        newUserNameField.getText(),
                        newUserEmailField.getText(),
                        newUserPhoneField.getText()
                );

                manager.addUser(finalUser);

                // refresh user lists
                updateUsersTable();
                initializeUserCombo();

            } else {
                finalUser = selectedUser;
            }

            int days = Integer.parseInt(rentDaysField.getText());
            manager.bookVehicle(finalUser.getUserId(), v.getVehicleId(), new Date(), days);
            refreshMaintenanceVehicleCombo(maintenanceVehicleBox);
        } catch (NumberFormatException ex) {
            showAlert("Invalid Input", "Please enter valid amount of days");
            return;
        }

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

    private void returnVehicle() {
        Vehicle v = returnVehicleBox.getValue();
        if (v == null) return;

        for (Booking b : manager.getBookings()) {
            if (b.getVehicle().equals(v) && b.isActive()) {
                b.checkOut();
                refreshMaintenanceVehicleCombo(maintenanceVehicleBox);
                break;
            }
        }

        returnVehicleBox.setValue(null);


        updateVehicleTable();
        updateUsersTable();
        updateRentVehicleOptions();
        updateReturnVehicleOptions();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String formatDate(Date date) {
        if (date == null) return "—";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy");
        return sdf.format(date);
    }
}
