package com.nain.tienda.models;

public class User {

    private String id;
    private String email;
    private String username;
    private String role;
    private String image_profile;
    private String tienda_id;
    private long timestamp;

    public User() {

    }

    public User(String id, String email, String username, String role, String image_profile,String tienda_id, long timestamp) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.role = role;
        this.image_profile = image_profile;
        this.tienda_id = tienda_id;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTienda_id() {
        return tienda_id;
    }

    public void setTienda_id(String tienda_id) {
        this.tienda_id = tienda_id;
    }

    public String getImage_profile() { return image_profile; }

    public void setImage_profile(String image_profile) {
        this.image_profile = image_profile;
    }
}
