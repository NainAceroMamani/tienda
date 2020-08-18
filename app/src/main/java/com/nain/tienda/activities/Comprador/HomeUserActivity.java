package com.nain.tienda.activities.Comprador;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.nain.tienda.R;
import com.nain.tienda.activities.LoginActivity;
import com.nain.tienda.activities.ProfileActivity;
import com.nain.tienda.providers.AuthProvider;
import com.nain.tienda.providers.UsersProvider;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeUserActivity extends AppCompatActivity {

    CardView cardViewTiendas, cardViewGoogle, cardViewprofile, cardViewLogout;
    AuthProvider mAuthProvider;
    Toolbar mToolbar;
    UsersProvider mUserProvider;
    String mUsername, mEmail;
    String mImageProfile = "";
    TextView mTxt_name, mTxt_email;
    CircleImageView mCircleImagePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user);

        cardViewTiendas = findViewById(R.id.cardViewTiendas);
        cardViewGoogle = findViewById(R.id.cardViewGoogle);
        cardViewprofile = findViewById(R.id.cardViewProfile);
        cardViewLogout = findViewById(R.id.cardViewSalir);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("BIENVENIDO");

        mUserProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();

        mCircleImagePhoto = findViewById(R.id.imageViewProfile);
        mTxt_name = findViewById(R.id.txt_name);
        mTxt_email = findViewById(R.id.txt_email);

        cargarUsuario();

        cardViewTiendas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeUserActivity.this, TiendasActivity.class);
                startActivity(intent);
            }
        });

        cardViewGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeUserActivity.this, GoogleMapsActivity.class);
                startActivity(intent);
            }
        });

        cardViewprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeUserActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        cardViewLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void cargarUsuario() {
        mUserProvider.getUser(mAuthProvider.getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    if(documentSnapshot.contains("username")){
                        mUsername = documentSnapshot.getString("username");
                        if(mUsername != null){
                            mTxt_name.setText(mUsername);
                        }else{
                            mTxt_name.setText("USUARIO");
                        }
                    }
                    if(documentSnapshot.contains("email")){
                        mEmail = documentSnapshot.getString("email");
                        mTxt_email.setText(mEmail);
                    }
                    if(documentSnapshot.contains("image_profile")){
                        mImageProfile = documentSnapshot.getString("image_profile");
                        if(mImageProfile != null){
                            if(!mImageProfile.isEmpty()) {
                                Picasso.with(HomeUserActivity.this).load(mImageProfile).into(mCircleImagePhoto);
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.itemProfile){
            Intent intent = new Intent(HomeUserActivity.this, ProfileActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.itemLogout) {
            logout();
        }

        return true;
    }

    private void logout() {
        mAuthProvider.logout();
        Intent intent = new Intent(HomeUserActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
