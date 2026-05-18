package com.agora.agoracampus.opportunity.core.service;

import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.opportunity.core.model.OpportunityActorRole;
import com.agora.agoracampus.profile.core.model.Profile;
import com.agora.agoracampus.profile.core.model.ProfileType;
import com.agora.agoracampus.profile.core.repository.ProfileRepository;
import com.agora.agoracampus.user.core.model.AppUser;
import com.agora.agoracampus.user.core.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class OpportunityPermissionService {

    private final AppUserRepository appUserRepository;
    private final ProfileRepository profileRepository;

    public OpportunityActor resolveActor(Long actingUserId) {
        if (actingUserId == null) {
            throw new BadRequestException("actingUserId is required.");
        }
        validateUserExists(actingUserId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = isAuthenticated(authentication);
        boolean adminFromAuthentication = authenticated && hasAdminAuthority(authentication);

        if (authenticated) {
            validateAuthenticatedIdentity(actingUserId, adminFromAuthentication, authentication);
        }

        if (adminFromAuthentication) {
            return new OpportunityActor(OpportunityActorRole.ADMIN);
        }
        return new OpportunityActor(resolveProfileRole(actingUserId));
    }

    public void requireOrganizationOrAdmin(OpportunityActor actor, String message) {
        if (actor.role() == OpportunityActorRole.INDIVIDUAL) {
            throw new BadRequestException(message);
        }
    }

    public void requireIndividualSelfApplicant(
            OpportunityActor actor,
            Long actingUserId,
            Long applicantUserId,
            String message
    ) {
        if (actor.role() != OpportunityActorRole.INDIVIDUAL || !actingUserId.equals(applicantUserId)) {
            throw new BadRequestException(message);
        }
    }

    public void requireAdminOrOwner(
            OpportunityActor actor,
            Long actingUserId,
            Long ownerId,
            String message
    ) {
        if (actor.role() != OpportunityActorRole.ADMIN && !actingUserId.equals(ownerId)) {
            throw new BadRequestException(message);
        }
    }

    private void validateUserExists(Long userId) {
        if (!appUserRepository.existsById(userId)) {
            throw new NotFoundException("User " + userId + " was not found.");
        }
    }

    private OpportunityActorRole resolveProfileRole(Long actingUserId) {
        Profile profile = profileRepository.findByAppUser_Id(actingUserId)
                .orElseThrow(() -> new BadRequestException(
                        "User " + actingUserId + " has no profile."
                ));

        if (profile.getProfileType() == ProfileType.ORGANIZATION) {
            return OpportunityActorRole.ORGANIZATION;
        }
        if (profile.getProfileType() == ProfileType.INDIVIDUAL) {
            return OpportunityActorRole.INDIVIDUAL;
        }

        throw new BadRequestException("User " + actingUserId + " has unsupported profile type.");
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private boolean hasAdminAuthority(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String normalizedAuthority = authority.getAuthority().toUpperCase(Locale.ROOT);
            if ("ROLE_ADMIN".equals(normalizedAuthority) || "ADMIN".equals(normalizedAuthority)) {
                return true;
            }
        }
        return false;
    }

    private void validateAuthenticatedIdentity(
            Long actingUserId,
            boolean adminFromAuthentication,
            Authentication authentication
    ) {
        if (adminFromAuthentication) {
            return;
        }

        String principalName = authentication.getName();
        if (principalName == null || principalName.isBlank() || "anonymousUser".equals(principalName)) {
            return;
        }

        AppUser authenticatedUser = appUserRepository.findByKeycloakId(principalName)
                .orElseThrow(() -> new BadRequestException(
                        "Authenticated principal is not registered as an application user."
                ));

        if (!authenticatedUser.getId().equals(actingUserId)) {
            throw new BadRequestException("actingUserId must match the authenticated user.");
        }
    }

    public record OpportunityActor(OpportunityActorRole role) {
    }
}
