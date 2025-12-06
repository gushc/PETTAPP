/*package com.example.animals.fragmentos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.animals.R;
import com.example.animals.actividades.bottom_sheet_comentariosActivity;

public class ShortFragment extends Fragment {

    // Views
    private ViewPager2 viewPagerShorts;
    private View btnComment;
    private View btnLike;
    private View btnShare;
    private View btnMore;
    private TextView tvLikeCount;
    private TextView tvCommentCount;
    private TextView tvUsername;
    private TextView tvDescription;
    private Button btnFollow;
    private ImageButton btnSearch;

    // Datos del video
    private String currentVideoId = "video_123";
    private int currentCommentsCount = 523;

    public ShortFragment() {
        // Constructor vacío requerido
    }

    public static ShortFragment newInstance() {
        return new ShortFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mascota, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inicializarVistas(view);
        configurarEventos();
    }

    private void inicializarVistas(View view) {
        viewPagerShorts = view.findViewById(R.id.viewPagerShorts);
        btnLike = view.findViewById(R.id.btnLike);
        btnComment = view.findViewById(R.id.btnComment);
        btnShare = view.findViewById(R.id.btnShare);
        btnMore = view.findViewById(R.id.btnMore);
        tvLikeCount = view.findViewById(R.id.tvLikeCount);
        tvCommentCount = view.findViewById(R.id.tvCommentCount);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvDescription = view.findViewById(R.id.tvDescription);
        btnFollow = view.findViewById(R.id.btnFollow);
        btnSearch = view.findViewById(R.id.btnSearch);
    }

    private void configurarEventos() {
        // EVENTO LIKE
        if (btnLike != null) {
            btnLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "❤️ Like", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // EVENTO COMENTARIOS - Igual que en PerfilUsuarioFragment
        if (btnComment != null) {
            btnComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Abrir BottomSheet de comentarios
                    bottom_sheet_comentariosActivity bottomSheet =
                            bottom_sheet_comentariosActivity.newInstance(currentVideoId, currentCommentsCount);
                    bottomSheet.show(getChildFragmentManager(), "comentarios");
                }
            });
        }

        // EVENTO COMPARTIR
        if (btnShare != null) {
            btnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "📤 Compartir", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // EVENTO MÁS OPCIONES
        if (btnMore != null) {
            btnMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "⋮ Más opciones", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // EVENTO SEGUIR
        if (btnFollow != null) {
            btnFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (btnFollow.getText().toString().equals("Seguir")) {
                        btnFollow.setText("Siguiendo");
                        Toast.makeText(getContext(), "✓ Siguiendo", Toast.LENGTH_SHORT).show();
                    } else {
                        btnFollow.setText("Seguir");
                        Toast.makeText(getContext(), "✗ Dejaste de seguir", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // EVENTO BUSCAR
        if (btnSearch != null) {
            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "🔍 Buscar", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // CONFIGURAR VIEWPAGER
        if (viewPagerShorts != null) {
            viewPagerShorts.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
            viewPagerShorts.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    // Actualizar datos del video
                    currentVideoId = "video_" + position;
                    currentCommentsCount = 100 + (position * 50);
                    if (tvCommentCount != null) {
                        tvCommentCount.setText(String.valueOf(currentCommentsCount));
                    }
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias
        viewPagerShorts = null;
        btnLike = null;
        btnComment = null;
        btnShare = null;
        btnMore = null;
        tvLikeCount = null;
        tvCommentCount = null;
        tvUsername = null;
        tvDescription = null;
        btnFollow = null;
        btnSearch = null;
    }
}*/