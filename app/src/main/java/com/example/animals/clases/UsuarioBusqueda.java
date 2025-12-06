package com.example.animals.clases;

public class UsuarioBusqueda {
    private int id;
    private String nombreCompleto;
    private String fotoBase64;

    public UsuarioBusqueda(int id, String nombreCompleto, String fotoBase64) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.fotoBase64 = fotoBase64;
    }

    public int getId() { return id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getFotoBase64() { return fotoBase64; }
}
