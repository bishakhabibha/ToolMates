package com.javaproj.ToolMates.auth.dto;

import java.time.LocalDate;

public class RentalRequestCreateRequest {

    private Long toolId;
    private String renterName;
    private String renterId;
    private LocalDate startDate;
    private Integer durationDays;
    private String message;

    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }

    public String getRenterName() { return renterName; }
    public void setRenterName(String renterName) { this.renterName = renterName; }

    public String getRenterId() { return renterId; }
    public void setRenterId(String renterId) { this.renterId = renterId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
