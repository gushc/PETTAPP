package com.example.animals.recomendaciones;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog; // Importante para el diálogo de borrar
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.animals.R;
import com.example.animals.clases.Recomendacion;
import com.example.animals.recomendaciones.EditarRecomendacionActivity;
import com.example.animals.recomendaciones.RecomendacionAdapter;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class DetalleRecomendacionFragment extends Fragment {

    private RecyclerView rvMisRecomendaciones;
    private RecomendacionAdapter adapter;
    private List<Recomendacion> listaMisRecomendaciones;

    public DetalleRecomendacionFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_recomendacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvMisRecomendaciones = view.findViewById(R.id.rvMisRecomendaciones);
        rvMisRecomendaciones.setLayoutManager(new LinearLayoutManager(requireContext()));
        listaMisRecomendaciones = new ArrayList<>();

        adapter = new RecomendacionAdapter(getContext(), listaMisRecomendaciones, true, new RecomendacionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Recomendacion item) {
                // Clic en la tarjeta (opcional)
            }

            @Override
            public void onEditarClick(Recomendacion item) {
                irAEditar(item);
            }

            @Override
            public void onEliminarClick(Recomendacion item) {
                confirmarEliminacion(item);
            }
        });

        rvMisRecomendaciones.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarMisRecomendaciones();
    }

    private void cargarMisRecomendaciones() {
        SharedPreferences preferences = requireActivity().getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        int idUsuario = preferences.getInt("id_usuario", 0);

        if (idUsuario == 0) {
            Toast.makeText(getContext(), "Error: Sesión no encontrada", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://miguelcardenas.atwebpages.com/proyecto/obtenerRecomendacionPorId.php";

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("idUsuario", idUsuario);

        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (getContext() == null) return;
                try {
                    String response = new String(responseBody);
                    if (response.isEmpty() || response.equals("null") || response.equals("[]")) {
                        Toast.makeText(getContext(), "No tienes publicaciones aún", Toast.LENGTH_SHORT).show();
                        listaMisRecomendaciones.clear();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    JSONArray jsonArray = new JSONArray(response);
                    listaMisRecomendaciones.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        Recomendacion r = new Recomendacion();
                        r.setId(obj.optInt("id")); // Ojo: Verifica si tu PHP manda "id" o "idRecomendacion"
                        r.setTitulo(obj.optString("titulo"));
                        r.setDescripcion(obj.optString("descripcion"));
                        r.setCategoria(obj.optString("categoria"));
                        r.setFotoUrl(obj.optString("foto"));
                        r.setAutor(obj.optString("autor"));
                        r.setLatitud(obj.optDouble("latitud", 0.0));
                        r.setLongitud(obj.optDouble("longitud", 0.0));
                        listaMisRecomendaciones.add(r);
                    }
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.e("ErrorJSON", "Error parsing: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (getContext() != null)
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void irAEditar(Recomendacion item) {
        Intent intent = new Intent(getContext(), EditarRecomendacionActivity.class);
        intent.putExtra("id", item.getId());
        intent.putExtra("titulo", item.getTitulo());
        intent.putExtra("descripcion", item.getDescripcion());
        intent.putExtra("categoria", item.getCategoria());
        intent.putExtra("latitud", item.getLatitud());
        intent.putExtra("longitud", item.getLongitud());
        startActivity(intent);
    }

    private void confirmarEliminacion(Recomendacion item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar")
                .setMessage("¿Estás seguro de eliminar '" + item.getTitulo() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> realizarEliminacion(item.getId()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void realizarEliminacion(int idRecomendacion) {
        String url = "http://miguelcardenas.atwebpages.com/proyecto/eliminarRecomendacion.php";
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("id", idRecomendacion);

        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (getContext() == null) return;
                try {
                    JSONObject json = new JSONObject(new String(responseBody));
                    if (json.optBoolean("success")) {
                        Toast.makeText(getContext(), "Eliminado correctamente", Toast.LENGTH_SHORT).show();
                        cargarMisRecomendaciones();
                    } else {
                        Toast.makeText(getContext(), "No se pudo eliminar", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (getContext() != null)
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}