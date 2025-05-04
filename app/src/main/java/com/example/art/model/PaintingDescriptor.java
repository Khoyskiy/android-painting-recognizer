package com.example.art.model;

public class PaintingDescriptor {
    public int id;
    public String name;
    public byte[] descriptor;

    public PaintingDescriptor(int id, String name, byte[] descriptor) {
        this.id = id;
        this.name = name;
        this.descriptor = descriptor;
    }
}
