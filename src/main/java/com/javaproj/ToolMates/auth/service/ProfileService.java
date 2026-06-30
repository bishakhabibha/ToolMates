package com.javaproj.ToolMates.auth.service;

import com.javaproj.ToolMates.auth.model.Tool;
import com.javaproj.ToolMates.auth.model.User;
import com.javaproj.ToolMates.auth.repository.RentalRequestDao;
import com.javaproj.ToolMates.auth.repository.ReportDao;
import com.javaproj.ToolMates.auth.repository.ReviewDao;
import com.javaproj.ToolMates.auth.repository.ToolDao;
import com.javaproj.ToolMates.auth.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProfileService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private ToolDao toolDao;

    @Autowired
    private ReviewDao reviewDao;

    @Autowired
    private RentalRequestDao rentalRequestDao;

    @Autowired
    private ReportDao reportDao;

    public Map<String, Object> getProfile(String studentId) {
        User user = userDao.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("userId", user.getUserId());
        profile.put("name", user.getFirstName() + " " + user.getLastName());
        profile.put("avatarUrl", user.getAvatarUrl());
        profile.put("bio", user.getBio());
        Double averageRating = reviewDao.findAverageForUser(user.getUserId());
        profile.put("averageRating", averageRating == null ? 0 : Math.round(averageRating * 100.0) / 100.0);

        List<Map<String, Object>> tools = toolDao.findByOwnerId(user.getStudentId()).stream()
                .map(tool -> mapTool(tool, user.getUserId()))
                .toList();
        profile.put("totalReportsReceived", reportDao.countReceivedByUser(user.getUserId()));
        profile.put("toolsListed", tools.size());
        profile.put("toolsSuccessfullyRented", rentalRequestDao.countSuccessfullyLentByOwner(user.getUserId()));
        profile.put("toolsSuccessfullyBorrowed", rentalRequestDao.countSuccessfullyBorrowedByUser(user.getUserId()));
        profile.put("listedTools", tools);
        return profile;
    }

    public Map<String, Object> updateBio(Long userId, String bio) {
        if (userId == null) throw new IllegalArgumentException("You must be logged in.");
        String cleanBio = bio == null || bio.isBlank() ? null : bio.trim();
        if (cleanBio != null && cleanBio.length() > 500) {
            throw new IllegalArgumentException("Bio must be 500 characters or less.");
        }
        User user = userDao.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        userDao.updateBio(userId, cleanBio);
        return getProfile(user.getStudentId());
    }

    private Map<String, Object> mapTool(Tool tool, Long ownerUserId) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", tool.getId());
        item.put("name", tool.getName());
        item.put("category", tool.getCategory());
        item.put("pricePerDay", tool.getPricePerDay());
        item.put("imageUrls", tool.getImageUrls());
        Double rating = reviewDao.findAverageForUser(ownerUserId);
        item.put("rating", rating == null ? 0 : Math.round(rating * 100.0) / 100.0);
        item.put("successfulRentalCount", rentalRequestDao.countClosedByToolId(tool.getId()));
        return item;
    }
}
