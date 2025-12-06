package com.example.animals.fragmentos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.animals.R;

public class ReportarFragment extends Fragment {

    // Declaración de variables
    RadioGroup radioGroupMotivos;
    RadioButton radioSpam, radioInapropiado, radioMaltrato, radioAcoso, radioFalsaInfo, radioOtro;
    EditText txtDetalleReporte;
    Button btnCancelar, btnEnviar;
    ImageView btnCerrar;

    // Variables para identificar qué se está reportando
    String tipoReporte; // "publicacion", "comentario", "usuario", etc.
    String idElemento;  // ID del elemento reportado

    public ReportarFragment() {
        // Constructor vacío requerido
    }

    // Constructor con parámetros para reutilizar el fragment
    public static ReportarFragment newInstance(String tipo, String id) {
        ReportarFragment fragment = new ReportarFragment();
        Bundle args = new Bundle();
        args.putString("tipo", tipo);
        args.putString("id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reportar, container, false);

        // Obtener argumentos
        if (getArguments() != null) {
            tipoReporte = getArguments().getString("tipo", "publicacion");
            idElemento = getArguments().getString("id", "");
        }

        // Referenciar vistas
        radioGroupMotivos = view.findViewById(R.id.radio_group_motivos);
        radioSpam = view.findViewById(R.id.radio_spam);
        radioInapropiado = view.findViewById(R.id.radio_inapropiado);
        radioMaltrato = view.findViewById(R.id.radio_maltrato);

        radioFalsaInfo = view.findViewById(R.id.radio_falsa_info);
        radioOtro = view.findViewById(R.id.radio_otro);
        txtDetalleReporte = view.findViewById(R.id.txt_detalle_reporte);
        btnCancelar = view.findViewById(R.id.btn_cancelar_reporte);
        btnEnviar = view.findViewById(R.id.btn_enviar_reporte);
        btnCerrar = view.findViewById(R.id.btn_cerrar_reporte);

        // Configurar eventos
        btnCerrar.setOnClickListener(v -> cerrarFragment());
        btnCancelar.setOnClickListener(v -> cerrarFragment());
        btnEnviar.setOnClickListener(v -> enviarReporte());

        // Cerrar al tocar fuera
        view.setOnClickListener(v -> cerrarFragment());

        return view;
    }

    private void enviarReporte() {
        // Validar que se haya seleccionado un motivo
        int selectedId = radioGroupMotivos.getCheckedRadioButtonId();

        if (selectedId == -1) {
            Toast.makeText(getContext(), "Por favor selecciona un motivo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener el motivo seleccionado
        RadioButton radioSeleccionado = requireView().findViewById(selectedId);
        String motivo = radioSeleccionado.getText().toString();
        String detalle = txtDetalleReporte.getText().toString().trim();

        // AQUÍ CONECTARÍAS CON TU BASE DE DATOS O API
        guardarReporte(motivo, detalle);
    }

    private void guardarReporte(String motivo, String detalle) {
        // TODO: Implementar guardado en base de datos
        // Ejemplo de lo que harías:
        // - Guardar en Firebase/SQLite
        // - Enviar notificación al administrador
        // - Registrar el reporte con fecha y usuario

        Toast.makeText(getContext(),
                "Reporte enviado correctamente\nTipo: " + tipoReporte + "\nMotivo: " + motivo,
                Toast.LENGTH_LONG).show();

        // Cerrar el fragment después de reportar
        cerrarFragment();
    }

    private void cerrarFragment() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}