package com.nain.tienda.providers;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nain.tienda.models.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UsersProvider {

    private CollectionReference mCollection;

    public UsersProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Users");
    }

    public Task<Void> create(User user) {
        user.setTienda_id(null);
        return mCollection.document(user.getId()).set(user);
    }

    public Task<DocumentSnapshot> getUser(String id) {
        return mCollection.document(id).get();
    }

    public Task<Void> update(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("username", user.getUsername());
        map.put("timestamp", new Date().getTime());
        map.put("image_profile", user.getImage_profile());
        return mCollection.document(user.getId()).update(map);
    }

    public Task<Void> updateSinFoto(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("username", user.getUsername());
        map.put("timestamp", new Date().getTime());
        return mCollection.document(user.getId()).update(map);
    }

    public Task<Void> updateLocal(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("tienda_id", user.getTienda_id());
        map.put("timestamp", new Date().getTime());
        return mCollection.document(user.getId()).update(map);
    }
}
