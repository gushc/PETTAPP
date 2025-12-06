package com.example.animals.fragmentos;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction; // Importante importar esto

import com.example.animals.R;
// import com.example.animals.actividades.PerfilMascotaActivity; // Ya no lo usamos aquí
import com.example.animals.actividades.SesionActivity;
import com.example.animals.actividades.PrivacidadActivity;
import com.example.animals.actividades.NotificacionActivity;
import com.example.animals.sqlite.Animals;

public class ConfigFragment extends Fragment {

    private Button btnCerrarSesion, btnPerfil, btnPrivacidad , btnNotificaciones ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_config, container, false);

        // Referencias
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);
        btnPerfil = view.findViewById(R.id.btnPerfil);
        btnPrivacidad = view.findViewById(R.id.btnPrivacidad);
        btnNotificaciones = view.findViewById(R.id.btnNotificaciones);

        // Eventos
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());

        // CAMBIO: Ahora llama al método que abre el Fragmento
        btnPerfil.setOnClickListener(v -> abrirPerfilFragment());

        btnPrivacidad.setOnClickListener(v -> abrirPrivacidad());
        btnNotificaciones.setOnClickListener(v -> abrirNotificaciones());

        return view;
    }

    private void abrirNotificaciones() {
        Intent intent = new Intent(getActivity(), NotificacionActivity.class);
        startActivity(intent);
    }

    private void abrirPrivacidad() {
        Intent intent = new Intent(getActivity(), PrivacidadActivity.class);
        startActivity(intent);
    }
    private void abrirPerfilFragment() {
        // 1. Crear instancia del fragmento
        PerfilFragment perfilFragment = new PerfilFragment();

        // 2. Iniciar transacción
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

        // 3. Reemplazar usando el ID del contenedor actual
        // TRUCO: getView().getParent() obtiene el layout donde está metido este fragmento ahora mismo.
        // ((View) getView().getParent()).getId() nos da el ID correcto sin tener que adivinarlo.
        if (getView() != null && getView().getParent() != null) {
            int idContenedorActual = ((View) getView().getParent()).getId();
            transaction.replace(idContenedorActual, perfilFragment);
        } else {
            // Si falla la detección, intentamos con el genérico de Android (a veces funciona de emergencia)
            transaction.replace(android.R.id.content, perfilFragment);
        }

        // 4. Agregar al BackStack
        transaction.addToBackStack(null);

        // 5. Confirmar
        transaction.commit();
    }
    // ----------------------------------------------

    private void cerrarSesion() {
        Toast.makeText(getActivity(), "Sesión cerrada exitosamente", Toast.LENGTH_SHORT).show();

        // Limpiar preferencias de usuario
        SharedPreferences userPrefs = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor userEditor = userPrefs.edit();
        userEditor.clear();
        userEditor.apply();

        // Limpiar flag modal
        SharedPreferences configPrefs = getActivity().getSharedPreferences("config_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor configEditor = configPrefs.edit();
        configEditor.putBoolean("modalMostrado", false);
        configEditor.apply();

        Animals animals = new Animals(getContext());
        animals.eliminarUsuario();

        // Redirigir al login
        Intent intent = new Intent(getActivity(), SesionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}