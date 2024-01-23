package cpen221.mp3.entity;


import cpen221.mp3.client.Client;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;


import java.io.IOException;
import java.io.*;
import java.net.Socket;


public class Sensor implements Entity {
    private final int id;
    private int clientId;
    private final String type;
    private String serverIP = null;
    private int serverPort = 0;
    private double eventGenerationFrequency = 1; // default value in Hz (1/s)
    private int count_fail = 0;
    private Socket socket;
    private final double start_time = System.currentTimeMillis();

    /**
     * @param id id of the sensor and not null
     * @param type type of the sensor
     */
    public Sensor(int id, String type) {
        this.id = id;
        this.clientId = -1;         // remains unregistered
        this.type = type;

    }


    /**
     * Set up new Sensor instance
     *
     * @param id id of the sensor and not null
     * @param clientId indicates which client this sensor belongs to if -1 this sensor is not register for the client
     * @param type type of the sensor
     */
    public Sensor(int id, int clientId, String type) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        if (clientId != -1) {
            Client.entities.add(this);
        }

    }


    /**
     * Set up new Sensor instance
     *
     * @param id id of the sensor and not null
     * @param type type of the sensor
     * @param serverIP serverIp of the sensor
     * @param serverPort serverPort of the sensor
     */
    public Sensor(int id, String type, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = -1;   // remains unregistered
        this.type = type;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        try {
            this.socket = new Socket(this.serverIP,this.serverPort);
        } catch (IOException e) {
            System.out.println("socket creation failed");
        }
        startMyThread();
    }


    /**
     *  Set up new Sensor instance
     *
     * @param id id of the sensor and not null
     * @param clientId indicates which client this sensor belongs to if -1 this sensor is not register for the client
     * @param type type of the sensor
     * @param serverIP serverIp of the sensor
     * @param serverPort serverPort of the sensor
     */
    public Sensor(int id, int clientId, String type, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        try {
            this.socket = new Socket(serverIP,serverPort);
        } catch (IOException e) {
            System.out.println("socket creation failed");
        }
        if (clientId != -1) {
            Client.entities.add(this);
        }
        startMyThread();


    }

    /**
     * Return id of the sensor
     *
     * @return id associated with the sensor
     */
    public int getId() {
        return id;
    }

    /**
     * Return client id of the sensor
     *
     * @return client id associated with the sensor
     */
    public int getClientId() {
        return clientId;
    }

    /**
     * Return type of the sensor
     *
     * @return this.type: the type of the sensor
     */
    public String getType() {
        return type;
    }

    /**
     * Return if this sensor is actuator nor not
     *
     * @return false since sensor is not actuator it should always return false
     */
    public boolean isActuator() {
        return false;
    }


    /**
     * Registers the sensor for the given client
     *
     * @return true if the sensor is new (clientID is -1 already) and gets successfully registered or if it is already registered for clientId, else false
     */
    public boolean registerForClient(int clientId) {
        // implement this method
        if (this.clientId == -1) {
            this.clientId = clientId;
            Client.entities.add(this);
            return true;
        } else {
            return this.clientId == clientId;
        }
    }


    /**
     * Sets or updates the http endpoint that
     * the sensor should send events to
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
            System.out.println("socket creation failed");
        }
        startMyThread();


    }


    /**
     * Sets the frequency of event generation
     *
     * @param frequency the frequency of event generation in Hz (1/s)
     */
    public void setEventGenerationFrequency(double frequency) {
        // implement this method
        this.eventGenerationFrequency = frequency;
    }

    /**
     * Send event to server/message-handler
     *
     * @param event to be sent to server/message-handler
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
     * Starts the thread responsible for generating and calling sendEvent to send event to server.
     * each event generation is delayed for 1/eventGenerationFrequency.
     * if sendingEvent call is count_fail is incremented by 1;
     * if count_fail becomes 5, event generation and attempting to send event are paused for 10 seconds. After count_fail is set back to 0.
     *
     */
    public void startMyThread() {
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
     * Generate randomized event:
     * If sensor type is "TempSensor" the value is randomly generated between 20 and 24
     * If sensor type is "PressureSensor" the value is randomly generated between 1020 and 1024
     * If sensor type is "CO2Sensor" the value is randomly generated between 400 and 450
     *
     * @return event is not null
     */
    private Event event_generation(){
        double min;
        double max;
        double value=0.0;
        if (type.equals("TempSensor")) {
            min = 20.0;
            max = 24.0;
            value = (Math.random() * ((max - min) + 1)) + min;
        } else if (type.equals("PressureSensor")) {
            min = 1020.0;
            max = 1024.0;
            value = (Math.random() * ((max - min) + 1)) + min;
        } else if (type.equals("CO2Sensor")) {
            min = 400.0;
            max = 450.0;
            value = (Math.random() * ((max - min) + 1)) + min;
        }
        double time = (System.currentTimeMillis() - start_time) / 1000.0;
        Event event = new SensorEvent(time, clientId, id, type, value);
        System.out.println(event+ " is created");
        return event;
    }
    public static void main(String[] args) {
        // you would need to initialize the RequestHandler with the port number
        // and then start it here
        System.out.println("## Server started ##");
        Entity CO2Sensor = new Sensor(1, 1,"CO2Sensor", "127.0.0.1", 1234);
        try {
            Thread.sleep(9000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Entity thermostat = new Sensor(2, 0,"TempSensor", "127.0.0.1", 1234);
    }
}
