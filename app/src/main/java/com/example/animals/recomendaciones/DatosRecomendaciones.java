package com.example.animals.recomendaciones;

import java.util.Map;

public class DatosRecomendaciones {
    public String id;
    public String title;
    public String description;
    public String photoUrl;
    public String category;
    public long timestamp;
    public int likesCount;
    public int commentsCount;
    public Map<String, Boolean> likesByUser;
    public Map<String, Boolean> favoritesByUser;
    public Double latitude; // puede venir por EXIF o por ubicación
    public Double longitude;

    public DatosRecomendaciones() {}
}
