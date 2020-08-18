package com.nain.tienda.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.UploadTask;
import com.nain.tienda.R;
import com.nain.tienda.activities.Comprador.HomeUserActivity;
import com.nain.tienda.activities.Vendedor.HomeAdminActivity;
import com.nain.tienda.models.User;
import com.nain.tienda.providers.AuthProvider;
import com.nain.tienda.providers.ImageProvider;
import com.nain.tienda.providers.UsersProvider;
import com.nain.tienda.utils.FileUtil;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView circleImageBack, mCircleImageViewProfile;
    EditText txtInputNombre, txtInputEmail;
    AlertDialog.Builder mBuilderSelector;
    CharSequence options[];
    File mImageFile;
    AuthProvider mAuthProvider;
    UsersProvider mUserProvider;
    private final int GALLERY_REQUEST_CODE = 1;
    private final int PHOTO_REQUEST_CODE = 2;

    Button mBtnProfile;
    ImageProvider mImageProvider;
    String mUsername, mEmail, url;
    AlertDialog mDialog;
    String mImageProfile = "";
    Boolean bandera = false;

    String mAbsolutePhotoPath;
    String mPhotoPath;
    File mPhotoFile;
    String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mCircleImageViewProfile =findViewById(R.id.imageViewProfile);
        txtInputNombre = findViewById(R.id.txtInputNombre);
        txtInputEmail = findViewById(R.id.txtInputCorreo);
        mBtnProfile = findViewById(R.id.btnProfileUpdate);
        mImageProvider = new ImageProvider();
        mAuthProvider = new AuthProvider();
        mUserProvider = new UsersProvider();

        circleImageBack = findViewById(R.id.circleImageBack);

        mBuilderSelector = new AlertDialog.Builder(this);
        mBuilderSelector.setTitle("Selecciona una opcion");
        options = new CharSequence[] {"Imagen de galeria", "Tomar foto"};

        getUser();

        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Espere un momento")
                .setCancelable(false).build();

        View parentLayout = findViewById(android.R.id.content);
        Snackbar.make(parentLayout, "Presione la Imagen para Actualizar la Foto", Snackbar.LENGTH_LONG).show();

        circleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(role.equals("VENDEDOR")){
                    Intent intent = new Intent(ProfileActivity.this, HomeAdminActivity.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(ProfileActivity.this, HomeUserActivity.class);
                    startActivity(intent);
                }
            }
        });

        mCircleImageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bandera = true;
                selectOptionImage(GALLERY_REQUEST_CODE);
            }
        });

        mBtnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });
    }

    private void saveProfile(){
        mUsername = txtInputNombre.getText().toString();
        if(!mUsername.isEmpty()){
            if(mImageFile != null && bandera){
                saveImage(mImageFile);
            }
            else if(mPhotoFile != null && bandera) {
                saveImage(mPhotoFile);
            }else{
                mDialog.show();
                User user = new User();
                user.setUsername(mUsername);
                user.setId(mAuthProvider.getUid());
                updateInfoSinFoto(user);
            }
            this.bandera = false;
        }else{
            Toast.makeText(this, "Complete los campos", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImage(File imagenFile){
        mDialog.show();
        mImageProvider.save(this, imagenFile).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            url = uri.toString();
                            User user = new User();
                            user.setUsername(mUsername);
                            user.setImage_profile(url);
                            user.setId(mAuthProvider.getUid());
                            updateInfo(user);
                        }
                    });
                }else{
                    mDialog.dismiss();
                    Toast.makeText(ProfileActivity.this, "La Imagen No se pudo Almacenar", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateInfo(User user){
        mUserProvider.update(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mDialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "La informacion se actualizo correctamente", Toast.LENGTH_SHORT).show();
                    if(role.equals("VENDEDOR")){
                        Intent intent = new Intent(ProfileActivity.this, HomeAdminActivity.class);
                        startActivity(intent);
                    }else{
                        Intent intent = new Intent(ProfileActivity.this, HomeUserActivity.class);
                        startActivity(intent);
                    }
                }
                else {
                    Toast.makeText(ProfileActivity.this, "La informacion no se pudo actualizar", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateInfoSinFoto(User user){
        mUserProvider.updateSinFoto(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mDialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "La informacion se actualizo correctamente", Toast.LENGTH_SHORT).show();
                    if(role.equals("VENDEDOR")){
                        Intent intent = new Intent(ProfileActivity.this, HomeAdminActivity.class);
                        startActivity(intent);
                    }else{
                        Intent intent = new Intent(ProfileActivity.this, HomeUserActivity.class);
                        startActivity(intent);
                    }
                }
                else {
                    Toast.makeText(ProfileActivity.this, "La informacion no se pudo actualizar", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getUser(){
        mUserProvider.getUser(mAuthProvider.getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    if(documentSnapshot.contains("username")){
                        mUsername = documentSnapshot.getString("username");
                        txtInputNombre.setText(mUsername);
                    }
                    if(documentSnapshot.contains("email")){
                        mEmail = documentSnapshot.getString("email");
                        role = documentSnapshot.getString("role");
                        txtInputEmail.setText(mEmail);
                        txtInputEmail.setFocusable(false);
                        txtInputEmail.setEnabled(false);
                        txtInputEmail.setCursorVisible(false);
                        txtInputEmail.setKeyListener(null);
                        txtInputEmail.setBackgroundColor(Color.TRANSPARENT);
                    }
                    if(documentSnapshot.contains("image_profile")){
                        mImageProfile = documentSnapshot.getString("image_profile");
                        if(mImageProfile != null){
                            if(!mImageProfile.isEmpty()) {
                                Picasso.with(ProfileActivity.this).load(mImageProfile).into(mCircleImageViewProfile);
                            }
                        }
                    }
                }
            }
        });
    }

    private void selectOptionImage(int requestCode){
        mBuilderSelector.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if(i == 0){
                    openGalery();
                }else if (i == 1){
                    takePhoto(PHOTO_REQUEST_CODE);
                }
            }
        });

        mBuilderSelector.show();
    }

    private void openGalery(){
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    private void takePhoto(int requestCode){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createPhotoFile(requestCode);
            } catch(Exception e) {
                Toast.makeText(this, "Hubo un error con el archivo " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(this, "com.nain.tienda", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, requestCode);
            }
        }
    }

    private File createPhotoFile(int requestCode) throws IOException {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File photoFile = File.createTempFile(
                new Date() + "_photo",
                ".jpg",
                storageDir
        );
        if (requestCode == PHOTO_REQUEST_CODE) {
            mPhotoPath = "file:" + photoFile.getAbsolutePath();
            mAbsolutePhotoPath = photoFile.getAbsolutePath();
        }
        return photoFile;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK){
            try {
                mImageFile = FileUtil.from(this, data.getData());
                mCircleImageViewProfile.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
            }catch (Exception e){
                Log.d("ERROR", "Se produjo un error " + e.getMessage());
                Toast.makeText(this, "Se produjo un error " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            mImageFile = null;
            mPhotoFile = new File(mAbsolutePhotoPath);
            Picasso.with(ProfileActivity.this).load(mPhotoPath).into(mCircleImageViewProfile);
        }
    }
}
