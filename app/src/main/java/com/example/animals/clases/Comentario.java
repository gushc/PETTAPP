package com.example.animals.clases;

public class Comentario {
    private int idComentario;
    private int idUsuario;
    private String nombreUsuario;
    private String fotoPerfil; // Base64
    private String texto;
    private String fecha;

    public Comentario(int idComentario, int idUsuario, String nombreUsuario, String fotoPerfil, String texto, String fecha) {
        this.idComentario = idComentario;
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.fotoPerfil = fotoPerfil;
        this.texto = texto;
        this.fecha = fecha;
    }

    // Getters
    public String getNombreUsuario() { return nombreUsuario; }
    public String getFotoPerfil() { return fotoPerfil; }
    public String getTexto() { return texto; }
    public String getFecha() { return fecha; }
}