package com.javaproj.ToolMates.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("tools")
public class Tool {

    @Id
    private Long id;

    private String name;
    private String category;

    @Column("price_per_hr")
    private Double pricePerHr;

    @Column("image_url")
    private String imageUrl;

    @Column("max_renting_period")
    private Integer maxRentingPeriod;

    public Tool() {}

    public Tool(String name, String category, Double pricePerHr, String imageUrl, Integer maxRentingPeriod) {
        this.name = name;
        this.category = category;
        this.pricePerHr = pricePerHr;
        this.imageUrl = imageUrl;
        this.maxRentingPeriod = maxRentingPeriod;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getPricePerHr() { return pricePerHr; }
    public void setPricePerHr(Double pricePerHr) { this.pricePerHr = pricePerHr; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getMaxRentingPeriod() { return maxRentingPeriod; }
    public void setMaxRentingPeriod(Integer maxRentingPeriod) { this.maxRentingPeriod = maxRentingPeriod; }
}