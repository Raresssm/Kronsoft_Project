package com.agora.agoracampus.web;

import com.agora.agoracampus.opportunity.application.dto.request.CreateOpportunityApplicationRequest;
import com.agora.agoracampus.opportunity.competition.controller.CompetitionController;
import com.agora.agoracampus.opportunity.competition.dto.request.CreateCompetitionRequest;
import com.agora.agoracampus.opportunity.competition.dto.request.UpdateCompetitionRequest;
import com.agora.agoracampus.opportunity.competition.service.CompetitionService;
import com.agora.agoracampus.opportunity.core.controller.OpportunityController;
import com.agora.agoracampus.opportunity.core.dto.request.CreateOpportunityRequest;
import com.agora.agoracampus.opportunity.core.model.OpportunityType;
import com.agora.agoracampus.opportunity.core.service.OpportunityService;
import com.agora.agoracampus.opportunity.internship.controller.InternshipController;
import com.agora.agoracampus.opportunity.internship.dto.request.CreateInternshipRequest;
import com.agora.agoracampus.opportunity.internship.dto.request.UpdateInternshipRequest;
import com.agora.agoracampus.opportunity.internship.service.InternshipService;
import com.agora.agoracampus.opportunity.studentproject.controller.StudentProjectController;
import com.agora.agoracampus.opportunity.studentproject.controller.StudentProjectOpportunityController;
import com.agora.agoracampus.opportunity.studentproject.dto.request.CreateStudentProjectRequest;
import com.agora.agoracampus.opportunity.studentproject.dto.request.UpdateStudentProjectRequest;
import com.agora.agoracampus.opportunity.studentproject.service.StudentProjectService;
import com.agora.agoracampus.opportunity.volunteering.controller.VolunteeringController;
import com.agora.agoracampus.opportunity.volunteering.service.VolunteeringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class OpportunityEndpointTest {

    @Mock
    private OpportunityService opportunityService;

    @Mock
    private CompetitionService competitionService;

    @Mock
    private InternshipService internshipService;

    @Mock
    private StudentProjectService studentProjectService;

    @Mock
    private VolunteeringService volunteeringService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(
                new OpportunityController(opportunityService),
                new CompetitionController(competitionService),
                new InternshipController(internshipService),
                new StudentProjectController(studentProjectService),
                new StudentProjectOpportunityController(studentProjectService),
                new VolunteeringController(volunteeringService)
        ).build();
    }

    @Test
    void coreOpportunityEndpointsAreMapped() throws Exception {
        when(opportunityService.listOpportunities(OpportunityType.INTERNSHIP, "Cluj", 1L)).thenReturn(List.of());
        when(opportunityService.getApplications(10L)).thenReturn(List.of());

        mockMvc.perform(post("/api/opportunities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "postedByUserId": 1,
                                  "organizationProfileId": 10,
                                  "type": "INTERNSHIP",
                                  "title": "Internship",
                                  "location": "Cluj",
                                  "period": "Summer",
                                  "description": "Description"
                                }
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/opportunities")
                        .param("type", "INTERNSHIP")
                        .param("location", "Cluj")
                        .param("postedByUserId", "1"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/opportunities/10")).andExpect(status().isOk());
        mockMvc.perform(post("/api/opportunities/10/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "applicantUserId": 2
                                }
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/opportunities/10/applications")).andExpect(status().isOk());

        verify(opportunityService).createOpportunity(any(CreateOpportunityRequest.class));
        verify(opportunityService).listOpportunities(OpportunityType.INTERNSHIP, "Cluj", 1L);
        verify(opportunityService).getOpportunity(10L);
        verify(opportunityService).applyToOpportunity(eq(10L), any(CreateOpportunityApplicationRequest.class));
        verify(opportunityService).getApplications(10L);
    }

    @Test
    void competitionEndpointsAreMapped() throws Exception {
        when(competitionService.findAll()).thenReturn(List.of());
        when(competitionService.findExpired()).thenReturn(List.of());

        mockMvc.perform(get("/api/competitions")).andExpect(status().isOk());
        mockMvc.perform(get("/api/competitions/10")).andExpect(status().isOk());
        mockMvc.perform(get("/api/competitions/by-opportunity/20")).andExpect(status().isOk());
        mockMvc.perform(get("/api/competitions/expired")).andExpect(status().isOk());
        mockMvc.perform(post("/api/competitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(competitionJson()))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/competitions/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "theme": "Theme",
                                  "eligibility": "Everyone",
                                  "prize": "Prize",
                                  "deadline": "2099-01-01"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/competitions/10")).andExpect(status().isNoContent());

        verify(competitionService).findAll();
        verify(competitionService).findById(10L);
        verify(competitionService).findByOpportunityId(20L);
        verify(competitionService).findExpired();
        verify(competitionService).create(any(CreateCompetitionRequest.class));
        verify(competitionService).update(eq(10L), any(UpdateCompetitionRequest.class));
        verify(competitionService).deleteById(10L);
    }

    @Test
    void internshipEndpointsAreMapped() throws Exception {
        when(internshipService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/internships")).andExpect(status().isOk());
        mockMvc.perform(get("/api/internships/10")).andExpect(status().isOk());
        mockMvc.perform(get("/api/internships/by-opportunity/20")).andExpect(status().isOk());
        mockMvc.perform(post("/api/internships")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "opportunityId": 20,
                                  "duration": "3 months",
                                  "compensation": "Paid",
                                  "requirements": "Java"
                                }
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/internships/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "duration": "6 months",
                                  "compensation": "Paid",
                                  "requirements": "Java"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/internships/10")).andExpect(status().isNoContent());

        verify(internshipService).findAll();
        verify(internshipService).findById(10L);
        verify(internshipService).findByOpportunityId(20L);
        verify(internshipService).create(any(CreateInternshipRequest.class));
        verify(internshipService).update(eq(10L), any(UpdateInternshipRequest.class));
        verify(internshipService).deleteById(10L);
    }

    @Test
    void studentProjectEndpointsAreMapped() throws Exception {
        when(studentProjectService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/student-projects")).andExpect(status().isOk());
        mockMvc.perform(get("/api/student-projects/10")).andExpect(status().isOk());
        mockMvc.perform(post("/api/student-projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "opportunityId": 20,
                                  "projectDomain": "Web",
                                  "requiredSkills": "Java",
                                  "teamSize": 4
                                }
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/student-projects/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectDomain": "Mobile",
                                  "requiredSkills": "Java",
                                  "teamSize": 3
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/student-projects/10")).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/opportunities/20/student-project")).andExpect(status().isOk());

        verify(studentProjectService).findAll();
        verify(studentProjectService).findById(10L);
        verify(studentProjectService).create(any(CreateStudentProjectRequest.class));
        verify(studentProjectService).update(eq(10L), any(UpdateStudentProjectRequest.class));
        verify(studentProjectService).deleteById(10L);
        verify(studentProjectService).getByOpportunityId(20L);
    }

    @Test
    void volunteeringEndpointIsMapped() throws Exception {
        mockMvc.perform(get("/api/opportunities/20/volunteering")).andExpect(status().isOk());

        verify(volunteeringService).getByOpportunityId(20L);
    }

    private String competitionJson() {
        return """
                {
                  "opportunityId": 20,
                  "theme": "Theme",
                  "eligibility": "Everyone",
                  "prize": "Prize",
                  "deadline": "2099-01-01"
                }
                """;
    }
}
