package com.team7.api.controller;

import com.team7.api.dto.user.UserDtos;
import com.team7.api.response.ApiSuccessResponse;
import com.team7.model.client.Address;
import com.team7.persistence.AddressEntityMappings;
import com.team7.persistence.AddressJpaRepository;
import com.team7.persistence.AppAccountJpaRepository;
import com.team7.persistence.UserJpaRepository;
import com.team7.persistence.entity.AppAccountEntity;
import com.team7.persistence.entity.AppRole;
import com.team7.persistence.entity.UserEntity;
import com.team7.service.telegramnotificationservice.OrderNotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final AppAccountJpaRepository appAccountJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final AddressJpaRepository addressJpaRepository;
    private final OrderNotificationService orderNotificationService;

    public UserController(
            AppAccountJpaRepository appAccountJpaRepository,
            UserJpaRepository userJpaRepository,
            AddressJpaRepository addressJpaRepository,
            OrderNotificationService orderNotificationService
    ) {
        this.appAccountJpaRepository = appAccountJpaRepository;
        this.userJpaRepository = userJpaRepository;
        this.addressJpaRepository = addressJpaRepository;
        this.orderNotificationService = orderNotificationService;
    }

    @GetMapping("/me")
    public ApiSuccessResponse<UserDtos.UserProfileResponse> me(Authentication authentication) {
        return ApiSuccessResponse.of(toProfileResponse(resolveCurrentUser(authentication)));
    }

    @PutMapping("/me")
    @Transactional
    public ApiSuccessResponse<UserDtos.UserProfileResponse> updateMe(
            @RequestBody UserDtos.UpdateUserProfileRequest request,
            Authentication authentication
    ) {
        UserEntity user = resolveCurrentUser(authentication);
        if (request != null) {
            if (request.fullName() != null && !request.fullName().isBlank()) {
                user.setFullName(request.fullName().trim());
            }
            if (request.phone() != null && !request.phone().isBlank()) {
                user.setPhone(request.phone().trim());
            }
        }
        return ApiSuccessResponse.of(toProfileResponse(userJpaRepository.save(user)));
    }

    @PutMapping("/me/telegram-chat-id")
    @Transactional
    public ApiSuccessResponse<UserDtos.UserProfileResponse> updateTelegramChatId(
            @RequestBody UserDtos.UpdateTelegramChatIdRequest request,
            Authentication authentication
    ) {
        UserEntity user = resolveCurrentUser(authentication);

        boolean shouldSendConfirmation = false;

        if (request == null || request.telegramChatId() == null || request.telegramChatId().isBlank()) {
            user.setTelegramChatId(null);
        } else {
            user.setTelegramChatId(request.telegramChatId().trim());
            shouldSendConfirmation = true;
        }

        UserEntity saved = userJpaRepository.save(user);

        if (shouldSendConfirmation) {
            orderNotificationService.sendTelegramConnected(saved.getId());
        }

        return ApiSuccessResponse.of(toProfileResponse(saved));
    }

    private UserEntity resolveCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("User is not authenticated");
        }
        AppAccountEntity account = appAccountJpaRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (account.getRole() != AppRole.USER) {
            throw new IllegalArgumentException("User profile is available only for USER accounts");
        }
        Long userId = account.getLinkedUserId();
        if (userId == null) {
            throw new IllegalArgumentException("User profile is not linked");
        }
        return userJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User profile not found"));
    }

    private UserDtos.UserProfileResponse toProfileResponse(UserEntity user) {
        List<Address> addresses = addressJpaRepository.findByUserIdOrderByIdAsc(user.getId()).stream()
                .map(AddressEntityMappings::toDto)
                .toList();
        return new UserDtos.UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getTelegramChatId(),
                addresses
        );
    }
}