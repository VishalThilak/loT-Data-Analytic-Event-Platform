package cpen221.mp3.server;

import cpen221.mp3.client.Client;
import cpen221.mp3.entity.Actuator;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;
import cpen221.mp3.CSVEventReader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleServerTests {

    String csvFilePath = "data/tests/single_client_1000_events_in-order.csv";
    CSVEventReader eventReader = new CSVEventReader(csvFilePath);
    List<Event> eventList = eventReader.readEvents();

    Client client = new Client(0, "test@test.com", "1.1.1.1", 1);
    Actuator actuator1 = new Actuator(97, 0, "Switch", true);

    @Test
    public void testSetActuatorStateIf() {
        Server server = new Server(client);
        for (int i = 0; i < 10; i++) {
            server.processIncomingEvent(eventList.get(i));
        }
        Filter sensorValueFilter = new Filter("value", DoubleOperator.GREATER_THAN_OR_EQUALS, 23);
        server.setActuatorStateIf(sensorValueFilter, actuator1);
        assertEquals(true, actuator1.getState());
    }

    @Test
    public void testToggleActuatorStateIf() {
        Server server = new Server(client);
        for (int i = 0; i < 10; i++) {
            server.processIncomingEvent(eventList.get(i));
        }
        Filter sensorValueFilter = new Filter("value", DoubleOperator.GREATER_THAN_OR_EQUALS, 23);
        server.toggleActuatorStateIf(sensorValueFilter, actuator1);
        assertEquals(true, actuator1.getState());
    }

    @Test
    public void testEventsInTimeWindow() {
        Server server = new Server(client);
        TimeWindow tw = new TimeWindow(0.2, 1);
        for (int i = 0; i < 100; i++) {
            server.processIncomingEvent(eventList.get(i));
        }
        List<Event> result = server.eventsInTimeWindow(tw);
        assertEquals(9, result.size());
    }

    @Test
    public void testLastNEvents() {
        Server server = new Server(client);
        for (int i = 0; i < 10; i++) {
            server.processIncomingEvent(eventList.get(i));
        }
        List<Event> result = server.lastNEvents(2);
        assertEquals(2, result.size());
        assertEquals("PressureSensor", result.get(1).getEntityType());
        assertEquals(144, result.get(1).getEntityId());
    }

    @Test
    public void testMostActiveEntity() {
        Server server = new Server(client);
        Event event1 = new ActuatorEvent(0.00010015, 0, 11,"Switch", true);
        Event event2 = new SensorEvent(0.000111818, 0, 1,"TempSensor", 1.0);
        Event event3 = new ActuatorEvent(0.00015, 0, 5,"Switch", false);
        Event event4 = new SensorEvent(0.00022, 0, 1,"TempSensor", 11.0);
        Event event5 = new ActuatorEvent(0.00027, 0, 11,"Switch", true);
        Event event6 = new ActuatorEvent(0.00047, 0, 11,"Switch", true);
        List<Event> simulatedEvents = new ArrayList<>();
        simulatedEvents.add(event1);
        simulatedEvents.add(event2);
        simulatedEvents.add(event3);
        simulatedEvents.add(event4);
        simulatedEvents.add(event5);
        simulatedEvents.add(event6);
        for (int i = 0; i < simulatedEvents.size(); i++) {
            server.processIncomingEvent(simulatedEvents.get(i));
        }
        int mostActiveEntity = server.mostActiveEntity();
        assertEquals(11, mostActiveEntity);
    }

    @Test
    public void testGetAllEntities() {
        Server server = new Server(client);
        Event event1 = new ActuatorEvent(0.00010015, 0, 11,"Switch", true);
        Event event2 = new SensorEvent(0.000111818, 0, 1,"TempSensor", 1.0);
        Event event3 = new ActuatorEvent(0.00015, 0, 5,"Switch", false);
        Event event4 = new SensorEvent(0.00022, 0, 1,"TempSensor", 11.0);
        Event event5 = new ActuatorEvent(0.00027, 0, 11,"Switch", true);
        Event event6 = new ActuatorEvent(0.00047, 0, 11,"Switch", true);
        List<Event> simulatedEvents = new ArrayList<>();
        simulatedEvents.add(event1);
        simulatedEvents.add(event2);
        simulatedEvents.add(event3);
        simulatedEvents.add(event4);
        simulatedEvents.add(event5);
        simulatedEvents.add(event6);
        Set<Integer> expected = new HashSet<>();
        for (int i = 0; i < simulatedEvents.size(); i++) {
            server.processIncomingEvent(simulatedEvents.get(i));
            int entityId = (simulatedEvents.get(i)).getEntityId();
            expected.add(entityId);
        }
        Set<Integer> result = new HashSet<>(server.getAllEntities());
        assertEquals(expected,result);
    }

    @Test
    public void testGetAllEntitiesLargeSet() {
        Server server = new Server(client);
        Event event1 = new ActuatorEvent(0.00010015, 0, 11,"Switch", true);
        Event event2 = new SensorEvent(0.000111818, 0, 1,"TempSensor", 1.0);
        Event event3 = new ActuatorEvent(0.00015, 0, 5,"Switch", true);
        Event event4 = new SensorEvent(0.00022, 0, 1,"TempSensor", 11.0);
        Event event5 = new ActuatorEvent(0.00027, 0, 11,"Switch", true);
        Event event6 = new ActuatorEvent(0.00047, 0, 11,"Switch", true);
        Event event7 = new ActuatorEvent(0.003010015, 0, 5,"Switch", true);
        Event event8 = new SensorEvent(0.00012111818, 0, 1,"TempSensor", 1.0);
        Event event9 = new ActuatorEvent(321, 0, 5,"Switch", false);
        Event event10 = new SensorEvent(0.00022, 0, 1,"TempSensor", 11.0);
        Event event11 = new ActuatorEvent(2233, 1, 131,"Switch", true);
        Event event12 = new ActuatorEvent(0.0220047, 0, 11,"Switch", true);

        List<Event> simulatedEvents = new ArrayList<>();
        simulatedEvents.add(event1);
        simulatedEvents.add(event2);
        simulatedEvents.add(event3);
        simulatedEvents.add(event4);
        simulatedEvents.add(event5);
        simulatedEvents.add(event6);
        simulatedEvents.add(event7);
        simulatedEvents.add(event8);
        simulatedEvents.add(event9);
        simulatedEvents.add(event10);
        simulatedEvents.add(event11);
        simulatedEvents.add(event12);

        Set<Integer> expected = new HashSet<>();
        for (int i = 0; i < simulatedEvents.size(); i++) {
            server.processIncomingEvent(simulatedEvents.get(i));
            int entityId = (simulatedEvents.get(i)).getEntityId();
            expected.add(entityId);
        }
        Set<Integer> result = new HashSet<>(server.getAllEntities());
        assertEquals(expected,result);
    }

}