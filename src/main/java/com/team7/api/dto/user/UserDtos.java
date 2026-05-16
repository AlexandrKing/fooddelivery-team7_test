package com.team7.api.dto.user;

import com.team7.model.client.Address;

import java.util.List;

public final class UserDtos {
    private UserDtos() {
    }

    public record UserProfileResponse(
            Long id,
            String fullName,
            String email,
            String phone,
            String telegramChatId,
            List<Address> addresses
    ) {
    }

    public record UpdateUserProfileRequest(
            String fullName,
            String phone
    ) {
    }

    public record UpdateTelegramChatIdRequest(
            String telegramChatId
    ) {
    }
}