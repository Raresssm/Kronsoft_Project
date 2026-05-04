package com.agora.agoracampus.web;

import com.agora.agoracampus.profile.background.controller.BackgroundController;
import com.agora.agoracampus.profile.background.dto.request.BackgroundCreateRequest;
import com.agora.agoracampus.profile.background.dto.request.BackgroundUpdateRequest;
import com.agora.agoracampus.profile.background.service.BackgroundService;
import com.agora.agoracampus.profile.core.controller.ProfileController;
import com.agora.agoracampus.profile.core.dto.request.ProfileUpdateRequest;
import com.agora.agoracampus.profile.core.service.ProfileService;
import com.agora.agoracampus.profile.individual.controller.IndividualController;
import com.agora.agoracampus.profile.individual.dto.request.IndividualProfileCreateRequest;
import com.agora.agoracampus.profile.individual.dto.request.IndividualProfileUpdateRequest;
import com.agora.agoracampus.profile.individual.service.IndividualProfileService;
import com.agora.agoracampus.profile.organization.controller.OrganizationController;
import com.agora.agoracampus.profile.organization.dto.request.CreateOrganizationProfileRequest;
import com.agora.agoracampus.profile.organization.dto.request.OrganizationProfileUpdateRequest;
import com.agora.agoracampus.profile.organization.service.OrganizationProfileService;
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
class ProfileEndpointTest {

    @Mock
    private ProfileService profileService;

    @Mock
    private IndividualProfileService individualProfileService;

    @Mock
    private OrganizationProfileService organizationProfileService;

    @Mock
    private BackgroundService backgroundService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(
                new ProfileController(profileService),
                new IndividualController(individualProfileService),
                new OrganizationController(organizationProfileService),
                new BackgroundController(backgroundService)
        ).build();
    }

    @Test
    void profileEndpointsAreMapped() throws Exception {
        mockMvc.perform(get("/api/profiles/10").param("actingUserId", "1"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/profiles/10")
                        .param("actingUserId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileUpdateJson()))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/profiles/10").param("actingUserId", "1"))
                .andExpect(status().isNoContent());

        verify(profileService).getProfileById(10L, 1L);
        verify(profileService).updateProfile(eq(10L), eq(1L), any(ProfileUpdateRequest.class));
        verify(profileService).deleteProfile(10L, 1L);
    }

    @Test
    void individualProfileEndpointsAreMapped() throws Exception {
        when(individualProfileService.getByTitle("Engineer")).thenReturn(List.of());
        when(individualProfileService.getProfileByName("Ana")).thenReturn(List.of());
        when(individualProfileService.getProfileByLocation("Cluj")).thenReturn(List.of());

        mockMvc.perform(get("/api/individuals/title/Engineer")).andExpect(status().isOk());
        mockMvc.perform(get("/api/individuals/10").param("actingUserId", "1")).andExpect(status().isOk());
        mockMvc.perform(post("/api/individuals")
                        .param("actingUserId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "appUserId": 1,
                                  "headline": "Student",
                                  "description": "Description",
                                  "location": "Cluj",
                                  "website": "https://example.com",
                                  "profilePicture": "pic",
                                  "coverImage": "cover",
                                  "firstName": "Ana",
                                  "lastName": "Pop",
                                  "phone": "+401234567890",
                                  "cvDocument": "cv"
                                }
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/individuals/10")
                        .param("actingUserId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "headline": "Student",
                                  "description": "Description",
                                  "location": "Cluj",
                                  "website": "https://example.com",
                                  "profilePicture": "pic",
                                  "coverImage": "cover",
                                  "firstName": "Ana",
                                  "lastName": "Pop",
                                  "phone": "+401234567890",
                                  "cvDocument": "cv"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/individuals/10").param("actingUserId", "1"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/individuals/name/Ana")).andExpect(status().isOk());
        mockMvc.perform(get("/api/individuals/location/Cluj")).andExpect(status().isOk());

        verify(individualProfileService).getByTitle("Engineer");
        verify(individualProfileService).getByProfileId(10L, 1L);
        verify(individualProfileService).create(eq(1L), any(IndividualProfileCreateRequest.class));
        verify(individualProfileService).update(eq(10L), eq(1L), any(IndividualProfileUpdateRequest.class));
        verify(individualProfileService).delete(10L, 1L);
        verify(individualProfileService).getProfileByName("Ana");
        verify(individualProfileService).getProfileByLocation("Cluj");
    }

    @Test
    void organizationProfileEndpointsAreMapped() throws Exception {
        when(organizationProfileService.searchByName("Org")).thenReturn(List.of());
        when(organizationProfileService.searchBySpecialties("Java")).thenReturn(List.of());
        when(organizationProfileService.searchByIndustry("Tech")).thenReturn(List.of());
        when(organizationProfileService.searchByLocation("Cluj")).thenReturn(List.of());

        mockMvc.perform(get("/api/organization/10").param("actingUserId", "1")).andExpect(status().isOk());
        mockMvc.perform(post("/api/organization")
                        .param("actingUserId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "appUserId": 1,
                                  "headline": "Org",
                                  "description": "Description",
                                  "location": "Cluj",
                                  "website": "https://example.com",
                                  "profilePicture": "pic",
                                  "coverImage": "cover",
                                  "organizationName": "Org",
                                  "phone": "+401234567890",
                                  "industry": "Tech",
                                  "specialties": "Java"
                                }
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/organization/name/Org")).andExpect(status().isOk());
        mockMvc.perform(get("/api/organization/specialty/Java")).andExpect(status().isOk());
        mockMvc.perform(get("/api/organization/industry/Tech")).andExpect(status().isOk());
        mockMvc.perform(get("/api/organization/location/Cluj")).andExpect(status().isOk());
        mockMvc.perform(put("/api/organization/10")
                        .param("actingUserId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 10,
                                  "headline": "Org",
                                  "description": "Description",
                                  "location": "Cluj",
                                  "website": "https://example.com",
                                  "profilePicture": "pic",
                                  "coverImage": "cover",
                                  "organizationName": "Org",
                                  "phone": "+401234567890",
                                  "industry": "Tech",
                                  "specialties": "Java"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/organization/10").param("actingUserId", "1"))
                .andExpect(status().isNoContent());

        verify(organizationProfileService).getByProfileId(10L, 1L);
        verify(organizationProfileService).create(eq(1L), any(CreateOrganizationProfileRequest.class));
        verify(organizationProfileService).searchByName("Org");
        verify(organizationProfileService).searchBySpecialties("Java");
        verify(organizationProfileService).searchByIndustry("Tech");
        verify(organizationProfileService).searchByLocation("Cluj");
        verify(organizationProfileService).update(eq(10L), eq(1L), any(OrganizationProfileUpdateRequest.class));
        verify(organizationProfileService).delete(10L, 1L);
    }

    @Test
    void backgroundEndpointsAreMapped() throws Exception {
        mockMvc.perform(post("/api/individuals/10/backgrounds")
                        .param("actingUserId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(backgroundJson("School")))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/individuals/10/backgrounds/20")
                        .param("actingUserId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(backgroundJson("Work")))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/individuals/10/backgrounds/20").param("actingUserId", "1"))
                .andExpect(status().isNoContent());

        verify(backgroundService).createBackground(eq(10L), eq(1L), any(BackgroundCreateRequest.class));
        verify(backgroundService).updateBackground(eq(10L), eq(20L), eq(1L), any(BackgroundUpdateRequest.class));
        verify(backgroundService).deleteBackground(10L, 20L, 1L);
    }

    private String profileUpdateJson() {
        return """
                {
                  "headline": "Headline",
                  "description": "Description",
                  "location": "Cluj",
                  "website": "https://example.com",
                  "profilePicture": "pic",
                  "coverImage": "cover"
                }
                """;
    }

    private String backgroundJson(String title) {
        return """
                {
                  "individualProfileId": 10,
                  "type": "EDUCATION",
                  "title": "%s",
                  "description": "Description",
                  "startDate": "2020-01-01",
                  "currentlyOngoing": true
                }
                """.formatted(title);
    }
}
