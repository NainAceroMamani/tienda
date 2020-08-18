package com.nain.tienda.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.nain.tienda.models.Google;

import java.util.HashMap;
import java.util.Map;

public class GoogleProvider {
    private CollectionReference mCollection;

    public GoogleProvider() { mCollection = FirebaseFirestore.getInstance().collection("GoogleMaps"); }

    public Task<Void> create(Google googleMaps) { return mCollection.document().set(googleMaps); }

    public Task<QuerySnapshot> getGoogleByTiendaId(String tienda_id) {
        return mCollection.whereEqualTo("tienda_id", tienda_id).get();
    }

    public Task<QuerySnapshot> getGoogleTiendas() {
        return mCollection.get();
    }

    public Task<Void> update(Google googleMaps) {
        Map<String, Object> map = new HashMap<>();
        map.put("tienda_id", googleMaps.getTienda_id());
        map.put("latitud", googleMaps.getLatitud());
        map.put("longitud", googleMaps.getLongitud());
        return mCollection.document(googleMaps.getId()).update(map);
    }
}
