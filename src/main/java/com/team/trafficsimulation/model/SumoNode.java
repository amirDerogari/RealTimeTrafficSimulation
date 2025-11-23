package com.team.trafficsimulation.model;

/**
 * The {@code SumoNode} record represents a node (Junction) in the SUMO network topology.
 * <p>
 * Nodes define the connectivity of the network, serving as the start and end points
 * for {@link SumoEdge}s. They typically represent intersections, dead ends, or
 * geometry points where the road attributes change.
 * </p>
 *
 * @param id The unique identifier of the node/junction (e.g., "J1").
 * @param x  The X coordinate of the node in the simulation world (in meters).
 * @param y  The Y coordinate of the node in the simulation world (in meters).
 */
public record SumoNode(String id, double x, double y) {
}
