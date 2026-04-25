package com.agora.agoracampus.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpportunityApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreateVolunteeringOpportunityAndAllowApplying() throws Exception {
        Long posterUserId = extractId(postJson("/api/users", """
                {
                  "keycloakId": "kc-org-1",
                  "email": "org1@example.com",
                  "username": "org1"
                }
                """), "id");

        Long organizationProfileId = extractId(postJson("/api/profiles/organizations", """
                {
                  "appUserId": %d,
                  "header": "Helping hands",
                  "description": "Community programs",
                  "location": "Cluj",
                  "website": "https://example.org",
                  "profilePicture": "profile.png",
                  "coverImage": "cover.png",
                  "organizationName": "Agora Volunteers",
                  "phone": "+40123456789",
                  "industry": "Non-profit",
                  "specialties": "Community outreach"
                }
                """.formatted(posterUserId)), "organizationProfileId");

        Long opportunityId = extractId(postJson("/api/opportunities", """
                {
                  "postedByUserId": %d,
                  "organizationProfileId": %d,
                  "type": "VOLUNTEERING",
                  "title": "Campus Cleanup Drive",
                  "location": "Cluj-Napoca",
                  "period": "May-June 2026",
                  "description": "Help organize recurring campus cleanup events.",
                  "additionalInfo": "Bring outdoor clothes.",
                  "volunteering": {
                    "cause": "Environment",
                    "schedule": "Weekends",
                    "benefits": "Volunteer certificate"
                  }
                }
                """.formatted(posterUserId, organizationProfileId)), "opportunityId");

        Long applicantUserId = extractId(postJson("/api/users", """
                {
                  "keycloakId": "kc-student-1",
                  "email": "student1@example.com",
                  "username": "student1"
                }
                """), "id");

        postJson("/api/opportunities/%d/applications".formatted(opportunityId), """
                {
                  "applicantUserId": %d
                }
                """.formatted(applicantUserId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.applicantUserId").value(applicantUserId));

        mockMvc.perform(get("/api/opportunities/{id}", opportunityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.opportunityId").value(opportunityId))
                .andExpect(jsonPath("$.type").value("VOLUNTEERING"))
                .andExpect(jsonPath("$.postingProfile.profileType").value("ORGANIZATION"))
                .andExpect(jsonPath("$.volunteering.cause").value("Environment"));

        mockMvc.perform(get("/api/opportunities")
                        .param("type", "VOLUNTEERING")
                        .param("location", "Cluj"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].opportunityId").value(opportunityId));

        mockMvc.perform(get("/api/opportunities/{id}/applications", opportunityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicantUserId").value(applicantUserId))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void shouldRejectOpportunityWithoutPostingProfile() throws Exception {
        Long posterUserId = extractId(postJson("/api/users", """
                {
                  "keycloakId": "kc-org-2",
                  "email": "org2@example.com",
                  "username": "org2"
                }
                """), "id");

        mockMvc.perform(post("/api/opportunities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "postedByUserId": %d,
                                  "type": "VOLUNTEERING",
                                  "title": "Invalid opportunity",
                                  "location": "Bucharest",
                                  "period": "June 2026",
                                  "description": "Missing posting profile.",
                                  "volunteering": {
                                    "cause": "Community"
                                  }
                                }
                                """.formatted(posterUserId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Exactly one posting profile ID must be provided."));
    }

    private org.springframework.test.web.servlet.ResultActions postJson(String path, String body) throws Exception {
        return mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private Long extractId(org.springframework.test.web.servlet.ResultActions resultActions, String fieldName) throws Exception {
        MvcResult result = resultActions
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return jsonNode.get(fieldName).asLong();
    }
}
