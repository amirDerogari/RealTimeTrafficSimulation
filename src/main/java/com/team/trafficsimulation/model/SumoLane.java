package com.team.trafficsimulation.model;

import java.util.List;

/**
 * The {@code SumoLane} record represents a single lane within a road segment (Edge).
 * <p>
 * A lane defines the actual geometry where vehicles travel. It contains the physical
 * shape data (lists of coordinates) used for rendering the road on the canvas.
 * </p>
 *
 * @param id     The unique identifier of the lane (usually "EdgeID_LaneIndex", e.g., "E1_0").
 * @param width  The width of the lane in meters.
 * @param shapeX A list of X coordinates defining the lane's geometry shape points.
 * @param shapeY A list of Y coordinates defining the lane's geometry shape points.
 */
public record SumoLane(String id, double width, List<Double> shapeX, List<Double> shapeY) {
}
