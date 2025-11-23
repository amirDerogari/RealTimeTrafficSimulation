package com.team.trafficsimulation.controller;

import com.team.trafficsimulation.model.*;
import com.team.trafficsimulation.view.*;
import de.tudresden.sumo.cmd.Vehicle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The {@code MainController} class serves as the central controller for the Traffic Simulation application.
 * <p>
 * It manages the interaction between the user interface (JavaFX), the simulation logic (via {@link SimulationManager}),
 * and the underlying data models. This class handles user inputs, rendering of the simulation map,
 * file loading operations (network, config, routes), and the simulation execution loop.
 * </p>
 */
public class MainController {

    /** Flag indicating if the simulation is in single-step mode. */
    private boolean step=false;

    @FXML private Canvas mapCanvas;
    @FXML private StackPane canvasContainer;
    @FXML private Label statusLabel;
    @FXML private Label infoLabel;
    @FXML private Label timerLabel;
    @FXML private Label statsLabel;
    @FXML private Slider spawnIntervalSlider;
    @FXML private CheckBox autoSpawnCheckbox;
    @FXML private CheckBox showDetailsCheckbox;

    private SimulationManager simulationManager;
    private MapData mapData;
    private Viewport viewport;

    // Mouse tracking variables for panning logic
    private double lastMouseX, lastMouseY;

    private boolean showVehicleDetails = true;
    private final List<String> availableVehicleTypes = new ArrayList<>();

    private String loadedConfigPath;
    private String loadedNetPath;
    private String loadedRoutePath;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     * <p>
     * It sets up the viewport, canvas bindings, simulation manager, and UI event handlers.
     * </p>
     */
    public void initialize() {
        viewport = new Viewport();

        // Bind canvas size to its container to allow resizing
        mapCanvas.widthProperty().bind(canvasContainer.widthProperty());
        mapCanvas.heightProperty().bind(canvasContainer.heightProperty());
        mapCanvas.widthProperty().addListener((obs, oldVal, newVal) -> redraw());
        mapCanvas.heightProperty().addListener((obs, oldVal, newVal) -> redraw());

        this.simulationManager = new SimulationManager(this);

        setupMouseHandlers();
        setupUIControls();
    }

    /**
     * Configures the UI controls such as sliders and checkboxes.
     * Sets up listeners for value changes to update simulation parameters in real-time.
     */
    private void setupUIControls() {
        if (spawnIntervalSlider != null) {
            spawnIntervalSlider.setMin(1);
            spawnIntervalSlider.setMax(10);
            spawnIntervalSlider.setValue(2);
            spawnIntervalSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                updateStatus("Spawn interval: " + newVal.intValue() + "s");
            });
        }

        if (autoSpawnCheckbox != null) {
            autoSpawnCheckbox.setSelected(true);
            autoSpawnCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                // Listener logic can be added here if needed
            });
        }

        if (showDetailsCheckbox != null) {
            showDetailsCheckbox.setSelected(true);
            showDetailsCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                showVehicleDetails = newVal;
                redraw();
            });
        }
    }

    /**
     * Sets up mouse event handlers for the canvas.
     * <p>
     * Supports:
     * <ul>
     *   <li>Panning: Mouse drag</li>
     *   <li>Selection: Mouse click (Left button)</li>
     *   <li>Zooming: Mouse scroll</li>
     * </ul>
     * </p>
     */
    private void setupMouseHandlers() {
        mapCanvas.setOnMousePressed(event -> {
            lastMouseX = event.getX();
            lastMouseY = event.getY();
        });

        mapCanvas.setOnMouseDragged(event -> {
            double deltaX = event.getX() - lastMouseX;
            double deltaY = event.getY() - lastMouseY;
            viewport.pan(deltaX, deltaY);
            redraw();
            lastMouseX = event.getX();
            lastMouseY = event.getY();
        });

        mapCanvas.setOnMouseClicked(event -> {
            double worldX = viewport.screenToWorldX(event.getX());
            double worldY = viewport.screenToWorldY(event.getY());

            if (event.getButton() == MouseButton.PRIMARY) {
                handleSingleClick(worldX, worldY, event.isControlDown());
            }
        });

        mapCanvas.setOnScroll(event -> {
            double factor = (event.getDeltaY() > 0) ? 1.1 : 0.9;
            viewport.setZoom(viewport.getZoom() * factor);
            redraw();
            event.consume();
        });
    }

    /**
     * Handles a single mouse click on the simulation map.
     * Attempts to select a traffic light first; if none is found, attempts to select a vehicle.
     *
     * @param worldX     The X coordinate in the world space.
     * @param worldY     The Y coordinate in the world space.
     * @param isCtrlDown True if the Control key was pressed during the click.
     */
    private void handleSingleClick(double worldX, double worldY, boolean isCtrlDown) {
        simulationManager.handleTrafficLightClick(worldX, worldY);

        if (simulationManager.getSelectedTrafficLight() == null) {
            handleVehicleClick(worldX, worldY);
        }

        redraw();
    }

    /**
     * Checks if a vehicle exists at the given world coordinates and displays its information.
     * Finds the closest vehicle within a specific tolerance radius.
     *
     * @param worldX The X coordinate in the world space.
     * @param worldY The Y coordinate in the world space.
     */
    private void handleVehicleClick(double worldX, double worldY) {
        Optional<VehicleState> clickedVehicleOpt = simulationManager.getVehicles()
                .values()
                .stream()
                .min((v1, v2) -> {
                    double distSq1 = Math.pow(v1.getX() - worldX, 2) +
                            Math.pow(v1.getY() - worldY, 2);
                    double distSq2 = Math.pow(v2.getX() - worldX, 2) +
                            Math.pow(v2.getY() - worldY, 2);
                    return Double.compare(distSq1, distSq2);
                });

        if (clickedVehicleOpt.isPresent()) {
            VehicleState vehicle = clickedVehicleOpt.get();
            double dx = vehicle.getX() - worldX;
            double dy = vehicle.getY() - worldY;
            double distSq = dx * dx + dy * dy;
            double clickTolerance = 10.0 / viewport.getZoom();

            if (distSq < clickTolerance * clickTolerance) {
                displayVehicleInfo(vehicle);
            } else {
                infoLabel.setText("Click on a vehicle or traffic light to see details.");
            }
        } else {
            infoLabel.setText("Click on a vehicle or traffic light to see details.");
        }
    }

    /**
     * Retrieves and displays detailed information about a specific vehicle.
     * Fetches real-time data such as speed, position, and lane info from the TraCI connection.
     *
     * @param vehicle The {@link VehicleState} object representing the selected vehicle.
     */
    private void displayVehicleInfo(VehicleState vehicle) {
        try {
            double speed = (Double) simulationManager.getTraCIConnection()
                    .do_job_get(Vehicle.getSpeed(vehicle.getId()));

            String info = String.format(
                    "🚗 Vehicle Information\n" +
                            "━━━━━━━━━━━━━━━━━━━━\n" +
                            "ID: %s\n" +
                            "Type: %s\n" +
                            "Position: (%.2f, %.2f)\n" +
                            "Speed: %.2f m/s (%.1f km/h)\n" +
                            "Angle: %.1f°\n" +
                            "Current Edge: %s\n" +
                            "Current Lane: %s\n" +
                            "Lane Position: %.2f m\n" +
                            "Alive Time: %.1f s",
                    vehicle.getId(),
                    vehicle.getType(),
                    vehicle.getX(),
                    vehicle.getY(),
                    speed,
                    speed * 3.6,
                    vehicle.getAngle(),
                    vehicle.getCurrentEdge() != null ? vehicle.getCurrentEdge() : "N/A",
                    vehicle.getCurrentLane() != null ? vehicle.getCurrentLane() : "N/A",
                    vehicle.getLanePosition(),
                    vehicle.getAliveTime()
            );
            infoLabel.setText(info);
        } catch (Exception e) {
            infoLabel.setText("ID: " + vehicle.getId() + "\n❌ Error fetching details");
        }
    }

    /**
     * Opens a file chooser to load a SUMO Network file (.net.xml).
     * Parses the file and auto-fits the viewport to the map dimensions.
     */
    @FXML
    private void loadMapAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open SUMO Network File (.net.xml)");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("SUMO Net Files", "*.net.xml")
        );

        File file = fileChooser.showOpenDialog((Stage) mapCanvas.getScene().getWindow());

        if (file != null) {
            try {
                statusLabel.setText("Status: Loading map from " + file.getName() + "...");
                mapData = NetXMLParser.parse(file.getAbsolutePath());
                this.loadedNetPath = file.getAbsolutePath();

                autoFitView(mapData);
                statusLabel.setText(String.format(
                        "Status: Map loaded ✓ Nodes: %d, Edges: %d",
                        mapData.getNodes().size(),
                        mapData.getEdges().size()
                ));
                redraw();
            } catch (Exception e) {
                statusLabel.setText("ERROR: Failed to load map - " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Opens a file chooser to load a SUMO Configuration file (.sumocfg).
     */
    @FXML
    private void loadConfigFileAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load SUMO Configuration File (.sumocfg)");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("SUMO Config Files", "*.sumocfg")
        );

        File selectedFile = fileChooser.showOpenDialog(
                (Stage) mapCanvas.getScene().getWindow()
        );

        if (selectedFile != null) {
            this.loadedConfigPath = selectedFile.getAbsolutePath();
            updateStatus("✅ Configuration file loaded: " + selectedFile.getName());

            try {
                updateStatus("Ready to start simulation");
            } catch (Exception e) {
                updateStatus("⚠️ Config loaded, but couldn't extract network");
            }
        }
    }

    /**
     * Opens a file chooser to load a SUMO Route file (.rou.xml) and parses available vehicle types.
     */
    @FXML
    private void loadRouteFileAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load SUMO Route File (.rou.xml)");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("SUMO Route Files", "*.rou.xml")
        );

        File selectedFile = fileChooser.showOpenDialog(
                (Stage) mapCanvas.getScene().getWindow()
        );

        try {
            File inputFile = new File(selectedFile.getAbsolutePath());
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            doc.getElementsByTagName("flow");

            NodeList vTypeList = doc.getElementsByTagName("vType");
            availableVehicleTypes.clear();
            for (int i = 0; i < vTypeList.getLength(); i++) {
                Node node = vTypeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String typeId = element.getAttribute("id");
                    availableVehicleTypes.add(typeId);
                    System.out.println("Loaded Vehicle Type: " + typeId);
                }
            }

        }catch (Exception e){e.printStackTrace();}
        if (selectedFile != null) {
            this.loadedRoutePath = selectedFile.getAbsolutePath();
            updateStatus("✅ Route file loaded: " + selectedFile.getName());
        }
    }

    /**
     * Starts the simulation loop based on loaded files.
     * Handles different initialization scenarios (Config file vs Net+Route files).
     */
    @FXML
    private void startSimulationAction() {
        try {
            if(step==true){
                simulationManager.startSimulationLoop();

            }else {
                if (loadedConfigPath != null) {
                    simulationManager.startSimulationWithConfig(loadedConfigPath);
                } else if (loadedNetPath != null && loadedRoutePath != null) {
                    simulationManager.startSimulationWithFiles(loadedNetPath, loadedRoutePath);
                } else if (loadedNetPath != null) {
                    simulationManager.startSimulationWithFiles(
                            loadedNetPath,
                            createEmptyRouteFile()
                    );
                } else {
                    updateStatus("❌ Please load files first (.sumocfg or .net.xml + .rou.xml)");
                }
            }
        } catch (Exception e) {
            updateStatus("❌ ERROR starting simulation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a temporary empty route file if no route file is provided.
     * This allows the simulation to start with just a network file.
     *
     * @return The absolute path to the temporary route file.
     * @throws Exception If file creation fails.
     */
    private String createEmptyRouteFile() throws Exception {
        File tempRoute = File.createTempFile("empty_route_", ".rou.xml");
        tempRoute.deleteOnExit();

        try (java.io.PrintWriter out = new java.io.PrintWriter(tempRoute)) {
            out.println("<routes>");
            out.println("    <vType id=\"DEFAULT_VEHTYPE\" accel=\"2.6\" decel=\"4.5\" " +
                    "sigma=\"0.5\" length=\"5\" maxSpeed=\"50\"/>");
            out.println("</routes>");
        }

        return tempRoute.getAbsolutePath();
    }

    /**
     * Stops the currently running simulation.
     */
    @FXML
    private void stopSimulationAction() {
        step=false;
        simulationManager.stopSimulation();
    }

    /**
     * Exits the application, ensuring the simulation is stopped properly.
     */
    @FXML
    private void exitApplication() {
        simulationManager.stopSimulation();
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void zoomIn() {
        viewport.setZoom(viewport.getZoom() * 1.2);
        redraw();
    }

    @FXML
    private void zoomOut() {
        viewport.setZoom(viewport.getZoom() / 1.2);
        redraw();
    }

    /**
     * Redraws the entire canvas.
     * Clears the previous frame and renders roads, vehicles, nodes, and traffic lights.
     * Updates the statistics display after rendering.
     */
    public void redraw() {
        if (mapData == null) return;

        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());
        viewport.setCanvasDimensions(mapCanvas.getWidth(), mapCanvas.getHeight());

        renderRoads(gc);


        renderVehicles(gc);


         renderNodes(gc);
        renderTrafficLights(gc);


        //  renderSelection(gc);

        updateStatistics();
    }

    /**
     * Renders the road network (edges and lanes) onto the graphics context.
     * Handles coordinate transformation from world to screen space.
     *
     * @param gc The GraphicsContext to draw on.
     */
    private void renderRoads(GraphicsContext gc) {
        for (SumoEdge edge : mapData.getEdges()) {
            for (SumoLane lane : edge.lanes()) {
                List<Double> xCoords = lane.shapeX();
                List<Double> yCoords = lane.shapeY();

                if (xCoords.size() > 1) {
                    double screenWidth = lane.width() * viewport.getZoom();
                    gc.setStroke(Color.rgb(80, 80, 80));
                    gc.setLineWidth(screenWidth);

                    for (int i = 0; i < xCoords.size() - 1; i++) {
                        double startX = viewport.worldToScreenX(xCoords.get(i));
                        double startY = viewport.worldToScreenY(yCoords.get(i));
                        double endX = viewport.worldToScreenX(xCoords.get(i + 1));
                        double endY = viewport.worldToScreenY(yCoords.get(i + 1));
                        gc.strokeLine(startX, startY, endX, endY);
                    }

                    if (viewport.getZoom() > 2.0) {
                        gc.setStroke(Color.rgb(255, 255, 200));
                        gc.setLineWidth(0.3);
                        gc.setLineDashes(5, 5);

                        for (int i = 0; i < xCoords.size() - 1; i++) {
                            double startX = viewport.worldToScreenX(xCoords.get(i));
                            double startY = viewport.worldToScreenY(yCoords.get(i));
                            double endX = viewport.worldToScreenX(xCoords.get(i + 1));
                            double endY = viewport.worldToScreenY(yCoords.get(i + 1));
                            gc.strokeLine(startX, startY, endX, endY);
                        }

                        gc.setLineDashes(null);
                    }
                }
            }
        }
    }

    /**
     * Delegates the rendering of traffic lights to the {@link TrafficLightRenderer}.
     *
     * @param gc The GraphicsContext to draw on.
     */
    private void renderTrafficLights(GraphicsContext gc) {
        TrafficLightRenderer.renderTrafficLights(
                gc,
                simulationManager.getTrafficLights().values(),
                viewport,
                showVehicleDetails
        );
    }

    /**
     * Iterates through all active vehicles and renders them.
     *
     * @param gc The GraphicsContext to draw on.
     */
    private void renderVehicles(GraphicsContext gc) {
        for (VehicleState vehicle : simulationManager.getVehicles().values()) {
            VehicleRenderer.renderVehicle(gc, vehicle, viewport, showVehicleDetails);
        }
    }

    /**
     * Renders the nodes (junctions) of the map.
     * Currently commented out in the main redraw loop but available for debug.
     *
     * @param gc The GraphicsContext to draw on.
     */
    private void renderNodes(GraphicsContext gc) {
        if (viewport.getZoom() < 1.0) return;

        for (SumoNode node : mapData.getNodes()) {
            double radius = 1.2 * viewport.getZoom();
            double screenX = viewport.worldToScreenX(node.x());
            double screenY = viewport.worldToScreenY(node.y());

            gc.setFill(Color.rgb(200, 50, 50));
            gc.fillOval(screenX - radius, screenY - radius, 2 * radius, 2 * radius);

            gc.setStroke(Color.BLACK);
            gc.setLineWidth(0.3);
            gc.strokeOval(screenX - radius, screenY - radius, 2 * radius, 2 * radius);
        }
    }


    /**
     * Automatically adjusts the viewport zoom and offset to fit the entire map within the canvas.
     *
     * @param mapData The map data containing all nodes.
     */
    private void autoFitView(MapData mapData) {
        if (mapData.getNodes().isEmpty()) return;

        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

        for (SumoNode node : mapData.getNodes()) {
            minX = Math.min(minX, node.x());
            maxX = Math.max(maxX, node.x());
            minY = Math.min(minY, node.y());
            maxY = Math.max(maxY, node.y());
        }

        double mapWidth = maxX - minX;
        double mapHeight = maxY - minY;
        double padding = 1.1;
        double zoomX = mapCanvas.getWidth() / (mapWidth * padding);
        double zoomY = mapCanvas.getHeight() / (mapHeight * padding);

        viewport.setZoom(Math.min(zoomX, zoomY));
        viewport.setOffsetX(minX - (mapCanvas.getWidth() / viewport.getZoom() - mapWidth) / 2);
        viewport.setOffsetY(minY - (mapCanvas.getHeight() / viewport.getZoom() - mapHeight) / 2);
    }

    /**
     * Updates the statistics label on the UI with current simulation counts.
     */
    private void updateStatistics() {
        if (statsLabel != null) {
            Platform.runLater(() -> {
                String stats = String.format(
                        "📊 Statistics\n" +
                                "━━━━━━━━━━━━━━━━\n" +
                                "Current: %d vehicles\n" +
                                "Spawned: %d total\n" +
                                "Arrived: %d total\n" +
                                "Traffic Lights: %d",
                        simulationManager.getCurrentVehicleCount(),
                        simulationManager.getTotalVehiclesSpawned(),
                        simulationManager.getTotalVehiclesRemoved(),
                        simulationManager.getTrafficLights().size()
                );
                statsLabel.setText(stats);
            });
        }
    }

    /**
     * Updates the timer display on the UI.
     *
     * @param time The formatted time string to display.
     */
    public void updateTimerDisplay(String time) {
        if (timerLabel != null) {
            Platform.runLater(() -> timerLabel.setText("⏱️ " + time));
        }
    }

    /**
     * Updates the status bar with a message.
     *
     * @param message The message string to display.
     */
    public void updateStatus(String message) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText("Status: " + message);
            }
        });
    }

    public MapData getMapData() {
        return mapData;
    }


    /**
     * Executes a single step of the simulation manually.
     */
    public void step() {
        step=true;
        if (loadedNetPath != null && loadedRoutePath != null) {
            try {
                simulationManager.startSimulationWithStep(loadedConfigPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        simulationManager.doStep();
    }
}
