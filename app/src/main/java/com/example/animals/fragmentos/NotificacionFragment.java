package com.example.animals.fragmentos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.animals.R;

import java.util.ArrayList;
import java.util.List;

public class NotificacionFragment extends Fragment {

    // Declaración de variables
    RecyclerView recyclerNotificaciones;
    LinearLayout layoutSinNotificaciones;
    ImageView btnCerrar;
    NotificacionesAdapter adapter;
    List<Notificacion> listaNotificaciones;

    public NotificacionFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notificacion, container, false);

        recyclerNotificaciones = view.findViewById(R.id.recycler_notificaciones);
        layoutSinNotificaciones = view.findViewById(R.id.layout_sin_notificaciones);
        btnCerrar = view.findViewById(R.id.btn_cerrar_notificaciones);

        recyclerNotificaciones.setLayoutManager(new LinearLayoutManager(getContext()));

        cargarNotificaciones();

        if (listaNotificaciones.isEmpty()) {
            mostrarSinNotificaciones();
        } else {
            mostrarNotificaciones();
        }
        btnCerrar.setOnClickListener(v -> cerrarFragment());
        view.setOnClickListener(v -> cerrarFragment());

        return view;
    }

    private void cargarNotificaciones() {
        listaNotificaciones = new ArrayList<>();

        listaNotificaciones.add(new Notificacion(
                "Nueva mascota registrada",
                "Canelito ha sido registrado exitosamente en PetMate",
                "Hace 5 minutos",
                false, // No leída
                android.R.drawable.ic_dialog_info
        ));

        listaNotificaciones.add(new Notificacion(
                "Recordatorio de vacuna",
                "Tu mascota Onelito tiene una vacuna pendiente para mañana",
                "Hace 2 horas",
                false, // No leída
                android.R.drawable.ic_popup_reminder
        ));

        listaNotificaciones.add(new Notificacion(
                "Nuevo seguidor",
                "Onelito comenzó a seguirte",
                "Hace 5 horas",
                true, // Ya leída
                android.R.drawable.ic_menu_add
        ));

        listaNotificaciones.add(new Notificacion(
                "Me gusta en tu publicación",
                "A Canelito le gustó tu publicación: ¡Feliz día de aventuras!",
                "Ayer",
                true, // Ya leída
                android.R.drawable.btn_star_big_on
        ));

        listaNotificaciones.add(new Notificacion(
                "Nuevo comentario",
                "Onelito comentó en tu publicación",
                "Hace 2 días",
                true, // Ya leída
                android.R.drawable.ic_dialog_email
        ));
    }

    private void mostrarNotificaciones() {
        recyclerNotificaciones.setVisibility(View.VISIBLE);
        layoutSinNotificaciones.setVisibility(View.GONE);
        adapter = new NotificacionesAdapter(listaNotificaciones);
        recyclerNotificaciones.setAdapter(adapter);
    }

    private void mostrarSinNotificaciones() {
        recyclerNotificaciones.setVisibility(View.GONE);
        layoutSinNotificaciones.setVisibility(View.VISIBLE);
    }

    private void cerrarFragment() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    public static class Notificacion {
        private String titulo;
        private String mensaje;
        private String tiempo;
        private boolean leida;
        private int tipoIcono;

        public Notificacion(String titulo, String mensaje, String tiempo, boolean leida, int tipoIcono) {
            this.titulo = titulo;
            this.mensaje = mensaje;
            this.tiempo = tiempo;
            this.leida = leida;
            this.tipoIcono = tipoIcono;
        }

        public String getTitulo() {
            return titulo;
        }

        public String getMensaje() {
            return mensaje;
        }

        public String getTiempo() {
            return tiempo;
        }

        public boolean isLeida() {
            return leida;
        }

        public void setLeida(boolean leida) {
            this.leida = leida;
        }

        public int getTipoIcono() {
            return tipoIcono;
        }
    }

    public static class NotificacionesAdapter extends RecyclerView.Adapter<NotificacionesAdapter.NotificacionViewHolder> {

        private List<Notificacion> notificaciones;

        public NotificacionesAdapter(List<Notificacion> notificaciones) {
            this.notificaciones = notificaciones;
        }

        @NonNull
        @Override
        public NotificacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notificacion, parent, false);
            return new NotificacionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NotificacionViewHolder holder, int position) {
            Notificacion notificacion = notificaciones.get(position);

            holder.txtTitulo.setText(notificacion.getTitulo());
            holder.txtMensaje.setText(notificacion.getMensaje());
            holder.txtTiempo.setText(notificacion.getTiempo());
            holder.imgIcono.setImageResource(notificacion.getTipoIcono());

            if (notificacion.isLeida()) {
                holder.indicadorNoLeida.setVisibility(View.GONE);
            } else {
                holder.indicadorNoLeida.setVisibility(View.VISIBLE);
            }

            holder.itemView.setOnClickListener(v -> {
                if (!notificacion.isLeida()) {
                    notificacion.setLeida(true);
                    notifyItemChanged(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return notificaciones.size();
        }

        static class NotificacionViewHolder extends RecyclerView.ViewHolder {
            TextView txtTitulo, txtMensaje, txtTiempo;
            ImageView imgIcono;
            View indicadorNoLeida;

            public NotificacionViewHolder(@NonNull View itemView) {
                super(itemView);
                txtTitulo = itemView.findViewById(R.id.txt_notificacion_titulo);
                txtMensaje = itemView.findViewById(R.id.txt_notificacion_mensaje);
                txtTiempo = itemView.findViewById(R.id.txt_notificacion_tiempo);
                imgIcono = itemView.findViewById(R.id.img_notificacion_icono);
                indicadorNoLeida = itemView.findViewById(R.id.indicator_no_leida);
            }
        }
    }
}