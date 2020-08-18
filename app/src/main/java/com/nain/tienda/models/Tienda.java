package com.nain.tienda.models;

public class Tienda {

    private String id;
    private String nombre;
    private String pagina_url;
    private String telefono;
    private String url_imagen;
    private String correo;
    private String descripcion;

    public Tienda() {
    }

    public Tienda(String id, String nombre, String pagina_url, String telefono, String url_imagen, String correo, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.pagina_url = pagina_url;
        this.telefono = telefono;
        this.url_imagen = url_imagen;
        this.correo = correo;
        this.descripcion = descripcion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPagina_url() {
        return pagina_url;
    }

    public void setPagina_url(String pagina_url) {
        this.pagina_url = pagina_url;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getUrl_imagen() {
        return url_imagen;
    }

    public void setUrl_imagen(String url_imagen) {
        this.url_imagen = url_imagen;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
