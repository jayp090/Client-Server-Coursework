/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resources;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resource class for Sensor operations.
 *
 * Handles:
 * GET /api/v1/sensors
 * POST /api/v1/sensors
 *
 * Also contains the sub-resource locator for:
 * /api/v1/sensors/{sensorId}/readings
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    /**
     * GET /api/v1/sensors
     * Optional filter:
     * /api/v1/sensors?type=CO2
     */
    @GET
    public List<Sensor> getSensors(@QueryParam("type") @DefaultValue("") String type) {
        List<Sensor> allSensors = new ArrayList<>(RoomResource.sensors.values());

        // If no type filter is provided, return all sensors
        if (type == null || type.trim().isEmpty()) {
            return allSensors;
        }

        // Otherwise return only matching sensor types
        return allSensors.stream()
                .filter(sensor -> sensor.getType() != null && sensor.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    /**
     * POST /api/v1/sensors
     * Creates a new sensor.
     *
     * Coursework rule:
     * The roomId in the request body must already exist.
     */
    @POST
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        Room room = RoomResource.rooms.get(sensor.getRoomId());

        if (room == null) {
            throw new LinkedResourceNotFoundException(
                    "The roomId provided does not exist."
            );
        }

        // Store sensor
        RoomResource.sensors.put(sensor.getId(), sensor);

        // Link sensor to its room
        room.getSensorIds().add(sensor.getId());

        // Create an empty history list for this sensor
        RoomResource.sensorReadings.put(sensor.getId(), new ArrayList<SensorReading>());

        URI uri = uriInfo.getAbsolutePathBuilder()
                .path(sensor.getId())
                .build();

        return Response.created(uri)
                .entity(sensor)
                .build();
    }

    /**
     * Sub-resource locator for sensor readings.
     *
     * Path:
     * /api/v1/sensors/{sensorId}/readings
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
