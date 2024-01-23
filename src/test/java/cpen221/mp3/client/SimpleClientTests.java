package cpen221.mp3.client;

import cpen221.mp3.entity.Actuator;
import cpen221.mp3.entity.Entity;
import cpen221.mp3.entity.Sensor;

import cpen221.mp3.handler.MessageHandler;
import cpen221.mp3.server.Server;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleClientTests{

    @Test
    public void testRegisterEntities() {
        Client client = new Client(0, "test@test.com", "127.0.0.1", 1234);

        Entity thermostat = new Sensor(0, client.getClientId(), "TempSensor");
        Entity valve = new Actuator(0, -1, "Switch", false);

        assertFalse(thermostat.registerForClient(1));   // thermostat is already registered to client 0
        assertTrue(thermostat.registerForClient(0));    // registering thermostat for existing client (client 0) is fine and should return true
        assertTrue(valve.registerForClient(1));         // valve was unregistered, and can be registered to client 1, even if it does not exist
    }

    @Test
    public void testAddEntities() {
        Client client1 = new Client(0, "test1@test.com", "127.0.0.1", 4578);
        Client client2 = new Client(1, "test2@test.com", "127.0.0.1", 4578);

        Entity valve = new Actuator(0, -1, "Switch", false);

        assertTrue(client1.addEntity(valve));
        assertFalse(client2.addEntity(valve));
    }

    @Test
    public void testAddEntities2() {
        Client client1 = new Client(0, "test1@test.com", "127.0.0.1", 4578);
        Client client2 = new Client(1, "test2@test.com", "127.0.0.1", 4578);

        Entity valve = new Actuator(0, -1, "Switch", false);
        Entity valve2 = new Actuator(1, -1, "Switch", false);
        assertTrue(client1.addEntity(valve));
        assertTrue(client2.addEntity(valve2));
    }

    @Test
    public void testclients() {

        Client client1 = new Client(0, "test1@test.com", "127.0.0.1", 4578);
        Client client2 = new Client(1, "test2@test.com", "127.0.0.1", 4578);

        Entity S1 = new Actuator(0, 0, "Switch", false);
        S1.setEndpoint("127.0.0.1", 1234);
        Entity temp = new Sensor(1, 1,"TempSensor");
        temp.setEndpoint("127.0.0.1", 1234);
        Entity  co2 = new Sensor(2, 1,"CO2Sensor");
        co2.setEndpoint("127.0.0.1", 1234);
        Entity S2 = new Actuator(3, 0, "Switch", false);
        S2.setEndpoint("127.0.0.1", 1234);

        client1.addEntity(S1);
        client2.addEntity(temp);
        Server SE_1 = new Server(client1);
        Server SE_2 = new Server(client1);
        client1.addEntity(S2);
        client2.addEntity(co2);


        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Request r5 = new Request(RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_ALL_ENTITIES, "Sensor");
        client1.sendRequest(r5);
/*
        assertEquals(client1.getClientId(), 0);
        assertEquals(client2.getClientId(), 1);
        assertTrue(SE_1.getAllEntities().contains(S1.getId()));
        assertTrue(SE_1.getAllEntities().contains(S2.getId()));
        assertTrue(SE_2.getAllEntities().contains(temp.getId()));
        assertTrue(SE_2.getAllEntities().contains(co2.getId()));

 */
    }

    @Test
    public void request_tests() {
        Request r1 = new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "1000");
        Request r2 = new Request(RequestType.CONTROL, RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE, "false");
        Request r3 = new Request(RequestType.CONTROL, RequestCommand.CONTROL_SET_ACTUATOR_STATE, "true");

        assertEquals(r1.getRequestCommand(), RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME);
        assertEquals(r2.getRequestData(), "false");
        assertEquals(r3.getRequestType(), RequestType.CONTROL);


    }
}