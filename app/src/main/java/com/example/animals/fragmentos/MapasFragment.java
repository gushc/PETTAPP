package com.example.animals.fragmentos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.animals.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapasFragment extends Fragment {

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }, 10);
                }
            } else {
                googleMap.setMyLocationEnabled(true);
            }

            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

            double latRecibida = 0.0;
            double lonRecibida = 0.0;
            String tituloRecibido = "Ubicación";

            if (getArguments() != null) {
                latRecibida = getArguments().getDouble("latitud", 0.0);
                lonRecibida = getArguments().getDouble("longitud", 0.0);
                tituloRecibido = getArguments().getString("recTitle", "Ubicación Recomendada");
            }

            if (latRecibida != 0.0 && lonRecibida != 0.0) {
                LatLng punto = new LatLng(latRecibida, lonRecibida);
                googleMap.addMarker(new MarkerOptions().position(punto).title(tituloRecibido));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(punto, 16f)); // Zoom cercano
            } else {
                LatLng sede = new LatLng(-11.985376, -77.004187);
                googleMap.addMarker(new MarkerOptions().position(sede).title("UPN - SJL"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sede, 19f));
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mapas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
}