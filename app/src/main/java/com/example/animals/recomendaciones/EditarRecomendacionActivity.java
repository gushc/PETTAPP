package com.example.animals.recomendaciones;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.animals.R;
import com.example.animals.actividades.MapaPickerActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import cz.msebera.android.httpclient.Header;

public class EditarRecomendacionActivity extends AppCompatActivity {

    // Vistas
    private EditText edtTitulo, edtDesc, edtCat;
    private ImageView imgFoto;
    private Switch switchUbicacion;
    private Button btnActualizar, btnCancelar;

    // Mapa Visual
    private MapView mapaPreview;
    private View cardMapaPreview;
    private GoogleMap googleMapPreview;

    // Datos
    private int idRecomendacion;
    private Uri nuevaUriImagen = null;
    private double latitud, longitud;
    private boolean cambioUbicacion = false;
    private boolean tieneUbicacionInicial = false;

    // Launchers
    private ActivityResultLauncher<Intent> launcherFoto;
    private ActivityResultLauncher<Intent> launcherMapa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_recomendacion);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setupLaunchers();

        edtTitulo = findViewById(R.id.edtEditarTitulo);
        edtDesc = findViewById(R.id.edtEditarDescripcion);
        edtCat = findViewById(R.id.edtEditarCategoria);
        imgFoto = findViewById(R.id.imgEditarFoto);
        switchUbicacion = findViewById(R.id.switchEditarUbicacion);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnCancelar = findViewById(R.id.btnCancelarEdicion);
        cardMapaPreview = findViewById(R.id.cardMapaPreview);
        mapaPreview = findViewById(R.id.mapaPreview);

        if (getIntent().getExtras() != null) {
            idRecomendacion = getIntent().getIntExtra("id", -1);
            edtTitulo.setText(getIntent().getStringExtra("titulo"));
            edtDesc.setText(getIntent().getStringExtra("descripcion"));
            edtCat.setText(getIntent().getStringExtra("categoria"));

            latitud = getIntent().getDoubleExtra("latitud", 0.0);
            longitud = getIntent().getDoubleExtra("longitud", 0.0);

            if (latitud != 0.0 && longitud != 0.0) {
                tieneUbicacionInicial = true;
                switchUbicacion.setChecked(true);
            }
        }

        if (mapaPreview != null) {
            mapaPreview.onCreate(savedInstanceState);
            mapaPreview.getMapAsync(googleMap -> {
                googleMapPreview = googleMap;
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                if (latitud != 0.0 && longitud != 0.0) {
                    mostrarMiniMapa(latitud, longitud);
                }
            });
        }

        imgFoto.setOnClickListener(v -> seleccionarNuevaFoto());
        btnActualizar.setOnClickListener(v -> subirCambios());
        btnCancelar.setOnClickListener(v -> finish());

        switchUbicacion.setOnClickListener(v -> {
            if (switchUbicacion.isChecked()) {
                Intent intent = new Intent(this, MapaPickerActivity.class);
                launcherMapa.launch(intent);
            } else {
                cambioUbicacion = true;
                latitud = 0.0;
                longitud = 0.0;
                ocultarMiniMapa();
                Toast.makeText(this, "Ubicación removida (guarde para confirmar)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupLaunchers() {
        launcherFoto = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                nuevaUriImagen = result.getData().getData();
                imgFoto.setImageURI(nuevaUriImagen);
            }
        });

        launcherMapa = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                latitud = result.getData().getDoubleExtra("latitud", 0.0);
                longitud = result.getData().getDoubleExtra("longitud", 0.0);
                cambioUbicacion = true;
                switchUbicacion.setChecked(true);
                mostrarMiniMapa(latitud, longitud);
                Toast.makeText(this, "Ubicación actualizada", Toast.LENGTH_SHORT).show();
            } else {
                if (!tieneUbicacionInicial && !cambioUbicacion) {
                    switchUbicacion.setChecked(false);
                }
            }
        });
    }


    private void mostrarMiniMapa(double lat, double lon) {
        if (cardMapaPreview != null) cardMapaPreview.setVisibility(View.VISIBLE);
        if (googleMapPreview != null) {
            googleMapPreview.clear();
            LatLng pos = new LatLng(lat, lon);
            googleMapPreview.addMarker(new MarkerOptions().position(pos));
            googleMapPreview.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f));
        }
    }

    private void ocultarMiniMapa() {
        if (cardMapaPreview != null) cardMapaPreview.setVisibility(View.GONE);
    }

    private void seleccionarNuevaFoto() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            launcherFoto.launch(intent);
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }

    private void subirCambios() {
        String titulo = edtTitulo.getText().toString().trim();
        String desc = edtDesc.getText().toString().trim();
        String cat = edtCat.getText().toString().trim();

        if (titulo.isEmpty() || desc.isEmpty() || cat.isEmpty()) {
            Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestParams params = new RequestParams();
        params.put("id", idRecomendacion);
        params.put("titulo", titulo);
        params.put("descripcion", desc);
        params.put("categoria", cat);

        if (cambioUbicacion) {
            params.put("latitud", latitud);
            params.put("longitud", longitud);
        } else if (tieneUbicacionInicial) {
            params.put("latitud", latitud);
            params.put("longitud", longitud);
        } else {
            params.put("latitud", 0.0);
            params.put("longitud", 0.0);
        }

        // Si seleccionó NUEVA FOTO, la convertimos y enviamos
        if (nuevaUriImagen != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), nuevaUriImagen);
                String fotoBase64 = bitmapToBase64(bitmap);
                params.put("foto", fotoBase64); // Enviamos la nueva foto
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String url = "http://miguelcardenas.atwebpages.com/proyecto/editarRecomendacion.php";
        AsyncHttpClient client = new AsyncHttpClient();

        btnActualizar.setEnabled(false);
        btnActualizar.setText("Guardando...");

        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                btnActualizar.setEnabled(true);
                btnActualizar.setText("Actualizar");
                try {
                    String response = new String(responseBody);
                    JSONObject json = new JSONObject(response);

                    if (json.optBoolean("success")) {
                        Toast.makeText(EditarRecomendacionActivity.this, "¡Actualizado correctamente!", Toast.LENGTH_SHORT).show();
                        finish(); // Cierra y vuelve
                    } else {
                        String errorMsg = json.optString("mensaje", "Error desconocido");
                        Toast.makeText(EditarRecomendacionActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(EditarRecomendacionActivity.this, "Error en respuesta del servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                btnActualizar.setEnabled(true);
                btnActualizar.setText("Actualizar");
                Toast.makeText(EditarRecomendacionActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // Comprimimos a JPEG con calidad 80
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
    @Override protected void onResume() { super.onResume(); if (mapaPreview != null) mapaPreview.onResume(); }
    @Override protected void onPause() { super.onPause(); if (mapaPreview != null) mapaPreview.onPause(); }
    @Override protected void onDestroy() { super.onDestroy(); if (mapaPreview != null) mapaPreview.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); if (mapaPreview != null) mapaPreview.onLowMemory(); }
}