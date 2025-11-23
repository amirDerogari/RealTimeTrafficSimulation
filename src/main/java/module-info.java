module com.team.trafficsimulation {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.base;
    requires java.xml;

    exports com.team.trafficsimulation;
    opens com.team.trafficsimulation.controller to javafx.fxml;
    exports com.team.trafficsimulation.model;
    requires TraaS;
}