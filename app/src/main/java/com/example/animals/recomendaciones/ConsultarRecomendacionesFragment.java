package com.example.animals.recomendaciones;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.animals.R;
import com.example.animals.clases.Recomendacion;
import com.example.animals.recomendaciones.DetalleRecomendacionFragment;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class ConsultarRecomendacionesFragment extends Fragment {

    private Spinner spinnerCategoria;
    private Spinner spinnerOrden;
    private RecyclerView rvOtrasRecomendaciones;

    private RecomendacionAdapter adapter;
    private List<Recomendacion> listaRecomendaciones;

    public ConsultarRecomendacionesFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_consultar_recomendaciones, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar Vistas
        spinnerCategoria = view.findViewById(R.id.spinnerCategoriaRecomendacion);
        spinnerOrden = view.findViewById(R.id.spinnerOrdenRecomendacion);
        rvOtrasRecomendaciones = view.findViewById(R.id.rvOtrasRecomendaciones);

        rvOtrasRecomendaciones.setLayoutManager(new LinearLayoutManager(requireContext()));

        listaRecomendaciones = new ArrayList<>();

        adapter = new RecomendacionAdapter(getContext(), listaRecomendaciones, false, new RecomendacionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Recomendacion item) {
                Toast.makeText(getContext(), "Recomendación de: " + item.getAutor(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEditarClick(Recomendacion item) {
            }

            @Override
            public void onEliminarClick(Recomendacion item) {
                // No hace nada en la lista pública
            }
        });

        rvOtrasRecomendaciones.setAdapter(adapter);

        View btnVerMis = view.findViewById(R.id.btnVerMisRecomendaciones);
        if (btnVerMis != null) {
            btnVerMis.setOnClickListener(v -> {
                cambiarFragmento(new DetalleRecomendacionFragment());
            });
        }

        View btnAgregar = view.findViewById(R.id.btnAgregarRecomendacion);
        if (btnAgregar != null) {
            btnAgregar.setOnClickListener(v -> {
                cambiarFragmento(new RegistrarRecomendacionFragment());
            });
        }

        setupSpinners(requireContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarDatosDelServidor();
    }

    private void cargarDatosDelServidor() {
        String url = "http://miguelcardenas.atwebpages.com/proyecto/listarRecomendaciones.php";

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (getContext() == null) return;
                try {
                    String response = new String(responseBody);
                    if (response.isEmpty() || response.equals("null")) return;

                    JSONArray jsonArray = new JSONArray(response);
                    listaRecomendaciones.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        Recomendacion r = new Recomendacion();

                        r.setId(obj.optInt("id"));
                        r.setTitulo(obj.optString("titulo"));
                        r.setDescripcion(obj.optString("descripcion"));
                        r.setCategoria(obj.optString("categoria"));
                        r.setFotoUrl(obj.optString("foto"));
                        r.setAutor(obj.optString("autor"));

                        r.setLatitud(obj.optDouble("latitud", 0.0));
                        r.setLongitud(obj.optDouble("longitud", 0.0));

                        listaRecomendaciones.add(r);
                    }
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e("ErrorJSON", "Error: " + e.getMessage());
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {}
        });
    }

    private void cambiarFragmento(Fragment fragment) {
        FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.slide_out_right);
        ft.replace(R.id.frameLayout, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void setupSpinners(Context ctx) {
        List<String> categorias = new ArrayList<>();
        categorias.add("Todos"); categorias.add("Parques"); categorias.add("Veterinarias");
        List<String> ordenes = new ArrayList<>();
        ordenes.add("Todos"); ordenes.add("Recientes");

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(ctx, R.layout.spinner_general_base, categorias);
        catAdapter.setDropDownViewResource(R.layout.spinner_general_base_dropdown);
        spinnerCategoria.setAdapter(catAdapter);

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(ctx, R.layout.spinner_general_base, ordenes);
        sortAdapter.setDropDownViewResource(R.layout.spinner_general_base_dropdown);
        spinnerOrden.setAdapter(sortAdapter);
    }
}