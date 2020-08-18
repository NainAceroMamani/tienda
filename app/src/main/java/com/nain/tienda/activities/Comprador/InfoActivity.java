package com.nain.tienda.activities.Comprador;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nain.tienda.R;
import com.nain.tienda.providers.GoogleProvider;
import com.nain.tienda.providers.TiendaProvider;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class InfoActivity extends AppCompatActivity {

    String mExtraTiendaId, nombre, descripcion, correo, latitud, longitud, pagina_url, telefono;
    CircleImageView circleImageViewTienda, circleImageBack;
    TiendaProvider tiendaProvider;
    GoogleProvider googleProvider;
    Button buttonPagina, buttonWhatsApp, buttonGoogle, buttonCorreo;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        buttonPagina = findViewById(R.id.btnPaginaWeb);
        buttonWhatsApp = findViewById(R.id.btnCall);
        buttonCorreo = findViewById(R.id.btnCorreo);
        buttonGoogle = findViewById(R.id.btnGoogleMaps);
        circleImageViewTienda = findViewById(R.id.imageViewTienda);
        circleImageBack = findViewById(R.id.circleImageBack);

        tiendaProvider = new TiendaProvider();
        googleProvider = new GoogleProvider();
        mExtraTiendaId =  getIntent().getStringExtra("id");

        cargarTienda();

        buttonPagina.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pagina();
            }
        });

        buttonWhatsApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WhatsApp();
            }
        });

        buttonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Google();
            }
        });

        buttonCorreo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Correo();
            }
        });

        circleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void cargarTienda(){
        if(mExtraTiendaId != null) {
            tiendaProvider.getTiendaById(mExtraTiendaId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()){
                        if(documentSnapshot.contains("url_imagen")){
                            String image = documentSnapshot.getString("url_imagen");
                            if(!image.isEmpty()){
                                Picasso.with(InfoActivity.this).load(image).into(circleImageViewTienda);
                            }
                        }
                        if(documentSnapshot.contains("nombre")){
                            nombre = documentSnapshot.getString("nombre");
                        }
                        if(documentSnapshot.contains("descripcion")){
                            descripcion = documentSnapshot.getString("descripcion");
                        }
                        if(documentSnapshot.contains("correo")){
                            correo = documentSnapshot.getString("correo");
                        }
                        if(documentSnapshot.contains("pagina_url")){
                            pagina_url = documentSnapshot.getString("pagina_url");
                        }
                        if(documentSnapshot.contains("telefono")){
                            telefono = documentSnapshot.getString("telefono");
                        }
                        cargarGoogleMaps();
                    }
                }
            });
        }
    }

    private void cargarGoogleMaps(){
        if(mExtraTiendaId != null) {
            googleProvider.getGoogleByTiendaId(mExtraTiendaId).addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if(!queryDocumentSnapshots.isEmpty()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                            latitud = queryDocumentSnapshot.getString("latitud");
                            longitud = queryDocumentSnapshot.getString("longitud");
                        }
                    }
                }
            });
        }
    }

    private void Pagina(){
        uri = Uri.parse(pagina_url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void WhatsApp(){
        try {
            uri = Uri.parse("smsto:" + telefono);
            Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
            intent.putExtra(Intent.EXTRA_TEXT, "Más Información de la tienda " + nombre);
            intent.setPackage("com.whatsapp");
            startActivity(Intent.createChooser(intent, ""));
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not Installed", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void Google(){
        try {
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr="+"-18.056441891181393"+","+"-70.25109359989592"+"&daddr="+latitud+","+longitud));
            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Google Maps not Installed", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void Correo(){
        try {
            Intent intent1 = new Intent(Intent.ACTION_SEND);
            intent1.setType("text/plain");
            intent1.putExtra(Intent.EXTRA_SUBJECT, nombre);
            intent1.putExtra(Intent.EXTRA_TEXT, descripcion);
            intent1.putExtra(Intent.EXTRA_EMAIL, new String[]{correo});
            startActivity(intent1);
        } catch (Exception e) {
            Toast.makeText(this, "Ocurrio un Problema", Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
