package com.example.animals.actividades;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.animals.R;
import com.example.animals.clases.Usuario;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity implements View.OnClickListener {

    EditText txtNombre, txtApellido, txtCelular, txtEdad, txtCorreo, txtPass, txtConPass;
    Button btnRegistrar, btnIniciar;
    RadioGroup grpSexo;
    RadioButton rbtNoBinario, rbtMasculino, rbtFemenino;
    CheckBox chkAceptarTerminos;
    private boolean isGoogleSignIn = false;

    // URL del script PHP para el registro
    private String urlAgregarUsuario = "http://miguelcardenas.atwebpages.com/proyecto/agregarUsuario.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Vinculación de componentes según los IDs del XML
        txtNombre = findViewById(R.id.regTxtNombre);
        txtApellido = findViewById(R.id.regTxtApellido);
        // Asumo que estos IDs son para Celular y Edad según el flujo de datos:
        txtCelular = findViewById(R.id.regTxtEspecie);
        txtEdad = findViewById(R.id.regTxtRaza);

        grpSexo = findViewById(R.id.regGrpSexo);
        rbtNoBinario = findViewById(R.id.regBtnNoBinario);
        rbtMasculino = findViewById(R.id.regBtnMasculino);
        rbtFemenino = findViewById(R.id.regBtnFemenino);

        txtCorreo = findViewById(R.id.regTxtCorreo);
        txtPass = findViewById(R.id.regTxtPass);
        txtConPass = findViewById(R.id.regTxtConfPass);
        chkAceptarTerminos = findViewById(R.id.chkAceptarTerminos);
        btnRegistrar = findViewById(R.id.regBtnRegistrar);
        btnIniciar = findViewById(R.id.regBtnIniciarSesion);

        btnRegistrar.setOnClickListener(this);
        btnIniciar.setOnClickListener(this);

        // --- Lógica de precarga para Google Sign-In ---
        Intent intent = getIntent();
        String correo = intent.getStringExtra("correo");
        String nombre = intent.getStringExtra("nombre");
        // Capturar el indicador de Google Sign-In
        isGoogleSignIn = intent.getBooleanExtra("isGoogleSignIn", false);

        if (correo != null) {
            txtCorreo.setText(correo);
            // Si el correo viene de Google, no se puede cambiar
            txtCorreo.setEnabled(false);

            if (nombre != null) {
                // Asumimos que el nombre completo de Google va al campo de nombre
                txtNombre.setText(nombre);
            }

            // Si viene de Google, ocultamos los campos de contraseña
            if (isGoogleSignIn) {
                txtPass.setVisibility(View.GONE);
                txtConPass.setVisibility(View.GONE);
                Toast.makeText(this, "Registro con Google. Solo complete los campos restantes.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.regBtnRegistrar) {
            registrarUsuario();
        } else if (v.getId() == R.id.regBtnIniciarSesion) {
            iniciarSesion();
        }
    }

    private void registrarUsuario() {
        // Recolección de datos
        String nombre = txtNombre.getText().toString().trim();
        String apellido = txtApellido.getText().toString().trim();
        String celular = txtCelular.getText().toString().trim();
        String edadStr = txtEdad.getText().toString().trim();
        String correo = txtCorreo.getText().toString().trim();

        String pass = txtPass.getText().toString().trim();
        String confPass = txtConPass.getText().toString().trim();

        // Determinar el género seleccionado (como String para validación)
        String generoStr;
        final int selectedId = grpSexo.getCheckedRadioButtonId();
        if (selectedId == R.id.regBtnNoBinario) {
            generoStr = "No binario";
        } else if (selectedId == R.id.regBtnMasculino) {
            generoStr = "Masculino";
        } else if (selectedId == R.id.regBtnFemenino) {
            generoStr = "Femenino";
        } else {
            generoStr = "";
        }

        // CRÍTICO: Mapeo del género a un valor numérico para la DB (PHP)
        // Debe coincidir con lo que espera tu columna 'Genero' (asumo BIT/INT).
        // 1 = Masculino, 0 = Femenino, 2 = Otro/No Binario.
        final int generoInt;
        if (generoStr.equals("Masculino")) {
            generoInt = 1;
        } else if (generoStr.equals("Femenino")) {
            generoInt = 0;
        } else if (generoStr.equals("No binario")) {
            generoInt = 2; // Usamos 2 o el valor que uses para "Otro"
        } else {
            generoInt = -1; // Valor para indicar que no se seleccionó
        }

        // --- VALIDACIONES ---
        if (nombre.isEmpty() || apellido.isEmpty() || celular.isEmpty() || edadStr.isEmpty() ||
                generoInt == -1 || correo.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos, incluyendo el género.", Toast.LENGTH_LONG).show();
            return;
        }

        // Conversión de Edad a int
        int edad;
        try {
            edad = Integer.parseInt(edadStr);
            if (edad <= 0) {
                Toast.makeText(this, "La edad debe ser un número positivo.", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "La edad debe ser un número válido.", Toast.LENGTH_LONG).show();
            return;
        }


        if (!isGoogleSignIn && (pass.isEmpty() || confPass.isEmpty())) {
            Toast.makeText(this, "Por favor, complete los campos de contraseña.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!chkAceptarTerminos.isChecked()) {
            Toast.makeText(this, "Debe aceptar los términos y condiciones.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!esCorreoValido(correo)) {
            Toast.makeText(this, "Por favor, ingrese un correo válido.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!isGoogleSignIn) {
            if (pass.length() < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_LONG).show();
                return;
            }
            if (!pass.equals(confPass)) {
                Toast.makeText(this, "Las contraseñas no coinciden.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        // 🔹 AQUÍ SE HACE LA CONEXIÓN AL PHP 🔹

        Response.Listener<String> responseListener = response -> {
            try {
                // ⚠️ AHORA VERIFICAMOS QUE LA RESPUESTA NO ESTÉ VACÍA
                if (response == null || response.isEmpty()) {
                    Toast.makeText(RegistroActivity.this, "Error: El servidor no devolvió respuesta.", Toast.LENGTH_LONG).show();
                    return;
                }

                // Mostrar la respuesta completa del servidor para depuración
                Log.d("RegistroActivity", "Respuesta PHP: " + response);

                // Parsear la respuesta JSON
                JSONObject jsonResponse = new JSONObject(response);

                // CRÍTICO: Cambiamos de 'estado' a 'exito' (clave que usa el PHP)
                boolean exito = jsonResponse.getBoolean("exito");

                if (exito) {
                    // ÉXITO: El usuario fue registrado.
                    // El mensaje de éxito usa la clave 'mensaje'
                    String mensaje = jsonResponse.getString("mensaje");
                    Toast.makeText(RegistroActivity.this, mensaje, Toast.LENGTH_LONG).show();

                    // --- INICIO DE SESIÓN AUTOMÁTICO DESPUÉS DEL REGISTRO ---

                    // 1. Obtener el ID del usuario recién insertado (Si el PHP lo devuelve)
                    // NOTA: El PHP que te di NO devuelve 'id_usuario' por ahora.
                    // Si el PHP lo devolviera, se usaría:
                    // int id_nuevo_usuario = jsonResponse.optInt("id_usuario", -1);

                    // Como el PHP actual NO devuelve el ID, te recomiendo ir a la pantalla de Login
                    // para que el usuario inicie sesión con su nuevo correo/clave.

                    Intent loginIntent = new Intent(RegistroActivity.this, SesionActivity.class);
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(loginIntent);
                    finish();

                    // Para evitar errores, comentamos la lógica de Bienvenida que requiere ID.
                    /*
                    int id_nuevo_usuario;
                    try {
                        id_nuevo_usuario = jsonResponse.getInt("id_usuario");
                    } catch (JSONException idEx) {
                        Toast.makeText(RegistroActivity.this, "Error interno: El ID de usuario no fue devuelto por el servidor.", Toast.LENGTH_LONG).show();
                        Log.e("JSON_ERROR", "Faltó el campo 'id_usuario' en el JSON. Verifique agregarUsuario.php", idEx);
                        return; // Detenemos la ejecución si no hay ID
                    }

                    // 2. Crear y configurar el objeto Usuario para pasarlo a BienvenidaActivity
                    Usuario nuevoUsuario = new Usuario();
                    nuevoUsuario.setID(id_nuevo_usuario);
                    nuevoUsuario.setNombre(nombre);
                    nuevoUsuario.setApellido(apellido);
                    nuevoUsuario.setCorreo(correo);

                    nuevoUsuario.setCelular(celular);
                    nuevoUsuario.setEdad(edad);
                    nuevoUsuario.setGenero(generoStr); // Usamos el String para el objeto Usuario

                    // 3. Guardar el ID de usuario en SharedPreferences para mantener la sesión
                    SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("idUsuario", nuevoUsuario.getID());
                    editor.apply();

                    // 4. NAVEGACIÓN CLAVE: Ir a la pantalla de Bienvenida
                    Intent bienvenida = new Intent(RegistroActivity.this, BienvenidaActivity.class);

                    // Pasar el objeto Usuario completo
                    bienvenida.putExtra("usuario", nuevoUsuario);

                    // CRUCIAL: Limpiar la pila de actividades para evitar el duplicado al presionar "Atrás"
                    bienvenida.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(bienvenida);

                    // CLAVE: Cerrar RegistroActivity para que no se pueda volver
                    finish();
                    */
                } else {
                    // ERROR: (Ej. correo duplicado, datos incompletos, etc.)
                    // CRÍTICO: Usamos 'error_msg' (clave que usa el PHP)
                    String errorMsg = jsonResponse.optString("error_msg", "Error de registro desconocido por el servidor.");
                    Toast.makeText(RegistroActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                }

            } catch (JSONException e) {
                // Error al procesar el JSON si la respuesta no es válida o está contaminada
                Toast.makeText(RegistroActivity.this, "Error al procesar la respuesta del servidor (Formato JSON inválido).", Toast.LENGTH_LONG).show();
                Log.e("JSON_ERROR", "Respuesta inválida: " + response, e);
            }
        };

        // Petición HTTP usando Volley
        StringRequest request = new StringRequest(Request.Method.POST, urlAgregarUsuario, responseListener,
                error -> {
                    // Log detallado en caso de error de red o timeout
                    String errorMsg = error.getMessage() != null ? error.getMessage() : "Error de red o Timeout";
                    Toast.makeText(RegistroActivity.this, "Error de conexión: " + errorMsg, Toast.LENGTH_LONG).show();
                    Log.e("VOLLEY_ERROR", "Detalles del error: " + errorMsg);
                    if (error.networkResponse != null) {
                        try {
                            String responseBody = new String(error.networkResponse.data, "UTF-8");
                            Log.e("VOLLEY_ERROR_BODY", "Respuesta no JSON: " + responseBody);
                            Toast.makeText(RegistroActivity.this, "Respuesta incompleta o HTML inesperado. Verifique PHP.", Toast.LENGTH_LONG).show();
                        } catch (UnsupportedEncodingException e) {
                            Log.e("VOLLEY_ERROR_BODY", "Error al leer cuerpo de respuesta: " + e.getMessage());
                        }
                    }
                }) {

            // ⚠️ AJUSTE CRÍTICO DE ENCODING: Este override asegura que Volley use UTF-8 para leer la respuesta.
            @Override
            protected Response<String> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                String parsed;
                try {
                    parsed = new String(response.data, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    parsed = new String(response.data);
                }
                return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
            }


            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parametros = new HashMap<>();
                parametros.put("nombres", nombre);
                parametros.put("apellidos", apellido);
                parametros.put("celular", celular);
                // La edad se envía como String, ya que PHP la manejará así
                parametros.put("edad", edadStr);

                // CRÍTICO: Enviamos el valor numérico del género (1, 0, o 2)
                parametros.put("genero", String.valueOf(generoInt));

                parametros.put("correo", correo);

                // Lógica para enviar la clave y el tipo de autenticación
                if(isGoogleSignIn) {
                    // Si es Google, enviamos un placeholder y el tipo "google"
                    parametros.put("clave", ""); // Enviar cadena vacía para Google
                    parametros.put("auth_type", "google");
                } else {
                    // Si es manual, enviamos la clave ingresada y el tipo "manual"
                    // NOTA: Se recomienda hashear la clave ANTES de enviarla al servidor.
                    parametros.put("clave", pass);
                    parametros.put("auth_type", "manual");
                }

                Log.d("VOLLEY_PARAMS", "Parámetros de registro: " + parametros.toString());
                return parametros;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(RegistroActivity.this);
        queue.add(request);
    }

    private boolean esCorreoValido(String correo) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        return correo.matches(regex);
    }

    private void iniciarSesion() {
        Intent intent = new Intent(this, SesionActivity.class);
        startActivity(intent);
        finish();
    }
}