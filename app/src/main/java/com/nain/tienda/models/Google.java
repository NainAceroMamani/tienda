package com.nain.tienda.models;

public class Google {
    private String id;
    private String latitud;
    private String longitud;
    private String tienda_id;

    public Google() {
    }

    public Google(String id, String latitud, String longitud, String tienda_id) {
        this.id = id;
        this.latitud = latitud;
        this.longitud = longitud;
        this.tienda_id = tienda_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public String getTienda_id() {
        return tienda_id;
    }

    public void setTienda_id(String tienda_id) {
        this.tienda_id = tienda_id;
    }
}
