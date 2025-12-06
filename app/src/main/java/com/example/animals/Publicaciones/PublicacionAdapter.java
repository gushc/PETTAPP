    package com.example.animals.Publicaciones;

    import android.app.AlertDialog;
    import android.content.Context;
    import android.content.SharedPreferences;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.util.Base64;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.EditText;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import com.example.animals.R;
    import com.example.animals.clases.Comentario;
    import com.loopj.android.http.AsyncHttpClient;
    import com.loopj.android.http.AsyncHttpResponseHandler;
    import com.loopj.android.http.RequestParams;

    import org.json.JSONArray;
    import org.json.JSONObject;

    import java.util.ArrayList;
    import java.util.List;

    import cz.msebera.android.httpclient.Header;

    public class PublicacionAdapter extends RecyclerView.Adapter<PublicacionAdapter.PublicacionViewHolder> {

        private Context context;
        private List<Publicacion> listaPublicaciones;

        public PublicacionAdapter(Context context, List<Publicacion> listaPublicaciones) {
            this.context = context;
            this.listaPublicaciones = listaPublicaciones;
        }

        @NonNull
        @Override
        public PublicacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_publicacion, parent, false);
            return new PublicacionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PublicacionViewHolder holder, int position) {
            Publicacion publicacion = listaPublicaciones.get(position);

            // 1. Datos del Usuario
            if (publicacion.getNombreUsuario() != null && !publicacion.getNombreUsuario().isEmpty()) {
                holder.txtNombreUsuario.setText(publicacion.getNombreUsuario());
            } else {
                holder.txtNombreUsuario.setText("Usuario");
            }

            // 2. Foto de Perfil
            String fotoPerfilStr = publicacion.getFotoPerfilUsuario();
            if (fotoPerfilStr != null && !fotoPerfilStr.isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(fotoPerfilStr, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    if (decodedByte != null) holder.imgPerfilUsuario.setImageBitmap(decodedByte);
                    else holder.imgPerfilUsuario.setImageResource(android.R.drawable.sym_def_app_icon);
                } catch (Exception e) {
                    holder.imgPerfilUsuario.setImageResource(android.R.drawable.sym_def_app_icon);
                }
            } else {
                holder.imgPerfilUsuario.setImageResource(android.R.drawable.sym_def_app_icon);
            }

            // 3. Datos de la Publicación
            if (publicacion.getFechaRegistro() != null) holder.txtFecha.setText(publicacion.getFechaRegistro());

            if (publicacion.getDescripcion() != null && !publicacion.getDescripcion().isEmpty()) {
                holder.txtContenido.setText(publicacion.getDescripcion());
                holder.txtContenido.setVisibility(View.VISIBLE);
            } else {
                holder.txtContenido.setVisibility(View.GONE);
            }

            // 4. Foto Grande
            String fotoPubBase64 = publicacion.getFotoBase64();
            if (fotoPubBase64 != null && !fotoPubBase64.isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(fotoPubBase64, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    if (decodedByte != null) {
                        holder.imgPublicacion.setImageBitmap(decodedByte);
                        holder.imgPublicacion.setVisibility(View.VISIBLE);
                    } else {
                        holder.imgPublicacion.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    holder.imgPublicacion.setVisibility(View.GONE);
                }
            } else {
                holder.imgPublicacion.setVisibility(View.GONE);
            }

            holder.txtContadorLikes.setText(publicacion.getCantidadLikes() + " Me gusta");
            holder.txtContadorComentarios.setText(publicacion.getCantidadComentarios() + " Comentarios");

            holder.btnComentar.setOnClickListener(v -> mostrarDialogoComentario(v.getContext(), publicacion.getIdPublicacion(), position));
        }


        private void mostrarDialogoComentario(Context activityContext, int idPublicacion, int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
            LayoutInflater inflater = LayoutInflater.from(activityContext);

            // Inflamos el diseño del diálogo
            View dialogView = inflater.inflate(R.layout.dialog_comentarios, null);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();

            // Vincular vistas del diálogo
            RecyclerView recyclerComentarios = dialogView.findViewById(R.id.recyclerComentarios);
            EditText edtComentario = dialogView.findViewById(R.id.edtComentario);
            ImageView btnEnviar = dialogView.findViewById(R.id.btnEnviarComentario);

            if (recyclerComentarios == null || edtComentario == null || btnEnviar == null) {
                Toast.makeText(activityContext, "Error en diseño de diálogo", Toast.LENGTH_SHORT).show();
                return;
            }

            // Configurar lista de comentarios
            recyclerComentarios.setLayoutManager(new LinearLayoutManager(activityContext));
            List<Comentario> listaComentarios = new ArrayList<>();
            ComentarioAdapter adapterComentarios = new ComentarioAdapter(activityContext, listaComentarios);
            recyclerComentarios.setAdapter(adapterComentarios);

            cargarComentarios(idPublicacion, listaComentarios, adapterComentarios);

            btnEnviar.setOnClickListener(v -> {
                String texto = edtComentario.getText().toString().trim();
                if (!texto.isEmpty()) {

                    enviarComentario(activityContext, idPublicacion, texto, listaComentarios, adapterComentarios, edtComentario, position);
                } else {
                    Toast.makeText(activityContext, "Escribe un comentario", Toast.LENGTH_SHORT).show();
                }
            });

            dialog.show();
        }

        private void cargarComentarios(int idPublicacion, List<Comentario> lista, ComentarioAdapter adapter) {
            String url = "http://miguelcardenas.atwebpages.com/proyecto/listarComentarios.php";
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            params.put("idPublicacion", idPublicacion);

            client.post(url, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    try {
                        String resp = new String(responseBody);
                        JSONArray jsonArray = new JSONArray(resp);
                        lista.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            lista.add(new Comentario(
                                    obj.optInt("idComentario"),
                                    obj.optInt("idUsuario"),
                                    obj.optString("nombreUsuario"),
                                    obj.optString("fotoPerfil", ""),
                                    obj.optString("comentario"),
                                    obj.optString("fecha")
                            ));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {}
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {}
            });
        }

        private void enviarComentario(Context ctx, int idPublicacion, String comentario, List<Comentario> lista, ComentarioAdapter adapter, EditText edt, int position) {
            SharedPreferences prefs = ctx.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            int idUsuario = prefs.getInt("idUsuario", -1);

            if (idUsuario == -1) {
                Toast.makeText(ctx, "Inicia sesión primero", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = "http://miguelcardenas.atwebpages.com/proyecto/comentarPublicacion.php";
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            params.put("idPublicacion", idPublicacion);
            params.put("idUsuario", idUsuario);
            params.put("comentario", comentario);

            client.post(url, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    try {
                        JSONObject json = new JSONObject(new String(responseBody));
                        if (json.optBoolean("success")) {
                            Toast.makeText(ctx, "Comentario enviado", Toast.LENGTH_SHORT).show();
                            edt.setText(""); // Limpiar campo
                            cargarComentarios(idPublicacion, lista, adapter); // Refrescar lista del diálogo

                            // 🔴 AQUÍ ESTÁ LA MAGIA: Actualizar contador en la lista principal
                            Publicacion p = listaPublicaciones.get(position);
                            p.setCantidadComentarios(p.getCantidadComentarios() + 1); // Sumamos 1
                            notifyItemChanged(position); // Le decimos al adaptador que redibuje esa fila

                        } else {
                            Toast.makeText(ctx, "Error: " + json.optString("error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {}
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(ctx, "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return listaPublicaciones.size();
        }

        // --- VIEWHOLDER ---
        public static class PublicacionViewHolder extends RecyclerView.ViewHolder {
            TextView txtNombreUsuario, txtFecha, txtContenido;
            TextView txtContadorLikes, txtContadorComentarios;
            ImageView imgPerfilUsuario, imgPublicacion, btnMasOpciones;
            LinearLayout btnMeGusta, btnComentar, btnCompartir;

            public PublicacionViewHolder(@NonNull View itemView) {
                super(itemView);
                txtNombreUsuario = itemView.findViewById(R.id.txt_nombre_usuario);
                txtFecha = itemView.findViewById(R.id.txt_fecha_publicacion);
                imgPerfilUsuario = itemView.findViewById(R.id.img_perfil_usuario);
                btnMasOpciones = itemView.findViewById(R.id.btn_mas_opciones);
                txtContenido = itemView.findViewById(R.id.txt_contenido_publicacion);
                imgPublicacion = itemView.findViewById(R.id.img_publicacion);
                txtContadorLikes = itemView.findViewById(R.id.txt_contador_likes);
                txtContadorComentarios = itemView.findViewById(R.id.txt_contador_comentarios);
                btnMeGusta = itemView.findViewById(R.id.btn_me_gusta);
                btnComentar = itemView.findViewById(R.id.btn_comentar);
                btnCompartir = itemView.findViewById(R.id.btn_compartir);
            }
        }
    }