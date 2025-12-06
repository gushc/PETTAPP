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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.animals.R;
import com.example.animals.sqlite.Animals;
import com.example.animals.clases.Hash;
import com.example.animals.clases.Usuario;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

// Importaciones necesarias para Google y Firebase
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class SesionActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "AppLoginAnimals";
    private String urlIniciarSesion = "http://miguelcardenas.atwebpages.com/proyecto/iniciarSesion.php";
    private String urlVerificarCorreo = "http://miguelcardenas.atwebpages.com/proyecto/verificarCorreo.php";
    EditText txtCorreo, txtPass;
    Button btnIngresar, btnRegistrar, btnGoogleSignIn;
    CheckBox chkRecordar;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sesion);

        txtCorreo = findViewById(R.id.logTxtCorreo);
        txtPass = findViewById(R.id.logTxtClave);
        btnIngresar = findViewById(R.id.btnIngresar);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        chkRecordar = findViewById(R.id.ChkSesionRecordar);

        btnRegistrar.setOnClickListener(this);
        btnIngresar.setOnClickListener(this);

        String webClientId = getString(R.string.default_web_client_id);
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

    }
    private void signInWithGoogle() {
        mGoogleSignInClient.signOut();

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(@NonNull Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            Log.w(TAG, "Fallo en Google Sign-In: " + e.getMessage(), e);
            Toast.makeText(this, "Fallo en Google Sign-In. Error: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        verificarUsuarioEnBackend(acct);
                    } else {
                        Toast.makeText(SesionActivity.this, "Fallo la autenticación de Firebase.", Toast.LENGTH_SHORT).show();
                        mGoogleSignInClient.signOut();
                    }
                });
    }

    private void verificarUsuarioEnBackend(GoogleSignInAccount acct) {
        AsyncHttpClient ahc = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("correo", acct.getEmail());

        // BaseJsonHttpResponseHandler<JSONObject> se usa para recibir un objeto {}
        ahc.post(urlVerificarCorreo, params, new BaseJsonHttpResponseHandler<JSONObject>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONObject response) {
                // ⭐ DEPURACIÓN: Muestra la respuesta cruda del servidor
                Log.d(TAG, "VerificarCorreo RAW (" + statusCode + "): " + rawJsonResponse);

                try {
                    // Esperamos una estructura: {"existe": true, "id_usuario": 1, "nombres": "..."}
                    if (response != null && response.has("existe")) {
                        boolean existe = response.getBoolean("existe");

                        if (existe) {
                            // *** CORRECCIÓN CRÍTICA AQUÍ: USO DE optInt/optString PARA ROBUSTEZ ***
                            // Esto evita que el código falle si los campos son NULL o el tipo no coincide (como int vs String)

                            int id = response.optInt("id_usuario", 0);
                            String nombre = response.optString("nombres", "Usuario");
                            String apellido = response.optString("apellidos", "");
                            String celular = response.optString("celular", "");
                            int edad = response.optInt("edad", 0);

                            // Lectura robusta de 'genero', que puede ser int (0/1) o String ("M"/"F")
                            String genero = response.optString("genero", "");
                            if (genero.isEmpty()) {
                                // Intenta leer como int si viene de BIT(1)
                                int genero_int = response.optInt("genero", -1);
                                if (genero_int == 1) {
                                    genero = "Masculino";
                                } else if (genero_int == 0) {
                                    genero = "Femenino";
                                }
                            }

                            String correo = response.optString("correo", acct.getEmail());

                            Usuario usuario = new Usuario();
                            usuario.setID(id);
                            usuario.setNombre(nombre);
                            usuario.setApellido(apellido);
                            usuario.setCorreo(correo);
                            usuario.setCelular(celular);
                            usuario.setEdad(edad);
                            usuario.setGenero(genero);

                            if (id > 0) {
                                Toast.makeText(SesionActivity.this, "Sesión de Google iniciada. Bienvenido: " + nombre, Toast.LENGTH_LONG).show();
                                verificarYRedirigir(usuario);
                            } else {
                                Log.e(TAG, "VerificarCorreo - ID de usuario no válido (ID=0) a pesar de existir.");
                                Toast.makeText(SesionActivity.this, "Error de Servidor: Datos de usuario incompletos. ID no válido.", Toast.LENGTH_LONG).show();
                            }


                        } else {
                            Toast.makeText(SesionActivity.this, "Usuario nuevo de Google. Complete sus datos de registro.", Toast.LENGTH_LONG).show();

                            mGoogleSignInClient.signOut();
                            // Redirigir a Registro
                            redirigirARegistro(acct.getEmail(), acct.getDisplayName());
                        }
                    } else {
                        // Respuesta sin la clave "existe" (problema del servidor PHP)
                        Log.e(TAG, "VerificarCorreo - Respuesta sin la clave 'existe'.");
                        Toast.makeText(SesionActivity.this, "Error de Servidor: Respuesta incompleta o no sigue el formato esperado.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error JSON en verificarUsuarioEnBackend: Respuesta cruda: " + rawJsonResponse, e);
                    Toast.makeText(SesionActivity.this, "Error de Servidor 200: La respuesta no es un JSON válido. Revisa el Logcat.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONObject errorResponse) {
                Log.e(TAG, "Fallo de conexión al verificar usuario: " + statusCode, throwable);
                Toast.makeText(SesionActivity.this, "Error de conexión al servidor: " + statusCode, Toast.LENGTH_LONG).show();
            }

            @Override
            protected JSONObject parseResponse(String rawJsonData, boolean isprefix) throws Throwable {
                if (rawJsonData != null) {
                    String trimmedData = rawJsonData.trim();
                    if (trimmedData.startsWith("{")) {
                        return new JSONObject(trimmedData);
                    }
                }
                if (rawJsonData == null || rawJsonData.trim().isEmpty()) {
                    return new JSONObject();
                }
                return null;
            }
        });
    }

    private void redirigirABienvenida(Usuario usuario, boolean tieneMascotas) {
        Intent bienvenida = new Intent(getApplicationContext(), BienvenidaActivity.class);

        SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("idUsuario", usuario.getID());
        editor.apply();

        bienvenida.putExtra("usuario", usuario);
        bienvenida.putExtra("TIENE_MASCOTAS", tieneMascotas);

        bienvenida.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(bienvenida);
        finish();
    }
    private void redirigirARegistro(String correo, String nombre) {
        Intent registro = new Intent(this, RegistroActivity.class);
        registro.putExtra("correo", correo);
        registro.putExtra("nombre", nombre);
        registro.putExtra("isGoogleSignIn", true);
        startActivity(registro);
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnIngresar) {
            String correo = txtCorreo.getText().toString().trim();
            String clave = txtPass.getText().toString().trim();

            if (correo.isEmpty() || clave.isEmpty()) {
                Toast.makeText(this, "Por favor, ingrese todas las credenciales.", Toast.LENGTH_LONG).show();
                return;
            }
            ingresar(correo, clave);
        } else if (v.getId() == R.id.btnRegistrar) {
            Intent registro = new Intent(this, RegistroActivity.class);
            registro.putExtra("isGoogleSignIn", false);
            startActivity(registro);
            finish();
        }
    }

    private void ingresar(String correo, String clave) {
        Hash hash = new Hash();

        // 1. CAPTURAR Y HASHEAR la clave usando SHA256 (COMO LO HACES ACTUALMENTE)
        final String claveHash = hash.StringToHash(clave,"SHA256");

        AsyncHttpClient ahclogin = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("correo",correo);
        // 2. Usar la variable final en los parámetros
        params.put("clave",claveHash);

        // ⭐️ CORRECCIÓN: Usar BaseJsonHttpResponseHandler<JSONObject> para esperar un Objeto {}
        ahclogin.post(urlIniciarSesion, params, new BaseJsonHttpResponseHandler<JSONObject>() {
            @Override
            public void onSuccess(int i, Header[] headers, String s, JSONObject response) {
                // ⭐ DEPURACIÓN: Muestra la respuesta cruda del servidor
                Log.d(TAG, "IniciarSesion RAW (" + i + "): " + s);

                // 3. Verificamos el código HTTP y que la respuesta no sea nula.
                if(i == 200 && response != null){
                    try{
                        // 4. Ahora esperamos el JSONObject {"estado": "..."}
                        String estado = response.optString("estado", "error");
                        String mensaje = response.optString("mensaje", "Respuesta desconocida del servidor.");

                        if(estado.equals("ok")){
                            // ÉXITO: Los datos del usuario están dentro del objeto "usuario"
                            JSONObject usuarioJson = response.getJSONObject("usuario");

                            Usuario usuario = new Usuario();

                            // Mapeo de datos (Usando opt* para mayor seguridad)
                            usuario.setID(usuarioJson.optInt("id", 0));
                            usuario.setNombre(usuarioJson.optString("nombres", ""));
                            usuario.setApellido(usuarioJson.optString("apellidos", ""));
                            usuario.setCorreo(usuarioJson.optString("correo", ""));

                            usuario.setCelular(usuarioJson.optString("celular", ""));
                            usuario.setEdad(usuarioJson.optInt("edad", 0));
                            usuario.setGenero(usuarioJson.optString("genero", ""));

                            if(chkRecordar.isChecked()){
                                Animals animals = new Animals(getApplicationContext());
                                animals.agregarUsuario(usuario.getID(), usuario.getCorreo(), claveHash);
                            }
                            Toast.makeText(getApplicationContext(), "Bienvenido, " + usuario.getNombre(), Toast.LENGTH_LONG).show();
                            verificarYRedirigir(usuario);

                        } else {
                            // FALLO: Estado "error" (Credenciales incorrectas o error controlado en PHP)
                            Toast.makeText(getApplicationContext(), "Fallo: " + mensaje, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        // CATCH para estructura interna rota
                        Log.e(TAG, "Error JSON interno en ingresar (Manual): " + s, e);
                        Toast.makeText(getApplicationContext(), "Error de Servidor: Estructura de usuario incompleta.", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    // Error de Servidor, pero el código HTTP es 200
                    Toast.makeText(getApplicationContext(), "Error de Servidor: Código HTTP 200 con respuesta nula o fallida.", Toast.LENGTH_SHORT).show();
                }
            }

            // ⭐️ CORRECCIÓN: Cambiar el tipo de errorResponse a JSONObject
            @Override
            public void onFailure(int i, Header[] headers, Throwable throwable, String rawJsonData, JSONObject errorResponse) {
                Log.e(TAG, "Fallo de Conexión (Manual) - HTTP Code: " + i, throwable);
                Toast.makeText(getApplicationContext(), "Error de Conexión o Servidor (HTTP " + i + ").", Toast.LENGTH_LONG).show();
            }

            @Override
            protected JSONObject parseResponse(String rawJsonData, boolean isprefix) throws Throwable {
                // ⭐️ CORRECCIÓN: Intentar devolver un JSONObject
                if (rawJsonData != null) {
                    String trimmedData = rawJsonData.trim();
                    if (trimmedData.startsWith("{")) {
                        return new JSONObject(trimmedData);
                    }
                }
                // Si la respuesta no empieza con {, devolvemos nulo, que Volley maneja.
                return null;
            }
        });
    }

    private void verificarYRedirigir(Usuario usuario) {
        SharedPreferences preferences = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("id_usuario", usuario.getID());
        editor.apply();

        String url = "http://miguelcardenas.atwebpages.com/proyecto/verificarMascota.php";

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("idUsuario", usuario.getID());

        client.post(url, params, new BaseJsonHttpResponseHandler<JSONObject>() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, JSONObject response) {
                boolean tieneMascotas = false;
                try {
                    if (response != null && response.optBoolean("success")) {
                        tieneMascotas = response.optBoolean("tieneMascotas", false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                redirigirABienvenida(usuario, tieneMascotas);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, JSONObject errorResponse) {
                // En caso de fallo de red, asumimos true para no bloquear, o false según tu lógica.
                // Tu código original tenía 'true', lo dejo así.
                redirigirABienvenida(usuario, true);
            }

            @Override
            protected JSONObject parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                if (rawJsonData == null) return null;
                return new JSONObject(rawJsonData);
            }
        });
    }
}