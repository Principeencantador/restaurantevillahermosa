package com.example.demo.dao;

public class LoginResponse {
    private String token;
    private String rol;

    public LoginResponse(String token, String rol) {
        this.token = token;
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public String getRol() {
        return rol;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}


