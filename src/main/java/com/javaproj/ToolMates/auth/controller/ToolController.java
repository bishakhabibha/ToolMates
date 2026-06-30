package com.javaproj.ToolMates.auth.controller;

import com.javaproj.ToolMates.auth.model.Tool;
import com.javaproj.ToolMates.auth.service.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tools")
@CrossOrigin(origins = {"http://localhost:63342", "http://127.0.0.1:63342", "http://localhost:8080"}, allowCredentials = "true")
public class ToolController {

    @Autowired
    private ToolService toolService;

    @PostMapping("/add")
    public ResponseEntity<Tool> addTool(@RequestBody Tool tool) {
        try {
            Tool savedTool = toolService.saveToolListing(tool);
            return new ResponseEntity<>(savedTool, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Tool>> getRecentTools() {
        try {
            List<Tool> tools = toolService.getRecentTools();
            return new ResponseEntity<>(tools, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Tool>> searchTools(@RequestParam(required = false) String query) {
        try {
            return new ResponseEntity<>(toolService.searchTools(query), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tool> getToolById(@PathVariable Long id) {
        try {
            Tool tool = toolService.getToolById(id);
            if (tool != null) {
                return new ResponseEntity<>(tool, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTool(@PathVariable Long id, @RequestBody Tool tool, HttpSession session) {
        try {
            String studentId = (String) session.getAttribute("studentId");
            return ResponseEntity.ok(toolService.updateTool(id, tool, studentId));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{id}/unlist")
    public ResponseEntity<?> unlistTool(@PathVariable Long id, HttpSession session) {
        try {
            String studentId = (String) session.getAttribute("studentId");
            toolService.unlistTool(id, studentId);
            return ResponseEntity.ok(Map.of("message", "Tool unlisted."));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
