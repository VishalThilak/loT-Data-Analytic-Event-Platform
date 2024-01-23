package cpen221.mp3.server;

import cpen221.mp3.event.Event;

import java.util.ArrayList;
import java.util.List;

public class Filter {
    private String field;
    public BooleanOperator boolOperator;
    private boolean boolValue;
    public DoubleOperator doubleOperator;
    private double doubleValue;
    private List<Filter> filters = new ArrayList<>();

    /**
     * Constructs a filter that compares the boolean (actuator) event value
     * to the given boolean value using the given BooleanOperator.
     * (X (BooleanOperator) value), where X is the event's value passed by satisfies or sift methods.
     * A BooleanOperator can be one of the following:
     * 
     * BooleanOperator.EQUALS
     * BooleanOperator.NOT_EQUALS
     *
     * @param operator the BooleanOperator to use to compare the event value with the given value
     * @param value the boolean value to match
     */
    public Filter(BooleanOperator operator, boolean value) {
        this.boolOperator = operator;
        this.boolValue = value;
    }

    /**
     * Constructs a filter that compares a double field in events
     * with the given double value using the given DoubleOperator.
     * (X (DoubleOperator) value), where X is the event's value passed by satisfies or sift methods.
     * A DoubleOperator can be one of the following:
     * 
     * DoubleOperator.EQUALS
     * DoubleOperator.GREATER_THAN
     * DoubleOperator.LESS_THAN
     * DoubleOperator.GREATER_THAN_OR_EQUALS
     * DoubleOperator.LESS_THAN_OR_EQUALS
     * 
     * For non-double (boolean) value events, the satisfies method should return false.
     *
     * @param field the field to match (event "value" or event "timestamp")
     * @param operator the DoubleOperator to use to compare the event value with the given value
     * @param value the double value to match
     *
     * @throws IllegalArgumentException if the given field is not "value" or "timestamp"
     */
    public Filter(String field, DoubleOperator operator, double value) {
        if(!field.equals("value") && !field.equals("timestamp")){
            throw new IllegalArgumentException();
        }
        this.field = field;
        this.doubleOperator = operator;
        this.doubleValue = value;

    }
    
    /**
     * A filter can be composed of other filters.
     * in this case, the filter should satisfy all the filters in the list.
     * Constructs a complex filter composed of other filters.
     *
     * @param filters the list of filters to use in the composition
     */
    public Filter(List<Filter> filters) {
        this.filters = filters;
    }

    /**
     * Returns true if the given event satisfies the filter criteria.
     *
     * @param event the event to check
     * @return true if the event satisfies the filter criteria, false otherwise
     */
    public boolean satisfies(Event event) {
        if(boolOperator != null){
            if(boolOperator.equals(BooleanOperator.EQUALS)){
                return event.getValueBoolean() == (boolValue);
            }
            if(boolOperator.equals(BooleanOperator.NOT_EQUALS)){
                return !(event.getValueBoolean() == (boolValue));
            }
        }
        else if(doubleOperator != null){
            double val;
            if(field.equals("value")){
                val = event.getValueDouble();
            }
            else{
                val = event.getTimeStamp();
            }

            if(doubleOperator.equals(DoubleOperator.EQUALS)){
                return val == this.doubleValue;
            }
            if(doubleOperator.equals(DoubleOperator.LESS_THAN)){
                return val < this.doubleValue;
            }
            if(doubleOperator.equals(DoubleOperator.LESS_THAN_OR_EQUALS)){
                return val <= this.doubleValue;
            }
            if(doubleOperator.equals(DoubleOperator.GREATER_THAN)){
                return val > this.doubleValue;
            }
            if(doubleOperator.equals(DoubleOperator.GREATER_THAN_OR_EQUALS)){
                return val >= this.doubleValue;
            }
        }
        //for third constructor
        else{
            for(int i = 0; i < filters.size(); i++){
                if(!filters.get(i).satisfies(event)){
                    return false;
                }
            }
            return true;
        }
        return true; //shouldn't come here
    }

    /**
     * Returns true if the given list of events satisfies the filter criteria.
     *
     * @param events the list of events to check
     * @return true if every event in the list satisfies the filter criteria, false otherwise
     */
    public boolean satisfies(List<Event> events) {
        for(int i = 0; i < events.size(); i++){
            if(!satisfies(events.get(i))){
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a new event if it satisfies the filter criteria.
     * If the given event does not satisfy the filter criteria, then this method should return null.
     *
     * @param event the event to sift
     * @return a new event if it satisfies the filter criteria, null otherwise
     */
    public Event sift(Event event) {
        if(satisfies(event)){
            return event;
        }
        return null;
    }

    /**
     * Returns a list of events that contains only the events in the given list that satisfy the filter criteria.
     * If no events in the given list satisfy the filter criteria, then this method should return an empty list.
     *
     * @param events the list of events to sift
     * @return a list of events that contains only the events in the given list that satisfy the filter criteria
     *        or an empty list if no events in the given list satisfy the filter criteria
     */
    public List<Event> sift(List<Event> events) {
        List<Event> list = new ArrayList<>();
        for(Event e : events){
            if(satisfies(e)){
                list.add(e);
            }
        }
        return list;
    }

    @Override
    public String toString() {
        if(boolOperator != null){
            return "Filter{" +
                    "boolOperator=" + boolOperator +
                    ",, boolValue =" + boolValue +
                    '}';
        }
        return "Filter{" +
                "doubleOperator=" + doubleOperator +
                ",, doubleValue =" + doubleValue +
                '}';

    }
}
