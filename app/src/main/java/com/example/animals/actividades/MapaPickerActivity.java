package com.example.animals.actividades;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.animals.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapaPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng ubicacionSeleccionada = null;
    private Button btnConfirmar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_picker);

        btnConfirmar = findViewById(R.id.btnConfirmarUbicacion);

        // Cargar el mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnConfirmar.setOnClickListener(v -> {
            if (ubicacionSeleccionada != null) {
                // Devolvemos los datos a la pantalla anterior
                Intent returnIntent = new Intent();
                returnIntent.putExtra("latitud", ubicacionSeleccionada.latitude);
                returnIntent.putExtra("longitud", ubicacionSeleccionada.longitude);
                setResult(RESULT_OK, returnIntent);
                finish();
            } else {
                Toast.makeText(this, "Toca el mapa para seleccionar un punto", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Configuración inicial (Ej: Centrar en un punto default o ubicación actual)
        // Aquí podrías pedir permisos de ubicación para hacer setMyLocationEnabled(true)
        LatLng puntoInicial = new LatLng(-12.0464, -77.0428); // Lima (ejemplo)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(puntoInicial, 10));

        // Listener: Al tocar el mapa, ponemos un marcador
        mMap.setOnMapClickListener(latLng -> {
            mMap.clear(); // Borrar marcadores anteriores
            mMap.addMarker(new MarkerOptions().position(latLng).title("Ubicación seleccionada"));
            ubicacionSeleccionada = latLng;
        });
    }
}