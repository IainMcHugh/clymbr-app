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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateAccountActivity extends AppCompatActivity {

    private static final String TAG = "CreateAccountActivity";

    private EditText nUsername, nEmail, nPassword, nRepassword;

    private Button nCreateacc;
    private Button nReturn;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        initWidgets();
        Log.d(TAG, "setupFirebaseAuth: setting up Firbase Auth");
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance();
        nCreateacc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });

        nReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "nReturn: Returning to Main activity");
                startActivity(new Intent(CreateAccountActivity.this, MainActivity.class));
            }
        });

    }

    private void initWidgets() {

        Log.d(TAG, "initWidgets: initializing widgets");

        nUsername = findViewById(R.id.username_editText);
        nEmail = findViewById(R.id.email_editText);
        nPassword = findViewById(R.id.password_editText);
        nRepassword = findViewById(R.id.repassword_editText);

        nCreateacc = findViewById(R.id.createacc_button);
        nReturn = findViewById(R.id.goback_button);
    }

    private void createNewAccount() {

        Log.d(TAG, "createNewAccount: Creating new account");

        final String username = nUsername.getText().toString();
        final String email = nEmail.getText().toString();
        String password = nPassword.getText().toString();
        String repassword = nRepassword.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(repassword)) {

            Log.d(TAG, "TextUtils is empty");

            Toast.makeText(CreateAccountActivity.this, "All fields required", Toast.LENGTH_LONG).show();
        } else if (!password.equals(repassword)) {

            Log.d(TAG, "Passwords do not match");

            Toast.makeText(CreateAccountActivity.this, "Passwords do not match", Toast.LENGTH_LONG).show();
        }
        // else if username already taken
        else {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(CreateAccountActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserwithEmail: Success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        onAuthSuccess(user, username, email);
                    } else {
                        Log.d(TAG, "createUserwithEmail: Failure");
                        Toast.makeText(CreateAccountActivity.this, "Failure to create account.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }

    private void onAuthSuccess(FirebaseUser user, String username, String email) {
        if (user != null){

            //Write stuff to database - user
            DatabaseReference myRef = mRef.getReference();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            String Id = currentUser.getUid();
            //String Id = myRef.push().getKey();
            String bio = getString(R.string.nulll);
            String local_gym = getString(R.string.nulll);
            String active_gym = "";
            Integer points = 0;
            String grade = getString(R.string.nulll);
            Integer total_climbs = 0;
            Integer total_tries = 0;
            Integer avg_tries = 0;
            Integer rank = 0;
            String profile_url = "https://firebasestorage.googleapis.com/v0/b/clymbr-alpha.appspot.com/o/profile_photos%2Fdefault-profile-picture.png?alt=media&token=626ba068-fe47-4488-bf24-3b558a2dcdc0";
            String parse_email = email.replace(".", "");
            Users users = new Users(Id, username, email, bio, local_gym, active_gym, points, grade,
                    total_climbs, total_tries, avg_tries, rank, profile_url);


            myRef.child(getString(R.string.dbname_users)).child(Id).setValue(users);
            // Will navigate to HomeActivity
            startActivity(new Intent(CreateAccountActivity.this, selectGymActivity.class).putExtra("username", username));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }
}
