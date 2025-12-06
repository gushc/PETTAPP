package com.example.animals.Mascota;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.animals.R;

import java.util.List;

public class MascotaAdapter extends RecyclerView.Adapter<MascotaAdapter.ViewHolder> {

    private List<MascotaModel> listaMascotas;
    private OnItemClickListener listener;
    private Context context;

    // 1. INTERFAZ (Para Eliminar y Editar)
    public interface OnItemClickListener {
        void onDeleteClick(MascotaModel mascota, int position);
        void onEditClick(MascotaModel mascota);
        void onViewClick(MascotaModel mascota);
    }

    // CONSTRUCTOR
    public MascotaAdapter(Context context, List<MascotaModel> listaMascotas, OnItemClickListener listener) {
        this.context = context;
        this.listaMascotas = listaMascotas;
        this.listener = listener;
    }

    public void actualizarLista(List<MascotaModel> nuevaLista) {
        this.listaMascotas = nuevaLista;
        notifyDataSetChanged();
    }

    // Método para eliminar visualmente la fila
    public void removerItem(int position) {
        if (position >= 0 && position < listaMascotas.size()) {
            listaMascotas.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public MascotaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mascotas, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MascotaAdapter.ViewHolder holder, int position) {
        MascotaModel mascota = listaMascotas.get(position);

        holder.nombre.setText(mascota.getNombre());
        holder.especie.setText(mascota.getEspecie() + " - " + mascota.getRaza());

        // 👇 DIAGNÓSTICO MANUAL (SIN GLIDE) 👇
        String base64Image = mascota.getFotoBase64();

        if (base64Image == null || base64Image.isEmpty()) {
            Log.e("FOTO_TEST", "❌ La foto viene VACÍA o NULA para: " + mascota.getNombre());
            holder.imagen.setImageResource(R.drawable.ic_launcher_foreground);
        } else {
            try {
                // 1. Ver qué estamos recibiendo (los primeros 20 caracteres)
                String preview = base64Image.length() > 20 ? base64Image.substring(0, 20) : base64Image;
                Log.d("FOTO_TEST", "🔍 Datos recibidos (" + base64Image.length() + " chars): " + preview + "...");

                // 2. Intentar convertir texto a bytes
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                Log.d("FOTO_TEST", "✅ Conversión a bytes EXITOSA. Tamaño: " + decodedString.length);

                // 3. Intentar crear imagen (Bitmap) desde los bytes
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                if (decodedByte != null) {
                    Log.d("FOTO_TEST", "🎉 ¡IMAGEN CREADA CON ÉXITO! (Ancho: " + decodedByte.getWidth() + ")");
                    holder.imagen.setImageBitmap(decodedByte); // Poner la imagen directamente
                } else {
                    Log.e("FOTO_TEST", "💀 Los bytes existen, pero NO SON UNA IMAGEN válida.");
                    holder.imagen.setImageResource(R.drawable.ic_launcher_foreground);
                }

            } catch (Exception e) {
                Log.e("FOTO_TEST", "💥 CRASH al convertir: " + e.getMessage());
                e.printStackTrace();
                holder.imagen.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewClick(mascota);
            }
        });

        // Listeners de botones (igual que antes)
        holder.btnEliminar.setOnClickListener(v -> { if (listener != null) listener.onDeleteClick(mascota, position); });
        holder.btnEditar.setOnClickListener(v -> { if (listener != null) listener.onEditClick(mascota); });
    }

    @Override
    public int getItemCount() {
        return listaMascotas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView nombre, especie;
        ImageView imagen, btnEliminar, btnEditar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nombre = itemView.findViewById(R.id.TxtNombreMascotaItem);
            especie = itemView.findViewById(R.id.TxtRazaItem);
            imagen = itemView.findViewById(R.id.imgMascotaItem);

            // Botones
            btnEliminar = itemView.findViewById(R.id.btnEliminarMascota);
            btnEditar = itemView.findViewById(R.id.btnEditarMascota);
        }
    }
}