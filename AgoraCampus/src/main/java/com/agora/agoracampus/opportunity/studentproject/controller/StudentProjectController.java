package com.agora.agoracampus.opportunity.studentproject.controller;

import com.agora.agoracampus.opportunity.studentproject.dto.request.CreateStudentProjectRequest;
import com.agora.agoracampus.opportunity.studentproject.dto.request.UpdateStudentProjectRequest;
import com.agora.agoracampus.opportunity.studentproject.dto.response.StudentProjectResponse;
import com.agora.agoracampus.opportunity.studentproject.service.StudentProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<StudentProjectResponse> create(@Valid @RequestBody CreateStudentProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentProjectService.create(request));
    }

    @PutMapping("/{id}")
    public StudentProjectResponse update(@PathVariable Long id, @Valid @RequestBody UpdateStudentProjectRequest request) {
        return studentProjectService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentProjectService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
