package com.example.animals.Publicaciones;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.animals.R;
import com.example.animals.clases.Comentario;

import java.util.List;

public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ViewHolder> {

    private Context context;
    private List<Comentario> listaComentarios;

    public ComentarioAdapter(Context context, List<Comentario> listaComentarios) {
        this.context = context;
        this.listaComentarios = listaComentarios;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comentario, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comentario comentario = listaComentarios.get(position);

        holder.txtNombre.setText(comentario.getNombreUsuario());
        holder.txtComentario.setText(comentario.getTexto());
        holder.txtFecha.setText(comentario.getFecha());

        // Foto de perfil del usuario que comentó
        String fotoBase64 = comentario.getFotoPerfil();
        if (fotoBase64 != null && !fotoBase64.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(fotoBase64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (decodedByte != null) {
                    holder.imgPerfil.setImageBitmap(decodedByte);
                } else {
                    holder.imgPerfil.setImageResource(R.drawable.perfil); // Tu imagen por defecto
                }
            } catch (Exception e) {
                holder.imgPerfil.setImageResource(R.drawable.perfil);
            }
        } else {
            holder.imgPerfil.setImageResource(R.drawable.perfil);
        }
    }

    @Override
    public int getItemCount() {
        return listaComentarios.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPerfil;
        TextView txtNombre, txtComentario, txtFecha;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPerfil = itemView.findViewById(R.id.imgPerfilComentario);
            txtNombre = itemView.findViewById(R.id.txtNombreUsuarioComentario);
            txtComentario = itemView.findViewById(R.id.txtTextoComentario);
            txtFecha = itemView.findViewById(R.id.txtFechaComentario);
        }
    }
}