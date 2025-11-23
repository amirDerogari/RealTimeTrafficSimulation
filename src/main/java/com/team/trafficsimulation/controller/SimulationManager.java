package com.team.trafficsimulation.controller;

import com.team.trafficsimulation.manager.*;
import com.team.trafficsimulation.model.*;
import it.polito.appeal.traci.SumoTraciConnection;
import javafx.application.Platform;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The {@code SimulationManager} class acts as the core engine of the application.
 * <p>
 * It manages the lifecycle of the SUMO simulation (start, stop, pause, step), maintains
 * the connection to the TraCI server, and coordinates data updates between the backend physics
 * engine and the JavaFX frontend. It delegates specific entity management to
 * {@link DynamicVehicleManager} and {@link TrafficLightManager}.
 * </p>
 */
public class SimulationManager {

    /** Path to the SUMO executable binary. */
    private static final String SUMO_BINARY = "C:\\Program Files (x86)\\Eclipse\\SUMO\\bin\\sumo.exe";

    private SumoTraciConnection conn;
    private final MainController controller;

    /** Thread-safe flag to track if the simulation loop is currently active. */
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private ScheduledExecutorService executorService;
    private SimulationTimer timer;

    private DynamicVehicleManager vehicleManager;
    private TrafficLightManager trafficLightManager;

    private int totalVehiclesSpawned = 0;
    private int totalVehiclesRemoved = 0;

    private TrafficLight selectedTrafficLight = null;

    /**
     * Constructs a new SimulationManager.
     *
     * @param controller The main UI controller that this manager will update.
     */
    public SimulationManager(MainController controller) {
        this.controller = controller;
        this.timer = new SimulationTimer();
        System.out.println("✅ SimulationManager initialized");
    }


    /**
     * Initializes and starts the simulation using a SUMO configuration file (.sumocfg).
     * <p>
     * This method sets up the TraCI connection, initializes entity managers, and begins
     * the automatic simulation loop.
     * </p>
     *
     * @param configPath The absolute path to the .sumocfg file.
     * @throws Exception If the simulation is already running or if initialization fails.
     */
    public void startSimulationWithConfig(String configPath) throws Exception {
        if (isRunning.get()) {
            controller.updateStatus("Simulation already running!");
            return;
        }

        controller.updateStatus("Loading configuration...");

        initializeSUMO(configPath);

        initializeManagers();

        startSimulationLoop();

        controller.updateStatus("✅ Simulation started from: " +
                new java.io.File(configPath).getName());
    }


    /**
     * Starts the simulation using separate network and route files.
     *
     * @param netPath   The absolute path to the .net.xml file.
     * @param routePath The absolute path to the .rou.xml file.
     * @throws Exception If the simulation is already running.
     */
    public void startSimulationWithFiles(String netPath, String routePath) throws Exception {
        if (isRunning.get()) {
            controller.updateStatus("Simulation already running!");
            return;
        }

        controller.updateStatus("Loading network and routes...");

        //    initializeManagers();

        startSimulationLoop();

        controller.updateStatus("✅ Simulation started");
    }

    /**
     * Establishes the connection to the SUMO server via TraCI.
     * Configures simulation options such as step length and log files.
     *
     * @param configPath The path to the configuration file.
     * @throws Exception If the connection cannot be established.
     */
    private void initializeSUMO(String configPath) throws Exception {
        executorService = Executors.newSingleThreadScheduledExecutor();

        conn = new SumoTraciConnection(SUMO_BINARY, configPath);

        conn.addOption("step-length", "1.0");
        conn.addOption("no-step-log", "true");
        conn.addOption("log", "sumo.log");
        conn.addOption("no-warnings", "true");
        conn.addOption("ignore-route-errors", "true");

        conn.runServer();
        conn.setOrder(1);

        System.out.println("✅ SUMO connection established");
    }


    /**
     * Initializes the helper managers for vehicles and traffic lights.
     * Requires an active SUMO connection.
     *
     * @throws Exception If manager initialization fails.
     */
    private void initializeManagers() throws Exception {
        vehicleManager = new DynamicVehicleManager(
                conn,
                controller.getMapData()
        );

        trafficLightManager = new TrafficLightManager(controller.getMapData());
        trafficLightManager.initializeFromSUMO(conn);

        System.out.println("✅ Managers initialized");
        System.out.println("   Vehicles: Ready for spawning");
        System.out.println("   Traffic Lights: " +
                trafficLightManager.getTrafficLights().size());
    }


    /**
     * Begins the main simulation loop.
     * Schedules the {@link #simulationStep()} method to run periodically on a background thread.
     */
    void startSimulationLoop() {
        isRunning.set(true);
        timer.start();


        executorService.scheduleAtFixedRate(
                this::simulationStep,
                0,
                100,
                TimeUnit.MILLISECONDS
        );
    }




    /**
     * Performs a single simulation step manually.
     * Intended for use when the simulation is paused or in single-step mode.
     */
    public void doStep(){

        simulationStep();

    }

    /**
     * The core logic executed during each time step.
     * <p>
     * 1. Advances the SUMO simulation by one timestep.<br>
     * 2. Updates vehicle positions and states.<br>
     * 3. Updates traffic light statuses.<br>
     * 4. Triggers a UI redraw on the JavaFX Application Thread.
     * </p>
     */
    private void simulationStep() {
        if (!isRunning.get()) return;

        try {
            conn.do_timestep();

            vehicleManager.updateVehicles();
            updateRemovedVehiclesCount();

            trafficLightManager.updateFromSUMO();

            Platform.runLater(() -> {
                controller.redraw();
                controller.updateTimerDisplay(timer.getFormattedTime());
            });

        } catch (Exception e) {
            System.err.println("❌ Simulation step error: " + e.getMessage());
            e.printStackTrace();
            stopSimulation();
        }
    }


    /**
     * Calculates the number of vehicles that have finished their route or left the simulation
     * by comparing the total spawned count against the current active count.
     */
    private void updateRemovedVehiclesCount() {
        int currentCount = vehicleManager.getVehicles().size();
        int expected = totalVehiclesSpawned - totalVehiclesRemoved;

        if (currentCount < expected) {
            totalVehiclesRemoved += (expected - currentCount);
        }
    }


    /**
     * Stops the simulation, shuts down the background thread, and closes the TraCI connection.
     */
    public void stopSimulation() {
        if (!isRunning.get()) return;

        isRunning.set(false);
        timer.stop();

        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }

        try {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        } catch (Exception e) {
            System.err.println("❌ Error closing connection: " + e.getMessage());
        }

        controller.updateStatus(String.format(
                "⏹️ Stopped. Time: %s, Spawned: %d, Arrived: %d",
                timer.getFormattedTime(),
                totalVehiclesSpawned,
                totalVehiclesRemoved
        ));
    }


    /**
     * Checks if a user click corresponds to a Traffic Light on the map.
     *
     * @param worldX The X coordinate in the world.
     * @param worldY The Y coordinate in the world.
     */
    public void handleTrafficLightClick(double worldX, double worldY) {
        if (trafficLightManager == null) return;

        double tolerance = 15.0;
        TrafficLight clicked = trafficLightManager.findClickedLight(
                worldX, worldY, tolerance
        );

        if (clicked != null) {
            selectedTrafficLight = clicked;
        } else {
            selectedTrafficLight = null;
        }
    }


    /**
     * Sets the state code (e.g., "rRgG") for a specific traffic light junction.
     *
     * @param junctionId The ID of the junction.
     * @param newState   The new state string.
     */
    public void setTrafficLightState(String junctionId, String newState) {
        if (trafficLightManager != null) {

            trafficLightManager.setTrafficLightState(junctionId, newState);
        }
    }

    /**
     * Manually sets the phase index for a traffic light logic program.
     *
     * @param junctionId The ID of the junction.
     * @param phaseIndex The index of the phase to switch to.
     */
    public void setTrafficLightPhase(String junctionId, int phaseIndex) {
        if (trafficLightManager != null) {
            trafficLightManager.setTrafficLightPhase(junctionId, phaseIndex);
        }
    }

    /**
     * Switches the logic program (e.g., from "0" to "off") for a traffic light.
     *
     * @param junctionId The ID of the junction.
     * @param programId  The ID of the program to load.
     */
    public void setTrafficLightProgram(String junctionId, String programId) {
        if (trafficLightManager != null) {
            trafficLightManager.setTrafficLightProgram(junctionId, programId);
        }
    }




    public SumoTraciConnection getTraCIConnection() { return conn; }

    /** @return A map of all currently active vehicle states. */
    public Map<String, VehicleState> getVehicles() {
        return vehicleManager != null ? vehicleManager.getVehicles() : Collections.emptyMap();
    }

    /** @return A map of all loaded traffic lights. */
    public Map<String, TrafficLight> getTrafficLights() {
        return trafficLightManager != null ?
                trafficLightManager.getTrafficLights() :
                Collections.emptyMap();
    }
    public int getTotalVehiclesSpawned() { return totalVehiclesSpawned; }
    public int getTotalVehiclesRemoved() { return totalVehiclesRemoved; }
    public int getCurrentVehicleCount() {
        return vehicleManager != null ? vehicleManager.getVehicles().size() : 0;
    }
    public TrafficLight getSelectedTrafficLight() { return selectedTrafficLight; }
    public DynamicVehicleManager getVehicleManager() { return vehicleManager; }
    public TrafficLightManager getTrafficLightManager() { return trafficLightManager; }

    /**
     * Initializes the simulation for manual stepping mode (Step-by-Step).
     * Sets up the connection and managers but does not start the automatic loop.
     *
     * @param configPath The path to the configuration file.
     * @throws Exception If initialization fails.
     */
    public void startSimulationWithStep(String configPath) throws Exception {
        if (isRunning.get()) {
            controller.updateStatus("Simulation already running!");
            return;
        }

        isRunning.set(true);
        controller.updateStatus("Loading network and routes...");
        initializeSUMO(configPath);

        initializeManagers();

        // startSimulationLoop();

        controller.updateStatus("✅ Simulation started");

        controller.updateStatus("✅ Simulation step started from: " +
                new java.io.File(configPath).getName());
    }
}
