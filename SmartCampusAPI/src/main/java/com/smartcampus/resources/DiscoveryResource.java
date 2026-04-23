/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resources;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/*
 * Root discovery endpoint.
 * 
 * GET /api/v1/
 * 
 * Returns basic API metadata and links to main collections.
 */
@Path("")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> discover() {

        Map<String, Object> response = new HashMap<>();
        Map<String, String> resources = new HashMap<>();

        // Main collection links
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");

        // API metadata required by the coursework
        response.put("version", "v1");
        response.put("contact", "admin@smartcampus.local");
        response.put("resources", resources);

        return response;
    }
}
