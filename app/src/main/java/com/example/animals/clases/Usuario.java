package com.example.animals.clases;

import java.io.Serializable;

// Se necesita implementar Serializable para pasar el objeto entre Activities
public class Usuario implements Serializable {

    // CAMPOS ORIGINALES
    private int ID;
    private String correo;
    private String clave;
    private String nombre;
    private String apellido;

    // CAMPOS AÑADIDOS PARA GOOGLE SIGN-IN / PERFIL COMPLETO
    private String celular;
    private int edad;
    private String genero;

    // Constructor vacío
    public Usuario() {
    }

    // --- GETTERS ---

    public int getID() {
        return ID;
    }

    public String getCorreo() {
        return correo;
    }

    public String getClave() {
        return clave;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    // GETTERS AÑADIDOS
    public String getCelular() {
        return celular;
    }

    public int getEdad() {
        return edad;
    }

    public String getGenero() {
        return genero;
    }


    // --- SETTERS ---

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    // SETTERS AÑADIDOS (Los que causaban el error en SesionActivity)
    public void setCelular(String celular) {
        this.celular = celular;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }
}