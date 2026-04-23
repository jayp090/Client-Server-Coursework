/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.model;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Admin
 */
// POJO representing a Room in the Smart Campus system
public class Room {

    // Unique room identifier, e.g. LIB-301
    private String id;

    // Human-readable room name
    private String name;

    // Maximum room capacity
    private int capacity;

    // IDs of sensors assigned to this room
    private List<String> sensorIds = new ArrayList<>();

    // No-arg constructor required for JSON deserialisation
    public Room() {
    }

    // Constructor for quickly creating Room objects
    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<String> getSensorIds() {
        return sensorIds;
    }

    public void setSensorIds(List<String> sensorIds) {
        this.sensorIds = sensorIds;
    }
}