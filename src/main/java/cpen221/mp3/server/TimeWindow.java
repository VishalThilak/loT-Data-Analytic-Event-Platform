package cpen221.mp3.server;

public class TimeWindow {
    public final double startTime;
    public final double endTime;


    /**
     * Create new TimeWindow instance
     *
     * @param startTime start time of time window
     * @param endTime end time of time window
     */
    public TimeWindow(double startTime, double endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Returns start time of this time window
     *
     * @return startTime
     */
    public double getStartTime() {
        return startTime;
    }

    /**
     * Returns end time of this time window
     *
     * @return endTime
     */
    public double getEndTime() {
        return endTime;
    }

    /**
     * Concatenate information of this time window, startTime and endTime, and make them String data type
     *
     * @return String data type which contains information of TimeWindow instance
     */
    @Override
    public String toString() {
        return "TimeWindow{" +
               "StartTime=" + getStartTime() +
               ",EndTime=" + getEndTime() +
               '}';
    }
}
