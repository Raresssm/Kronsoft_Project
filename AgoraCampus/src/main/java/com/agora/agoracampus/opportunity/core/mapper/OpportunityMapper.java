package com.agora.agoracampus.opportunity.core.mapper;

import com.agora.agoracampus.opportunity.core.dto.request.CreateOpportunityRequest;
import com.agora.agoracampus.opportunity.core.dto.response.OpportunityResponse;
import com.agora.agoracampus.opportunity.core.model.Opportunity;
import com.agora.agoracampus.opportunity.volunteering.dto.response.VolunteeringResponse;
import com.agora.agoracampus.opportunity.volunteering.mapper.VolunteeringMapper;
import com.agora.agoracampus.profile.core.dto.response.PostingProfileResponse;
import com.agora.agoracampus.profile.core.model.ProfileType;
import com.agora.agoracampus.profile.individual.model.IndividualProfile;
import com.agora.agoracampus.profile.organization.model.OrganizationProfile;
import com.agora.agoracampus.user.core.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpportunityMapper {

    private final VolunteeringMapper volunteeringMapper;

    public Opportunity toEntity(
            CreateOpportunityRequest request,
            AppUser postedByUser,
            OrganizationProfile organizationProfile,
            IndividualProfile individualProfile
    ) {
        Opportunity opportunity = new Opportunity();
        opportunity.setPostedByUser(postedByUser);
        opportunity.setOrganizationProfile(organizationProfile);
        opportunity.setIndividualProfile(individualProfile);
        opportunity.setType(request.type());
        opportunity.setTitle(request.title());
        opportunity.setLocation(request.location());
        opportunity.setPeriod(request.period());
        opportunity.setDescription(request.description());
        opportunity.setAdditionalInfo(request.additionalInfo());
        return opportunity;
    }

    public OpportunityResponse toResponse(Opportunity opportunity) {
        return new OpportunityResponse(
                opportunity.getId(),
                opportunity.getPostedByUser().getId(),
                opportunity.getType(),
                opportunity.getTitle(),
                opportunity.getLocation(),
                opportunity.getPeriod(),
                opportunity.getDescription(),
                opportunity.getAdditionalInfo(),
                opportunity.getCreatedAt(),
                toPostingProfileResponse(opportunity),
                toVolunteeringResponse(opportunity)
        );
    }

    private PostingProfileResponse toPostingProfileResponse(Opportunity opportunity) {
        if (opportunity.getOrganizationProfile() != null) {
            OrganizationProfile organizationProfile = opportunity.getOrganizationProfile();
            return new PostingProfileResponse(
                    ProfileType.ORGANIZATION,
                    organizationProfile.getId(),
                    null,
                    organizationProfile.getProfile().getId(),
                    organizationProfile.getProfile().getAppUser().getId(),
                    organizationProfile.getOrganizationName()
            );
        }

        IndividualProfile individualProfile = opportunity.getIndividualProfile();
        return new PostingProfileResponse(
                ProfileType.INDIVIDUAL,
                null,
                individualProfile.getId(),
                individualProfile.getProfile().getId(),
                individualProfile.getProfile().getAppUser().getId(),
                individualProfile.getFirstName() + " " + individualProfile.getLastName()
        );
    }

    private VolunteeringResponse toVolunteeringResponse(Opportunity opportunity) {
        return volunteeringMapper.toResponse(opportunity.getVolunteering());
    }
}
