package com.example.animals.recomendaciones;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.animals.actividades.MapaPickerActivity;
import com.example.animals.R;
// IMPORTANTE: Asegúrate de importar tu clase de base de datos
import com.example.animals.sqlite.Animals3;

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

public class RegistrarRecomendacionFragment extends Fragment {

    private EditText edtTitulo, edtDescripcion, edtCategoria;
    private ImageView imgFoto;
    private Switch switchUbicacion;
    private Button btnGuardar, btnRegresar;

    private MapView mapaPreview;
    private View cardMapaPreview;
    private GoogleMap googleMapPreview;

    private Uri uriImagenSeleccionada;
    private double latitudFinal = 0.0;
    private double longitudFinal = 0.0;
    private boolean tieneUbicacion = false;

    private Animals3 dbHelper;

    // Launcher Foto
    private final ActivityResultLauncher<Intent> launcherFoto =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    uriImagenSeleccionada = result.getData().getData();
                    imgFoto.setImageURI(uriImagenSeleccionada);
                }
            });

    private final ActivityResultLauncher<Intent> launcherMapa =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    latitudFinal = result.getData().getDoubleExtra("latitud", 0.0);
                    longitudFinal = result.getData().getDoubleExtra("longitud", 0.0);

                    tieneUbicacion = true;
                    switchUbicacion.setChecked(true);
                    mostrarMiniMapa(latitudFinal, longitudFinal);

                    Toast.makeText(getContext(), "Ubicación adjuntada correctamente", Toast.LENGTH_SHORT).show();
                } else {
                    if (!tieneUbicacion) {
                        switchUbicacion.setChecked(false);
                        ocultarMiniMapa();
                    }
                }
            });

    public RegistrarRecomendacionFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registrar_recomendacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new Animals3(getContext());

        edtTitulo = view.findViewById(R.id.edtTitulo);
        edtDescripcion = view.findViewById(R.id.edtDescripcion);
        edtCategoria = view.findViewById(R.id.edtCategoria);
        imgFoto = view.findViewById(R.id.imgFotoRecomendacion);
        switchUbicacion = view.findViewById(R.id.switch_compartir_ubicacion);
        btnGuardar = view.findViewById(R.id.btnGuardar);
        btnRegresar = view.findViewById(R.id.btnRegresar);

        cardMapaPreview = view.findViewById(R.id.cardMapaPreview);
        mapaPreview = view.findViewById(R.id.mapaPreview);

        if (mapaPreview != null) {
            mapaPreview.onCreate(savedInstanceState);
            mapaPreview.getMapAsync(googleMap -> {
                googleMapPreview = googleMap;
                googleMap.getUiSettings().setMapToolbarEnabled(false);
            });
        }

        cargarBorrador();

        imgFoto.setOnClickListener(v -> seleccionarFoto());

        switchUbicacion.setOnClickListener(v -> {
            if (switchUbicacion.isChecked()) {
                Intent intent = new Intent(getContext(), MapaPickerActivity.class);
                launcherMapa.launch(intent);
            } else {
                latitudFinal = 0.0;
                longitudFinal = 0.0;
                tieneUbicacion = false;
                ocultarMiniMapa();
                Toast.makeText(getContext(), "Ubicación eliminada", Toast.LENGTH_SHORT).show();
            }
        });

        btnGuardar.setOnClickListener(v -> subirRecomendacion());

        if (btnRegresar != null) {
            btnRegresar.setOnClickListener(v -> requireActivity().onBackPressed());
        }
    }


    private void cargarBorrador() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        if (idUsuario != -1) {
            String[] datos = dbHelper.obtenerBorradorRecomendacion(idUsuario);
            if (datos != null) {
                if (datos[0] != null) edtTitulo.setText(datos[0]);
                if (datos[1] != null) edtDescripcion.setText(datos[1]);
                if (datos[2] != null && !datos[2].isEmpty()) {
                    try {
                        uriImagenSeleccionada = Uri.parse(datos[2]);
                        imgFoto.setImageURI(uriImagenSeleccionada);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Toast.makeText(getContext(), "Borrador recuperado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void guardarBorrador() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        String titulo = edtTitulo.getText().toString();
        String desc = edtDescripcion.getText().toString();
        String fotoStr = (uriImagenSeleccionada != null) ? uriImagenSeleccionada.toString() : "";

        if (idUsuario != -1 && (!titulo.isEmpty() || !desc.isEmpty() || !fotoStr.isEmpty())) {
            dbHelper.guardarBorradorRecomendacion(idUsuario, titulo, desc, fotoStr);
        }
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

    private void seleccionarFoto() {
        if (checkStoragePermission()) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            launcherFoto.launch(intent);
        }
    }

    private void subirRecomendacion() {
        String titulo = edtTitulo.getText().toString().trim();
        String desc = edtDescripcion.getText().toString().trim();
        String cat = edtCategoria.getText().toString().trim();

        if (titulo.isEmpty() || desc.isEmpty() || cat.isEmpty()) {
            Toast.makeText(getContext(), "Completa los campos de texto", Toast.LENGTH_SHORT).show();
            return;
        }

        if (uriImagenSeleccionada == null) {
            Toast.makeText(getContext(), "Debes seleccionar una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        String url = "http://miguelcardenas.atwebpages.com/proyecto/registrarRecomendacion.php";

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("idUsuario", idUsuario);
        params.put("titulo", titulo);
        params.put("descripcion", desc);
        params.put("categoria", cat);

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uriImagenSeleccionada);
            String fotoBase64 = bitmapToBase64(bitmap);
            params.put("foto", fotoBase64);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tieneUbicacion && switchUbicacion.isChecked()) {
            params.put("latitud", latitudFinal);
            params.put("longitud", longitudFinal);
        } else {
            params.put("latitud", "");
            params.put("longitud", "");
        }

        btnGuardar.setEnabled(false);

        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                btnGuardar.setEnabled(true);
                try {
                    String resp = new String(responseBody);
                    JSONObject json = new JSONObject(resp);

                    if (json.optBoolean("success")) {
                        dbHelper.limpiarBorradorRecomendacion(idUsuario);

                        uriImagenSeleccionada = null;
                        edtTitulo.setText("");
                        edtDescripcion.setText("");

                        Toast.makeText(getContext(), "¡Recomendación Guardada!", Toast.LENGTH_LONG).show();
                        requireActivity().onBackPressed();
                    } else {
                        Toast.makeText(getContext(), "Error: " + json.optString("error"), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error en respuesta del servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                btnGuardar.setEnabled(true);
                Toast.makeText(getContext(), "Error de conexión: " + statusCode, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return true;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            return false;
        }
        return true;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mapaPreview != null) mapaPreview.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapaPreview != null) mapaPreview.onPause();

        guardarBorrador();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapaPreview != null) mapaPreview.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapaPreview != null) mapaPreview.onLowMemory();
    }
}