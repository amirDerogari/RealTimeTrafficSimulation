package com.team.trafficsimulation.model;


/**
 * The {@code TrafficLight} class represents a traffic light system located at a specific junction.
 * <p>
 * It acts as a model to store the static properties (ID, location) and dynamic state
 * (current phase, timing) of a traffic light retrieved from the SUMO simulation.
 * This class is primarily used by the {@link com.team.trafficsimulation.manager.TrafficLightManager}
 * for rendering and state management.
 * </p>
 */
public class TrafficLight {

    /** The unique ID of the junction where this traffic light is located. */
    private final String junctionId;

    /** The X coordinate of the traffic light in the simulation world. */
    private final double x;

    /** The Y coordinate of the traffic light in the simulation world. */
    private final double y;


    /**
     * Constructs a new EnhancedTrafficLight.
     *
     * @param junctionId The unique identifier string from SUMO.
     * @param x          The X coordinate.
     * @param y          The Y coordinate.
     */
    public TrafficLight(String junctionId, double x, double y) {
        this.junctionId = junctionId;
        this.x = x;
        this.y = y;

    }


    /** @return The unique ID of the junction. */
    public String getJunctionId() { return junctionId; }

    /** @return The X coordinate in world space. */
    public double getX() { return x; }

    /** @return The Y coordinate in world space. */
    public double getY() { return y; }



    /**
     * The {@code Phase} class represents a single step in a traffic light's signal plan.
     * <p>
     * A phase consists of a signal state string (e.g., "GrGr" for Green-Red) and duration constraints.
     * </p>
     */
    public static class Phase {
        /** The signal state string (e.g., 'r', 'y', 'G', 'g'). */
        public final String state;

        /** The standard duration of this phase in seconds. */
        public final double duration;

        /** The minimum duration of this phase (used for actuated lights). */
        public final double minDuration;

        /** The maximum duration of this phase (used for actuated lights). */
        public final double maxDuration;

        /**
         * Constructs a new Phase.
         *
         * @param state    The character string representing the signal state for all links.
         * @param duration The programmed duration in seconds.
         * @param minDur   The minimum duration.
         * @param maxDur   The maximum duration.
         */
        public Phase(String state, double duration, double minDur, double maxDur) {
            this.state = state;
            this.duration = duration;
            this.minDuration = minDur;
            this.maxDuration = maxDur;
        }

        @Override
        public String toString() {
            return String.format("%s (%.1fs)", state, duration);
        }
    }
}
