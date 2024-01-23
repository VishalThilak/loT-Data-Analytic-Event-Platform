package cpen221.mp3.event;

public class SensorEvent implements Event {
    private double Timestamp;
    private int ClientId;
    private int EntityId;
    private String EntityType;
    private double value;

    /**
     * Initializes new instance of SensorEvent
     *
     * @param TimeStamp time event occurred
     * @param ClientId integer ID of the client
     * @param EntityId integer ID of entity
     * @param EntityType string representing type of entity
     * @param Value double value representing sensor event
     */
    public SensorEvent(double TimeStamp,
                        int ClientId,
                        int EntityId, 
                        String EntityType, 
                        double Value) {
        this.Timestamp = TimeStamp;
        this.ClientId = ClientId;
        this.EntityId = EntityId;
        this.value = Value;
        this.EntityType = EntityType;
    }

    /**
     * Return Timestamp of sensor-event
     *
     * @return Timestamp
     */
    public double getTimeStamp() {
        return this.Timestamp;
    }

    /**
     * Return ClientId of sensor-event
     *
     * @return ClientId
     */
    public int getClientId() {
        return this.ClientId;
    }

    /**
     * Return EntityId of sensor-event which is either true or false
     *
     * @return EntityId
     */
    public int getEntityId() {
        return this.EntityId;
    }

    /**
     * Return EntityType of sensor-event
     *
     * @return EntityType
     */
    public String getEntityType() {
        return this.EntityType;
    }

    /**
     * Return value of sensor-event
     *
     * @return Value
     */
    public double getValueDouble() {
        return this.value;
    }

    // Sensor events do not have a boolean value
    // no need to implement this method
    public boolean getValueBoolean() {
        return false;
    }

    @Override
    public String toString() {
        return "SensorEvent{" +
               "TimeStamp=" + getTimeStamp() +
               ",ClientId=" + getClientId() + 
               ",EntityId=" + getEntityId() +
               ",EntityType=" + getEntityType() + 
               ",Value=" + getValueDouble() + 
               '}';
    }
}
