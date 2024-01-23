# IoT Entities Project Overview

This project focuses on creating a server-side implementation for handling IoT entities and their interactions. It involves developing classes, handling events, and managing client-server communication in a simulated IoT environment.

## Core Concepts

### Entities
- **Sensor**: Passive elements that report data via SensorEvents.
- **Actuator**: Similar to sensors but with mutable states, allowing remote control.

### Events
- **SensorEvent**: For passive entities (sensors).
- **ActuatorEvent**: For active entities (actuators) that can be controlled remotely.
- Events contain metadata like `timestamp`, `clientID`, `entityID`, `entityType`, and `value`.

### Entity Registration
- Entities are registered to a single client and cannot be re-registered.

### Event Generation
- Entities send events to the server using a network (Socket and SocketServer).
- Clients set the event reporting frequency (default 0.2Hz).
- Random values simulate different entity types (e.g., TempSensor, PressureSensor, etc.).
- Entities must have an endpoint set to send events.

## Tasks

### Clients and Requests
- Implement `Client` and `Request` datatypes.
- Handle different request types: CONFIG, CONTROL, ANALYSIS, and PREDICT.
- Serialize complex objects for network transmission.

### Providing Services to Clients
- Implement various server-side capabilities for control and analytics services.
- Develop the `Filter` class for complex criteria-based actions.
- Manage Actuator state updates from the server side.
- Implement Quality of Service (QoS) handling, focusing on event order and processing delays.

## Implementation Notes
- Focus on handling single-client scenarios, but design for potential multi-client expansion.
- The server should reconstruct an image of entities based on incoming events and requests.
- All implementation aspects missing from the provided skeleton code need to be completed.

## Conclusion
This project provides a comprehensive framework for managing IoT entities and their interactions in a simulated environment
