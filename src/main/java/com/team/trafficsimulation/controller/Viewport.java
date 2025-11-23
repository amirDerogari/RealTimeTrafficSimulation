package com.team.trafficsimulation.controller;

/**
 * The {@code Viewport} class handles the coordinate transformation between the
 * <p>
 * It manages zooming and panning operations, allowing the user to navigate the
 * map. It is critical for rendering, as it converts the Cartesian coordinates
 * used by SUMO (where Y usually increases upwards) into the screen coordinates
 * used by JavaFX (where (0,0) is top-left and Y increases downwards).
 * </p>
 */
public class Viewport {

    /** The current zoom level (scale factor). Default is 1.0. */
    private double zoom = 1.0;

    /** The X offset of the view in world coordinates. */
    private double offsetX = 0.0;

    /** The Y offset of the view in world coordinates. */
    private double offsetY = 0.0;

    double canvasWidth = 800;
    private double canvasHeight = 600;

    /**
     * Updates the dimensions of the viewing area (canvas).
     * This should be called whenever the canvas is resized.
     *
     * @param width  The new width of the canvas.
     * @param height The new height of the canvas.
     */
    public void setCanvasDimensions(double width, double height) {
        this.canvasWidth = width;
        this.canvasHeight = height;
    }

    /**
     * Converts a World X coordinate to a Screen X coordinate.
     *
     * @param worldX The X coordinate in the simulation world.
     * @return The corresponding X coordinate on the screen (pixels).
     */
    public double worldToScreenX(double worldX) {
        return (worldX - offsetX) * zoom;
    }

    /**
     * Converts a World Y coordinate to a Screen Y coordinate.
     * <p>
     * <b>Note:</b> This method performs a Y-axis flip. In standard mathematics and SUMO,
     * Y increases upwards. In JavaFX, Y increases downwards. This calculation ensures
     * the map is rendered with the correct orientation.
     * </p>
     *
     * @param worldY The Y coordinate in the simulation world.
     * @return The corresponding Y coordinate on the screen (pixels).
     */
    public double worldToScreenY(double worldY) {
        return canvasHeight - ((worldY - offsetY) * zoom);
    }

    /**
     * Pans (moves) the view by a specified amount in screen pixels.
     * <p>
     * This adjusts the {@code offsetX} and {@code offsetY} based on the zoom level,
     * allowing "dragging" behavior.
     * </p>
     *
     * @param deltaScreenX The change in X pixels (mouse drag distance).
     * @param deltaScreenY The change in Y pixels (mouse drag distance).
     */
    public void pan(double deltaScreenX, double deltaScreenY) {
        this.offsetX -= deltaScreenX / zoom;
        this.offsetY += deltaScreenY / zoom;
    }

    public double getZoom() { return zoom; }

    /**
     * Sets the zoom level.
     * <p>
     * Prevents the zoom from becoming too small (clamped to minimum 0.1) to avoid
     * rendering issues or inversion.
     * </p>
     *
     * @param zoom The new scale factor.
     */
    public void setZoom(double zoom) {
        if (zoom > 0.1) {
            this.zoom = zoom;
        }
    }

    public void setOffsetX(double offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetY(double offsetY) {
        this.offsetY = offsetY;
    }


    /**
     * Converts a Screen X coordinate (e.g., mouse click) to a World X coordinate.
     *
     * @param screenX The X coordinate on the screen.
     * @return The corresponding X coordinate in the simulation world.
     */
    public double screenToWorldX(double screenX) {
        return screenX / zoom + offsetX;
    }

    /**
     * Converts a Screen Y coordinate (e.g., mouse click) to a World Y coordinate.
     * <p>
     * This reverses the Y-axis flip performed in {@link #worldToScreenY(double)}.
     * </p>
     *
     * @param screenY The Y coordinate on the screen.
     * @return The corresponding Y coordinate in the simulation world.
     */
    public double screenToWorldY(double screenY) {
        double invertedScreenY = canvasHeight - screenY;
        return invertedScreenY / zoom + offsetY;
    }
}
