package com.javaproj.ToolMates.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.util.List;

@Table("tools")
public class Tool {

    @Id
    private Long id;

    @Column("owner_name")
    private String ownerName;

    @Column("owner_id")
    private String ownerId;

    private String name;
    private String category;

    @Column("tool_condition")
    private String condition;

    @Column("price_per_day")
    private Double pricePerDay;

    @Column("max_renting_period")
    private Integer maxRentingPeriod;

    @Column("pickup_location")
    private String pickupLocation;

    private String description;

    @Column("additional_info")
    private String additionalInfo;

    @Column("is_active")
    private Boolean active = true;

    private List<String> imageUrls;

    public Tool() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Double getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(Double pricePerDay) { this.pricePerDay = pricePerDay; }

    public Integer getMaxRentingPeriod() { return maxRentingPeriod; }
    public void setMaxRentingPeriod(Integer maxRentingPeriod) { this.maxRentingPeriod = maxRentingPeriod; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
}
