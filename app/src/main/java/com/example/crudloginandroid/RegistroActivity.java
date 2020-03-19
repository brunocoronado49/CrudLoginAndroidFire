package com.example.crudloginandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RegistroActivity extends AppCompatActivity {

    //Registro
    private EditText rNombre;
    private EditText rCorreo;
    private EditText rPassword;
    private Button btnRegistrarse;
    private String nombre = "";
    private String telefono = "";
    private String correo = "";
    private String password = "";

    //Registro Normal y por Mensage
    private EditText rPhone;

    //Telefono
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String mVerificationId;

    FirebaseAuth mAuth;
    DatabaseReference referenceDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mAuth = FirebaseAuth.getInstance();
        referenceDb = FirebaseDatabase.getInstance().getReference();

        rNombre = findViewById(R.id.txtNombreR);
        rPhone = findViewById(R.id.txtPhoneR);
        rCorreo = findViewById(R.id.txtCorreoR);
        rPassword = findViewById(R.id.txtPasswordR);
        btnRegistrarse = findViewById(R.id.btnRegistro);

        //TELEFONO
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    Toast.makeText(RegistroActivity.this, getString(R.string.now_logged_in) + firebaseAuth.getCurrentUser().getProviderId(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegistroActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };

        //REGISTRO
        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nombre = rNombre.getText().toString();
                telefono = rPhone.getText().toString();
                correo = rCorreo.getText().toString();
                password = rPassword.getText().toString();
                if(!nombre.isEmpty() && !telefono.isEmpty() && !correo.isEmpty() && !password.isEmpty()){
                    if(password.length() >= 6){
                        registrarUsuario();
                    }
                    else{
                        Toast.makeText( RegistroActivity.this, "Debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText( RegistroActivity.this, "Complete los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    //TELEFONO
    public void requestCode(View view) {
        String phone = rPhone.getText().toString();
        if (TextUtils.isEmpty(phone))
            return;
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone, 60, TimeUnit.SECONDS, RegistroActivity.this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        //Called if it is not needed to enter verification code
                        signInWithCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        //incorrect phone number, verification code, emulator, etc.
                        Toast.makeText(RegistroActivity.this, "onVerificationFailed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        //now the code has been sent, save the verificationId we may need it
                        super.onCodeSent(verificationId, forceResendingToken);

                        mVerificationId = verificationId;
                    }

                    @Override
                    public void onCodeAutoRetrievalTimeOut(String verificationId) {
                        //called after timeout if onVerificationCompleted has not been called
                        super.onCodeAutoRetrievalTimeOut(verificationId);
                        Toast.makeText(RegistroActivity.this, "onCodeAutoRetrievalTimeOut :" + verificationId, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
    private void signInWithCredential(PhoneAuthCredential phoneAuthCredential) {
        mAuth.signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegistroActivity.this, R.string.signed_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegistroActivity.this, getString(R.string.sign_credential_fail) + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //CONFUSIONPARAELTELEFONO
    private void registrarUsuario() {
        final String name = rNombre.getText().toString().trim();
        final String phone = rPhone.getText().toString().trim();
        final String email = rCorreo.getText().toString().trim();
        final String password = rPassword.getText().toString().trim();
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                mAuth.signInWithEmailAndPassword(email, password);
                                String id = mAuth.getCurrentUser().getUid();
                                Map<String, Object> map = new HashMap<>();
                                map.put("nombre", name);
                                map.put("telefono", phone);
                                map.put("correo", email);
                                map.put("password", password);
                                referenceDb.child("Usuarios").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task2) {
                                        if(task2.isSuccessful()){
                                            startActivity(new Intent(RegistroActivity.this, MainActivity.class));
                                            finish();
                                        }
                                        else{
                                            Toast.makeText(RegistroActivity.this, "No se crearon los datos", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else
                                Toast.makeText(RegistroActivity.this, "error registering user", Toast.LENGTH_SHORT).show();

                        }
                    });
        }

    }

    @Override
    protected void onStart () {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(RegistroActivity.this, MainActivity.class));
            finish();
        }
    }
}
