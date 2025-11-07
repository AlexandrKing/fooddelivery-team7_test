package com.team7.courieradmin.service;

public class Admin {
    private String login;
    private String password;
    private String email;

    // Конструктор
    public Admin(String login, String password, String email) {
        this.login = login;
        this.password = password;
        this.email = email;
    }

    // Геттеры и сеттеры
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}