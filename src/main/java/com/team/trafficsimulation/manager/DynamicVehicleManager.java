package com.team.trafficsimulation.manager;

import com.team.trafficsimulation.controller.MainController;
import com.team.trafficsimulation.model.*;
import de.tudresden.sumo.cmd.*;
import de.tudresden.sumo.objects.SumoRoadPosition;
import de.tudresden.sumo.objects.SumoStringList;
import it.polito.appeal.traci.SumoTraciConnection;
import java.util.*;


/**
 * The {@code DynamicVehicleManager} is responsible for tracking and managing the state
 * of all vehicles within the simulation.
 * <p>
 * It acts as a bridge between the SUMO physics engine and the Java application. Its primary duties include:
 * <ul>
 *     <li>Analyzing the network to find valid spawn (entry) and destination (exit) points.</li>
 *     <li>Synchronizing the list of active vehicles with the SUMO server.</li>
 *     <li>Updating the real-time properties (position, speed, lane) of every vehicle for rendering.</li>
 * </ul>
 * </p>
 */
public class DynamicVehicleManager {

    private final SumoTraciConnection conn;
    private final MapData mapData;

    /** A local cache of active vehicles, mapped by their unique ID. */
    private final Map<String, VehicleState> vehicles = new HashMap<>();

    private List<String> entryEdges;
    private List<String> exitEdges;
    Random random;

    /**
     * Constructs a new DynamicVehicleManager.
     * <p>
     * Upon initialization, it automatically analyzes the provided map data to
     * determine valid entry and exit edges for traffic generation.
     * </p>
     *
     * @param conn    The active connection to the SUMO TraCI server.
     * @param mapData The parsed network topology data.
     */
    public DynamicVehicleManager(SumoTraciConnection conn,
                                 MapData mapData) {
        this.conn = conn;
        this.mapData = mapData;
        this.random = new Random();

        identifyEntryExitPoints();
    }


    /**
     * Analyzes the map topology to identify "Source" and "Sink" edges.
     * <p>
     * <b>Entry Edges:</b> Edges that have no incoming connections (or limited connectivity),
     * suitable for spawning new vehicles.<br>
     * <b>Exit Edges:</b> Edges that have no outgoing connections, suitable as destinations.
     * </p>
     * Internal junctions (ids starting with ":") are ignored.
     */
    private void identifyEntryExitPoints() {
        entryEdges = new ArrayList<>();
        exitEdges = new ArrayList<>();

        Set<String> allEdgeIds = new HashSet<>();
        Map<String, Set<String>> incomingEdges = new HashMap<>();
        Map<String, Set<String>> outgoingEdges = new HashMap<>();

        // First pass: map connectivity
        for (SumoEdge edge : mapData.getEdges()) {
            if (edge.id().startsWith(":")) continue;

            allEdgeIds.add(edge.id());

            outgoingEdges.computeIfAbsent(edge.fromNodeId(), k -> new HashSet<>()).add(edge.id());
            incomingEdges.computeIfAbsent(edge.toNodeId(), k -> new HashSet<>()).add(edge.id());
        }

        // Identify Entry Edges (Sources)
        for (SumoEdge edge : mapData.getEdges()) {
            if (edge.id().startsWith(":")) continue;

            Set<String> incoming = incomingEdges.getOrDefault(edge.fromNodeId(), Collections.emptySet());
            if (incoming.isEmpty() || incoming.size() == 1) {
                entryEdges.add(edge.id());
            }
        }

        // Identify Exit Edges (Sinks)
        for (SumoEdge edge : mapData.getEdges()) {
            if (edge.id().startsWith(":")) continue;

            Set<String> outgoing = outgoingEdges.getOrDefault(edge.toNodeId(), Collections.emptySet());
            if (outgoing.isEmpty() || outgoing.size() == 1) {
                exitEdges.add(edge.id());
            }
        }

        // Fallback: If topology is complex/circular, allow any edge
        if (entryEdges.isEmpty()) {
            entryEdges = allEdgeIds.stream().toList();
        }
        if (exitEdges.isEmpty()) {
            exitEdges = allEdgeIds.stream().toList();
        }

        System.out.println("✅ Entry edges: " + entryEdges.size());
        System.out.println("✅ Exit edges: " + exitEdges.size());
    }


    /**
     * Synchronizes the local vehicle registry with the current state of the SUMO simulation.
     * <p>
     * 1. Fetches the list of all currently active vehicle IDs from TraCI.<br>
     * 2. Removes any vehicles from the local map that are no longer active (arrived or teleported).<br>
     * 3. Updates the state (position, speed, etc.) for all currently active vehicles.
     * </p>
     *
     * @throws Exception If a TraCI communication error occurs.
     */
    public void updateVehicles() throws Exception {
        @SuppressWarnings("unchecked")
        List<String> currentVehicles = (List<String>) conn.do_job_get(Vehicle.getIDList());

        // Remove vehicles that have left the simulation
        vehicles.keySet().retainAll(currentVehicles);

        // Update state for all current vehicles
        for (String vehId : currentVehicles) {
            updateVehicleState(vehId);
        }

    }


    /**
     * Queries SUMO for detailed information about a specific vehicle and updates
     * the local {@link VehicleState} object.
     *
     * @param vehicleId The ID of the vehicle to update.
     * @throws Exception If a TraCI communication error occurs.
     */
    private void updateVehicleState(String vehicleId) throws Exception {
        var position = conn.do_job_get(Vehicle.getPosition(vehicleId));
        double speed = (Double) conn.do_job_get(Vehicle.getSpeed(vehicleId));

        String edgeId = (String) conn.do_job_get(Vehicle.getRoadID(vehicleId));
        String laneId = (String) conn.do_job_get(Vehicle.getLaneID(vehicleId));
        double lanePos = (Double) conn.do_job_get(Vehicle.getLanePosition(vehicleId));

        double x = 0, y = 0;
        // Parse SUMO 2D position
        if (position instanceof de.tudresden.sumo.objects.SumoPosition2D) {
            de.tudresden.sumo.objects.SumoPosition2D pos =
                    (de.tudresden.sumo.objects.SumoPosition2D) position;
            x = pos.x;
            y = pos.y;
        }

        VehicleState state = vehicles.get(vehicleId);
        if (state == null) {
            state = new VehicleState(vehicleId, x, y);
            vehicles.put(vehicleId, state);
        }

        state.updateFullState(x, y, speed, edgeId, laneId, lanePos);

    }


    /**
     * Returns the map of currently active vehicles.
     *
     * @return A map where Key is Vehicle ID and Value is the {@link VehicleState} object.
     */
    public Map<String, VehicleState> getVehicles() { return vehicles; }
}
