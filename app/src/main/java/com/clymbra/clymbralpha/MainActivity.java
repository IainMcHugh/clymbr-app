package com.clymbra.clymbralpha;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private EditText mEmail, mPassword;

    private Button login, create_account;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: Test");
        FirebaseApp.initializeApp(this);
        Log.d(TAG, "onCreate: Started");
        initWidgets();
        setupFirebaseAuth();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLogin();
            }
        });

        create_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });

    }

    private void initWidgets(){
        Log.d(TAG, "initWidgets: initializing widgets");
        mAuth = FirebaseAuth.getInstance();

        mEmail = findViewById(R.id.email_editText);
        mPassword = findViewById(R.id.password_editText);
        login = findViewById(R.id.login_button);
        create_account = findViewById(R.id.createacc_button);

    }

    private void createAccount() {
        Log.d(TAG, "createAccount: Creating new account");
        Toast.makeText(MainActivity.this, "Currently Locked", Toast.LENGTH_SHORT).show();
//        startActivity(new Intent(MainActivity.this, CreateAccountActivity.class));

    }

    // stops a user that just logged out from getting back in by disabling back button on login screen
    @Override
    public void onBackPressed() {
    }

    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up Firebase Auth");
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.d(TAG, "onAuthStateChanged: commencing");
                //check if the user is already logged in
                FirebaseUser user = firebaseAuth.getCurrentUser();

                // checkCurrentUser(user);
                Log.d(TAG, "Test 2");
                if(user != null) {
                    //User is signed in
                    Log.d(TAG, "onAuthStateChanged: signed in: " + firebaseAuth.getCurrentUser().getUid());
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed out");
                }
            }
        };
    }

    // This initially checks to see if the user is currently signed in

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            Log.d(TAG, "onStart: Logged in as " + currentUser.getUid());
            // Toast.makeText(MainActivity.this, "Logged in as" + currentUser.getUid(), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void onAuthSuccess(FirebaseUser user) {

        if (user != null) {

            // Will navigate to HomeActivity
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
        }
    }

    private void startLogin() {
        Log.d(TAG, "onClick: attempting to log in.");

        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {

            Toast.makeText(MainActivity.this, "Enter email and password", Toast.LENGTH_LONG).show();

        } else {

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (!task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Login Error", Toast.LENGTH_LONG).show();
                    } else {

                        onAuthSuccess(task.getResult().getUser());
                    }
                }

            });
        }
    }

}
