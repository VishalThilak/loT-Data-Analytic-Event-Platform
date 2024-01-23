package cpen221.mp3.handler;

import cpen221.mp3.client.Client;
import cpen221.mp3.entity.Actuator;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;
import cpen221.mp3.server.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static cpen221.mp3.server.Server.*;


public class MessageHandlerThread implements Runnable {
    private Socket incomingSocket;
    private BufferedReader in;
    private BufferedWriter out;
    private boolean firsttime = true;
    private String type;

    private Server server;

    /**
     * Constructs MessageHandlerThread object
     *
     * @param incomingSocket socket object to connect with client
     */
    public MessageHandlerThread(Socket incomingSocket) {
        try{
            this.incomingSocket = incomingSocket;
            this.in = new BufferedReader(new InputStreamReader(incomingSocket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(incomingSocket.getOutputStream()));
        }catch(IOException e){
            closeEverything(incomingSocket, in, out);
        }

    }

    /**
     * Run method for MessageHandlerThread
     * that processes messages from client and handles
     * those events and requests.
     */
    @Override
    public void run() {
        String line;
        Client client_for_entity = null;
        while (incomingSocket.isConnected()) {
            try {
                line = in.readLine();
                System.out.println("Received message: " + line);
                //data.add(line);
                //System.out.println("Received message: " + data.get(data.size()-1));
                    
                    if (line.contains("SensorEvent") || line.contains("ActuatorEvent")) {
                        String keyValuePairs = line.substring(line.indexOf('{') + 1, line.indexOf('}'));
                        String[] pairs = keyValuePairs.split(",");

                        double timeStamp = 0;
                        int clientId = 0;
                        int entityId = 0;
                        String entityType = "";
                        double value = 0;
                        boolean value_bool = false;

                        for (String pair : pairs) {
                            String[] keyValue = pair.split("=");

                            switch (keyValue[0].trim()) {
                                case "TimeStamp":
                                    timeStamp = Double.parseDouble(keyValue[1].trim());
                                    break;
                                case "ClientId":
                                    clientId = Integer.parseInt(keyValue[1].trim());
                                    break;
                                case "EntityId":
                                    entityId = Integer.parseInt(keyValue[1].trim());
                                    break;
                                case "EntityType":
                                    entityType = keyValue[1].trim();
                                    break;
                                case "Value":
                                    if (line.contains("ActuatorEvent"))
                                        value_bool = Boolean.parseBoolean(keyValue[1].trim());
                                    else{
                                        value = Double.parseDouble(keyValue[1].trim());
                                    }
                                    break;
                            }
                        }

                        if (firsttime) {
                            firsttime = false;
                            boolean isnew = true;


                            for (Client client : clientMap.keySet()) {
                                if (client.getClientId() == clientId) {
                                    isnew = false;
                                    client_for_entity = client;
                                    break;
                                }
                            }
                            if (isnew) {
                                client_for_entity = new Client(clientId, "email", "127.0.0.1", 1234);
                                clientMap.put(client_for_entity, new ConcurrentHashMap<>());
                                events_of_client.put(client_for_entity, new ArrayList<>());
                            }
                            server = new Server(client_for_entity);
                            clientMap.get(client_for_entity).put(entityId, new ArrayList<>());
                        }



                        Event event;
                        if (line.contains("SensorEvent")) {
                            event = new SensorEvent(timeStamp, clientId, entityId, entityType, value);
                        }
                        else {
                            event = new ActuatorEvent(timeStamp, clientId, entityId, entityType, value_bool);
                        }

                        server.processIncomingEvent(event);

                        if (filters.get(client_for_entity) != null){
                            if (filters.get(client_for_entity).satisfies(event)){
                                event_log.putIfAbsent(client_for_entity, new ArrayList<>());
                                event_log.get(client_for_entity).add(event);
                            }
                        }

                    }
                    else if (line.contains("Request")){





                        int bracePosition = line.lastIndexOf('}');
                        String requestPart = line.substring(0, bracePosition + 1);
                        int clientId = Integer.parseInt(line.substring(bracePosition + 1)) ;

                        String keyValuePairs = requestPart.substring(requestPart.indexOf('{') + 1, requestPart.indexOf('}'));
                        String[] pairs = keyValuePairs.split(",,");


                        String requestType = "";
                        String requestCommand = "";
                        String requestData = "";

                        for (String pair : pairs) {
                            String[] keyValue = pair.split("=");
                            String key = keyValue[0].trim();
                            String value = keyValue[1].trim();

                            switch (key) {
                                case "requestType":
                                    requestType = value;
                                    break;
                                case "requestCommand":
                                    requestCommand = value;
                                    break;
                                case "requestData":
                                    requestData = value;
                                    break;
                            }
                        }




                        if (firsttime) {
                            firsttime = false;
                            boolean isnew = true;

                            for (Client client : clientMap.keySet()) {
                                if (client.getClientId() == clientId) {
                                    isnew = false;
                                    client_for_entity = client;
                                    break;
                                }
                            }
                            if (isnew) {
                                client_for_entity = new Client(clientId, "email", "127.0.0.1", 1234);
                                clientMap.put(client_for_entity, new ConcurrentHashMap<>());
                                events_of_client.put(client_for_entity, new ArrayList<>());
                            }
                            server = new Server(client_for_entity);
                        }





                        switch (requestCommand) {
                            case "CONFIG_UPDATE_MAX_WAIT_TIME" -> {
                                server.updateMaxWaitTime(Double.parseDouble(requestData));
                                out.write("updated");
                            }
                            case "CONTROL_SET_ACTUATOR_STATE" -> {
                                int actuatorid;

                                    Filter filter = null;

                                    String[] keyValues = requestData.split(",");
                                    actuatorid = Integer.parseInt(keyValues[0]);

                                    if (keyValues[2].equals("equals")) {
                                        filter = new Filter(BooleanOperator.EQUALS, Boolean.parseBoolean(keyValues[3]));
                                    }
                                    else if (keyValues[2].equals("not_equals")) {
                                        filter = new Filter(BooleanOperator.NOT_EQUALS, Boolean.parseBoolean(keyValues[3]));
                                    }

                                    if (filter != null) {
                                        if (!clientMap.get(client_for_entity).containsKey(actuatorid)) {
                                            out.write("actuator not found");
                                            break;
                                        }
                                        Event event = clientMap.get(client_for_entity).get(actuatorid).get(clientMap.get(client_for_entity).get(actuatorid).size() - 1);
                                        boolean result = filter.satisfies(event);
                                        if (result) {
                                            Socket socket = new Socket(incomingSocket.getInetAddress().getHostAddress(), Actuator.port);
                                            BufferedWriter sendout = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                                            if (keyValues[1].equals("true")) {
                                                sendout.write(actuatorid + "," + true);
                                                System.out.println("command sent");
                                                sendout.newLine();
                                                sendout.flush();
                                                out.write("command sent");

                                            } else if (keyValues[1].equals("false")) {
                                                sendout.write(actuatorid + "," + false);
                                                System.out.println("command sent");
                                                sendout.newLine();
                                                sendout.flush();
                                                out.write("command sent");
                                            } else {
                                                out.write("invalid");
                                                System.out.println("invalid");
                                            }
                                        }
                                        else {
                                            System.out.println("command not sent");
                                            out.write("filter not satisfied");
                                        }
                                    }
                                    else {
                                        out.write("invalid");
                                        System.out.println("invalid");
                                    }







                            }
                            case "CONTROL_TOGGLE_ACTUATOR_STATE" -> {
                                int actuatorid;
                                    Filter filter = null;

                                    String[] keyValues = requestData.split(",");
                                    actuatorid = Integer.parseInt(keyValues[0]);


                                    if (keyValues[1].equals("equals")) {
                                        filter = new Filter(BooleanOperator.EQUALS, Boolean.parseBoolean(keyValues[2]));
                                    } else if (keyValues[1].equals("not_equals")) {
                                        filter = new Filter(BooleanOperator.NOT_EQUALS, Boolean.parseBoolean(keyValues[2]));
                                    }


                                    if (filter != null) {
                                        if (!clientMap.get(client_for_entity).containsKey(actuatorid)) {
                                            out.write("actuator not found");
                                            break;
                                        }
                                        Event event = clientMap.get(client_for_entity).get(actuatorid).get(clientMap.get(client_for_entity).get(actuatorid).size() - 1);
                                        boolean result = filter.satisfies(event);
                                        if (result){
                                            boolean current_state = event.getValueBoolean();

                                            Socket socket = new Socket(incomingSocket.getInetAddress().getHostAddress(), Actuator.port);
                                            BufferedWriter sendout = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                                            sendout.write(actuatorid + "," + !current_state);
                                            sendout.newLine();
                                            sendout.flush();
                                            out.write("command sent");
                                            System.out.println("command sent");
                                        }
                                        else {
                                            System.out.println("command not sent");
                                            out.write("filter not satisfied");
                                        }

                                    } else {
                                        out.write("invalid");
                                        System.out.println("invalid");
                                    }


                            }




                            case "CONTROL_NOTIFY_IF" -> {
                                String[] keyValues;
                                if (requestData.contains("/")){
                                     keyValues = requestData.split("/");
                                }
                                else {
                                     keyValues = new String[1];
                                     keyValues[0] = requestData;
                                }
                                Filter filter = decompose_filter(keyValues);

                                assert client_for_entity != null;

                                if (filter != null) {
                                    event_log.putIfAbsent(client_for_entity, new ArrayList<>());
                                    event_log.get(client_for_entity).clear();
                                    filters.put(client_for_entity, filter);
                                    out.write("filter set");
                                    System.out.println("filter set");
                                }
                                else {
                                    out.write("invalid");
                                    System.out.println("invalid");
                                }

                            }




                            case "ANALYSIS_GET_EVENTS_IN_WINDOW" -> {
                                try {
                                    long start = Long.parseLong(requestData.split(",")[0]);
                                    long end = Long.parseLong(requestData.split(",")[1]);
                                    out.write(server.eventsInTimeWindow(new TimeWindow(start, end)).toString());
                                }
                                catch (Exception e) {
                                    out.write("invalid");
                                    System.out.println("invalid");
                                }
                            }
                            case "ANALYSIS_GET_ALL_ENTITIES" -> {
                                try {
                                    out.write(server.getAllEntities().toString());
                                }
                                catch (Exception e) {
                                    out.write("invalid");
                                    System.out.println("invalid");
                                }
                            }
                            case "ANALYSIS_GET_LATEST_EVENTS" -> {
                                out.write(server.lastNEvents(Integer.parseInt(requestData)).toString());
                            }
                            case "ANALYSIS_GET_MOST_ACTIVE_ENTITY" -> {
                                out.write(server.mostActiveEntity());
                            }
                        }



                        out.newLine();
                        out.flush();
                    }
                    else {
                        out.write("invalid");
                        out.newLine();
                        out.flush();
                        System.out.println("invalid");
                    }

            } catch (IOException e) {
                closeEverything(incomingSocket, in, out);
                break;
            }
        }
    }



    /**
     *When IOException is catched in check the condition of socket, inputstream, and outputstream.
     * When the variable is null close the connection.
     *
     * @param incomingSocket the socket of incoming Socket which has requested connection to server/message-handler
     * @param in InputStream to message-handler
     * @param out OutputStream to incoming socket
     *
     */
    private void closeEverything(Socket incomingSocket, BufferedReader in, BufferedWriter out) {
        try{
            if(out != null){
                out.close();
            }
            if(in != null){
                in.close();
            }
            if(incomingSocket != null){
                incomingSocket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Convert string array of filters descriptions to Filter object
     *
     * @param strings array of strings, where each string is
     *                either Boolean filters (with its operators)
     *                or Double filters (with its operators)
     * @return filter object
     */
    private Filter decompose_filter(String[] strings) {
        List<Filter> filters = new ArrayList<>();
        Iterator<String> iterator = List.of(strings).iterator();
        try {
            while (iterator.hasNext()) {
                String element = iterator.next();
                String[] values = element.split(",");
                if (values.length == 2) {
                    if (values[0].equals("equals")) {
                        filters.add(new Filter(BooleanOperator.EQUALS, Boolean.parseBoolean(values[1])));
                    } else if (values[0].equals("not_equals")) {
                        filters.add(new Filter(BooleanOperator.NOT_EQUALS, Boolean.parseBoolean(values[1])));
                    }
                } else if (values.length == 3) {
                    switch (values[0]) {
                        case "equals" -> filters.add(new Filter(values[2], DoubleOperator.EQUALS, Double.parseDouble(values[1])));
                        case "greater_than" -> filters.add(new Filter(values[2], DoubleOperator.GREATER_THAN, Double.parseDouble(values[1])));
                        case "less_than" -> filters.add(new Filter(values[2], DoubleOperator.LESS_THAN, Double.parseDouble(values[1])));
                        case "geq" -> filters.add(new Filter(values[2], DoubleOperator.GREATER_THAN_OR_EQUALS, Double.parseDouble(values[1])));
                        case "leq" -> filters.add(new Filter(values[2], DoubleOperator.LESS_THAN_OR_EQUALS, Double.parseDouble(values[1])));
                    }

                }
            }

            if (filters.get(0).boolOperator != null) {
                for (Filter filter : filters) {
                    if (filter.doubleOperator != null) {
                        return null;
                    }
                }
            } else {
                for (Filter filter : filters) {
                    if (filter.boolOperator != null) {
                        return null;
                    }
                }
            }

        } catch (Exception e) {
            return null;
        }

        return new Filter(filters);
    }


}