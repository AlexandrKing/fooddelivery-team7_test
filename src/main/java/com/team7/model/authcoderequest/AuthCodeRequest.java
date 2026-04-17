package com.team7.model.authcoderequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthCodeRequest {
    private String chatId;
    private String code;
    private String userId;
}