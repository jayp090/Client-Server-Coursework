/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resources;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

/**
 * Sub-resource for managing sensor readings.
 *
 * Path: /api/v1/sensors/{sensorId}/readings
 *
 * Handles:
 * GET /api/v1/sensors/{sensorId}/readings
 * POST /api/v1/sensors/{sensorId}/readings
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private String sensorId;

    // Constructor receives the sensorId from the parent resource
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     * Returns the historical readings for this sensor.
     * @return 
     */
    @GET
    public Response getReadings() {
        // Verify sensor exists
        Sensor sensor = RoomResource.sensors.get(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor not found: " + sensorId);
        }

        List<SensorReading> readings = RoomResource.sensorReadings.get(sensorId);
        return Response.ok(readings).build();
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     * Adds a new reading for this sensor.Side effect: Updates the sensor's currentValue.Constraint: Sensor must be ACTIVE (not MAINTENANCE or OFFLINE).
     *
     * @param reading
     * @return 
     */
    @POST
    public Response createReading(SensorReading reading) {
        // Verify sensor exists
        Sensor sensor = RoomResource.sensors.get(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor not found: " + sensorId);
        }

        // Check if sensor is available to accept readings
        if (!"ACTIVE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Cannot add reading: sensor is currently in " + sensor.getStatus() + " status."
            );
        }

        // Add the reading
        List<SensorReading> readings = RoomResource.sensorReadings.get(sensorId);
        if (readings == null) {
            throw new NotFoundException("Sensor reading history not found: " + sensorId);
        }
        readings.add(reading);

        // Update the sensor's currentValue
        sensor.setCurrentValue(reading.getValue());

        URI uri = javax.ws.rs.core.UriBuilder.fromPath("/api/v1/sensors/{sensorId}/readings/{readingId}")
                .resolveTemplate("sensorId", sensorId)
                .resolveTemplate("readingId", reading.getId())
                .build();

        return Response.created(uri)
                .entity(reading)
                .build();
    }
}
