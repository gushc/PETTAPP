package com.example.animals.actividades;

import android.os.Bundle;
import android.view.View; // Necesario para View.GONE / VISIBLE

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.animals.R;
import com.example.animals.Mascota.RegistrarMascotaFragment; // ⚠️ Asegúrate de importar esto
import com.example.animals.fragmentos.ConfigFragment;
import com.example.animals.fragmentos.MapasFragment; // Revisa si es Mapas o MenuFragment
import com.example.animals.fragmentos.MenuFragment;
import com.example.animals.fragmentos.PerfilUsuarioFragment;
import com.example.animals.fragmentos.MascotaFragment;
import com.example.animals.recomendaciones.ConsultarRecomendacionesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BienvenidaActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bienvenida);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        bottomNavigationView = findViewById(R.id.btnNavView);

        if (savedInstanceState == null) {
            boolean tieneMascotas = getIntent().getBooleanExtra("TIENE_MASCOTAS", true);

            // Preparamos el fragmento del menú
            MenuFragment menuFragment = new MenuFragment();
            if (!tieneMascotas) {
                Bundle args = new Bundle();
                args.putBoolean("MOSTRAR_BIENVENIDA", true);
                menuFragment.setArguments(args);
            }

            // ✅ SIEMPRE cargamos el menú (ya no vamos directo a registrar)
            loadFragment(menuFragment);
            bottomNavigationView.setSelectedItemId(R.id.menu);
        }

        // El resto de tu listener sigue igual...
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.menu) {
                selectedFragment = new MenuFragment();
            } else if (item.getItemId() == R.id.cuenta) {
                selectedFragment = new PerfilUsuarioFragment();
            } else if (item.getItemId() == R.id.mascotas) {
                selectedFragment = new MascotaFragment();
            } else if (item.getItemId() == R.id.mensajes) {
                selectedFragment = new ConsultarRecomendacionesFragment();
            } else if (item.getItemId() == R.id.config) {
                selectedFragment = new ConfigFragment();
            }

            return loadFragment(selectedFragment);
        });
    }

    // Método para cargar el fragmento seleccionado
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frameLayout, fragment);
            // transaction.addToBackStack(null); // Generalmente no se usa backstack en menú principal
            transaction.commit();
            return true;
        }
        return false;
    }

    public void bloquearNavegacion() {
        if (bottomNavigationView != null) {
            // Opción A: Ocultarlo totalmente (Recomendado para registro forzoso)
            bottomNavigationView.setVisibility(View.GONE);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Acceso denegado")
                        .setMessage("Complete los campos necesarios para el registro.")
                        .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                        .setCancelable(false)
                        .show();
                return false;
            });
        }
    }

    public void habilitarNavegacion() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);

            // Restauramos el listener original
            bottomNavigationView.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                if (item.getItemId() == R.id.menu) selectedFragment = new MenuFragment();
                else if (item.getItemId() == R.id.cuenta) selectedFragment = new PerfilUsuarioFragment();
                else if (item.getItemId() == R.id.mascotas) selectedFragment = new MascotaFragment();
                else if (item.getItemId() == R.id.mensajes) selectedFragment = new ConsultarRecomendacionesFragment(); // O MapasFragment según tu código original
                else if (item.getItemId() == R.id.config) selectedFragment = new ConfigFragment();

                return loadFragment(selectedFragment);
            });
        }
    }
}