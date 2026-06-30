package com.javaproj.ToolMates.auth.dto;

public class ReviewRequest {

    private Long reviewerId;
    private Long reviewedUserId;
    private String reviewedStudentId;
    private Integer ratingStars;
    private String reviewText;

    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }

    public Long getReviewedUserId() { return reviewedUserId; }
    public void setReviewedUserId(Long reviewedUserId) { this.reviewedUserId = reviewedUserId; }

    public String getReviewedStudentId() { return reviewedStudentId; }
    public void setReviewedStudentId(String reviewedStudentId) { this.reviewedStudentId = reviewedStudentId; }

    public Integer getRatingStars() { return ratingStars; }
    public void setRatingStars(Integer ratingStars) { this.ratingStars = ratingStars; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
}
