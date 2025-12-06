package com.example.animals.fragmentos;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.animals.BuscadorActivity;
import com.example.animals.R;
import com.example.animals.Mascota.RegistrarMascotaFragment;
import com.example.animals.Publicaciones.Publicacion;
import com.example.animals.Publicaciones.PublicacionAdapter;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MenuFragment extends Fragment {

    private static final String TAG = "MenuFragment";

    private RecyclerView recyclerFeed;
    private SwipeRefreshLayout swipeRefreshFeed;
    private PublicacionAdapter adapterFeed;
    private List<Publicacion> listaPublicaciones;

    private ImageView imgAvatarCrear, imgPreviewCrear;
    private EditText edtDescripcionCrear;
    private Button btnAdjuntarFoto, btnPublicarAhora;
    private TextView txtNombreMascotaCrear;
    private Uri imagenUriSeleccionada;

    private final ActivityResultLauncher<Intent> seleccionarImagenLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == -1 && result.getData() != null) { // -1 es RESULT_OK
                    imagenUriSeleccionada = result.getData().getData();
                    if (imgPreviewCrear != null) {
                        imgPreviewCrear.setImageURI(imagenUriSeleccionada);
                        imgPreviewCrear.setVisibility(View.VISIBLE);
                        if(btnAdjuntarFoto != null) btnAdjuntarFoto.setText("Cambiar Foto");
                    }
                }
            });

    public MenuFragment() { }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerFeed = view.findViewById(R.id.recyclerFeed);
        swipeRefreshFeed = view.findViewById(R.id.swipeRefreshFeed);

        imgAvatarCrear = view.findViewById(R.id.imgAvatarCrear);
        imgPreviewCrear = view.findViewById(R.id.imgPreviewCrear);
        edtDescripcionCrear = view.findViewById(R.id.edtDescripcionCrear);
        txtNombreMascotaCrear = view.findViewById(R.id.txtNombreMascotaCrear);
        btnAdjuntarFoto = view.findViewById(R.id.btnAdjuntarFoto);
        btnPublicarAhora = view.findViewById(R.id.btnPublicarAhora);

        cargarAvatarUsuarioActual();

        if(btnAdjuntarFoto != null) btnAdjuntarFoto.setOnClickListener(v -> seleccionarFoto());
        if(btnPublicarAhora != null) btnPublicarAhora.setOnClickListener(v -> subirPublicacion());

        if (recyclerFeed != null) {
            recyclerFeed.setLayoutManager(new LinearLayoutManager(getContext()));
            listaPublicaciones = new ArrayList<>();
            adapterFeed = new PublicacionAdapter(getContext(), listaPublicaciones);
            recyclerFeed.setAdapter(adapterFeed);
        }

        cargarFeedGlobal();

        if (swipeRefreshFeed != null) {
            swipeRefreshFeed.setOnRefreshListener(() -> cargarFeedGlobal());
        }

        if (getArguments() != null && getArguments().getBoolean("MOSTRAR_BIENVENIDA", false)) {
            mostrarDialogoRegistro();
            getArguments().putBoolean("MOSTRAR_BIENVENIDA", false);
        }

        configurarBotonesCabecera();
    }

    private void cargarAvatarUsuarioActual() {
        if (getContext() == null) return;
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String nombre = prefs.getString("nombreMascotaPrincipal", "Tu Mascota");
        if (txtNombreMascotaCrear != null) {
            txtNombreMascotaCrear.setText(nombre);
        }
        String fotoBase64 = prefs.getString("fotoMascotaPrincipal", "");

        if (!fotoBase64.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(fotoBase64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if(imgAvatarCrear != null) imgAvatarCrear.setImageBitmap(decodedByte);
            } catch (Exception e) {
                if(imgAvatarCrear != null) imgAvatarCrear.setImageResource(R.drawable.ic_launcher_background);
            }
        }
    }

    private void seleccionarFoto() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        seleccionarImagenLauncher.launch(intent);
    }

    private void subirPublicacion() {
        String texto = "";
        if (edtDescripcionCrear != null) texto = edtDescripcionCrear.getText().toString().trim();

        if (texto.isEmpty() && imagenUriSeleccionada == null) {
            Toast.makeText(getContext(), "Escribe algo o sube una foto", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        if (idUsuario == -1) return;

        String url = "http://miguelcardenas.atwebpages.com/proyecto/crearPublicacion.php";
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("idUsuario", idUsuario);
        params.put("descripcion", texto);

        if (imagenUriSeleccionada != null) {
            try {
                File file = uriToFile(imagenUriSeleccionada);
                if (file != null) params.put("foto", file);
            } catch (Exception e) { e.printStackTrace(); }
        }

        if(btnPublicarAhora != null) {
            btnPublicarAhora.setEnabled(false);
            btnPublicarAhora.setText("Publicando...");
        }

        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(btnPublicarAhora != null) {
                    btnPublicarAhora.setEnabled(true);
                    btnPublicarAhora.setText("Publicar");
                }
                try {
                    JSONObject json = new JSONObject(new String(responseBody));
                    if (json.optBoolean("success")) {
                        Toast.makeText(getContext(), "¡Publicado!", Toast.LENGTH_SHORT).show();

                        // LIMPIAR CAMPOS
                        if(edtDescripcionCrear != null) edtDescripcionCrear.setText("");
                        imagenUriSeleccionada = null;
                        if(imgPreviewCrear != null) {
                            imgPreviewCrear.setVisibility(View.GONE);
                            imgPreviewCrear.setImageDrawable(null);
                        }
                        if(btnAdjuntarFoto != null) btnAdjuntarFoto.setText("📷 Foto");

                        cargarFeedGlobal();
                    } else {
                        Toast.makeText(getContext(), "Error: " + json.optString("error"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {}
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if(btnPublicarAhora != null) {
                    btnPublicarAhora.setEnabled(true);
                    btnPublicarAhora.setText("Publicar");
                }
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream inputStream = requireActivity().getContentResolver().openInputStream(uri);
            File file = new File(requireActivity().getCacheDir(), "temp_menu_pub.jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) outputStream.write(buffer, 0, length);
            outputStream.close();
            inputStream.close();
            return file;
        } catch (Exception e) { return null; }
    }

    private void cargarFeedGlobal() {
        String url = "http://miguelcardenas.atwebpages.com/proyecto/feedPublicaciones.php";

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (!isAdded()) return;
                if (swipeRefreshFeed != null) swipeRefreshFeed.setRefreshing(false);

                try {
                    String response = new String(responseBody);
                    JSONArray jsonArray = new JSONArray(response);

                    listaPublicaciones.clear();

                    for(int i=0; i<jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        Publicacion p = new Publicacion();

                        p.setIdPublicacion(obj.optInt("idPublicacion"));
                        p.setDescripcion(obj.optString("descripcion"));
                        p.setFotoBase64(obj.optString("foto"));
                        p.setFechaRegistro(obj.optString("fechaRegistro"));
                        p.setNombreUsuario(obj.optString("nombreUsuario", "Anónimo"));
                        p.setFotoPerfilUsuario(obj.optString("fotoPerfil", ""));
                        p.setCantidadComentarios(obj.optInt("cantidadComentarios", 0));

                        listaPublicaciones.add(p);
                    }

                    if (adapterFeed != null) adapterFeed.notifyDataSetChanged();

                } catch (Exception e) {
                    Log.e(TAG, "Error cargando feed: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (!isAdded()) return;
                if (swipeRefreshFeed != null) swipeRefreshFeed.setRefreshing(false);
            }
        });
    }

    private void configurarBotonesCabecera() {
        ImageView searchButton = requireActivity().findViewById(R.id.icon_search_button);
        if (searchButton != null) {
            searchButton.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), BuscadorActivity.class);
                startActivity(intent);
            });
        }
        ImageView btnNotificaciones = requireActivity().findViewById(R.id.icon_notificaciones_button);
        if (btnNotificaciones != null) {
            btnNotificaciones.setOnClickListener(v -> abrirNotificaciones());
        }
    }

    private void abrirNotificaciones() {
        FragmentTransaction transaction = requireActivity()
                .getSupportFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.frameLayout, new NotificacionFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void mostrarDialogoRegistro() {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("¡Bienvenido!");
        builder.setMessage("Registra a tu primera mascota antes de comenzar.");
        builder.setCancelable(false);
        builder.setPositiveButton("Registrar", (dialog, which) -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, new RegistrarMascotaFragment())
                    .commit();
        });
        builder.setNegativeButton("Más tarde", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarFeedGlobal();
        cargarAvatarUsuarioActual();
    }
}