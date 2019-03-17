package com.barmej.driverapllication.domain.model;

import java.io.Serializable;

public class Rider implements Serializable {
    private String id;
    private String status;
    private String assignedTrip;
    public Rider() {
    }

    public Rider(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignedTrip() {
        return assignedTrip;
    }

    public void setAssignedTrip(String assignedTrip) {
        this.assignedTrip = assignedTrip;
    }

    public enum Status {
        FREE,
        REQUESTING_TRIP,
        REQUEST_FAILED,
        ON_TRIP,
        ARRIVED

    }
}
