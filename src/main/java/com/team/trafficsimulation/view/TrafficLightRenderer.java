package com.team.trafficsimulation.view;

import com.team.trafficsimulation.controller.Viewport;
import com.team.trafficsimulation.model.TrafficLight;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;


/**
 * The {@code TrafficLightRenderer} class is a static utility responsible for drawing traffic lights
 * onto the JavaFX canvas.
 * <p>
 * It converts the logical state of {@link TrafficLight} objects (position, color status)
 * into visual elements. It handles scaling based on the current {@link Viewport} zoom level
 * and conditionally renders details like IDs based on user settings.
 * </p>
 */
public class TrafficLightRenderer {

    /** The base diameter of the traffic light circle in world units (meters). */
    private static final double BASE_LIGHT_SIZE = 3.5;


    /**
     * Renders a collection of traffic lights.
     *
     * @param gc          The GraphicsContext to draw on.
     * @param lights      An iterable collection of traffic light objects.
     * @param viewport    The current viewport for coordinate transformation.
     * @param showDetails Flag indicating whether to draw text labels (IDs).
     */
    public static void renderTrafficLights(GraphicsContext gc,
                                           Iterable<TrafficLight> lights,
                                           Viewport viewport,
                                           boolean showDetails) {
        for (TrafficLight light : lights) {
            renderTrafficLight(gc, light, viewport, showDetails);
        }
    }

    /**
     * Renders a single traffic light.
     * <p>
     * Currently delegates to {@link #renderCentralLight}, but could be expanded to support
     * complex junction rendering logic in the future.
     * </p>
     *
     * @param gc          The GraphicsContext to draw on.
     * @param light       The specific traffic light to render.
     * @param viewport    The current viewport.
     * @param showDetails Flag indicating whether to draw text labels.
     */
    public static void renderTrafficLight(GraphicsContext gc,
                                          TrafficLight light,
                                          Viewport viewport,
                                          boolean showDetails) {
/*
        double zoom = viewport.getZoom();
*/




        renderCentralLight(gc, light, viewport, showDetails);


    }

    /**
     * Draws the visual representation of the traffic light at its center coordinates.
     * <p>
     * Drawing steps:
     * <ol>
     *   <li>Draws a dark background box (housing).</li>
     *   <li>Calculates the screen position based on the viewport.</li>
     *   <li>Draws a glow effect if the light is active.</li>
     *   <li>Draws the main colored circle (Red, Yellow, or Green).</li>
     *   <li>Optionally draws the Junction ID text if zoomed in sufficiently.</li>
     * </ol>
     * </p>
     *
     * @param gc          The GraphicsContext.
     * @param light       The traffic light model.
     * @param viewport    The viewport for coordinate conversion.
     * @param showDetails Whether to render the ID text.
     */
    private static void renderCentralLight(GraphicsContext gc,
                                           TrafficLight light,
                                           Viewport viewport,
                                           boolean showDetails) {

        double screenX = viewport.worldToScreenX(light.getX());
        double screenY = viewport.worldToScreenY(light.getY());
        double size = BASE_LIGHT_SIZE * viewport.getZoom();

        // Draw the housing background
        gc.setFill(Color.rgb(40, 40, 40, 0.9));
        gc.fillRect(screenX - size/2 - 2, screenY - size/2 - 2, size + 4, size + 4);

        gc.setStroke(Color.rgb(80, 80, 80));
        gc.setLineWidth(1.0);
        gc.strokeRect(screenX - size/2 - 2, screenY - size/2 - 2, size + 4, size + 4);

        // TODO: This color logic is currently a placeholder (Always YELLOW).
        // It should be updated to reflect light.getCurrentColor() from the model.
        Color lightColor = Color.YELLOW;

        // Draw glow effect
        if (!lightColor.equals(Color.rgb(100, 100, 100))) {
            gc.setFill(lightColor.deriveColor(1, 1, 1.5, 0.3));
            gc.fillOval(screenX - size/2 - 3, screenY - size/2 - 3, size + 6, size + 6);
        }

        // Draw the main light circle
        gc.setFill(lightColor);
        gc.fillOval(screenX - size/2, screenY - size/2, size, size);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(0.8);
        gc.strokeOval(screenX - size/2, screenY - size/2, size, size);

        // Draw text details if zoomed in
        if (viewport.getZoom() > 2.0 && showDetails) {
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(8 / viewport.getZoom()));
            gc.setTextAlign(TextAlignment.CENTER);

            String displayId = light.getJunctionId();
            if (displayId.length() > 10) {
                displayId = displayId.substring(0, 10) + "...";
            }

            gc.fillText(displayId, screenX, screenY + size);
        }
    }


}
