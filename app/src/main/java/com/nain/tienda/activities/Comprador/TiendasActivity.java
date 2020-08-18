package com.nain.tienda.activities.Comprador;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.nain.tienda.R;
import com.nain.tienda.adapters.TiendaAdapter;
import com.nain.tienda.models.Tienda;
import com.nain.tienda.providers.TiendaProvider;

public class TiendasActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    Toolbar mToolbar;
    TiendaAdapter tiendaAdapter;
    TiendaProvider tiendaProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tiendas);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("TIENDAS");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tiendaProvider = new TiendaProvider();
        mRecyclerView = findViewById(R.id.recyclerViewTienda);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Query query = tiendaProvider.getAll();
        FirestoreRecyclerOptions<Tienda> options = new FirestoreRecyclerOptions.Builder<Tienda>()
                .setQuery(query, Tienda.class)
                .build();

        tiendaAdapter = new TiendaAdapter(options, this);
        mRecyclerView.setAdapter(tiendaAdapter);
        tiendaAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        tiendaAdapter.stopListening();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }
}
