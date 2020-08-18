package com.nain.tienda.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.nain.tienda.models.Tienda;

import java.util.HashMap;
import java.util.Map;

public class TiendaProvider {
    private CollectionReference mCollection;

    public TiendaProvider() { mCollection = FirebaseFirestore.getInstance().collection("Tiendas"); }

    public Task<Void> create(Tienda tienda) {
        return mCollection.document(tienda.getId()).set(tienda);
    }

    public Query getAll() {
        return mCollection.orderBy("nombre", Query.Direction.DESCENDING);
    }

    public Task<DocumentSnapshot> getTiendaById(String id) {
        return mCollection.document(id).get();
    }

    public Task<Void> update(Tienda tienda) {
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", tienda.getNombre());
        map.put("descripcion", tienda.getDescripcion());
        map.put("url_imagen", tienda.getUrl_imagen());
        map.put("pagina_url", tienda.getPagina_url());
        map.put("telefono", tienda.getTelefono());
        map.put("correo", tienda.getCorreo());
        return mCollection.document(tienda.getId()).update(map);
    }

    public Task<Void> updateSinFoto(Tienda tienda) {
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", tienda.getNombre());
        map.put("descripcion", tienda.getDescripcion());
        map.put("pagina_url", tienda.getPagina_url());
        map.put("telefono", tienda.getTelefono());
        map.put("correo", tienda.getCorreo());
        return mCollection.document(tienda.getId()).update(map);
    }
}
