package com.example.animals;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.animals.Usuario.UsuarioAdapter;
import com.example.animals.clases.UsuarioBusqueda;
import com.example.animals.sqlite.Animals2;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class BuscadorActivity extends AppCompatActivity {

    private EditText searchBox;
    private ImageView searchIcon;

    private ListView historyListView;
    private Animals2 dbHelper;
    private ArrayAdapter<String> adapterHistorial;

    private RecyclerView recyclerResultados;
    private UsuarioAdapter adapterResultados;
    private List<UsuarioBusqueda> listaUsuarios;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscador);

        searchBox = findViewById(R.id.searchBox);
        searchIcon = findViewById(R.id.searchIcon);
        historyListView = findViewById(R.id.historyListView);

        recyclerResultados = findViewById(R.id.recyclerResultados);
        // ¡OJO! Debes agregar este ID en tu XML (activity_buscador.xml)

        dbHelper = new Animals2(this);
        loadSearchHistory();

        listaUsuarios = new ArrayList<>();
        adapterResultados = new UsuarioAdapter(this, listaUsuarios);
        recyclerResultados.setLayoutManager(new LinearLayoutManager(this));
        recyclerResultados.setAdapter(adapterResultados);

        mostrarVistaHistorial();

        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch(searchBox.getText().toString().trim());
            }
        });

        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                                && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    performSearch(searchBox.getText().toString().trim());
                    return true;
                }
                return false;
            }
        });

        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String terminoSeleccionado = adapterHistorial.getItem(position);
                if (terminoSeleccionado != null) {
                    searchBox.setText(terminoSeleccionado);
                    performSearch(terminoSeleccionado);
                }
            }
        });

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().isEmpty()){
                    mostrarVistaHistorial();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            Toast.makeText(this, "Ingresa un término para buscar.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        if (idUsuario != -1) {
            dbHelper.insertQuery(query, idUsuario); // <--- Pasamos el ID
            loadSearchHistory(); // Recargar lista
        }

        ocultarTeclado();
        mostrarVistaResultados();

        buscarUsuariosEnServidor(query);
    }

    private void buscarUsuariosEnServidor(String texto) {
        String url = "http://miguelcardenas.atwebpages.com/proyecto/buscarUsuarios.php?q=" + texto;

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String response = new String(responseBody);
                    JSONArray jsonArray = new JSONArray(response);

                    listaUsuarios.clear();
                    for(int i=0; i<jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        // Llenamos el modelo con ID, Nombre y Foto Base64
                        listaUsuarios.add(new UsuarioBusqueda(
                                obj.optInt("id"),
                                obj.optString("nombreCompleto"),
                                obj.optString("foto")
                        ));
                    }
                    adapterResultados.notifyDataSetChanged();

                    if (listaUsuarios.isEmpty()) {
                        Toast.makeText(BuscadorActivity.this, "No se encontraron usuarios", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(BuscadorActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSearchHistory() {
        // --- CAMBIO: OBTENER USUARIO ---
        SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        // Si no hay usuario logueado, no mostramos historial o mostramos lista vacía
        List<String> recentQueries;
        if (idUsuario != -1) {
            recentQueries = dbHelper.getRecentQueries(idUsuario); // <--- Pasamos el ID
        } else {
            recentQueries = new ArrayList<>();
        }

        if (adapterHistorial == null) {
            adapterHistorial = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, recentQueries);
            historyListView.setAdapter(adapterHistorial);
        } else {
            adapterHistorial.clear();
            adapterHistorial.addAll(recentQueries);
            adapterHistorial.notifyDataSetChanged();
        }
    }
    private void mostrarVistaHistorial() {
        historyListView.setVisibility(View.VISIBLE);
        recyclerResultados.setVisibility(View.GONE);
    }

    private void mostrarVistaResultados() {
        historyListView.setVisibility(View.GONE);
        recyclerResultados.setVisibility(View.VISIBLE);
    }

    private void ocultarTeclado() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSearchHistory();
    }
}