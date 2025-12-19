module VRS {
    requires javafx.fxml;
    requires javafx.controls;
    requires java.sql;
    opens ui;
    opens classes to javafx.base, javafx.fxml;
}