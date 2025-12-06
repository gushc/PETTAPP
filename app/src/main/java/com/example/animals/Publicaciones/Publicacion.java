package com.example.animals.Publicaciones;

public class Publicacion {

    // Atributos principales (Base de Datos 'publicacion')
    private int idPublicacion;
    private String descripcion;
    private String fotoBase64;      // La foto de la publicación
    private String fechaRegistro;

    // Atributos Adicionales (Para mostrar en la lista completa)
    private String nombreUsuario;
    private String fotoPerfilUsuario; // Foto de perfil del dueño
    private int cantidadLikes;
    private int cantidadComentarios;
    private boolean isLikedByMe;

    // Constructor vacío (Necesario)
    public Publicacion() {
    }

    // Constructor básico
    public Publicacion(int idPublicacion, String descripcion, String fotoBase64, String fechaRegistro) {
        this.idPublicacion = idPublicacion;
        this.descripcion = descripcion;
        this.fotoBase64 = fotoBase64;
        this.fechaRegistro = fechaRegistro;

        // Valores por defecto para evitar nulos
        this.nombreUsuario = "Usuario";
        this.cantidadLikes = 0;
        this.cantidadComentarios = 0;
        this.isLikedByMe = false;
    }

    // --- GETTERS Y SETTERS ---

    public int getIdPublicacion() {
        return idPublicacion;
    }

    public void setIdPublicacion(int idPublicacion) {
        this.idPublicacion = idPublicacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFotoBase64() {
        return fotoBase64;
    }

    public void setFotoBase64(String fotoBase64) {
        this.fotoBase64 = fotoBase64;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    // --- Getters y Setters para la UI ---

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getFotoPerfilUsuario() {
        return fotoPerfilUsuario;
    }

    public void setFotoPerfilUsuario(String fotoPerfilUsuario) {
        this.fotoPerfilUsuario = fotoPerfilUsuario;
    }

    public int getCantidadLikes() {
        return cantidadLikes;
    }

    public void setCantidadLikes(int cantidadLikes) {
        this.cantidadLikes = cantidadLikes;
    }

    public int getCantidadComentarios() {
        return cantidadComentarios;
    }

    public void setCantidadComentarios(int cantidadComentarios) {
        this.cantidadComentarios = cantidadComentarios;
    }

    public boolean isLikedByMe() {
        return isLikedByMe;
    }

    public void setLikedByMe(boolean likedByMe) {
        isLikedByMe = likedByMe;
    }
    public void incrementarComentarios() {
        this.cantidadComentarios++;
    }
}