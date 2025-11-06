package com.team7.client.model;

import java.util.List;

public class User {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String password;
    private UserRole role;
    private List<Address> addresses;
}
