package com.javaproj.ToolMates.auth.controller;

import com.javaproj.ToolMates.auth.model.Tool;
import com.javaproj.ToolMates.auth.service.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tools")
public class ToolController {

    @Autowired
    private ToolService toolService;

    @PostMapping("/add")
    public ResponseEntity<Tool> addTool(@RequestBody Tool tool) {
        try {
            Tool savedTool = toolService.saveToolListing(tool);
            return new ResponseEntity<>(savedTool, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Tool>> getRecentTools() {
        try {
            List<Tool> tools = toolService.getRecentTools();
            return new ResponseEntity<>(tools, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}