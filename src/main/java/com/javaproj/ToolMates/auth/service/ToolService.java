package com.javaproj.ToolMates.auth.service;

import com.javaproj.ToolMates.auth.model.Tool;
import com.javaproj.ToolMates.auth.repository.ToolDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolService {

    @Autowired
    private ToolDao toolDao;

    public Tool saveToolListing(Tool tool) {
        return toolDao.save(tool);
    }

    public List<Tool> getRecentTools() {
        return toolDao.findRecent();
    }

    public Tool getToolById(Long id) {
        return toolDao.findById(id);
    }
}