package com.example.animals.Usuario;

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
import com.example.animals.clases.UsuarioBusqueda;
import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private Context context;
    private List<UsuarioBusqueda> lista;

    public UsuarioAdapter(Context context, List<UsuarioBusqueda> lista) {
        this.context = context;
        this.lista = lista;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_usuario_busqueda, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        UsuarioBusqueda user = lista.get(position);
        holder.txtNombre.setText(user.getNombreCompleto());

        // Decodificar Foto
        if (user.getFotoBase64() != null && !user.getFotoBase64().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(user.getFotoBase64(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.imgPerfil.setImageBitmap(decodedByte);
            } catch (Exception e) {
                holder.imgPerfil.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } else {
            holder.imgPerfil.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // AQUÍ PUEDES AGREGAR EL CLICK PARA IR AL PERFIL DE ESA PERSONA
        holder.itemView.setOnClickListener(v -> {
            // Intent para ir al perfil del usuario...
        });
    }

    @Override
    public int getItemCount() { return lista.size(); }

    public static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPerfil;
        TextView txtNombre;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            // Asegúrate de crear un layout 'item_usuario_busqueda.xml' con estos IDs
            imgPerfil = itemView.findViewById(R.id.imgPerfilBusqueda);
            txtNombre = itemView.findViewById(R.id.txtNombreBusqueda);
        }
    }
}
