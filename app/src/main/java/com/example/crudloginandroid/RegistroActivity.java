package com.example.crudloginandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
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


    //TELEFONO
    private String code ="";
    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private EditText codigo;
    private Button verify;
    private String TAG ="RegisterActivity";

    FirebaseAuth mAuth;
    DatabaseReference referenceDb;

    @Override
    protected void onStart() {
        super.onStart();
        if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(rPhone.getText().toString());
        }
        mAuth.addAuthStateListener(mAuthListener);
    }

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
        codigo = findViewById(R.id.txtCodigo);
        verify = findViewById(R.id.btnVierificar);
        telefono = rPhone.getText().toString();
        code = codigo.getText().toString();

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
                validatePhoneNumber();
                startPhoneNumberVerification(telefono);
                verifyPhoneNumberWithCode(mVerificationId ,code);
            }
        });

        //LLAMADAS A METODOS TELEFONO
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
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "Verificacion Completada:" + credential);
                mVerificationInProgress = false;
                startActivity(new Intent(RegistroActivity.this, MainActivity.class));
                signInWithPhoneAuthCredential(credential);
            }
            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "Verificacion Fallida", e);
                mVerificationInProgress = false;
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    rPhone.setError("Numero invalido.");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(RegistroActivity.this, "Tiempo agotado. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(RegistroActivity.this, "Mala Verificacion", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "Enviando codigo:" + verificationId);
                mVerificationId = verificationId;
                mResendToken = token;
                Toast.makeText(RegistroActivity.this, "Enviado", Toast.LENGTH_SHORT).show();
            }
        };
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatePhoneNumber();
                startPhoneNumberVerification(telefono);
                verifyPhoneNumberWithCode(mVerificationId ,code);
            }
        });
    }

    //TELEFONO
    private void startPhoneNumberVerification(String telefono) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                telefono, 30, TimeUnit.SECONDS, this, mCallbacks);

        mVerificationInProgress = true;
    }
    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
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
    private boolean validatePhoneNumber() {
        String phoneNumber = rPhone.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            rPhone.setError("Numero invalido.");
            return false;
        }
        return true;
    }



    //REGISTROEMAILPASS
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
                                            Toast.makeText(RegistroActivity.this, "Registrado correctamente", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(RegistroActivity.this, RegistroActivity.class));
                                            finish();
                                        }
                                        else{
                                            Toast.makeText(RegistroActivity.this, "No se crearon los datos", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else
                                Toast.makeText(RegistroActivity.this, "Error, puede que ya exista", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }


}
