package com.example.crudloginandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
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

    FirebaseAuth mAuth;
    DatabaseReference referenceDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mAuth = FirebaseAuth.getInstance();
        referenceDb = FirebaseDatabase.getInstance().getReference();

        rNombre = findViewById(R.id.txtNombreR);
        rPhone = findViewById(R.id.txtNumeroR);
        rCorreo = findViewById(R.id.txtCorreoR);
        rPassword = findViewById(R.id.txtPasswordR);
        btnRegistrarse = findViewById(R.id.btnRegistro);

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

    //REGISTROEMAILPASS
    private void registrarUsuario() {
        mAuth.createUserWithEmailAndPassword(correo, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("nombre", nombre);
                    map.put("telefono", telefono);
                    map.put("correo", correo);
                    map.put("password", password);
                    String id = mAuth.getCurrentUser().getUid();
                    referenceDb.child("Usuarios").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task2) {
                            if(task2.isSuccessful()){
                                startActivity(new Intent(RegistroActivity.this, CodigoActivity.class));
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

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() != null){
            startActivity(new Intent(RegistroActivity.this, MainActivity.class));
            finish();
        }
    }
}
