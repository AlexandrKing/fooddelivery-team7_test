package com.team7;
import java.time.Instant;

public class Courier {
    private Long id;
    private String login;
    private String password;
    private String name;
    private Instant createdAt;
    private Boolean status;
    private Long money;
    private enum activityStatus{
        Active, NotActive
    }
    private enum status{
        Default, Banned
    }
}