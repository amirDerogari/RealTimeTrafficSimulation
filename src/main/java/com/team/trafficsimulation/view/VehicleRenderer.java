package com.team.trafficsimulation.view;

import com.team.trafficsimulation.model.VehicleState;
import com.team.trafficsimulation.controller.Viewport;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@code VehicleRenderer} class is a static utility responsible for drawing vehicles
 * on the JavaFX canvas.
 * <p>
 * It supports two rendering modes:
 * <ul>
 *   <li><b>Image-based:</b> Renders sprites (car.png, bus.png, etc.) if available and zoom is sufficient.</li>
 *   <li><b>Shape-based:</b> Renders geometric primitives (rectangles) as a fallback or at low zoom levels.</li>
 * </ul>
 * It handles the coordinate transformation, rotation based on vehicle heading, and display of
 * text labels (ID, Speed).
 * </p>
 */
public class VehicleRenderer {

    /** Cache for loaded vehicle images to prevent reloading from disk every frame. */
    private static final Map<String, Image> vehicleImages = new HashMap<>();

    /** Standard length of a vehicle in world units (meters). */
    private static final double DEFAULT_VEHICLE_SIZE = 4.5;

    /** Standard width of a vehicle in world units (meters). */
    private static final double DEFAULT_VEHICLE_WIDTH = 2.0;

    /** Rotation offset (in degrees) if the source images are not pointing East (0 degrees). */
    private static final double IMAGE_ORIENTATION_OFFSET = 0;

    // Static block to preload images when the class is first accessed
    static {
        loadVehicleImages();
    }

    /**
     * Loads vehicle sprite images from the "resources/vehicles/" directory.
     * Populates the {@code vehicleImages} map.
     */
    private static void loadVehicleImages() {
        try {
            String[] imageTypes = {"car", "bus", "truck", "motorcycle", "bike"};
            for (String type : imageTypes) {
                File imgFile = new File("resources/vehicles/" + type + ".png");
                if (imgFile.exists()) {
                    vehicleImages.put(type, new Image(imgFile.toURI().toString()));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading vehicle images: " + e.getMessage());
        }
    }

    /**
     * Main method to render a single vehicle.
     * <p>
     * Decides whether to use image or shape rendering based on zoom level and image availability.
     * Also handles the conditional rendering of detail text.
     * </p>
     *
     * @param gc          The GraphicsContext to draw on.
     * @param vehicle     The vehicle model containing position and state data.
     * @param viewport    The viewport for coordinate conversion.
     * @param showDetails Flag to enable text labels (ID, speed).
     */
    public static void renderVehicle(GraphicsContext gc, VehicleState vehicle,
                                     Viewport viewport, boolean showDetails) {
        double screenX = viewport.worldToScreenX(vehicle.getX());
        double screenY = viewport.worldToScreenY(vehicle.getY());

        double vehicleLength = DEFAULT_VEHICLE_SIZE * viewport.getZoom();
        double vehicleWidth = DEFAULT_VEHICLE_WIDTH * viewport.getZoom();

        Image vehicleImage = getVehicleImage(vehicle);

        // Use images only if available and zoom is close enough
        if (vehicleImage != null && viewport.getZoom() > 0.5) {
            renderWithImage(gc, vehicleImage, screenX, screenY,
                    vehicleLength, vehicleWidth, vehicle);
        } else {
            renderWithShape(gc, screenX, screenY, vehicleLength,
                    vehicleWidth, vehicle);
        }

        // Draw text details if zoomed in
        if (showDetails && viewport.getZoom() > 1.0) {
            renderVehicleDetails(gc, vehicle, screenX, screenY, viewport);
        }
    }

    /**
     * Renders the vehicle using a sprite image.
     * <p>
     * Rotates the canvas context to match the vehicle's heading angle before drawing the image.
     * </p>
     *
     * @param gc           The GraphicsContext.
     * @param image        The sprite to draw.
     * @param screenX      The center X coordinate on screen.
     * @param screenY      The center Y coordinate on screen.
     * @param length       The scaled length of the vehicle.
     * @param width        The scaled width of the vehicle.
     * @param vehicle      The vehicle model.
     */
    private static void renderWithImage(GraphicsContext gc, Image image,
                                        double screenX, double screenY,
                                        double length, double width,
                                        VehicleState vehicle) {
        gc.save();

        // Move to vehicle center
        gc.translate(screenX, screenY);

        // Rotate context. SUMO angle is usually 0=North, 90=East.
        // JavaFX rotation is standard arithmetic (0=East, clockwise).
        // The negative sign inverts the rotation to match coordinate systems.
        double visualAngle = -vehicle.getAngle() + IMAGE_ORIENTATION_OFFSET;
        gc.rotate(visualAngle);

        // Draw a colored underlay (useful if the image has transparency or fails to load partially)
        gc.setFill(vehicle.getColor());
        gc.fillRect(-length/2 - 1, -width/2 - 1, length + 2, width + 2);

        gc.drawImage(image, -length/2, -width/2, length, width);

        gc.restore();
    }

    /**
     * Renders the vehicle using simple geometric shapes (rectangles).
     * Used when images are missing or zoom level is low (performance optimization).
     *
     * @param gc       The GraphicsContext.
     * @param screenX  The center X coordinate.
     * @param screenY  The center Y coordinate.
     * @param length   The scaled length.
     * @param width    The scaled width.
     * @param vehicle  The vehicle model.
     */
    private static void renderWithShape(GraphicsContext gc, double screenX,
                                        double screenY, double length,
                                        double width, VehicleState vehicle) {
        gc.save();

        gc.translate(screenX, screenY);

        // Rotate based on vehicle heading
        gc.rotate(-vehicle.getAngle());

        // Body
        gc.setFill(vehicle.getColor());
        gc.fillRect(-length/2, -width/2, length, width);

        // Windshield (visual cue for direction)
        gc.setFill(Color.rgb(200, 220, 255, 0.9));
        gc.fillRect(length/4, -width/2 + 0.5, length/4, width - 1);

        // Outline
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(0.3);
        gc.strokeRect(-length/2, -width/2, length, width);

        gc.restore();
    }

    /**
     * Renders text labels next to the vehicle.
     *
     * @param gc       The GraphicsContext.
     * @param vehicle  The vehicle model.
     * @param screenX  The X position.
     * @param screenY  The Y position.
     * @param viewport The viewport (used to scale font size).
     */
    private static void renderVehicleDetails(GraphicsContext gc, VehicleState vehicle,
                                             double screenX, double screenY,
                                             Viewport viewport) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(10 / viewport.getZoom()));
        gc.setTextAlign(TextAlignment.CENTER);

        String label = vehicle.getId().length() > 8 ?
                vehicle.getId().substring(0, 8) : vehicle.getId();
        gc.fillText(label, screenX, screenY - 8 / viewport.getZoom());

        if (vehicle.getSpeed() > 0.1) {
            String speedText = String.format("%.1f m/s", vehicle.getSpeed());
            gc.setFill(Color.rgb(0, 100, 0));
            gc.fillText(speedText, screenX, screenY + 12 / viewport.getZoom());
        }
    }

    /**
     * Selects the appropriate image based on the vehicle type string.
     *
     * @param vehicle The vehicle state.
     * @return The corresponding Image, or the default "car" image.
     */
    private static Image getVehicleImage(VehicleState vehicle) {
        String type = vehicle.getType();
        if (type != null && vehicleImages.containsKey(type.toLowerCase())) {
            return vehicleImages.get(type.toLowerCase());
        }
        return vehicleImages.get("car");
    }
}
