package cpen221.mp3.client;

import cpen221.mp3.entity.Entity;
import cpen221.mp3.event.Event;

import java.io.*;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Client {
    private final int clientId;
    private final String email;
    private final String serverIP;
    private final int serverPort;
    public static Set<Entity> entities = new HashSet<>();
    Socket socket;

    /**
     * Rep Invariance:
     * cliendId is a unique integer
     * email is a string that's null and a proper email
     * serverIP is a valid IP address
     * serverPort is an integer
     *
     * Abstraction Function:
     * AF(r) = r is client in application
     *  clientId = r.clientId
     *  email = c.email
     *  serverIP = c.serverIP
     *  serverPort = c.serverPort
     *  entities = c.entities
     */

    /**
     * Creates new client
     *
     * @param clientId id of client, is unique
     * @param email email address of client
     * @param serverIP IP address of server
     * @param serverPort port number of server
     */
    public Client(int clientId, String email, String serverIP, int serverPort) {
        this.clientId = clientId;
        this.email = email;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    public int getClientId() {
        return clientId;
    }

    /**
     * Registers an entity for the client
     *
     * @return true if the entity is new and gets successfully registered, false if the Entity is already registered
     */
    public boolean addEntity(Entity entity) {
        if (entities.contains(entity)) {
            return false;
        }
        entities.add(entity);
        entity.registerForClient(clientId);
        return true;
    }

    /**
     * Sends a request to the server. Note here the Request datatype contains a requestData field which is a string, and
     * it depends on the request type and command. Below are the format you should put in the requestData for each
     * request command:
     * CONFIG_UPDATE_MAX_WAIT_TIME: "maxWaitTime" (in double), for example "5.0".
     *
     * CONTROL_SET_ACTUATOR_STATE: "actuatorId,actuatorState,filter_operator,boolean_value_compared_with", actuatorId is
     * the id of the actuator you want set the state, it can only be "true" or "false", actuatorState is the state you
     * want to set, filter_operator is the operator you want to use to filter the last event, it can only be "equals"
     * or "not_equals", boolean_value_compared_with is the value you want to compare with the event value, it can only
     * be "true" or "false". For example, "1,false,equals,true" means you want to set the state of actuator with id 1
     * to true if the last event value equals true.
     *
     * CONTROL_TOGGLE_ACTUATOR_STATE: "actuatorId,filter_operator,boolean_value_compared_with", exactly the same as
     * CONTROL_SET_ACTUATOR_STATE, except that actuatorState is not needed. it will toggle the state of the actuator.
     * For example, "1,equals,true" means you want to toggle the state of actuator with id 1 if the last event value
     * equals true.
     *
     *CONTROL_NOTIFY_IF: "operator, value_double, field"; or "operator, value_boolean"; or
     * "operator, value_double, field/operator, value_double, field", and more compositions of the first two formats.
     * You can use this command change the filter for the event log on the server of this client. The
     * filter can have three possible choices, only one boolean filter, only one double filter, or combinations of both.
     * The boolean filter has the format "operator, value_boolean", where operator can only be "equals" or "not_equals",
     * value_boolean can only be "true" or "false". The double filter has the format "operator, value_double, field",
     * where operator can only be "equals", "greater_than", "less_than", "geq" (means greater than or equals), "leq"
     * (means less than or equals), value_double is a double value, field can only be "timestamp" or "value". For
     * combinations of both, you can use "/" to separate two filters. For example,
     * "geq,2.0,value/leq,2.0,timestamp/geq,1.0,timestamp" means you want to get all events that has value greater than
     * or equals 2.0 and timestamp less than or equals 2.0 and timestamp greater than or equals 1.0. Note that the
     * composition of filters can only be composed of one type of filter, only boolean filters or only double filters.
     * Also note that the order of the filters does not matter.
     *
     * ANALYSIS_GET_EVENTS_IN_WINDOW: "start_timestamp,end_timestamp", start_timestamp is the start timestamp of the
     * window, end_timestamp is the end timestamp of the window. For example, "0.0,100.0" means you want to get all
     * events that belongs to this client into a list in the window [0.0, 100.0].
     *
     * ANALYSIS_GET_ALL_ENTITIES: "", returns a list of all entities that belongs to this client in the form of entity
     * ids, requestData is not needed.
     *
     * ANALYSIS_GET_LATEST_EVENTS: "n", returns a list of latest n events of all entities that belongs to this client,
     * requestData is the number of events you want to get. For example, "10" means you want to get the latest 10
     * events of all entities that belongs to this client.
     *
     * ANALYSIS_GET_MOST_ACTIVE_ENTITY: "", returns the id of the most active entity (entity that has the most events)
     * that belongs to this client, if there are multiple entities that have the same number of events, return the one
     * with the biggest id. requestData is not needed.
     *
     * PREDICT_NEXT_N_TIMESTAMPS: not functioning yet
     *
     * PREDICT_NEXT_N_VALUES: not functioning yet
     *
     * @param request the request to be sent
     */
    public void sendRequest(Request request) {
        try {
            this.socket = new Socket(serverIP, serverPort);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            System.out.println(request+"is send to server");
            out.write(request.toString() + clientId);
            out.newLine();
            out.flush();
            String response = in.readLine();
            System.out.println(response);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("sending failed");
        }
    }




}