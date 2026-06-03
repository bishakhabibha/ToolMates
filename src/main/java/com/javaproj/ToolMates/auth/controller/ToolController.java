package com.javaproj.ToolMates.auth.controller;

import com.javaproj.ToolMates.auth.model.Tool;
import com.javaproj.ToolMates.auth.repository.ToolDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tools")
// 🔥 Change this line to match port 63342 exactly!
@CrossOrigin(origins = "http://localhost:63342", allowCredentials = "true")
public class ToolController {

    @Autowired
    private ToolDao toolDao;

    @GetMapping("/recent")
    public ResponseEntity<List<Tool>> getRecentTools() {
        List<Tool> tools = toolDao.getRecentTools();
        return ResponseEntity.ok(tools);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Tool>> searchTools(@RequestParam(required = false) String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(toolDao.getRecentTools());
        }
        List<Tool> tools = toolDao.searchToolsByName(query);
        return ResponseEntity.ok(tools);
    }
}