package com.example.animals.fragmentos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.animals.Mascota.MascotaAdapter;
import com.example.animals.Mascota.MascotaModel;
import com.example.animals.Mascota.RegistrarMascotaFragment;
import com.example.animals.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import cz.msebera.android.httpclient.Header;

public class MascotaFragment extends Fragment {

    private ArrayList<MascotaModel> listaMascotasOriginal = new ArrayList<>();
    private ArrayList<MascotaModel> listaMascotas = new ArrayList<>();
    private View btnAgregarMascota;
    private RecyclerView rvMisMascotas;
    private Button btnOrdenarAlfabeticamente;
    private Spinner spCantidadMascotas;
    private MascotaAdapter mascotaAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mascota, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnAgregarMascota = view.findViewById(R.id.btnAgregarMascota);
        rvMisMascotas = view.findViewById(R.id.rvMisMascotas);
        btnOrdenarAlfabeticamente = view.findViewById(R.id.btnOrdenarAlfabeticamente);
        spCantidadMascotas = view.findViewById(R.id.spCantidadMascotas);

        rvMisMascotas.setLayoutManager(new LinearLayoutManager(getContext()));

        mascotaAdapter = new MascotaAdapter(getContext(), listaMascotas, new MascotaAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(MascotaModel mascota, int position) {
                confirmarEliminacion(mascota, position);
            }

            @Override
            public void onEditClick(MascotaModel mascota) {
                Toast.makeText(getContext(), "Editar: " + mascota.getNombre(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onViewClick(MascotaModel mascota) {
                // 👈 GUARDAR LA MASCOTA SELECCIONADA
                seleccionarMascotaPrincipal(mascota);
            }
        });

        rvMisMascotas.setAdapter(mascotaAdapter);

        // Configurar spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.spinner_cantidad_mascotas,
                getResources().getStringArray(R.array.cantidades_mascotas)
        );
        adapter.setDropDownViewResource(R.layout.spinner_cantidad_mascotas_dropdown);
        spCantidadMascotas.setAdapter(adapter);

        btnAgregarMascota.setOnClickListener(v -> abrirRegistrarMascota());
        btnOrdenarAlfabeticamente.setOnClickListener(v -> ordenarMascotas());
        spCantidadMascotas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                aplicarFiltroCantidad();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        cargarMascotas();
    }

    // 👇 MÉTODO CLAVE: Guardar mascota seleccionada en SharedPreferences
    private void seleccionarMascotaPrincipal(MascotaModel mascota) {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Guardamos TODOS los datos de la mascota
        editor.putInt("idMascotaPrincipal", mascota.getIdMascota());
        editor.putString("nombreMascotaPrincipal", mascota.getNombre());
        editor.putString("especieMascotaPrincipal", mascota.getEspecie());
        editor.putString("razaMascotaPrincipal", mascota.getRaza());
        editor.putString("sexoMascotaPrincipal", mascota.getSexo());
        editor.putString("edadMascotaPrincipal", mascota.getEdad());
        editor.putString("colorMascotaPrincipal", mascota.getColor());
        editor.putString("fotoMascotaPrincipal", mascota.getFotoBase64());
        editor.putLong("timestampActualizacion", System.currentTimeMillis()); // 👈 Para detectar cambios
        editor.apply();

        Toast.makeText(getContext(),
                "✓ Mascota principal: " + mascota.getNombre(),
                Toast.LENGTH_SHORT).show();

        Log.d("MASCOTA_SELECCIONADA", "ID: " + mascota.getIdMascota() + " - " + mascota.getNombre());
    }

    private void cargarMascotas() {
        if (getContext() == null) return;

        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        if (idUsuario == -1) {
            Toast.makeText(getContext(), "Usuario no identificado", Toast.LENGTH_LONG).show();
            return;
        }

        String url = "http://miguelcardenas.atwebpages.com/proyecto/listarMascotas.php";
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("idUsuario", idUsuario);

        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String resp = new String(responseBody);
                Log.d("MASCOTA_FRAGMENT", "RESPUESTA: " + resp);

                try {
                    JSONObject json = new JSONObject(resp);
                    if (json.getBoolean("success")) {
                        listaMascotas.clear();

                        if (json.has("mascotas")) {
                            JSONArray mascotasArray = json.getJSONArray("mascotas");

                            for (int i = 0; i < mascotasArray.length(); i++) {
                                JSONObject m = mascotasArray.getJSONObject(i);

                                int id = m.getInt("idMascota");
                                String nombre = m.optString("nombre", "Sin nombre");
                                String especie = m.optString("especie", "");
                                String raza = m.optString("raza", "");
                                String fotoBase64 = m.optString("fotoBase64", "");
                                String sexo = m.optString("sexo", "");
                                String edad = m.optString("edad", "");
                                String color = m.optString("color", "");

                                MascotaModel mascota = new MascotaModel(
                                        id, nombre, especie, raza, fotoBase64, sexo, edad, color
                                );
                                listaMascotas.add(mascota);
                            }

                            listaMascotasOriginal = new ArrayList<>(listaMascotas);
                            mascotaAdapter.actualizarLista(listaMascotas);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error procesando respuesta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void confirmarEliminacion(MascotaModel mascota, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Mascota")
                .setMessage("¿Estás seguro de que deseas eliminar a " + mascota.getNombre() + "?")
                .setPositiveButton("Sí", (dialog, which) -> eliminarMascota(mascota, position))
                .setNegativeButton("No", null)
                .show();
    }

    private void eliminarMascota(MascotaModel mascota, int position) {
        String url = "http://miguelcardenas.atwebpages.com/proyecto/eliminarMascota.php";
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("idMascota", mascota.getIdMascota());

        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String resp = new String(responseBody);
                try {
                    JSONObject json = new JSONObject(resp);
                    if (json.optBoolean("success")) {
                        mascotaAdapter.removerItem(position);
                        listaMascotasOriginal.removeIf(m -> m.getIdMascota() == mascota.getIdMascota());
                        Toast.makeText(getContext(), "Mascota eliminada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Error: " + json.optString("error"), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error en respuesta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ordenarMascotas() {
        Collections.sort(listaMascotas, (a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()));
        mascotaAdapter.notifyDataSetChanged();
    }

    private void aplicarFiltroCantidad() {
        if (spCantidadMascotas.getSelectedItem() == null) return;

        String seleccion = spCantidadMascotas.getSelectedItem().toString();
        ArrayList<MascotaModel> listaFiltrada = new ArrayList<>(listaMascotasOriginal);

        if (!seleccion.equals("Todas")) {
            try {
                int cantidad = Integer.parseInt(seleccion);
                if (listaFiltrada.size() > cantidad) {
                    listaFiltrada = new ArrayList<>(listaFiltrada.subList(0, cantidad));
                }
            } catch (NumberFormatException e) {}
        }

        mascotaAdapter.actualizarLista(listaFiltrada);
    }

    private void abrirRegistrarMascota() {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.fade_out);
        transaction.replace(R.id.frameLayout, new RegistrarMascotaFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarMascotas();
    }
}