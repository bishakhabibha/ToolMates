package com.javaproj.ToolMates.auth.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class PickupScheduleRequest {

    private LocalDate pickupDate;
    private LocalTime pickupTime;
    private String pickupLocation;
    private String pickupInstructions;

    public LocalDate getPickupDate() { return pickupDate; }
    public void setPickupDate(LocalDate pickupDate) { this.pickupDate = pickupDate; }

    public LocalTime getPickupTime() { return pickupTime; }
    public void setPickupTime(LocalTime pickupTime) { this.pickupTime = pickupTime; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getPickupInstructions() { return pickupInstructions; }
    public void setPickupInstructions(String pickupInstructions) { this.pickupInstructions = pickupInstructions; }
}
