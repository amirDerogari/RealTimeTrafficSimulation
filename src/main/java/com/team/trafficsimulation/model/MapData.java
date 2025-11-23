// در پکیج com.team.trafficsimulation.model
// MapData.java

package com.team.trafficsimulation.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code MapData} class acts as a container for the static network topology.
 * <p>
 * It holds the parsed data structure of the SUMO network, specifically the collections of
 * nodes (junctions) and edges (roads). This class is typically populated by the
 * {@code NetXMLParser} and consumed by the {@code MainController} for rendering
 * and the {@code SimulationManager} for logic calculations.
 * </p>
 */
public class MapData {

    /** The list of all junctions in the network. */
    private final List<SumoNode> nodes;

    /** The list of all road segments connecting the junctions. */
    private final List<SumoEdge> edges;

    /**
     * Constructs an empty MapData container.
     * Initializes the lists for nodes and edges.
     */
    public MapData() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    /**
     * Retrieves the list of network nodes.
     *
     * @return A list of {@link SumoNode} objects representing junctions and geometry points.
     */
    public List<SumoNode> getNodes() { return nodes; }

    /**
     * Retrieves the list of network edges.
     *
     * @return A list of {@link SumoEdge} objects representing the roads.
     */
    public List<SumoEdge> getEdges() { return edges; }
}
