package com.rizki.duwinanto.arkav_reader;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {
    private Button buttonSignup;
    private Spinner spinnerName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewSignup;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        buttonSignup = (Button) findViewById(R.id.buttonSignup);
        editTextEmail = (EditText) findViewById(R.id.emailSignup);
        editTextPassword = (EditText) findViewById(R.id.passwordSignup);
        spinnerName = (Spinner) findViewById(R.id.nameSignup);
        textViewSignup = (TextView) findViewById(R.id.textviewSignup);

        progressDialog = new ProgressDialog(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        ArrayList<String> startup = new ArrayList<String>();
        startup.add("GITS Indonesia");
        startup.add("Uang Teman");
        startup.add("Rentuff");
        startup.add("Cyberlabs");
        startup.add("Qlapa");
        startup.add("Prelo");
        startup.add("NoLimit");
        startup.add("DycodeX");
        startup.add("NIU Corp");
        startup.add("Meridian");
        startup.add("IDCloudHost");
        startup.add("Inkubator IT");
        startup.add("Kudo");
        startup.add("LAPI Divusi");
        startup.add("Scola");
        startup.add("Goers");
        startup.add("Dewaweb");
        startup.add("Amartha");
        startup.add("BNI");
        startup.add("Mandala");
        startup.add("Tripi");
        startup.add("Emago");
        startup.add("Cicil");
        startup.add("Techlab");
        startup.add("Atom");
        startup.add("My-3D");
        startup.add("Populix");
        startup.add("Noompang");
        startup.add("terasindonesia");
        startup.add("Jojonomic");
        startup.add("Beiergo");
        startup.add("Biops");
        startup.add("Bukalapak");
        startup.add("Telkomsel");
        startup.add("Agate");

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, startup);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerName.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        }

        buttonSignup.setOnClickListener(this);
        textViewSignup.setOnClickListener(this);

    }

    @Override
    public void onClick(View view){
        if (view == buttonSignup){
            registerStartup();
        }
        if (view == textViewSignup) {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    protected void registerStartup(){
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Registering Please Wait..");
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Log.w("SignUp", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            final String startupName = spinnerName.getSelectedItem().toString();
                            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(spinnerName.getSelectedItem().toString()).build();
                            user.updateProfile(profileUpdate);
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        } else {
                            Log.w("SignUp", "createUserWithEmail:failed");
                            Toast.makeText(SignupActivity.this, "Auth Failed", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });

    }

    public Integer getZone(String startupName){
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("GITS Indonesia", 1);
        map.put("Uang Teman", 1);
        map.put("Rentuff", 1);
        map.put("Cyberlabs", 1);
        map.put("Qlapa", 1);
        map.put("Prelo", 1);
        map.put("NoLimit", 2);
        map.put("DycodeX", 2);
        map.put("NIU Corp", 2);
        map.put("Meridian", 2);
        map.put("IDCloudHost", 2);
        map.put("Inkubator IT", 3);
        map.put("Kudo", 3);
        map.put("LAPI Divusi", 3);
        map.put("Scola", 3);
        map.put("Goers", 3);
        map.put("Dewaweb", 4);
        map.put("Amartha", 4);
        map.put("BNI", 4);
        map.put("Mandala", 5);
        map.put("Tripi", 5);
        map.put("Emago", 5);
        map.put("Cicil", 5);
        map.put("Techlab", 6);
        map.put("Atom", 6);
        map.put("My-3D", 6);
        map.put("Populix", 6);
        map.put("Noompang", 7);
        map.put("terasindonesia", 7);
        map.put("Jojonomic", 7);
        map.put("Beiergo", 7);
        map.put("Biops", 7);
        map.put("Bukalapak", 8);
        map.put("Telkomsel", 8);
        map.put("Agate", 9);
        return map.get(startupName);
    }
}
