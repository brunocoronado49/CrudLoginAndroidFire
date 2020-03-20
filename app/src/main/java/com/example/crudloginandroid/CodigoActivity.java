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

import java.util.concurrent.TimeUnit;

public class CodigoActivity extends AppCompatActivity {

    private EditText txtnumero;
    private EditText txtcodigo;
    private Button btnnumero;
    private Button btnverificar;

    private  String numero ="";
    private String codigo ="";

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private static final String TAG = "PhoneAuthActivity";

    private FirebaseAuth.AuthStateListener mAuthListener;

    FirebaseAuth mAuth;
    DatabaseReference referenceDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_codigo);

        mAuth = FirebaseAuth.getInstance();
        referenceDb = FirebaseDatabase.getInstance().getReference();

        txtnumero = findViewById(R.id.txtNumero);
        txtcodigo = findViewById(R.id.txtCodigo);

        btnnumero = findViewById(R.id.btnNumero);
        btnverificar = findViewById(R.id.btnVerificacion);

        numero = txtnumero.getText().toString();
        codigo = txtcodigo.getText().toString();

        btnnumero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPhoneNumberVerification(numero);
                mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        Log.d(TAG, "onVerificationCompleted:" + credential);
                        mVerificationInProgress = false;
                        signInWithPhoneAuthCredential(credential);
                    }
                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Log.w(TAG, "onVerificationFailed", e);
                        mVerificationInProgress = false;
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            txtnumero.setError("Número invalido.");
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            Toast.makeText(CodigoActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        Log.d(TAG, "onCodeSent:" + verificationId);
                        mVerificationId = verificationId;
                        mResendToken = token;
                    }
                };
            }
        });

        btnverificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyPhoneNumberWithCode(mVerificationId, codigo);
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    Toast.makeText(CodigoActivity.this, getString(R.string.now_logged_in) + firebaseAuth.getCurrentUser().getProviderId(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CodigoActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(txtnumero.getText().toString());
        }
    }

    private boolean validatePhoneNumber() {
        String phoneNumber = txtnumero.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            txtnumero.setError("Número invalido");
            return false;
        }

        return true;
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                30,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

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
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            //startActivity(new Intent(CodigoActivity.this, MainActivity.class));
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                txtcodigo.setError("Código invalido");
                            }
                            Toast.makeText(CodigoActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
