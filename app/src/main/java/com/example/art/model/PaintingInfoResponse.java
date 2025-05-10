package com.example.art.model;


public class PaintingInfoResponse {
    private int id;
    private String name;
    private String author;
    private String description;
    private String thumbnailUrl;
    private String imageUrl;
    public PaintingInfoResponse() {
    }

    // All-args constructor (optional, but can be handy)
    public PaintingInfoResponse(int id, String name,String author, String description,
                                String thumbnailUrl, String imageUrl) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.imageUrl = imageUrl;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {return author;}

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "PaintingInfoResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}