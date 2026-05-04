package com.agora.agoracampus.feed.service;

import com.agora.agoracampus.exception.BadRequestException;
import com.agora.agoracampus.exception.NotFoundException;
import com.agora.agoracampus.feed.model.FeedActorRole;
import com.agora.agoracampus.feed.model.FeedUserSubRole;
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
public class FeedPermissionService {

    private final AppUserRepository appUserRepository;
    private final ProfileRepository profileRepository;

    public FeedActor resolveActor(Long actingUserId) {
        if (actingUserId == null) {
            throw new BadRequestException("actingUserId is required.");
        }
        validateUser(actingUserId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = isAuthenticated(authentication);
        boolean adminFromAuthentication = authenticated && hasAdminAuthority(authentication);

        if (authenticated) {
            validateAuthenticatedIdentity(actingUserId, adminFromAuthentication, authentication);
        }

        if (adminFromAuthentication) {
            return new FeedActor(FeedActorRole.ADMIN, null);
        }
        return new FeedActor(FeedActorRole.USER, requireFeedUserSubRole(actingUserId));
    }

    public void requireAdminOrSelf(
            FeedActor actor,
            Long actingUserId,
            Long targetUserId,
            String message
    ) {
        if (actor.role() != FeedActorRole.ADMIN && !actingUserId.equals(targetUserId)) {
            throw new BadRequestException(message);
        }
    }

    public void requireUser(
            FeedActor actor,
            Long actingUserId,
            Long targetUserId,
            String message
    ) {
        if (actor.role() == FeedActorRole.ADMIN || !actingUserId.equals(targetUserId)) {
            throw new BadRequestException(message);
        }
    }

    private void validateUser(Long id) {
        if (!appUserRepository.existsById(id)) {
            throw new NotFoundException("User " + id + " was not found.");
        }
    }

    private FeedUserSubRole requireFeedUserSubRole(Long userId) {
        Profile profile = profileRepository.findByAppUser_Id(userId)
                .orElseThrow(() -> new BadRequestException(
                        "User " + userId + " has no profile and cannot use the feed service."
                ));

        ProfileType profileType = profile.getProfileType();
        if (profileType == ProfileType.INDIVIDUAL) {
            return FeedUserSubRole.INDIVIDUAL;
        }
        if (profileType == ProfileType.ORGANIZATION) {
            return FeedUserSubRole.ORGANIZATION;
        }

        throw new BadRequestException(
                "User " + userId + " has an unsupported profile type for the feed service."
        );
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

    public record FeedActor(FeedActorRole role, FeedUserSubRole userSubRole) {
    }
}
