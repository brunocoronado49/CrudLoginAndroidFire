package com.example.crudloginandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.crudloginandroid.model.Datos;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    //CRUD
    private List<Datos> listPerson = new ArrayList<Datos>();
    ArrayAdapter<Datos> arrayAdapterDatos;
    private EditText datoNombre;
    private EditText datoCorreo;
    private ListView lista;
    FirebaseDatabase database;
    DatabaseReference referenceDb;
    Datos datosSelected;

    //LOGINGOOGLE
    private Button salir;
    private TextView vName;
    private TextView vCorreo;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    //INICIO CRUD FIREBASE ANDROID
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //inicializar google
        vName = findViewById(R.id.vName);
        vCorreo = findViewById(R.id.vCorreo);
        salir = findViewById(R.id.btnSingOut);
        mAuth = FirebaseAuth.getInstance();

        //inicializar crud
        datoNombre = findViewById(R.id.txtNombre);
        datoCorreo = findViewById(R.id.txtCorreo);
        lista = findViewById(R.id.Lista);
        inicializarFirebase();
        listarDatos();

        //Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        //Google
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personEmail = acct.getEmail();
            vName.setText(personName);
            vCorreo.setText(personEmail);
        }
        getUserProfile();


        //LOGOUTGOOGLE
        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btnSingOut:
                        signOut();
                        break;
                }
            }
        });

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                datosSelected = (Datos) parent.getItemAtPosition(position);
                datoNombre.setText(datosSelected.getNombre());
                datoCorreo.setText(datosSelected.getCorreo());
            }
        });

    }

    //SIGNOUT GOOGLE
    private void signOut(){
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                finish();
            }
        });
        mAuth.signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    private void inicializarFirebase() {
        FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance();
        referenceDb = database.getReference();
    }

    //LISTA
    private void listarDatos() {
        referenceDb.child("DatosPersona").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listPerson.clear();
                for(DataSnapshot objSnapshot : dataSnapshot.getChildren()){
                    Datos d = objSnapshot.getValue(Datos.class);
                    listPerson.add(d);
                    arrayAdapterDatos = new ArrayAdapter<Datos>(MainActivity.this, android.R.layout.simple_list_item_1, listPerson);
                    lista.setAdapter(arrayAdapterDatos);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    //MENU Y ACCIONES
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String nombre = datoNombre.getText().toString();
        String correo = datoCorreo.getText().toString();
        switch (item.getItemId()){
            case R.id.icon_add:{
                if(nombre.equals("") || correo.equals("")){
                    Validacion();
                }
                else {
                    Datos p = new Datos();
                    p.setUid(UUID.randomUUID().toString());
                    p.setNombre(nombre);

                    p.setCorreo(correo);
                    referenceDb.child("DatosPersona").child(p.getUid()).setValue(p);
                    Toast.makeText(this, "Agregado", Toast.LENGTH_LONG).show();
                    limpiarCajas();
                }
                break;
            }
            case R.id.icon_update:{
                Datos p = new Datos();
                p.setUid(datosSelected.getUid());
                p.setNombre(datoNombre.getText().toString().trim());
                p.setCorreo(datoCorreo.getText().toString().trim());
                referenceDb.child("DatosPersona").child(p.getUid()).setValue(p);
                Toast.makeText( this, "Actualizado", Toast.LENGTH_LONG).show();
                limpiarCajas();
                break;
            }
            case R.id.icon_delete:{
                Datos p = new Datos();
                p.setUid(datosSelected.getUid());
                referenceDb.child("DatosPersona").child(p.getUid()).removeValue();
                Toast.makeText( this, "Eliminado", Toast.LENGTH_LONG).show();
                limpiarCajas();
                break;
            }
            default:break;
        }
        return true;
    }
    private void limpiarCajas() {
        datoNombre.setText("");
        datoCorreo.setText("");
    }
    private void Validacion() {
        String nombre = datoNombre.getText().toString();
        String correo = datoCorreo.getText().toString();
        if(nombre.equals("")){
            datoNombre.setError("Required");
        }
        else if(correo.equals("")){
            datoCorreo.setError("Required");
        }
    }

    public void getUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String nombre = user.getDisplayName();
            String email = user.getEmail();
            vName.setText(nombre);
            vCorreo.setText(email);
        }
    }


}
