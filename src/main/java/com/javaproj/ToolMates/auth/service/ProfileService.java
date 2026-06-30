package com.javaproj.ToolMates.auth.service;

import com.javaproj.ToolMates.auth.model.Tool;
import com.javaproj.ToolMates.auth.model.User;
import com.javaproj.ToolMates.auth.repository.RentalRequestDao;
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

    public Map<String, Object> getProfile(String studentId) {
        User user = userDao.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("userId", user.getUserId());
        profile.put("name", user.getFirstName() + " " + user.getLastName());
        profile.put("avatarUrl", user.getAvatarUrl());
        profile.put("bio", user.getBio());
        profile.put("suspendedTimes", user.getSuspendedTimes());
        Double averageRating = reviewDao.findAverageForUser(user.getUserId());
        profile.put("averageRating", averageRating == null ? 0 : Math.round(averageRating * 100.0) / 100.0);

        List<Map<String, Object>> tools = toolDao.findByOwnerId(user.getStudentId()).stream()
                .map(tool -> mapTool(tool, user.getUserId()))
                .toList();
        profile.put("listedTools", tools);
        return profile;
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
