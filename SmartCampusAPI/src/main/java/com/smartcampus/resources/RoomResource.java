/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resources;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
/**
 *
 * @author Admin
 */
/*
 * Resource class for Room operations.
 * 
 * Handles:
 * GET    /api/v1/rooms
 * POST   /api/v1/rooms
 * GET    /api/v1/rooms/{roomId}
 * DELETE /api/v1/rooms/{roomId}
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    /*
     * In-memory data structures only.
     * The coursework explicitly says not to use a database.
     */
    public static final Map<String, Room> rooms = new ConcurrentHashMap<>();
    public static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    public static final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    /*
     * Sample starter data so the API has content immediately.
     */
    static {
        Room room1 = new Room("LIB-301", "Library Quiet Study", 40);
        Room room2 = new Room("LAB-101", "Computer Lab", 30);

        rooms.put(room1.getId(), room1);
        rooms.put(room2.getId(), room2);

        Sensor sensor1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 21.5, "LIB-301");
        Sensor sensor2 = new Sensor("CO2-001", "CO2", "MAINTENANCE", 400.0, "LAB-101");

        sensors.put(sensor1.getId(), sensor1);
        sensors.put(sensor2.getId(), sensor2);

        room1.getSensorIds().add(sensor1.getId());
        room2.getSensorIds().add(sensor2.getId());

        sensorReadings.put(sensor1.getId(), new ArrayList<SensorReading>());
        sensorReadings.put(sensor2.getId(), new ArrayList<SensorReading>());
    }

    /*
     * GET /api/v1/rooms
     * Returns all rooms.
     */
    @GET
    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    /*
     * POST /api/v1/rooms
     * Creates a new room and returns 201 Created.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        rooms.put(room.getId(), room);

        URI uri = uriInfo.getAbsolutePathBuilder()
                .path(room.getId())
                .build();

        return Response.created(uri)
                .entity(room)
                .build();
    }

    /*
     * GET /api/v1/rooms/{roomId}
     * Returns a single room by ID.
     */
    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);

        if (room == null) {
            throw new NotFoundException("Room not found: " + roomId);
        }

        return Response.ok(room).build();
    }

    /*
     * DELETE /api/v1/rooms/{roomId}
     * Deletes a room only if it has no sensors assigned.
     */
    @DELETE
    @Path("/{roomId}")
    
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);

        if (room == null) {
            throw new NotFoundException("Room not found: " + roomId);
        }

        // Business rule from the coursework:
        // a room cannot be deleted if sensors are still assigned to it.
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Room cannot be deleted because it still has sensors assigned.");
        }

        rooms.remove(roomId);
        return Response.noContent().build();
    }
}
