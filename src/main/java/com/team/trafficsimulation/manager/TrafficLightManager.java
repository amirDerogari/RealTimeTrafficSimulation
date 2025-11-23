package com.team.trafficsimulation.manager;

import com.team.trafficsimulation.model.*;
import de.tudresden.sumo.cmd.Trafficlight;
import it.polito.appeal.traci.SumoTraciConnection;
import java.util.*;


/**
 * The {@code TrafficLightManager} handles the synchronization and control of traffic lights
 * between the Java application and the SUMO simulation.
 * <p>
 * It maintains a registry of all traffic lights in the network, maps them to their
 * physical locations (Junctions), and provides methods to read their status or
 * override their signal programs.
 * </p>
 */
public class TrafficLightManager {

    /** Registry of traffic lights mapped by their SUMO ID. */
    private final Map<String, TrafficLight> trafficLights = new HashMap<>();

    private SumoTraciConnection conn;
    private final MapData mapData;

    /**
     * Constructs a new TrafficLightManager.
     *
     * @param mapData The parsed map topology, used to locate junctions for traffic lights.
     */
    public TrafficLightManager(MapData mapData) {
        this.mapData = mapData;
    }

    /**
     * Initializes the local traffic light registry by querying SUMO.
     * <p>
     * 1. Fetches the list of all traffic light IDs from the simulation.<br>
     * 2. Correlates each ID with a {@link SumoNode} from the map data to determine its (X, Y) coordinates.<br>
     * 3. Creates {@link TrafficLight} objects for rendering and control.
     * </p>
     *
     * @param connection The active TraCI connection.
     * @throws Exception If a TraCI communication error occurs.
     */
    public void initializeFromSUMO(SumoTraciConnection connection) throws Exception {
        this.conn = connection;
        trafficLights.clear();

        @SuppressWarnings("unchecked")
        List<String> tlIds = (List<String>) conn.do_job_get(Trafficlight.getIDList());

        System.out.println("🚦 Found " + tlIds.size() + " traffic lights");

        for (String tlId : tlIds) {
            SumoNode junction = findJunctionById(tlId);

            if (junction != null) {


                TrafficLight light = new TrafficLight(
                        tlId,
                        junction.x(),
                        junction.y()
                );



                trafficLights.put(tlId, light);

            }
        }
    }


    /**
     * Helper method to find a MapNode by its ID.
     * Used to associate a traffic light ID with its physical coordinates.
     *
     * @param junctionId The string ID of the junction.
     * @return The {@link SumoNode} object, or {@code null} if not found.
     */
    private SumoNode findJunctionById(String junctionId) {
        return mapData.getNodes().stream()
                .filter(n -> n.id().equals(junctionId))
                .findFirst()
                .orElse(null);
    }


    /**
     * Updates the state of all traffic lights from the simulation.
     * <p>
     * <b>Note:</b> This method is currently empty. In a full implementation, it should:
     * <ul>
     *   <li>Iterate through known traffic lights.</li>
     *   <li>Query {@code Trafficlight.getRedYellowGreenState}.</li>
     *   <li>Update the internal state of the {@link TrafficLight} objects.</li>
     * </ul>
     * </p>
     */
    public void updateFromSUMO() {

    }


    /**
     * Manually sets the signal state string for a specific junction.
     * <p>
     * Example state string: "rRgG" (red, Red-Major, green, Green-Major).
     * </p>
     *
     * @param junctionId The ID of the traffic light.
     * @param newState   The character string representing the new state.
     */
    public void setTrafficLightState(String junctionId, String newState) {

    }

    /**
     * Forces the traffic light to switch to a specific phase index within its current program.
     *
     * @param junctionId The ID of the traffic light.
     * @param phaseIndex The index of the phase to activate.
     */
    public void setTrafficLightPhase(String junctionId, int phaseIndex) {

    }


    /**
     * Changes the active logic program for a traffic light.
     * <p>
     * Common Program IDs in SUMO:
     * <ul>
     *   <li>"0": Default fixed time program.</li>
     *   <li>"off": Turns the traffic light off (usually blinking yellow or priority).</li>
     * </ul>
     * </p>
     *
     * @param junctionId The ID of the traffic light.
     * @param programId  The ID of the program to load.
     */
    public void setTrafficLightProgram(String junctionId, String programId) {

    }


    /**
     * Identifies if a specific point in the world (e.g., a mouse click) interacts with a traffic light.
     *
     * @param worldX    The X coordinate in the simulation world.
     * @param worldY    The Y coordinate in the simulation world.
     * @param tolerance The radius around the point to check for intersection.
     * @return The {@link TrafficLight} if found, otherwise {@code null}.
     */
    public TrafficLight findClickedLight(double worldX, double worldY, double tolerance) {
        return null;
    }

    // Getters
    public Map<String, TrafficLight> getTrafficLights() {
        return trafficLights;
    }

}
