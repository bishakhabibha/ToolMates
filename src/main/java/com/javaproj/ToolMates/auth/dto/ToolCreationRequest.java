package com.javaproj.ToolMates.auth.dto;

import java.util.List;

public class ToolCreationRequest {
    private String ownerName;
    private String ownerId;
    private String name;
    private String category;
    private String condition;
    private double pricePerDay;
    private int maxRentingPeriod;
    private String pickupLocation;
    private List<String> imageUrls; // Array to collect up to 3 base64 strings
    private String description;
    private String additionalInfo;

    // Getters and Setters
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public double getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(double pricePerDay) { this.pricePerDay = pricePerDay; }

    public int getMaxRentingPeriod() { return maxRentingPeriod; }
    public void setMaxRentingPeriod(int maxRentingPeriod) { this.maxRentingPeriod = maxRentingPeriod; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }
}
