package cpen221.mp3.server;

import cpen221.mp3.client.RequestCommand;
import cpen221.mp3.client.RequestType;
import cpen221.mp3.entity.Actuator;
import cpen221.mp3.client.Client;
import cpen221.mp3.entity.Entity;
import cpen221.mp3.event.Event;
import cpen221.mp3.client.Request;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;



public class Server {
    private Client client;
    private double maxWaitTime = 2; // in seconds
    public static ConcurrentHashMap<Client, List<Event>> event_log = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Client, Filter> filters = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Client, ConcurrentHashMap<Integer, List<Event>>> clientMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Client, List<Event>> events_of_client = new ConcurrentHashMap<>();
    public Server(Client client) {
        this.client = client;

    }

    /**
     * Update the max wait time for the client.
     * The max wait time is the maximum amount of time
     * that the server can wait for before starting to process each event of the client:
     * It is the difference between the time the message was received on the server
     * (not the event timeStamp from above) and the time it started to be processed.
     *
     * @param maxWaitTime the new max wait time
     */
    public void updateMaxWaitTime(double maxWaitTime) {
        this.maxWaitTime = maxWaitTime;

        // Important note: updating maxWaitTime may not be as simple as
        // just updating the field. You may need to do some additional
        // work to ensure that events currently being processed are not
        // dropped or ignored by the change in maxWaitTime.
    }

    /**
     * Set the actuator state if the given filter is satisfied by the latest event.
     * Here the latest event is the event with the latest timestamp not the event 
     * that was received by the server the latest.
     *
     * If the actuator is not registered for the client, then this method should do nothing.
     * 
     * @param filter the filter to check
     * @param actuator the actuator to set the state of as true
     */
    public void setActuatorStateIf(Filter filter, Actuator actuator) {
        // implement this method and send the appropriate SeverCommandToActuator as a Request to the actuator
        List<Event> event =clientMap.get(this.client).get(actuator.getId());
        Event event_to_check = event.get(event.size()-1);
        if(event!=null && filter.satisfies(event_to_check)){
            Iterator<Entity> iterator = Client.entities.iterator();
            Actuator actuator_to_update = null;

            while (iterator.hasNext()) {
                Entity element = iterator.next();
                if (element.equals(actuator)) {
                    actuator_to_update = (Actuator) element;
                    actuator_to_update.processServerMessage( new Request(RequestType.CONTROL, RequestCommand.CONTROL_SET_ACTUATOR_STATE, SeverCommandToActuator.SET_STATE.toString()));
                }
            }

        }
    }
    
    /**
     * Toggle the actuator state if the given filter is satisfied by the latest event.
     * Here the latest event is the event with the latest timestamp not the event 
     * that was received by the server the latest.
     * 
     * If the actuator has never sent an event to the server, then this method should do nothing.
     * If the actuator is not registered for the client, then this method should do nothing.
     *
     * @param filter the filter to check
     * @param actuator the actuator to toggle the state of (true -> false, false -> true)
     */
    public void toggleActuatorStateIf(Filter filter, Actuator actuator) {
        // implement this method and send the appropriate SeverCommandToActuator as a Request to the actuator
        List<Event> event =clientMap.get(this.client).get(actuator.getId());
        Event event_to_check = event.get(event.size()-1);
        if(event!=null&& filter.satisfies(event_to_check)){
            Iterator<Entity> iterator = Client.entities.iterator();
            Actuator actuator_to_update = null;

            while (iterator.hasNext()) {
                Entity element = iterator.next();
                if (element.equals(actuator)) {
                    actuator_to_update = (Actuator) element;
                    actuator_to_update.processServerMessage( new Request(RequestType.CONTROL, RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE, SeverCommandToActuator.TOGGLE_STATE.toString()));
                }
            }

        }
    }

    /**
     * Log the event ID for which a given filter was satisfied.
     * This method is checked for every event received by the server.
     *
     * @param filter the filter to check
     */
    public void logIf(Filter filter) {
        event_log.put(client, filter.sift(events_of_client.get(client)));
    }

    /**
     * Return all the logs made by the "logIf" method so far.
     * If no logs have been made, then this method should return an empty list.
     * The list should be sorted in the order of event timestamps.
     * After the logs are read, they should be cleared from the server.
     *
     * @return list of event IDs 
     */
    public List<Integer> readLogs() {
        // implement this method
        List<Integer> reading = new ArrayList<>();
        for(Event check: event_log.get(client)){
            if(!reading.contains(check.getEntityId())){
                reading.add(check.getEntityId());
            }
        }
        return reading;
    }

    /**
     * List all the events of the client that occurred in the given time window.
     * Here the timestamp of an event is the time at which the event occurred, not 
     * the time at which the event was received by the server.
     * If no events occurred in the given time window, then this method should return an empty list.
     *
     * @param timeWindow the time window of events, inclusive of the start and end times
     * @return list of the events for the client in the given time window
     */
    public List<Event> eventsInTimeWindow(TimeWindow timeWindow) {
        List<Event> events_in_time = new ArrayList<>();

        for (Event event : events_of_client.get(client)) {
            if (event.getTimeStamp() >= timeWindow.getStartTime() && event.getTimeStamp() <= timeWindow.getEndTime()) {
                events_in_time.add(event);
            }
        }

        return events_in_time;
    }

     /**
     * Returns a set of IDs for all the entities of the client for which 
     * we have received events so far.
     * Returns an empty list if no events have been received for the client.
     * 
     * @return list of all the entities of the client for which we have received events so far
     */
    public List<Integer> getAllEntities() {
        List<Integer> entities_id = new ArrayList<>();
        for (Client client : clientMap.keySet()) {
            if (client.getClientId() == this.client.getClientId()) {
                entities_id.addAll(clientMap.get(this.client).keySet());
                return entities_id;
            }
        }

        return entities_id;
    }
    // public static HashMap<Client, HashMap<Integer, List<Event>>> clientMap = new HashMap<>();

    /**
     * List the latest n events of the client.
     * Here the order is based on the original timestamp of the events, not the time at which the events were received by the server.
     * If the client has fewer than n events, then this method should return all the events of the client.
     * If no events exist for the client, then this method should return an empty list.
     * If there are multiple events with the same timestamp in the boundary,
     * the ones with largest EntityId should be included in the list.
     *
     * @param n the max number of events to list
     * @return list of the latest n events of the client
     */
    public List<Event> lastNEvents(int n) {
        List <Event> events = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            events.add(events_of_client.get(client).get(events_of_client.get(client).size() - i - 1));
        }
        Collections.reverse(events);
        return events;
    }

    /**
     * returns the ID corresponding to the most active entity of the client
     * in terms of the number of events it has generated.
     *
     * If there was a tie, then this method should return the largest ID.
     * 
     * @return the most active entity ID of the client
     */
    public int mostActiveEntity() {


        int max = 0;
        int maxId = 0;

        for (int id : clientMap.get(client).keySet()) {
            if (clientMap.get(client).get(id).size() > max) {
                max = clientMap.get(client).get(id).size();
                maxId = id;
            }
            else if (clientMap.get(client).get(id).size() == max) {
                if (id > maxId) {
                    maxId = id;
                }
            }
        }

        return maxId;


    }

    /**
     * the client can ask the server to predict what will be 
     * the next n timestamps for the next n events 
     * of the given entity of the client (the entity is identified by its ID).
     * 
     * If the server has not received any events for an entity with that ID,
     * or if that Entity is not registered for the client, then this method should return an empty list.
     * 
     * @param entityId the ID of the entity
     * @param n the number of timestamps to predict
     * @return list of the predicted timestamps
     */
    public List<Double> predictNextNTimeStamps(int entityId, int n) {
        // implement this method
        return null;
    }

    /**
     * the client can ask the server to predict what will be 
     * the next n values of the timestamps for the next n events
     * of the given entity of the client (the entity is identified by its ID).
     * The values correspond to Event.getValueDouble() or Event.getValueBoolean() 
     * based on the type of the entity. That is why the return type is List<Object>.
     * 
     * If the server has not received any events for an entity with that ID,
     * or if that Entity is not registered for the client, then this method should return an empty list.
     * 
     * @param entityId the ID of the entity
     * @param n the number of double value to predict
     * @return list of the predicted timestamps
     */
    public List<Object> predictNextNValues(int entityId, int n) {
        // implement this method
        return null;
    }

    /**
     * Process event and add to log of client in server.
     * If event entity isn't in client map add a new entry.
     *
     * @param event the event to be processed
     */
    public void processIncomingEvent(Event event) {
        if (!clientMap.containsKey(client)) {
            clientMap.put(client, new ConcurrentHashMap<>());
        }
        if (!clientMap.get(client).containsKey(event.getEntityId())) {
            clientMap.get(client).put(event.getEntityId(), new ArrayList<>());
        }
        if (!events_of_client.containsKey(client)) {
            events_of_client.put(client, new ArrayList<>());
        }
        clientMap.get(client).get(event.getEntityId()).add(event);
        events_of_client.get(client).add(event);
    }

    /**
     * Process request and if request type is CONTROl
     * and the command is CONFIG_UPDATE then update max
     * wait time of server
     *
     * @param request the request to be processed
     */
    void processIncomingRequest(Request request) {
        if (request.getRequestType() == RequestType.CONTROL) {
            if (request.getRequestCommand() == RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME) {
                updateMaxWaitTime(Integer.parseInt(request.getRequestData()));
            }
        }
    }
}
