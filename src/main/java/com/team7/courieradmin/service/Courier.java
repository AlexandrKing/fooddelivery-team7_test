package com.team7.courieradmin.service;
import java.time.Instant;


public class Courier {
    private Long id;
    private String login;
    private String password;
    private String name;
    private String email;
    private Instant createdAt;
    private Long money;
    private ActivityStatus activityStatus = ActivityStatus.NotActive;
    private Status status = Status.Default;

    public enum ActivityStatus {
        Active, NotActive
    }

    public enum Status {
        Default, Banned
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Long getMoney() { return money; }
    public void setMoney(Long money) { this.money = money; }

    public ActivityStatus getActivityStatus() { return activityStatus; }
    public void setActivityStatus(ActivityStatus activityStatus) { this.activityStatus = activityStatus; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}