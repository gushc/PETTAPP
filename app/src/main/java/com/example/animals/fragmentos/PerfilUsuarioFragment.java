package com.example.animals.fragmentos;

import android.Manifest;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.animals.R;
import com.example.animals.Publicaciones.Publicacion;
import com.example.animals.Publicaciones.PublicacionAdapter;
// --- NUEVO: IMPORTAR TU CLASE SQLITE ---
import com.example.animals.sqlite.Animals3;

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

public class PerfilUsuarioFragment extends Fragment {

    private static final String TAG = "PerfilUsuarioFragment";
    private int idMascotaPrincipal = -1;

    private Animals3 dbBorradores;

    private EditText regTxtNombre, regTxtEspecie, regTxtRaza, regTxtSexo, regTxtEdad, regTxtColor;
    private ImageView imgFotoPerfil, imgEncabezado;

    private EditText editTextoPublicacion;
    private ImageView imgPreviewPublicacion;
    private ImageView imgAvatarPublicacion;
    private TextView txtNombreMascotaPublicando;
    private Button btnPublicarFoto, btnPublicar;
    private Uri imagenUriSeleccionada;

    private TextView tabMisPublicaciones, tabPublicacionesCompartidas;
    private View tabIndicator;
    private LinearLayout layoutMisPublicaciones, layoutPublicacionesCompartidas;
    private RecyclerView recyclerMisPublicaciones;
    private PublicacionAdapter adapterMisPublicaciones;
    private List<Publicacion> misPublicaciones;

    private View view;

    private final ActivityResultLauncher<Intent> seleccionarImagenLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == -1 && result.getData() != null) {
                    imagenUriSeleccionada = result.getData().getData();
                    if (imgPreviewPublicacion != null) {
                        imgPreviewPublicacion.setImageURI(imagenUriSeleccionada);
                        imgPreviewPublicacion.setVisibility(View.VISIBLE);
                    }
                    if (btnPublicarFoto != null) btnPublicarFoto.setText("Cambiar Foto");
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            view = inflater.inflate(R.layout.fragment_perfil_usuario, container, false);
        } catch (Exception e) {
            Log.e(TAG, "Error fatal inflando el layout XML: " + e.getMessage());
            return null;
        }

        if (view != null) {
            inicializarVistas();
            configurarPestanas();
            configurarBotones();
            dbBorradores = new Animals3(getContext());
            recuperarBorrador();
            cargarDatosMascota();
            cargarPublicaciones();
        }

        return view;
    }

    private void recuperarBorrador() {
        if (getContext() == null) return;
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        if (idUsuario != -1 && dbBorradores != null) {
            String[] datos = dbBorradores.obtenerBorradorPublicacion(idUsuario);

            if (datos != null) {

                if (editTextoPublicacion != null && datos[0] != null) {
                    editTextoPublicacion.setText(datos[0]);
                }

                if (datos[1] != null && !datos[1].isEmpty()) {
                    try {
                        imagenUriSeleccionada = Uri.parse(datos[1]);

                        if (imgPreviewPublicacion != null) {
                            imgPreviewPublicacion.setImageURI(imagenUriSeleccionada);
                            imgPreviewPublicacion.setVisibility(View.VISIBLE);
                        }
                        if (btnPublicarFoto != null) {
                            btnPublicarFoto.setText("Cambiar Foto");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    @Override
    public void onPause() {
        super.onPause();

        if (getContext() == null) return;

        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        String textoActual = "";
        if (editTextoPublicacion != null) {
            textoActual = editTextoPublicacion.getText().toString();
        }

        String uriFotoActual = "";
        if (imagenUriSeleccionada != null) {
            uriFotoActual = imagenUriSeleccionada.toString();
        }

        if (idUsuario != -1 && (!textoActual.isEmpty() || !uriFotoActual.isEmpty())) {
            if (dbBorradores != null) {
                dbBorradores.guardarBorradorPublicacion(idUsuario, textoActual, uriFotoActual);
                Log.d(TAG, "Borrador guardado localmente en Animals3");
            }
        }
    }

    private void inicializarVistas() {
        try {
            regTxtNombre = view.findViewById(R.id.regTxtNombre);
            regTxtEspecie = view.findViewById(R.id.regTxtEspecie);
            regTxtRaza = view.findViewById(R.id.regTxtRaza);
            regTxtSexo = view.findViewById(R.id.regTxtSexo);
            regTxtEdad = view.findViewById(R.id.regTxtEdad);
            regTxtColor = view.findViewById(R.id.regTxtColor);

            imgFotoPerfil = view.findViewById(R.id.imgFotoPerfil);
            imgEncabezado = view.findViewById(R.id.imgEncabezado);

            editTextoPublicacion = view.findViewById(R.id.editTextoPublicacion);
            btnPublicarFoto = view.findViewById(R.id.btnPublicarFoto);
            btnPublicar = view.findViewById(R.id.btnPublicar);
            imgPreviewPublicacion = view.findViewById(R.id.imgPreviewPublicacion);

            imgAvatarPublicacion = view.findViewById(R.id.imgAvatarPublicacion);
            txtNombreMascotaPublicando = view.findViewById(R.id.txtNombreMascotaPublicando);

            tabMisPublicaciones = view.findViewById(R.id.tab_mis_publicaciones);
            tabPublicacionesCompartidas = view.findViewById(R.id.tab_publicaciones_compartidas);
            tabIndicator = view.findViewById(R.id.tab_indicator);
            layoutMisPublicaciones = view.findViewById(R.id.layout_mis_publicaciones);
            layoutPublicacionesCompartidas = view.findViewById(R.id.layout_publicaciones_compartidas);

            recyclerMisPublicaciones = view.findViewById(R.id.recycler_mis_publicaciones);
            if (recyclerMisPublicaciones != null) {
                recyclerMisPublicaciones.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerMisPublicaciones.setNestedScrollingEnabled(false);
            }

            misPublicaciones = new ArrayList<>();

        } catch (Exception e) {
            Log.e(TAG, "Error inicializando vistas: " + e.getMessage());
        }
    }

    private void cargarDatosMascota() {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        int idMascota = prefs.getInt("idMascotaPrincipal", -1);
        String nombre = prefs.getString("nombreMascotaPrincipal", "Mascota");
        String fotoBase64 = prefs.getString("fotoMascotaPrincipal", "");

        this.idMascotaPrincipal = idMascota;

        if (regTxtNombre != null) regTxtNombre.setText(nombre);
        if (regTxtEspecie != null) regTxtEspecie.setText(prefs.getString("especieMascotaPrincipal", ""));
        if (regTxtRaza != null) regTxtRaza.setText(prefs.getString("razaMascotaPrincipal", ""));
        if (regTxtSexo != null) regTxtSexo.setText(prefs.getString("sexoMascotaPrincipal", ""));
        if (regTxtEdad != null) regTxtEdad.setText(prefs.getString("edadMascotaPrincipal", ""));
        if (regTxtColor != null) regTxtColor.setText(prefs.getString("colorMascotaPrincipal", ""));

        if (txtNombreMascotaPublicando != null) {
            txtNombreMascotaPublicando.setText(nombre);
        }

        if (fotoBase64 != null && !fotoBase64.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(fotoBase64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                if (imgFotoPerfil != null) imgFotoPerfil.setImageBitmap(decodedByte);
                if (imgAvatarPublicacion != null) imgAvatarPublicacion.setImageBitmap(decodedByte);

            } catch (Exception e) {
                if (imgFotoPerfil != null) imgFotoPerfil.setImageResource(R.drawable.ic_launcher_foreground);
                if (imgAvatarPublicacion != null) imgAvatarPublicacion.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } else {
            if (imgFotoPerfil != null) imgFotoPerfil.setImageResource(R.drawable.ic_launcher_foreground);
            if (imgAvatarPublicacion != null) imgAvatarPublicacion.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    private void configurarPestanas() {
        if(tabMisPublicaciones != null) tabMisPublicaciones.setOnClickListener(v -> mostrarMisPublicaciones());
        if(tabPublicacionesCompartidas != null) tabPublicacionesCompartidas.setOnClickListener(v -> mostrarPublicacionesCompartidas());

        mostrarMisPublicaciones();
    }

    private void configurarBotones() {
        if(btnPublicarFoto != null) btnPublicarFoto.setOnClickListener(v -> seleccionarFoto());
        if(btnPublicar != null) btnPublicar.setOnClickListener(v -> publicarContenido());
    }

    private void mostrarMisPublicaciones() {
        if(layoutMisPublicaciones != null) layoutMisPublicaciones.setVisibility(View.VISIBLE);
        if(layoutPublicacionesCompartidas != null) layoutPublicacionesCompartidas.setVisibility(View.GONE);

        if(tabMisPublicaciones != null) tabMisPublicaciones.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        if(tabPublicacionesCompartidas != null) tabPublicacionesCompartidas.setTextColor(getResources().getColor(android.R.color.darker_gray));

        if (tabIndicator != null) {
            tabIndicator.setVisibility(View.VISIBLE);
        }
    }

    private void mostrarPublicacionesCompartidas() {
        if(layoutMisPublicaciones != null) layoutMisPublicaciones.setVisibility(View.GONE);
        if(layoutPublicacionesCompartidas != null) layoutPublicacionesCompartidas.setVisibility(View.VISIBLE);

        if(tabPublicacionesCompartidas != null) tabPublicacionesCompartidas.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        if(tabMisPublicaciones != null) tabMisPublicaciones.setTextColor(getResources().getColor(android.R.color.darker_gray));
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

    private void publicarContenido() {
        if (editTextoPublicacion == null) return;

        String contenido = editTextoPublicacion.getText().toString().trim();
        if (contenido.isEmpty() && imagenUriSeleccionada == null) {
            editTextoPublicacion.setError("Escribe algo o sube una foto");
            return;
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        if (idUsuario == -1) {
            Toast.makeText(getContext(), "Error de sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://miguelcardenas.atwebpages.com/proyecto/crearPublicacion.php";

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("idUsuario", idUsuario);
        params.put("descripcion", contenido);

        if (imagenUriSeleccionada != null) {
            try {
                File file = uriToFile(imagenUriSeleccionada);
                if (file != null) params.put("foto", file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(btnPublicar != null) btnPublicar.setEnabled(false);

        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(btnPublicar != null) btnPublicar.setEnabled(true);
                try {
                    JSONObject json = new JSONObject(new String(responseBody));
                    if (json.optBoolean("success")) {
                        Toast.makeText(getContext(), "¡Publicado!", Toast.LENGTH_SHORT).show();

                        if(editTextoPublicacion != null) editTextoPublicacion.setText("");
                        if (imgPreviewPublicacion != null) {
                            imgPreviewPublicacion.setVisibility(View.GONE);
                            imgPreviewPublicacion.setImageDrawable(null);
                        }
                        imagenUriSeleccionada = null;
                        if (btnPublicarFoto != null) btnPublicarFoto.setText("📷 Adjuntar Foto");

                        if (dbBorradores != null) {
                            dbBorradores.limpiarBorradorPublicacion(idUsuario);
                            Log.d(TAG, "Borrador eliminado de Animals3 tras publicar");
                        }
                        cargarPublicaciones();
                    }
                } catch (Exception e) {}
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if(btnPublicar != null) btnPublicar.setEnabled(true);
                Toast.makeText(getContext(), "Error de conexión al publicar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarPublicaciones() {
        if (recyclerMisPublicaciones == null) return;

        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);
        if (idUsuario == -1) return;

        String url = "http://miguelcardenas.atwebpages.com/proyecto/listarPublicaciones.php";
        RequestParams params = new RequestParams();
        params.put("idUsuario", idUsuario);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String response = new String(responseBody);
                    JSONArray jsonArray = new JSONArray(response);

                    misPublicaciones.clear();
                    for(int i=0; i<jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        Publicacion p = new Publicacion();
                        p.setIdPublicacion(obj.optInt("idPublicacion"));
                        p.setDescripcion(obj.optString("descripcion"));
                        p.setFotoBase64(obj.optString("foto"));
                        p.setFechaRegistro(obj.optString("fechaRegistro"));
                        p.setNombreUsuario(obj.optString("nombreUsuario", "Usuario"));
                        p.setFotoPerfilUsuario(obj.optString("fotoPerfil"));
                        p.setCantidadComentarios(obj.optInt("cantidadComentarios", 0));
                        misPublicaciones.add(p);
                    }

                    if (adapterMisPublicaciones == null) {
                        adapterMisPublicaciones = new PublicacionAdapter(getContext(), misPublicaciones);
                        recyclerMisPublicaciones.setAdapter(adapterMisPublicaciones);
                    } else {
                        adapterMisPublicaciones.notifyDataSetChanged();
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {}
        });
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream inputStream = requireActivity().getContentResolver().openInputStream(uri);
            File file = new File(requireActivity().getCacheDir(), "temp_pub.jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) outputStream.write(buffer, 0, length);
            outputStream.close();
            inputStream.close();
            return file;
        } catch (Exception e) { return null; }
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarDatosMascota();
        cargarPublicaciones();
    }
}