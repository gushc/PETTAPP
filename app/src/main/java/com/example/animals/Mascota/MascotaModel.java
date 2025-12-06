package com.example.animals.Mascota;

public class MascotaModel {
    private int idMascota;
    private String nombre;
    private String especie;
    private String raza;
    private String fotoBase64;
    private String sexo;      // 👈 NUEVO
    private String edad;      // 👈 NUEVO
    private String color;     // 👈 NUEVO

    // Constructor completo
    public MascotaModel(int idMascota, String nombre, String especie, String raza,
                        String fotoBase64, String sexo, String edad, String color) {
        this.idMascota = idMascota;
        this.nombre = nombre;
        this.especie = especie;
        this.raza = raza;
        this.fotoBase64 = fotoBase64;
        this.sexo = sexo;
        this.edad = edad;
        this.color = color;
    }

    // Constructor sin campos extras (retrocompatibilidad)
    public MascotaModel(int idMascota, String nombre, String especie, String raza, String fotoBase64) {
        this(idMascota, nombre, especie, raza, fotoBase64, "", "", "");
    }

    // Getters y Setters
    public int getIdMascota() { return idMascota; }
    public void setIdMascota(int idMascota) { this.idMascota = idMascota; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }

    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }

    public String getFotoBase64() { return fotoBase64; }
    public void setFotoBase64(String fotoBase64) { this.fotoBase64 = fotoBase64; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getEdad() { return edad; }
    public void setEdad(String edad) { this.edad = edad; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}