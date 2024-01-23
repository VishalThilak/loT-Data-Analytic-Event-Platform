package cpen221.mp3.entity;

import cpen221.mp3.client.Client;
import cpen221.mp3.client.Request;
import cpen221.mp3.client.RequestCommand;
import cpen221.mp3.client.RequestType;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;
import cpen221.mp3.handler.MessageHandler;
import cpen221.mp3.server.SeverCommandToActuator;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class Actuator implements Entity {
    private final int id;
    private int clientId;
    private final String type;
    private boolean state;
    private double eventGenerationFrequency = 1; // default value in Hz (1/s)
    // the following specifies the http endpoint that the actuator should send events to
    private String serverIP = null;
    private int serverPort = 0;
    // the following specifies the http endpoint that the actuator should be able to receive commands on from server
    private String host = null;
    public static int port = 3234;
    private int count_fail = 0;
    private int count =0;
    private final double start_time = System.currentTimeMillis();
    private Socket socket;

    /**
     * Creates actuator object.
     *
     * @param id the id of actuator, is unique
     * @param type the type of the actuator
     * @param init_state the initial state of actuator
     */
    public Actuator(int id, String type, boolean init_state) {
        this.id = id;
        this.clientId = -1;         // remains unregistered
        this.type = type;
        this.state = init_state;

    }

    /**
     * Creates actuator object and registers a given client.
     *
     * @param id the id of actuator, is unique
     * @param clientId the ID of the client to register
     * @param type the type of the actuator
     * @param init_state the initial state of actuator
     */
    public Actuator(int id, int clientId, String type, boolean init_state) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        this.state = init_state;
        if (clientId != -1) {
            Client.entities.add(this);
        }

    }

    /**
     * Creates actuator object. Also, attempts to establish a connection
     * with a server endpoint.
     *
     * @param id the id of actuator, is unique
     * @param type the type of the actuator
     * @param init_state the initial state of actuator
     * @param serverIP IP address of the server
     * @param serverPort port number of the server
     */
    public Actuator(int id, String type, boolean init_state, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = -1;         // remains unregistered
        this.type = type;
        this.state = init_state;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        try {
            this.socket = new Socket(this.serverIP,this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        startMyThread();

    }

    /**
     * Creates actuator object and registers a given client. Also,
     * attempts to establish a connection with a server endpoint.
     *
     * @param id the id of actuator, is unique
     * @param clientId the ID of the client to register
     * @param type the type of the actuator
     * @param init_state the initial state of actuator
     * @param serverIP IP address of the server
     * @param serverPort port number of the server
     */
    public Actuator(int id, int clientId, String type, boolean init_state, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        this.state = init_state;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        if (clientId != -1) {
            Client.entities.add(this);
        }
        try {
            this.socket = new Socket(this.serverIP,this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        startMyThread();
    }

    /**
     * Return id of the actuator
     *
     * @return id associated with the actuator
     */
    public int getId() {
        return id;
    }

    /**
     * Return client id of the actuator
     *
     * @return client id associated with the actuator
     */
    public int getClientId() {
        return clientId;
    }

    /**
     * Return type of the actuator
     *
     * @return this.type: the type of the actuator
     */
    public String getType() {
        return type;
    }

    /**
     * Return if this entity is actuator nor not
     *
     * @return true:  since this is actuator, it should always return false
     */
    public boolean isActuator() {
        return true;
    }

    /**
     * Return the state of actuator
     *
     * @return state: the type of the actuator
     */
    public boolean getState() {
        return state;
    }

    /**
     * Return the host of actuator
     *
     * @return state: the host name of the actuator
     */
    public String getIP() {
        return host;
    }

    /**
     * Return the port number of this actuator
     *
     * @return port
     */
    public int getPort() {
        return port;
    }

    public void updateState(boolean new_state) {
        this.state = new_state;
    }

    /**
     * Registers the actuator for the given client
     *
     * @return true if the actuator is new (clientID is -1 already) and gets successfully registered or if it is already registered for clientId, else false
     */
    public boolean registerForClient(int clientId) {
        // implement this method
        if (this.clientId == -1) {
            this.clientId = clientId;
            Client.entities.add(this);
            return true;
        } else return this.clientId == clientId;
    }

    /**
     * Sets or updates the http endpoint that
     * the actuator should send events to
     *
     * @param serverIP   the IP address of the endpoint
     * @param serverPort the port number of the endpoint
     */
    public void setEndpoint(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        try {
            this.socket = new Socket(this.serverIP,this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        startMyThread();
    }

    /**
     * Sets the frequency of event generation
     *
     * @param frequency the frequency of event generation in Hz (1/s)
     */
    public void setEventGenerationFrequency(double frequency) {
            this.eventGenerationFrequency = frequency;

    }

    /**
     * Starts the thread responsible for generating and calling sendEvent to send event to server.
     * each event generation is delayed for 1/eventGenerationFrequency.
     * if sendingEvent call is count_fail is incremented by 1;
     * if count_fail becomes 5, event generation and attempting to send event are paused for 10 seconds. After count_fail is set back to 0.
     *
     */
    public void startMyThread() {
        startServer();
        Runnable myRunnable = () -> {
            double delay = 1/eventGenerationFrequency;
            while (socket.isConnected()) {
                try{
                    Thread.sleep((long)(delay*1000));
                    if(count_fail==5){
                        Thread.sleep(10*1000-(long)(1.0/eventGenerationFrequency)*1000);
                        count_fail = 0;
                    }
                    Event event = event_generation();
                    if (serverIP == null || serverPort ==0) {
                        System.out.println("Server endpoint not set for the sensor.");
                        System.out.println("sending failed");
                        count_fail++;
                    }else {
                        System.out.println("now sending" + event);
                        sendEvent(event);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("sending failed");
                    count_fail++;
                }
            }
        };

        Thread myThread = new Thread(myRunnable);
        myThread.start();
    }

    /**
     * Send event to server/message-handler
     *
     * @param event the evenet to be sent to server/message-handler
     */
    public void sendEvent(Event event) {
        try {
            System.out.println("Sending this following address: "+ serverIP+ " "+ serverPort);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            System.out.println(event+"is send to server");
            out.write(event.toString());
            out.newLine();
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("sending failed");
            count_fail++;
        }
    }

    /**
     * Process request that is sent to this actuator from server.
     * changes the state filed to true when request-command is CONTROL_SET_ACTUATOR_STATE
     * changes the state filed to opposite state when request-command is CONTROL_TOGGLE_ACTUATOR_STATE
     *
     * @param command is not null
     */
    public void processServerMessage(Request command) {
        //this.eventGenerationFrequency = frequency;
        String[] values = command.getRequestData().split(",");
        if (Integer.parseInt(values[0]) == this.id) {
            this.state = Boolean.parseBoolean(values[1]);
            System.out.println("state is changed to " + this.state);
        }

    }
    //CONTROL_SET_ACTUATOR_STATE,
    //    CONTROL_TOGGLE_ACTUATOR_STATE,

    @Override
    public String toString() {
        return "Actuator{" +
                "getId=" + getId() +
                ",ClientId=" + getClientId() +
                ",EntityType=" + getType() +
                ",IP=" + getIP() +
                ",Port=" + getPort() +
                '}';
    }

    /**
     * Generate randomized event:
     * when sending for first_time, initial state is sent to the server. after the state is randomized.
     *
     * @return event generated
     */
    private Event event_generation() {
        Random rd = new Random(); // creating Random object
        boolean value_boolean = rd.nextBoolean();

        double time = (System.currentTimeMillis() - start_time) / 1000.0;

        if (count == 0) {
                Event event = new ActuatorEvent(time, clientId, id, type, state);
                count++;
            System.out.println(event.toString() + " is created");
            return event;
        } else {
                Event event = new ActuatorEvent(time, clientId, id, type, value_boolean);
                updateState(value_boolean);
            System.out.println(event.toString() + " is created");
            return event;
        }

    }

    /**
     * Starts the server of the actuator which listens for commands
     * Sets up socket that listens on port for and handles request
     */
    private void startServer() {
        Runnable myRunnable = () -> {
            try {
                ServerSocket serverSocket = new ServerSocket(4567);
                while (true) {
                    Socket socket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line = in.readLine();
                    System.out.println("Received command: " + line);
                    Request request = new Request(RequestType.CONTROL, RequestCommand.CONTROL_SET_ACTUATOR_STATE, line);
                    processServerMessage(request);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Thread myThread = new Thread(myRunnable);
        myThread.start();
    }

    public static void main(String[] args) {
        // you would need to initialize the RequestHandler with the port number
        // and then start it here
        System.out.println("## Server started ##");
        Client client = new Client(1, "11", "127.0.0.1", MessageHandler.PORT_CONSTANT);
        Entity Switch = new Actuator(3, 1,"Switch", false, "127.0.0.1", 1234);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        client.sendRequest(new Request(RequestType.CONTROL, RequestCommand.CONTROL_NOTIFY_IF, "geq,2.0,value/leq,2.0,timestamp/geq,1.0,timestamp"));
    }

}