package com.team.trafficsimulation.model;

/**
 * The {@code SimulationTimer} class is a utility for tracking the elapsed wall-clock time
 * of a simulation session.
 * <p>
 * Unlike the simulation step counter (which tracks logical time steps), this class tracks
 * real-time execution duration. It provides functionality to start, stop, reset, and format
 * the elapsed time into a human-readable string (HH:MM:SS) for the UI.
 * </p>
 */
public class SimulationTimer {

    /** The system time (in milliseconds) when the timer was started. */
    private long startTime;

    /** The timestamp when the timer was last paused. */
    private long pausedTime;

    /** The accumulated duration (in milliseconds) that the timer has spent in a paused state. */
    private long totalPausedDuration;

    private boolean isRunning;
    private boolean isPaused;

    /**
     * Constructs a new SimulationTimer and resets it to a clean state (00:00:00).
     */
    public SimulationTimer() {
        reset();
    }

    /**
     * Starts the timer.
     * <p>
     * Records the current system time as the start time. If the timer was already running,
     * this method does nothing.
     * </p>
     */
    public void start() {
        if (!isRunning) {
            startTime = System.currentTimeMillis();
            totalPausedDuration = 0;
            isRunning = true;
            isPaused = false;
        }
    }


    /**
     * Stops the timer.
     * <p>
     * The timer enters a stopped state, and subsequent calls to {@link #getElapsedMillis()}
     * will return 0.
     * </p>
     */
    public void stop() {
        isRunning = false;
        isPaused = false;
    }


    /**
     * Resets the timer to its initial state.
     * Clear all time records and sets the state to stopped.
     */
    public void reset() {
        startTime = 0;
        pausedTime = 0;
        totalPausedDuration = 0;
        isRunning = false;
        isPaused = false;
    }


    /**
     * Calculates the total time elapsed since the timer started.
     * <p>
     * This calculation accounts for any duration during which the timer was paused,
     * subtracting that time from the total difference between now and the start time.
     * </p>
     *
     * @return The elapsed duration in milliseconds. Returns 0 if the timer is not running.
     */
    public long getElapsedMillis() {
        if (!isRunning) return 0;

        long currentTime = isPaused ? pausedTime : System.currentTimeMillis();
        return currentTime - startTime - totalPausedDuration;
    }


    /**
     * Returns the elapsed time formatted as a standard digital clock string.
     *
     * @return A string in the format "HH:MM:SS" (e.g., "01:05:30").
     */
    public String getFormattedTime() {
        long totalSeconds = getElapsedMillis() / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }


    /**
     * Returns a string representation of the timer's state for debugging purposes.
     *
     * @return A string containing the running state, paused state, and formatted time.
     */
    @Override
    public String toString() {
        return String.format("Timer[running=%s, paused=%s, time=%s]",
                isRunning, isPaused, getFormattedTime());
    }
}
