package com.agora.agoracampus.opportunity.internship.service;

import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.opportunity.core.model.Opportunity;
import com.agora.agoracampus.opportunity.core.repository.OpportunityRepository;
import com.agora.agoracampus.opportunity.internship.dto.request.CreateInternshipRequest;
import com.agora.agoracampus.opportunity.internship.dto.request.UpdateInternshipRequest;
import com.agora.agoracampus.opportunity.internship.dto.response.InternshipResponse;
import com.agora.agoracampus.opportunity.internship.mapper.InternshipMapper;
import com.agora.agoracampus.opportunity.internship.model.Internship;
import com.agora.agoracampus.opportunity.internship.repository.InternshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InternshipService {

    private final InternshipRepository internshipRepository;
    private final OpportunityRepository opportunityRepository;
    private final InternshipMapper internshipMapper;

    public List<InternshipResponse> findAll() {
        return internshipRepository.findAll()
                .stream()
                .map(internshipMapper::toResponse)
                .toList();
    }

    public InternshipResponse findById(Long id) {
        Internship internship = internshipRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Internship not found with the id:"+id));
        return internshipMapper.toResponse(internship);
    }

    public InternshipResponse findByOpportunityId(Long opportunityId) {
        Internship internship = internshipRepository.findByOpportunityId(opportunityId)
                .orElseThrow(() -> new NotFoundException(
                        "Internship for opportunity " + opportunityId + " was not found."
                ));
        return internshipMapper.toResponse(internship);
    }

    public InternshipResponse create(CreateInternshipRequest request) {
        Opportunity opportunity = opportunityRepository.findById(request.getOpportunityId())
                .orElseThrow(() -> new NotFoundException(
                        "Opportunity " + request.getOpportunityId() + " was not found."
                ));
        Internship internship = internshipMapper.toEntity(request, opportunity);
        Internship saved = internshipRepository.save(internship);
        return internshipMapper.toResponse(saved);
    }

    public InternshipResponse update(Long id, UpdateInternshipRequest request) {
        Internship existing = internshipRepository.findById(id)
                .orElseThrow(() ->  new NotFoundException("Internship not found with the id:"+id));
        internshipMapper.updateEntity(existing, request);
        Internship updated = internshipRepository.save(existing);
        return internshipMapper.toResponse(updated);
    }

    public void deleteById(Long id) {
        if (!internshipRepository.existsById(id)) {
            throw   new NotFoundException("Internship not found with the id:"+id);
        }
        internshipRepository.deleteById(id);
    }
}
