package com.nain.tienda.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.nain.tienda.R;
import com.nain.tienda.activities.Comprador.HomeUserActivity;
import com.nain.tienda.activities.Vendedor.HomeAdminActivity;
import com.nain.tienda.models.User;
import com.nain.tienda.providers.AuthProvider;
import com.nain.tienda.providers.UsersProvider;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class RegisterActivity extends AppCompatActivity {

    CircleImageView mCircleImagenViewBack;
    EditText mTextInputUsername, mTextInputEmail, mTextInputPassword;
    Button mButtonRegister;
    AuthProvider mAuthProvider;
    AlertDialog mDialog;
    Spinner mSpinner;
    String categoria;
    UsersProvider mUsersProvider;
    TextView mtextViewNameBookingDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mCircleImagenViewBack = findViewById(R.id.circleImageBack);
        mButtonRegister = findViewById(R.id.btnRegisterSave);
        mTextInputUsername = findViewById(R.id.txtInputNombre);
        mTextInputEmail = findViewById(R.id.txtInputEmail);
        mTextInputPassword = findViewById(R.id.txtInputPassword);
        mtextViewNameBookingDetail = findViewById(R.id.textViewNameBookingDetail);
        mSpinner = findViewById(R.id.sp_categoria);

        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();

        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Espere un momento")
                .setCancelable(false).build();

        this.Categorias();

        mCircleImagenViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
                categoria = adapter.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getApplicationContext(), "Nothing selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void register(){
        String username = mTextInputUsername.getText().toString();
        String email = mTextInputEmail.getText().toString();
        String password  = mTextInputPassword.getText().toString();

        if (!username.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
            if (isEmailValid(email)) {
                if (password.length() >= 6) {
                    createUser(username, email, password, categoria);
                }
                else {
                    Toast.makeText(this, R.string.txt_password_invalid, Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(this, R.string.txt_email_invalid, Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(this, R.string.txt_insertar_datos, Toast.LENGTH_SHORT).show();
        }
    }

    private void createUser(final String username, final String email, final String password, final String categoria){
        mDialog.show();
        mAuthProvider.register(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String id = mAuthProvider.getUid();
                    User user = new User();
                    user.setId(id);
                    user.setEmail(email);
                    user.setRole(categoria);
                    user.setTimestamp(new Date().getTime());
                    user.setUsername(username);

                    mUsersProvider.create(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mDialog.dismiss();
                            if (task.isSuccessful()) {
                                if(categoria.equals("VENDEDOR")){
                                    Intent intent = new Intent(RegisterActivity.this, HomeAdminActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }else{
                                    Intent intent = new Intent(RegisterActivity.this, HomeUserActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }

                            }else{
                                Toast.makeText(RegisterActivity.this, "No se pudo almacenar el usuario en la base de datos", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }else{
                    mDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "No se pudo registrar el usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void Categorias(){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.roles, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinner.setAdapter(adapter);
    }

    private boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
