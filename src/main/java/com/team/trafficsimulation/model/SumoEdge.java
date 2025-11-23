// در پکیج com.team.trafficsimulation.model
// SumoEdge.java

package com.team.trafficsimulation.model;

import java.util.List;

/**
 * The {@code SumoEdge} record represents a road segment (Edge) in the SUMO network topology.
 * <p>
 * In SUMO, an edge connects two nodes (junctions) and contains one or more lanes.
 * This immutable data structure holds the structural information required to render
 * the road network and map vehicles to their specific locations.
 * </p>
 *
 * @param id         The unique identifier of the edge (e.g., "E1").
 * @param fromNodeId The ID of the starting junction node.
 * @param toNodeId   The ID of the ending junction node.
 * @param lanes      The list of individual lanes belonging to this edge.
 */
public record SumoEdge(String id, String fromNodeId, String toNodeId, List<SumoLane> lanes) {
}
