package com.example.animals.recomendaciones;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.animals.R;
import com.example.animals.clases.Recomendacion;

import java.util.List;

public class RecomendacionAdapter extends RecyclerView.Adapter<RecomendacionAdapter.ViewHolder> {

    private Context context;
    private List<Recomendacion> lista;
    private OnItemClickListener listener;

    private boolean isEditable;

    public interface OnItemClickListener {
        void onItemClick(Recomendacion item);
        void onEditarClick(Recomendacion item);
        void onEliminarClick(Recomendacion item);
    }

    public RecomendacionAdapter(Context context, List<Recomendacion> lista, boolean isEditable, OnItemClickListener listener) {
        this.context = context;
        this.lista = lista;
        this.isEditable = isEditable;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recomendacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recomendacion item = lista.get(position);

        holder.txtTitulo.setText(item.getTitulo());
        holder.txtDesc.setText(item.getDescripcion());
        holder.txtCat.setText(item.getCategoria());
        holder.txtAutor.setText("Por: " + item.getAutor());

        if (isEditable) {
            holder.layoutEdicion.setVisibility(View.VISIBLE);
            holder.btnMapa.setVisibility(View.GONE);

            holder.btnEditar.setOnClickListener(v -> {
                if (listener != null) listener.onEditarClick(item);
            });

            holder.btnEliminar.setOnClickListener(v -> {
                if (listener != null) listener.onEliminarClick(item);
            });

        } else {
            holder.layoutEdicion.setVisibility(View.GONE);    // Ocultar Editar/Eliminar

            if (item.getLatitud() != 0.0 && item.getLongitud() != 0.0) {
                holder.btnMapa.setVisibility(View.VISIBLE);
                holder.btnMapa.setOnClickListener(v -> abrirMapa(item));
            } else {
                holder.btnMapa.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });

        cargarFoto(holder, item.getFotoUrl());
    }

    private void abrirMapa(Recomendacion item) {
        try {
            double lat = item.getLatitud();
            double lon = item.getLongitud();
            String label = Uri.encode(item.getTitulo());

            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + lat + "," + lon + "(" + label + ")");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            try {
                context.startActivity(mapIntent);
            } catch (Exception e) {
                Uri browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + lat + "," + lon);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
                context.startActivity(browserIntent);
            }
        } catch (Exception ex) {
            Toast.makeText(context, "No se pudo abrir el mapa", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarFoto(ViewHolder holder, String fotoString) {
        if (fotoString != null && !fotoString.isEmpty()) {
            try {
                if (fotoString.contains(",")) {
                    fotoString = fotoString.substring(fotoString.indexOf(",") + 1);
                }
                byte[] decodedString = Base64.decode(fotoString, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.imgFoto.setImageBitmap(decodedByte);
            } catch (Exception e) {
                holder.imgFoto.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.imgFoto.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtDesc, txtCat, txtAutor;
        ImageView imgFoto;
        Button btnMapa;

        LinearLayout layoutEdicion;
        Button btnEditar, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTituloItem);
            txtDesc = itemView.findViewById(R.id.txtDescripcionItem);
            txtCat = itemView.findViewById(R.id.txtCategoriaItem);
            txtAutor = itemView.findViewById(R.id.txtAutorItem);
            imgFoto = itemView.findViewById(R.id.imgFotoItem);
            btnMapa = itemView.findViewById(R.id.btnVerMapaItem);

            layoutEdicion = itemView.findViewById(R.id.layoutBotonesEdicion);
            btnEditar = itemView.findViewById(R.id.btnEditarItem);
            btnEliminar = itemView.findViewById(R.id.btnEliminarItem);
        }
    }
}