package com.example.animals;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RegistroRequest extends StringRequest {

    private static final String URL = "http://192.168.1.46/proyecto/agregarUsuario.php";
    private final Map<String, String> params;

    public RegistroRequest(String nombre, String apellidos, String celular, String edad,
                           String genero, String correo, String clave,
                           Response.Listener<String> listener) {

        super(Request.Method.POST, URL, listener, null);
        params = new HashMap<>();
        params.put("nombre", nombre);
        params.put("apellidos", apellidos);
        params.put("celular", celular);
        params.put("edad", edad);
        params.put("genero", genero);
        params.put("correo", correo);
        params.put("clave", clave);
    }

    @Override
    protected Map<String, String> getParams() {
        return params;
    }
}
