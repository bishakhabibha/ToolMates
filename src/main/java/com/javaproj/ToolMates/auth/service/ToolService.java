package com.javaproj.ToolMates.auth.service;

import com.javaproj.ToolMates.auth.model.Tool;
import com.javaproj.ToolMates.auth.model.User;
import com.javaproj.ToolMates.auth.repository.ToolDao;
import com.javaproj.ToolMates.auth.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolService {

    @Autowired
    private ToolDao toolDao;

    @Autowired
    private UserDao userDao;

    public Tool saveToolListing(Tool tool, Long actorUserId) {
        if (actorUserId == null) throw new SecurityException("You must be logged in.");
        User owner = userDao.findByUserId(actorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found."));
        if (tool == null) throw new IllegalArgumentException("Tool details are required.");
        tool.setOwnerId(owner.getStudentId());
        tool.setOwnerName((owner.getFirstName() + " " + owner.getLastName()).trim());
        validateTool(tool);
        return toolDao.save(tool);
    }

    public List<Tool> getRecentTools() {
        return toolDao.findRecent();
    }

    public List<Tool> searchTools(String query) {
        return toolDao.searchActive(query);
    }

    public Tool getToolById(Long id) {
        return toolDao.findById(id);
    }

    public Tool updateTool(Long id, Tool incoming, String actorStudentId) {
        if (actorStudentId == null || actorStudentId.isBlank()) {
            throw new SecurityException("You must be logged in.");
        }
        Tool existing = toolDao.findById(id);
        if (existing == null) throw new IllegalArgumentException("Tool not found.");
        if (!actorStudentId.equals(existing.getOwnerId())) {
            throw new SecurityException("Only the owner can edit this tool.");
        }
        applyUpdates(existing, incoming);
        validateTool(existing);
        int updated = toolDao.update(existing);
        if (updated != 1) throw new IllegalArgumentException("Could not update this tool.");
        return toolDao.findById(id);
    }

    public void unlistTool(Long id, String actorStudentId) {
        if (actorStudentId == null || actorStudentId.isBlank()) {
            throw new SecurityException("You must be logged in.");
        }
        Tool existing = toolDao.findById(id);
        if (existing == null) throw new IllegalArgumentException("Tool not found.");
        if (!actorStudentId.equals(existing.getOwnerId())) {
            throw new SecurityException("Only the owner can unlist this tool.");
        }
        if (toolDao.unlist(id, actorStudentId) != 1) {
            throw new IllegalArgumentException("Could not unlist this tool.");
        }
    }

    private void applyUpdates(Tool existing, Tool incoming) {
        if (incoming == null) throw new IllegalArgumentException("Tool details are required.");
        if (incoming.getName() != null) existing.setName(incoming.getName().trim());
        if (incoming.getCategory() != null) existing.setCategory(incoming.getCategory().trim());
        if (incoming.getCondition() != null) existing.setCondition(incoming.getCondition().trim());
        if (incoming.getPricePerDay() != null) existing.setPricePerDay(incoming.getPricePerDay());
        if (incoming.getMaxRentingPeriod() != null) existing.setMaxRentingPeriod(incoming.getMaxRentingPeriod());
        if (incoming.getPickupLocation() != null) existing.setPickupLocation(incoming.getPickupLocation().trim());
        if (incoming.getDescription() != null) existing.setDescription(incoming.getDescription().trim());
        if (incoming.getAdditionalInfo() != null) existing.setAdditionalInfo(incoming.getAdditionalInfo().trim());
        if (incoming.getImageUrls() != null) existing.setImageUrls(incoming.getImageUrls());
    }

    private void validateTool(Tool tool) {
        if (tool.getName() == null || tool.getName().isBlank()) throw new IllegalArgumentException("Tool name is required.");
        if (tool.getCategory() == null || tool.getCategory().isBlank()) throw new IllegalArgumentException("Category is required.");
        if (tool.getCondition() == null || tool.getCondition().isBlank()) throw new IllegalArgumentException("Tool condition is required.");
        if (tool.getPricePerDay() == null || tool.getPricePerDay() < 0) throw new IllegalArgumentException("Price must be valid.");
        if (tool.getMaxRentingPeriod() == null || tool.getMaxRentingPeriod() < 1) throw new IllegalArgumentException("Maximum renting period must be valid.");
        if (tool.getPickupLocation() == null || tool.getPickupLocation().isBlank()) throw new IllegalArgumentException("Pickup location is required.");
        if (tool.getDescription() == null || tool.getDescription().isBlank()) throw new IllegalArgumentException("Description is required.");
    }
}
