package com.example.animals.clases;

public class Recomendacion {
    private int id;
    private String titulo, descripcion, categoria, fotoUrl, autor, fecha;
    private double latitud, longitud;

    // Constructor vacío
    public Recomendacion() {}

    // Getters y Setters
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
    public int getId(){return id;}
    public void setId(int id){
        this.id = id;
    }
}