package cpen221.mp3.event;

public class ActuatorEvent implements Event {
    private double Timestamp;
    private int ClientId;
    private int EntityId;
    private String EntityType;
    private boolean Value;

    /**
     * Initializes new instance of ActuatorEvent
     *
     * @param TimeStamp time event occurred
     * @param ClientId integer ID of the client
     * @param EntityId integer ID of entity
     * @param EntityType string representing type of entity
     * @param Value boolean representing of actuator event
     */
    public ActuatorEvent(double TimeStamp,
                        int ClientId,
                        int EntityId, 
                        String EntityType, 
                        boolean Value) {
        this.Timestamp = TimeStamp;
        this.ClientId = ClientId;
        this.EntityId = EntityId;
        this.EntityType = EntityType;
        this.Value = Value;
    }


    /**
     * Return Timestamp of actuator event
     *
     * @return Timestamp
     */
    public double getTimeStamp() {
        return this.Timestamp;
    }

    /**
     * Return Timestamp of actuator event
     *
     * @return Timestamp
     */
    public int getClientId() {
        return this.ClientId;
    }

    /**
     * Return Timestamp of actuator event
     *
     * @return Timestamp
     */
    public int getEntityId() {
        return this.EntityId;
    }

    /**
     * Return Timestamp of actuator event
     *
     * @return Timestamp
     */
    public String getEntityType() {
        return this.EntityType;
    }

    /**
     * Return value of actuator event which is either true or false
     *
     * @return Value
     */
    public boolean getValueBoolean() {
        return this.Value;
    }

    // Actuator events do not have a double value
    // no need to implement this method
    public double getValueDouble() {
        return -1;
    }

    @Override
    public String toString() {
        return "ActuatorEvent{" +
                "TimeStamp=" + getTimeStamp() +
                ",ClientId=" + getClientId() +
                ",EntityId=" + getEntityId() +
                ",EntityType=" + getEntityType() +
                ",Value=" + getValueBoolean() +
                '}';
    }
}
