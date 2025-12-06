package com.example.animals.Mascota;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.animals.R;
import com.example.animals.actividades.SesionActivity;
import com.example.animals.fragmentos.MascotaFragment;
// --- NUEVO: IMPORTAR TU CLASE SQLITE ---
import com.example.animals.sqlite.Animals3;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class RegistrarMascotaFragment extends Fragment {

    // --- NUEVO: VARIABLE PARA LA BASE DE DATOS ---
    private Animals3 dbBorradores;

    private ImageView imgFotoPerfil;
    private Spinner spinnerSexoMascota;
    private EditText regTxtBoletinVacuna;

    // EditTexts que aparecen en tu XML
    private EditText regTxtNombreMascota, regTxtEspecie, regTxtRaza, regTxtEdadMascota, regTxtColorMascota;

    private Button btnRegistrar, btnSalir;
    private Uri imagenSeleccionadaUri;     // para boletín (URI) o archivo seleccionado
    private boolean cambiosPendientes = true;

    // Lanzadores para obtener imagen / foto / boletin
    private final ActivityResultLauncher<Intent> seleccionarImagen =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imagenSeleccionadaUri = result.getData().getData();
                    imgFotoPerfil.setImageURI(imagenSeleccionadaUri);
                    Toast.makeText(requireContext(), "Imagen seleccionada", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> tomarFoto =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null && extras.get("data") != null) {
                        Bitmap foto = (Bitmap) extras.get("data");
                        imgFotoPerfil.setImageBitmap(foto);
                        // guardar en cache como Uri alternativa
                        imagenSeleccionadaUri = getImageUriFromBitmap(foto);
                        Toast.makeText(requireContext(), "Foto tomada correctamente", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    // Este launcher se usa si quieres seleccionar boletín separado; aquí reutilizamos la misma variable imagenSeleccionadaUri
    private final ActivityResultLauncher<Intent> seleccionarBoletin =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uriBoletin = result.getData().getData();
                    // OJO: Tu lógica original usa imagenSeleccionadaUri para el boletín en validarYRegistrar
                    // Si seleccionas boletín, podrías sobrescribir la foto de perfil.
                    // Para el borrador, guardaremos la URI que esté actualmente en imagenSeleccionadaUri
                    regTxtBoletinVacuna.setText("Imagen adjunta");
                    Toast.makeText(requireContext(), "Boletín de vacunación adjuntado", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_registrar_mascota, container, false);
        imgFotoPerfil = vista.findViewById(R.id.imgFotoRegistroMascota);
        spinnerSexoMascota = vista.findViewById(R.id.spnSexoMascota);
        regTxtBoletinVacuna = vista.findViewById(R.id.regTxtBoletinVacuna);
        btnRegistrar = vista.findViewById(R.id.regBtnRegistrarMascota);
        btnSalir = vista.findViewById(R.id.regBtnSalirMascota);

        regTxtNombreMascota = vista.findViewById(R.id.regTxtNombreMascota);
        regTxtEspecie = vista.findViewById(R.id.regTxtEspecie);
        regTxtRaza = vista.findViewById(R.id.regTxtRaza);
        regTxtEdadMascota = vista.findViewById(R.id.regTxtEdadMascota);
        regTxtColorMascota = vista.findViewById(R.id.regTxtColorMascota);

        imgFotoPerfil.setBackgroundResource(R.drawable.custom_edittext);
        imgFotoPerfil.setClipToOutline(true);

        imgFotoPerfil.setOnClickListener(v -> mostrarOpcionesFoto());
        regTxtBoletinVacuna.setOnClickListener(v -> mostrarOpcionesBoletin());
        List<String> listaSexo = new ArrayList<>();
        listaSexo.add("Seleccione sexo");
        listaSexo.add("Masculino");
        listaSexo.add("Femenino");
        listaSexo.add("No definido");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                requireContext(),
                R.layout.spinner_item_personalizado, // Tu XML delgado
                listaSexo
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                if (position == 0) {
                    textView.setTextColor(android.graphics.Color.GRAY);
                    textView.setTypeface(null, android.graphics.Typeface.NORMAL);
                } else {
                    textView.setTextColor(android.graphics.Color.BLACK);
                    textView.setTypeface(null, android.graphics.Typeface.NORMAL);
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                if (position == 0) {
                    textView.setTextColor(android.graphics.Color.GRAY);
                } else {
                    textView.setTextColor(android.graphics.Color.BLACK);
                    textView.setTypeface(null, android.graphics.Typeface.NORMAL);
                }
                return view;
            }
        };
        spinnerSexoMascota.setAdapter(adapter);

        spinnerSexoMascota.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cambiosPendientes = position == 0;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                cambiosPendientes = true;
            }
        });

        // --- NUEVO: INICIALIZAR DB Y RECUPERAR ---
        dbBorradores = new Animals3(requireContext());
        recuperarBorradorMascota();
        // -----------------------------------------

        bloquearNavegacion();
        btnRegistrar.setOnClickListener(v -> validarYRegistrar());
        btnSalir.setOnClickListener(v -> volverALista());

        return vista;
    }

    // --- NUEVO: MÉTODO PARA RECUPERAR BORRADOR ---
    // --- MÉTODO MODIFICADO: RECUPERA TEXTO PERO NO LA FOTO ---
    private void recuperarBorradorMascota() {
        if (getContext() == null) return; // Validación de seguridad

        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        if (idUsuario != -1 && dbBorradores != null) {
            String[] datos = dbBorradores.obtenerBorradorMascota(idUsuario);

            if (datos != null) {
                // datos: 0=nombre, 1=especie, 2=raza, 3=sexo, 4=edad, 5=color, 6=foto

                // 1. RESTAURAR SOLO LOS TEXTOS (Esto es seguro)
                if (regTxtNombreMascota != null) regTxtNombreMascota.setText(datos[0]);
                if (regTxtEspecie != null) regTxtEspecie.setText(datos[1]);
                if (regTxtRaza != null) regTxtRaza.setText(datos[2]);

                // Restaurar Sexo (Spinner)
                if (datos[3] != null) {
                    if (datos[3].equals("Masculino")) spinnerSexoMascota.setSelection(1);
                    else if (datos[3].equals("Femenino")) spinnerSexoMascota.setSelection(2);
                    else if (datos[3].equals("No definido")) spinnerSexoMascota.setSelection(3);
                }

                if (regTxtEdadMascota != null) regTxtEdadMascota.setText(datos[4]);
                if (regTxtColorMascota != null) regTxtColorMascota.setText(datos[5]);

                // 2. FOTO: NO LA RECUPERAMOS PARA EVITAR EL CRASH
                // Aunque exista una ruta guardada en datos[6], la ignoramos.
                imagenSeleccionadaUri = null;

                // Opcional: Puedes mostrar un mensajito avisando
                if (datos[6] != null && !datos[6].isEmpty()) {
                    // Toast.makeText(getContext(), "Por seguridad, selecciona la foto de nuevo", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // --- NUEVO: GUARDAR AL PAUSAR ---
    @Override
    public void onPause() {
        super.onPause();

        // Evitamos crasheos si el contexto ya no existe
        if (getContext() == null) return;

        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);

        if (idUsuario != -1) {
            String nombre = regTxtNombreMascota.getText().toString();
            String especie = regTxtEspecie.getText().toString();
            String raza = regTxtRaza.getText().toString();
            String edad = regTxtEdadMascota.getText().toString();
            String color = regTxtColorMascota.getText().toString();

            // Obtenemos el texto del sexo, excepto si es el prompt "Seleccione sexo"
            String sexo = "";
            if (spinnerSexoMascota.getSelectedItemPosition() > 0) {
                sexo = spinnerSexoMascota.getSelectedItem().toString();
            }

            String foto = (imagenSeleccionadaUri != null) ? imagenSeleccionadaUri.toString() : "";

            // Solo guardamos si hay ALGO escrito o foto seleccionada
            if (!nombre.isEmpty() || !especie.isEmpty() || !foto.isEmpty()) {
                dbBorradores.guardarBorradorMascota(idUsuario, nombre, especie, raza, sexo, edad, color, foto);
                Log.d("BorradorMascota", "Guardado en SQLite");
            }
        }
    }

    private void mostrarOpcionesFoto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Seleccionar foto")
                .setItems(new CharSequence[]{"Elegir de galería", "Tomar foto", "Cancelar"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            if (checkAndRequestStoragePermission()) {
                                Intent intentGaleria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                seleccionarImagen.launch(intentGaleria);
                            }
                            break;
                        case 1:
                            if (checkAndRequestCameraPermission()) {
                                Intent intentCamara = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                tomarFoto.launch(intentCamara);
                            }
                            break;
                        default:
                            dialog.dismiss();
                            break;
                    }
                });
        builder.show();
    }

    private void mostrarOpcionesBoletin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Adjuntar boletín de vacunación")
                .setItems(new CharSequence[]{"Seleccionar imagen", "Cancelar"}, (dialog, which) -> {
                    if (which == 0) {
                        if (checkAndRequestStoragePermission()) {
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            seleccionarBoletin.launch(intent);
                        }
                    } else {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    private void bloquearNavegacion() {
        try {
            if (requireActivity() instanceof com.example.animals.actividades.BienvenidaActivity) {
                ((com.example.animals.actividades.BienvenidaActivity) requireActivity()).bloquearNavegacion();
            }
        } catch (Exception ignored) {}

        FragmentActivity fa = getActivity();
        if (fa != null) {
            View view = fa.findViewById(R.id.btnNavView);

            if (view instanceof com.google.android.material.bottomnavigation.BottomNavigationView) {
                com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigationView =
                        (com.google.android.material.bottomnavigation.BottomNavigationView) view;

                bottomNavigationView.setOnItemSelectedListener(item -> {
                    if (cambiosPendientes) {
                        mostrarDialogoAccesoDenegadoRegistro();
                        return false;
                    } else {
                        return true;
                    }
                });
            }
        }
    }

    private void habilitarNavegacion() {
        try {
            if (requireActivity() instanceof com.example.animals.actividades.BienvenidaActivity) {
                ((com.example.animals.actividades.BienvenidaActivity) requireActivity()).habilitarNavegacion();
            }
        } catch (Exception ignored) {}
    }

    private void mostrarDialogoAccesoDenegadoRegistro() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Acceso denegado")
                .setMessage("Complete el registro solicitado para continuar.")
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private void mostrarDialogoAccesoDenegado() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Acceso denegado")
                .setMessage("Complete los campos necesarios para el registro.")
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
    private void validarYRegistrar() {

        String nombre = regTxtNombreMascota.getText().toString().trim();
        String especie = regTxtEspecie.getText().toString().trim();
        String raza = regTxtRaza.getText().toString().trim();
        String edad = regTxtEdadMascota.getText().toString().trim();
        String color = regTxtColorMascota.getText().toString().trim();
        String sexo = spinnerSexoMascota.getSelectedItem().toString();

        if (nombre.isEmpty() || especie.isEmpty() || raza.isEmpty() ||
                edad.isEmpty() || color.isEmpty() || spinnerSexoMascota.getSelectedItemPosition() == 0) {
            mostrarDialogoAccesoDenegado();
            return;
        }

        if (imgFotoPerfil.getDrawable() == null || regTxtBoletinVacuna.getText().toString().isEmpty()) {
            mostrarDialogoAccesoDenegado();
            return;
        }
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", -1);
        if (idUsuario == -1) {
            Toast.makeText(requireContext(), "Error: usuario no encontrado. Inicie sesión de nuevo.", Toast.LENGTH_LONG).show();
            return;
        }
        subirMascotaAlServidor(idUsuario, nombre, especie, raza, sexo, edad, color);
    }
    private void subirMascotaAlServidor(int idUsuario, String nombre, String especie, String raza, String sexo, String edad, String color) {
        String url = "http://miguelcardenas.atwebpages.com/proyecto/registrarMascota.php";

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("idUsuario", idUsuario);
        params.put("nombre", nombre);
        params.put("especie", especie);
        params.put("raza", raza);
        params.put("sexo", sexo);
        params.put("edad", edad);
        params.put("color", color);
        try {
            Bitmap bitmap = ((BitmapDrawable) imgFotoPerfil.getDrawable()).getBitmap();
            File fotoFile = bitmapToFile(bitmap, "foto_mascota.jpg");
            params.put("foto", fotoFile);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error procesando foto", Toast.LENGTH_LONG).show();
            return;
        }
        if (imagenSeleccionadaUri != null) {
            try {
                File boletinFile = uriToFile(imagenSeleccionadaUri, "boletin_mascota.jpg");
                if (boletinFile != null) params.put("boletin", boletinFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                Toast.makeText(requireContext(), "Registrando mascota...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Log.d("REG_MASCOTA", "RESP: " + response);
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.optBoolean("success", false)) {

                        // --- NUEVO: LIMPIAR BORRADOR AL TENER ÉXITO ---
                        dbBorradores.limpiarBorradorMascota(idUsuario);
                        // ----------------------------------------------

                        cambiosPendientes = false;
                        habilitarNavegacion();
                        Toast.makeText(requireContext(), "Mascota registrada correctamente", Toast.LENGTH_LONG).show();
                        volverALista();
                    } else {
                        Toast.makeText(requireContext(), "Error: " + json.optString("error", "No especificado"), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "Respuesta inválida del servidor", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("REG_MASCOTA", "FAIL " + statusCode, error);
                Toast.makeText(requireContext(), "Error conexión: " + statusCode, Toast.LENGTH_LONG).show();
            }
        });
    }
    private File bitmapToFile(Bitmap bitmap, String fileName) throws Exception {
        File file = new File(requireContext().getCacheDir(), fileName);
        file.createNewFile();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, bos);
        byte[] bitmapData = bos.toByteArray();

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bitmapData);
        fos.flush();
        fos.close();

        return file;
    }
    private File uriToFile(Uri uri, String fileName) {
        try {
            InputStream inputStream = requireActivity().getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            File file = new File(requireContext().getCacheDir(), fileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buf = new byte[4096];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        try {
            File file = bitmapToFile(bitmap, "cam_temp_" + System.currentTimeMillis() + ".jpg");
            return Uri.fromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void cerrarSesion() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Seguro que desea salir?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    Context context = requireContext();
                    context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                            .edit()
                            .clear()
                            .apply();

                    Intent intent = new Intent(context, SesionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private boolean checkAndRequestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
            return false;
        }
        return true;
    }

    private boolean checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1002);
            return false;
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 || requestCode == 1002) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Permiso concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Permiso denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void volverALista() {
        cambiosPendientes = false;
        habilitarNavegacion();

        MascotaFragment mascotaFragment = new MascotaFragment();
        FragmentTransaction transaction = requireActivity()
                .getSupportFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.frameLayout, mascotaFragment);
        transaction.commit();
    }
}