package com.ong.donationexcel.model;

public enum CategoriaEnum {
    ALIMENTOS("Alimentos"),
    MEDICAMENTO("Medicamento"),
    HIGIENE("Higiene"),
    ROPA("Ropa"),
    JUGUETES("Juguetes"),
    UTILES_ESCOLARES("Útiles Escolares");

    private final String descripcion;

    CategoriaEnum(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}