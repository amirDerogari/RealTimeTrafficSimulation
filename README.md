***
# SUMO Traffic Simulation Controller

A JavaFX-based graphical application for visualizing and controlling SUMO (Simulation of Urban MObility) traffic simulations in real-time.

## Overview

This project provides an interactive interface to load, visualize, and manage SUMO traffic simulations. It connects to SUMO via the TraCI (Traffic Control Interface) protocol, allowing users to observe vehicle movements, traffic light states, and network topology on a dynamic canvas with zoom and pan capabilities.

## Features

- **Interactive Map Visualization**: Render SUMO road networks with nodes, edges, and lanes
- **Real-time Vehicle Tracking**: Display moving vehicles with customizable colors and details
- **Traffic Light Management**: View traffic light states at junctions
- **Viewport Controls**: Zoom in/out and pan across the simulation area
- **File Loading**: Support for `.net.xml` (network), `.rou.xml` (routes), and `.sumocfg` (configuration) files
- **Step-by-Step Execution**: Run simulations continuously or advance one step at a time
- **Statistics Display**: Track vehicles and simulation time

## Technology Stack

- **Java 17+** (or compatible version)
- **JavaFX** for GUI rendering
- **SUMO** (Simulation of Urban MObility) traffic simulator
- **TraCI** (Traffic Control Interface) for SUMO communication
- **TraaS Library** (it.polito.appeal.traci) for Java-TraCI bindings

## Project Structure

```
com.team.trafficsimulation/
├── controller/
│   ├── MainController.java       # Main UI controller
│   ├── SimulationManager.java    # Simulation lifecycle manager
│   └── Viewport.java              # Coordinate transformation handler
├── manager/
│   ├── DynamicVehicleManager.java    # Vehicle state synchronization
│   └── TrafficLightManager.java      # Traffic light control
├── model/
│   ├── MapData.java               # Network topology container
│   ├── NetXMLParser.java          # XML parser for .net.xml files
│   ├── VehicleState.java          # Vehicle data model
│   ├── EnhancedTrafficLight.java  # Traffic light model
│   ├── SimulationTimer.java       # Time tracker
│   ├── SumoNode.java              # Junction record
│   ├── SumoEdge.java              # Road segment record
│   └── SumoLane.java              # Lane geometry record
├── view/
│   ├── VehicleRenderer.java       # Vehicle drawing logic
│   └── TrafficLightRenderer.java  # Traffic light drawing logic
└── MainApplication.java           # JavaFX entry point
```

## Prerequisites

1. **SUMO Installation**: Download and install SUMO from [https://eclipse.dev/sumo/](https://eclipse.dev/sumo/)
2. **Java Development Kit**: JDK 17 or higher
3. **JavaFX SDK**: Ensure JavaFX libraries are available
4. **TraaS Library**: Include the TraCI Java client library in your project

## Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd traffic-simulation
```

2. Update the SUMO binary path in `SimulationManager.java`:
```java
private static final String SUMO_BINARY = "C:\\Program Files (x86)\\Eclipse\\SUMO\\bin\\sumo.exe";
```

3. Build the project using your IDE or build tool (Maven)

4. Run the `MainApplication` class

## Usage

### Loading a Simulation

**Step 1: Using a Configuration File**
1. Click **File → Load Config (.sumocfg)**
2. Select your SUMO configuration file
3. Click **Simulation → Start**

**Step 2: Using Separate Files**
1. Click **File → Load Network (.net.xml)**
2. Click **File → Load Routes (.rou.xml)**
3. Click **Simulation → Start or Step for stem simulation**

### Controls

- **Mouse Drag**: Pan the map
- **Mouse Scroll**: Zoom in/out
- **Left Click**: Select vehicles or traffic lights
- **Start Button**: Begin simulation loop
- **Stop Button**: Halt the simulation
- **Step Button**: Advance simulation by one timestep

### UI Elements

- **Status Bar**: Shows current file and simulation status
- **Info Panel**: Displays details of selected vehicle/traffic light
- **Timer**: Simulation time (HH:MM:SS)
***