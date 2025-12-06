package com.example.animals.fragmentos;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.animals.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PerfilFragment extends Fragment {
    EditText txtNombre, txtApellido, txtCelular, txtEdad;
    Button btnActualizar, btnCancelar;

    String URL_ACTUALIZAR = "http://miguelcardenas.atwebpages.com/proyecto/actualizarPerfil.php";
    String URL_OBTENER = "http://miguelcardenas.atwebpages.com/proyecto/obtenerPerfil.php";

    int idUsuario;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        txtNombre = view.findViewById(R.id.updTxtNombre);
        txtApellido = view.findViewById(R.id.updTxtApellido);
        txtCelular = view.findViewById(R.id.updTxtCelular);
        txtEdad = view.findViewById(R.id.updTxtEdad);

        btnActualizar = view.findViewById(R.id.btnActualizarDatos);
        btnCancelar = view.findViewById(R.id.btnCancelarEdicion);

        SharedPreferences preferences = this.getActivity().getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE);
        idUsuario = preferences.getInt("id_usuario", 0);

        if (idUsuario > 0) {
            cargarDatosUsuario();
        } else {
            Toast.makeText(getContext(), "Error: ID usuario inválido", Toast.LENGTH_SHORT).show();
        }

        btnActualizar.setOnClickListener(v -> actualizarDatosConVolley());

        btnCancelar.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                ConfigFragment configFragment = new ConfigFragment();
                if (getView() != null && getView().getParent() != null) {
                    getParentFragmentManager().beginTransaction()
                            .replace(((View) getView().getParent()).getId(), configFragment)
                            .commit();
                }
            }
        });

        return view;
    }

    private void cargarDatosUsuario() {
        StringRequest request = new StringRequest(Request.Method.POST, URL_OBTENER,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("exito")) {
                            txtNombre.setText(jsonObject.getString("nombres"));
                            txtApellido.setText(jsonObject.getString("apellidos"));
                            txtCelular.setText(jsonObject.getString("celular"));
                            txtEdad.setText(jsonObject.getString("edad"));
                            // Ya no cargamos correo ni clave
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(getContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(idUsuario));
                return params;
            }
        };
        Volley.newRequestQueue(getContext()).add(request);
    }

    private void actualizarDatosConVolley() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_ACTUALIZAR,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("exito")) {
                            Toast.makeText(getContext(), jsonObject.getString("mensaje"), Toast.LENGTH_SHORT).show();
                            getParentFragmentManager().popBackStack(); // Regresa a ConfigFragment
                        } else {
                            Toast.makeText(getContext(), "Error: " + jsonObject.getString("error_msg"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(idUsuario));
                params.put("nombres", txtNombre.getText().toString());
                params.put("apellidos", txtApellido.getText().toString());
                params.put("celular", txtCelular.getText().toString());
                params.put("edad", txtEdad.getText().toString());
                return params;
            }
        };
        Volley.newRequestQueue(getContext()).add(stringRequest);
    }
}