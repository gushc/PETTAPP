package com.example.animals.actividades;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.List;
import com.example.animals.R;

public class bottom_sheet_comentariosActivity extends BottomSheetDialogFragment {

    private RecyclerView recyclerViewComments;
    private EditText etComment;
    private ImageButton btnSendComment;
    private ImageButton btnCloseComments;
    private TextView tvCommentsCount;

    private String videoId;
    private int commentsCount;

    // Método estático para crear una nueva instancia
    public static bottom_sheet_comentariosActivity newInstance(String videoId, int commentsCount) {
        bottom_sheet_comentariosActivity fragment = new bottom_sheet_comentariosActivity();
        Bundle args = new Bundle();
        args.putString("videoId", videoId);
        args.putInt("commentsCount", commentsCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            videoId = getArguments().getString("videoId");
            commentsCount = getArguments().getInt("commentsCount", 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_bottom_sheet_comentarios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadComments();
    }

    private void initViews(View view) {
        recyclerViewComments = view.findViewById(R.id.recyclerViewComments);
        etComment = view.findViewById(R.id.etComment);
        btnSendComment = view.findViewById(R.id.btnSendComment);
        btnCloseComments = view.findViewById(R.id.btnCloseComments);
        tvCommentsCount = view.findViewById(R.id.tvCommentsCount);

        tvCommentsCount.setText(String.valueOf(commentsCount));
    }

    private void setupRecyclerView() {
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(getContext()));
        List<Comment> comments = getCommentsData();
        CommentsAdapter adapter = new CommentsAdapter(comments);
        recyclerViewComments.setAdapter(adapter);
    }

    private void setupListeners() {
        btnCloseComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentText = etComment.getText().toString().trim();
                if (!commentText.isEmpty()) {
                    sendComment(commentText);
                    etComment.setText("");
                }
            }
        });
    }

    private void loadComments() {
        // Aquí cargarías los comentarios desde tu backend o base de datos
        // Por ejemplo: hacer una petición HTTP, consultar Firebase, etc.
    }

    private void sendComment(String text) {
        // Aquí enviarías el comentario a tu backend
        // Por ejemplo: hacer POST a tu API, guardar en Firebase, etc.
        Toast.makeText(getContext(), "Comentario enviado: " + text, Toast.LENGTH_SHORT).show();

        // Después de enviar, podrías actualizar la lista de comentarios
        // loadComments();
    }

    private List<Comment> getCommentsData() {
        // Datos de ejemplo - Reemplazar con datos reales de tu backend
        List<Comment> comments = new ArrayList<>();
        comments.add(new Comment("1", "@usuario1", "¡Qué increíble video! Me encantó 🔥", "hace 2 horas", 24));
        comments.add(new Comment("2", "@usuario2", "Súper interesante, gracias por compartir", "hace 5 horas", 12));
        comments.add(new Comment("3", "@usuario3", "¿Dónde puedo conseguir más información sobre esto?", "hace 1 día", 8));
        comments.add(new Comment("4", "@usuario4", "Me suscribo inmediatamente", "hace 2 días", 45));
        comments.add(new Comment("5", "@usuario5", "Contenido de calidad 👏", "hace 3 días", 31));
        comments.add(new Comment("6", "@usuario6", "Esperando la segunda parte", "hace 4 días", 17));
        comments.add(new Comment("7", "@usuario7", "Excelente explicación", "hace 5 días", 22));
        return comments;
    }

    // ============================================
    // CLASE INTERNA: Comment
    // ============================================
    public static class Comment {
        private String id;
        private String username;
        private String text;
        private String time;
        private int likes;

        public Comment(String id, String username, String text, String time, int likes) {
            this.id = id;
            this.username = username;
            this.text = text;
            this.time = time;
            this.likes = likes;
        }

        public String getId() { return id; }
        public String getUsername() { return username; }
        public String getText() { return text; }
        public String getTime() { return time; }
        public int getLikes() { return likes; }
    }

    // ============================================
    // CLASE INTERNA: CommentsAdapter
    // ============================================
    public static class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

        private List<Comment> comments;

        public CommentsAdapter(List<Comment> comments) {
            this.comments = comments;
        }

        @NonNull
        @Override
        public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_comment, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
            Comment comment = comments.get(position);

            holder.tvUsername.setText(comment.getUsername());
            holder.tvCommentText.setText(comment.getText());
            holder.tvCommentTime.setText(comment.getTime());
            holder.tvCommentLikes.setText(String.valueOf(comment.getLikes()));

            holder.ivCommentLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Manejar like del comentario
                    Toast.makeText(v.getContext(), "Like al comentario", Toast.LENGTH_SHORT).show();
                }
            });

            holder.btnReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Manejar respuesta al comentario
                    Toast.makeText(v.getContext(), "Responder a " + comment.getUsername(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        // ============================================
        // CLASE INTERNA: CommentViewHolder
        // ============================================
        static class CommentViewHolder extends RecyclerView.ViewHolder {
            TextView tvUsername;
            TextView tvCommentText;
            TextView tvCommentTime;
            TextView tvCommentLikes;
            View ivCommentLike;
            TextView btnReply;

            public CommentViewHolder(@NonNull View itemView) {
                super(itemView);
                tvUsername = itemView.findViewById(R.id.tvCommentUsername);
                tvCommentText = itemView.findViewById(R.id.tvCommentText);
                tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
                tvCommentLikes = itemView.findViewById(R.id.tvCommentLikes);
                ivCommentLike = itemView.findViewById(R.id.ivCommentLike);
                btnReply = itemView.findViewById(R.id.btnCommentReply);
            }
        }
    }
}