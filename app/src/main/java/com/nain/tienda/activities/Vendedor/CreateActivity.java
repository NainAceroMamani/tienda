package com.nain.tienda.activities.Vendedor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.UploadTask;
import com.nain.tienda.R;
import com.nain.tienda.models.Tienda;
import com.nain.tienda.models.User;
import com.nain.tienda.providers.AuthProvider;
import com.nain.tienda.providers.ImageProvider;
import com.nain.tienda.providers.TiendaProvider;
import com.nain.tienda.providers.UsersProvider;
import com.google.firebase.database.DatabaseReference;
import com.nain.tienda.utils.FileUtil;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dmax.dialog.SpotsDialog;

public class CreateActivity extends AppCompatActivity {

    CardView cv1,cv2,cv3;
    Button mButtonNext1,mButtonNext2,mButtonGuardar;
    EditText mTxtNombre, mTxtDescription, mTxtPaginaWeb,mTxtTelefono, mTxtCorreo;
    String nombre,description,paginaWeb, correo,telefono,url,id;
    Spinner mSpinner;
    ImageView mImagenLocal;
    AlertDialog mDialog;

    TiendaProvider mTiendaProvider;
    AuthProvider mAuthProvider;
    UsersProvider mUserProvider;
    private DatabaseReference mDatabase;

    AlertDialog.Builder mBuilderSelector;
    CharSequence options[];
    Boolean bandera = false;
    File mImageFile;
    ImageProvider mImageProvider;
    private final int GALLERY_REQUEST_CODE = 1;
    private final int PHOTO_REQUEST_CODE = 2;
    String mImageProfile = "";

    String mAbsolutePhotoPath;
    String mPhotoPath;
    File mPhotoFile;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        cv1 = findViewById(R.id.cv);
        cv2 = findViewById(R.id.cv2);
        cv3 = findViewById(R.id.cv3);
        mButtonNext1 = findViewById(R.id.btnSiguiente);
        mButtonNext2 = findViewById(R.id.btnSiguiente2);
        mButtonGuardar = findViewById(R.id.btnGuardar);
        mTxtNombre = findViewById(R.id.txtInputNombre);
        mTxtPaginaWeb = findViewById(R.id.txtPaginaUrl);
        mTxtCorreo = findViewById(R.id.txtInputCorreo);
        mTxtTelefono = findViewById(R.id.txtInputTelefono);
        mTxtDescription = findViewById(R.id.txtInputDescription);
        mSpinner = findViewById(R.id.sp_categoria);
        mImagenLocal = findViewById(R.id.imagenLocal);

        mBuilderSelector = new AlertDialog.Builder(CreateActivity.this);
        mBuilderSelector.setTitle("Selecciona una opcion");
        options = new CharSequence[] {"Imagen de galeria", "Tomar foto"};

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Registrar Tienda");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mImageProvider = new ImageProvider();
        mTiendaProvider = new TiendaProvider();
        mAuthProvider = new AuthProvider();
        mUserProvider = new UsersProvider();

        mDatabase = FirebaseDatabase.getInstance().getReference("Locales");
        id = mDatabase.push().getKey();

        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Espere un momento")
                .setCancelable(false).build();

        mButtonNext1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nombre = mTxtNombre.getText().toString();
                description = mTxtDescription.getText().toString();
                if(!nombre.isEmpty()){
                    cv1.setVisibility(View.GONE);
                    cv2.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(CreateActivity.this, "El Nombre es Obligatorio", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mButtonNext2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paginaWeb = mTxtPaginaWeb.getText().toString();
                correo = mTxtCorreo.getText().toString();
                telefono = mTxtTelefono.getText().toString();
                if(!correo.isEmpty() && isEmailValid(correo)){
                    cv2.setVisibility(View.GONE);
                    cv3.setVisibility(View.VISIBLE);
                    Snackbar.make(v, "Presione la Imagen para subir foto", Snackbar.LENGTH_LONG).show();
                }else{
                    Toast.makeText(CreateActivity.this, "Correo no válido", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mImagenLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bandera = true;
                selectOptionImage(GALLERY_REQUEST_CODE);
            }
        });

        mButtonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

    }

    private void save(){
        if(!nombre.isEmpty()){
            if(mImageFile != null && bandera){
                saveImage(mImageFile);
            }
            else if(mPhotoFile != null && bandera) {
                saveImage(mPhotoFile);
            }else{
                mDialog.show();
                Tienda tienda = new Tienda();
                tienda.setId(id);
                tienda.setNombre(nombre);
                tienda.setCorreo(correo);
                tienda.setDescripcion(description);
                tienda.setTelefono(telefono);
                tienda.setPagina_url(paginaWeb);
                saveLocal(tienda);
            }
            this.bandera = false;
        }else{
            Toast.makeText(this, "Complete los campos", Toast.LENGTH_LONG).show();
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
                            Tienda tienda = new Tienda();
                            tienda.setId(id);
                            tienda.setNombre(nombre);
                            tienda.setCorreo(correo);
                            tienda.setUrl_imagen(url);
                            tienda.setDescripcion(description);
                            tienda.setTelefono(telefono);
                            tienda.setPagina_url(paginaWeb);
                            saveLocal(tienda);
                        }
                    });
                }else{
                    Toast.makeText(CreateActivity.this, "La Imagen No se pudo Almacenar", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void saveLocal(final Tienda tienda){
        mTiendaProvider.create(tienda).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    User user = new User();
                    user.setId(mAuthProvider.getUid());
                    user.setTienda_id(tienda.getId());
                    updateInfo(user);
                }else{
                    Toast.makeText(CreateActivity.this, "No se pudo registrar el local", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateInfo(User user){
        mUserProvider.updateLocal(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mDialog.dismiss();
                    Toast.makeText(CreateActivity.this, "Se ha creado su Local", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateActivity.this, HomeAdminActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }else{
                    mDialog.dismiss();
                    Toast.makeText(CreateActivity.this, "La informacion no se pudo actualizar", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(CreateActivity.this, "Hubo un error con el archivo " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(CreateActivity.this, "com.nain.tienda", photoFile);
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
                mImageFile = FileUtil.from(CreateActivity.this, data.getData());
                mImagenLocal.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
            }catch (Exception e){
                Log.d("ERROR", "Se produjo un error " + e.getMessage());
                Toast.makeText(CreateActivity.this, "Se produjo un error " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            mImageFile = null;
            mPhotoFile = new File(mAbsolutePhotoPath);
            Picasso.with(CreateActivity.this).load(mPhotoPath).into(mImagenLocal);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if(cv3.getVisibility() == View.VISIBLE){
            cv3.setVisibility(View.GONE);
            cv2.setVisibility(View.VISIBLE);
        }else if(cv2.getVisibility() == View.VISIBLE){
            cv2.setVisibility(View.GONE);
            cv1.setVisibility(View.VISIBLE);
        }else if(cv1.getVisibility() == View.VISIBLE){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Seguro que quiere Salir?")
                    .setPositiveButton("Si, Salir", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            onBackPressed();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            builder.show();
        }else{
            onBackPressed();
        }
        return false;
    }

    private boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
