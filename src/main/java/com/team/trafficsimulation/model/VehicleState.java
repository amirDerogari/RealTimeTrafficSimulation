package com.team.trafficsimulation.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;


/**
 * The {@code VehicleState} class represents the current status of a single vehicle within the simulation.
 * <p>
 * It serves as a mutable model specifically designed for JavaFX UI binding. Key properties
 * like position (X, Y), speed, and angle are stored as {@link javafx.beans.property.Property} objects,
 * allowing the view to automatically react to changes without manual polling if bindings are used.
 * </p>
 * <p>
 * This class holds both physical data (coordinates, speed) and logical network data
 * (current edge, lane index).
 * </p>
 */
public class VehicleState {

    private final StringProperty id;
    private final StringProperty type;
    private final DoubleProperty x;
    private final DoubleProperty y;
    private final DoubleProperty speed;

    /** The rotation angle of the vehicle in degrees (0 = East, 90 = South usually in JavaFX). */
    private final DoubleProperty angle;

    private Color color;
    private String currentEdge;
    private String currentLane;
    private double lanePosition;
    private long spawnTime;

    /**
     * Constructs a vehicle with a default type of "car".
     *
     * @param id The unique identifier for the vehicle.
     * @param x  The initial X coordinate.
     * @param y  The initial Y coordinate.
     */
    public VehicleState(String id, double x, double y) {
        this(id, x, y, "car");
    }

    /**
     * Constructs a new VehicleState.
     * <p>
     * Initializes properties, sets the spawn timestamp, and assigns a random color
     * for visual distinction.
     * </p>
     *
     * @param id   The unique identifier for the vehicle.
     * @param x    The initial X coordinate.
     * @param y    The initial Y coordinate.
     * @param type The vehicle type identifier (e.g., "bus", "truck").
     */
    public VehicleState(String id, double x, double y, String type) {
        this.id = new SimpleStringProperty(id);
        this.type = new SimpleStringProperty(type);
        this.x = new SimpleDoubleProperty(x);
        this.y = new SimpleDoubleProperty(y);
        this.speed = new SimpleDoubleProperty(0.0);
        this.angle = new SimpleDoubleProperty(0.0);
        this.color = generateRandomColor();
        this.spawnTime = System.currentTimeMillis();
    }


    /**
     * Updates the physical coordinates of the vehicle.
     * <p>
     * This method automatically calculates the new heading {@code angle} based on the
     * movement vector (delta X, delta Y) if the vehicle has moved significantly.
     * </p>
     *
     * @param newX The new X coordinate.
     * @param newY The new Y coordinate.
     */
    public void updatePosition(double newX, double newY) {
        double oldX = this.x.get();
        double oldY = this.y.get();

        // Only update angle if movement is significant to avoid jitter
        if (Math.abs(newX - oldX) > 0.01 || Math.abs(newY - oldY) > 0.01) {
            double dx = newX - oldX;
            double dy = newY - oldY;
            // Calculate angle in radians and convert to degrees
            double angleRad = Math.atan2(dy, dx);
            this.angle.set(Math.toDegrees(angleRad));
        }

        this.x.set(newX);
        this.y.set(newY);
    }

    /**
     * Updates all dynamic properties of the vehicle in a single call.
     *
     * @param newX     The new X coordinate.
     * @param newY     The new Y coordinate.
     * @param newSpeed The current speed in m/s.
     * @param edge     The ID of the edge the vehicle is currently on.
     * @param lane     The ID of the specific lane.
     * @param lanePos  The distance from the start of the lane (in meters).
     */
    public void updateFullState(double newX, double newY, double newSpeed,
                                String edge, String lane, double lanePos) {
        updatePosition(newX, newY);
        this.speed.set(newSpeed);
        this.currentEdge = edge;
        this.currentLane = lane;
        this.lanePosition = lanePos;
    }


    /**
     * Selects a random color from a predefined palette.
     *
     * @return A JavaFX {@link Color} object.
     */
    private Color generateRandomColor() {
        Color[] vehicleColors = {
                Color.rgb(220, 20, 20),
                Color.rgb(20, 100, 220),
                Color.rgb(20, 180, 20),
                Color.rgb(200, 180, 20),
                Color.rgb(150, 50, 200),
                Color.rgb(255, 140, 0),
                Color.rgb(100, 100, 100),
                Color.rgb(0, 150, 150)
        };
        return vehicleColors[(int)(Math.random() * vehicleColors.length)];
    }

    public String getId() { return id.get(); }
    public String getType() { return type.get(); }
    public double getX() { return x.get(); }
    public double getY() { return y.get(); }
    public double getSpeed() { return speed.get(); }

    /** @return The current heading angle in degrees. */
    public double getAngle() { return angle.get(); }
    public Color getColor() { return color; }
    public String getCurrentEdge() { return currentEdge; }
    public String getCurrentLane() { return currentLane; }
    public double getLanePosition() { return lanePosition; }


    /**
     * Calculates how long the vehicle has been tracked by this state object.
     *
     * @return The duration in seconds since the object was instantiated.
     */
    public double getAliveTime() {
        return (System.currentTimeMillis() - spawnTime) / 1000.0;
    }

    @Override
    public String toString() {
        return String.format("Vehicle[id=%s, type=%s, x=%.2f, y=%.2f, speed=%.2f m/s]",
                getId(), getType(), getX(), getY(), getSpeed());
    }
}
