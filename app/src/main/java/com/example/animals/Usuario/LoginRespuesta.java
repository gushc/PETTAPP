package com.example.animals.Usuario;

public class LoginRespuesta {
    private boolean success;
    private String message;
    private int id;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getId() {
        return id;
    }
}

