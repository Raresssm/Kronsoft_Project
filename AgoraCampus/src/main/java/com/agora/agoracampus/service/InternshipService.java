package com.agora.agoracampus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.agora.agoracampus.dto.request.CreateInternshipRequest;
import com.agora.agoracampus.dto.request.UpdateInternshipRequest;
import  com.agora.agoracampus.dto.response.InternshipResponse;
import  com.agora.agoracampus.exception.InternshipNotFoundException;
import  com.agora.agoracampus.mappers.InternshipMapper;
import  com.agora.agoracampus.models.Internship;
import  com.agora.agoracampus.repository.InternshipRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InternshipService {

    private final InternshipRepository internshipRepository;
    private final InternshipMapper internshipMapper;

    public List<InternshipResponse> findAll() {
        return internshipRepository.findAll()
                .stream()
                .map(internshipMapper::toResponse)
                .toList();
    }

    public InternshipResponse findById(Integer id) {
        Internship internship = internshipRepository.findById(id)
                .orElseThrow(() -> new InternshipNotFoundException(id));
        return internshipMapper.toResponse(internship);
    }

    public List<InternshipResponse> findByOpportunityId(Integer opportunityId) {
        return internshipRepository.findByOpportunityId(opportunityId)
                .stream()
                .map(internshipMapper::toResponse)
                .toList();
    }

    public InternshipResponse create(CreateInternshipRequest request) {
        Internship internship = internshipMapper.toEntity(request);
        Internship saved = internshipRepository.save(internship);
        return internshipMapper.toResponse(saved);
    }

    public InternshipResponse update(Integer id, UpdateInternshipRequest request) {
        Internship existing = internshipRepository.findById(id)
                .orElseThrow(() -> new InternshipNotFoundException(id));
        internshipMapper.updateEntity(existing, request);
        Internship updated = internshipRepository.save(existing);
        return internshipMapper.toResponse(updated);
    }

    public void deleteById(Integer id) {
        if (!internshipRepository.existsById(id)) {
            throw new InternshipNotFoundException(id);
        }
        internshipRepository.deleteById(id);
    }
}
