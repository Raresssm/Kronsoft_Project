package com.agora.agoracampus.opportunity.studentproject.controller;

import com.agora.agoracampus.opportunity.studentproject.dto.response.StudentProjectResponse;
import com.agora.agoracampus.opportunity.studentproject.service.StudentProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student-projects")
public class StudentProjectController {

    private final StudentProjectService studentProjectService;

    @GetMapping
    public List<StudentProjectResponse> findAll() {
        return studentProjectService.findAll();
    }

    @GetMapping("/{id}")
    public StudentProjectResponse getById(@PathVariable Long id) {
        return studentProjectService.findById(id);
    }
}
